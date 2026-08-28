package com.minimall.module.wallet.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import com.minimall.module.wallet.mapper.AccountLogMapper;
import com.minimall.module.wallet.mapper.RechargeOrderMapper;
import com.minimall.module.wallet.mapper.RechargeTemplateMapper;
import com.minimall.module.wallet.mapper.WithdrawApplyMapper;
import com.minimall.module.wallet.model.entity.AccountLog;
import com.minimall.module.wallet.model.entity.RechargeOrder;
import com.minimall.module.wallet.model.entity.RechargeTemplate;
import com.minimall.module.wallet.model.entity.WithdrawApply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final AccountLogMapper accountLogMapper;
    private final RechargeTemplateMapper templateMapper;
    private final RechargeOrderMapper rechargeOrderMapper;
    private final WithdrawApplyMapper withdrawApplyMapper;
    private final UserService userService;

    // ---------------- 余额流水 ----------------

    /** 余额变动（含流水记录），delta 为正入负出 */
    @Transactional(rollbackFor = Exception.class)
    public Long changeBalance(Long userId, long delta, int type, String remark) {
        User user = userService.getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, userId).setSql("balance = balance + " + delta));
        Long newBalance = userService.getById(userId).getBalance();
        AccountLog log = new AccountLog();
        log.setUserId(userId);
        log.setType(type);
        log.setChangeAmount(delta);
        log.setBalanceAfter(newBalance);
        log.setRemark(remark);
        accountLogMapper.insert(log);
        return newBalance;
    }

    /** 只写流水（模拟退款：不实际动账） */
    public void logOnly(Long userId, int type, long amount, String remark) {
        User user = userService.getById(userId);
        AccountLog log = new AccountLog();
        log.setUserId(userId);
        log.setType(type);
        log.setChangeAmount(amount);
        log.setBalanceAfter(user == null ? 0 : (user.getBalance() == null ? 0 : user.getBalance()));
        log.setRemark(remark);
        accountLogMapper.insert(log);
    }

    /** 账户流水 */
    public List<AccountLog> logs(Long userId, Integer type) {
        return accountLogMapper.selectList(Wrappers.<AccountLog>lambdaQuery()
                .eq(AccountLog::getUserId, userId)
                .eq(type != null, AccountLog::getType, type)
                .orderByDesc(AccountLog::getId));
    }

    // ---------------- 充值 ----------------

    public List<RechargeTemplate> templates() {
        return templateMapper.selectList(Wrappers.<RechargeTemplate>lambdaQuery().eq(RechargeTemplate::getStatus, 1));
    }

    @Transactional(rollbackFor = Exception.class)
    public RechargeOrder createRecharge(Long userId, Long templateId, Long amount) {
        RechargeOrder order = new RechargeOrder();
        order.setOrderSn("RC" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        if (templateId != null) {
            RechargeTemplate t = templateMapper.selectById(templateId);
            if (t == null) throw new BusinessException("充值档位不存在");
            order.setTemplateId(templateId);
            order.setAmount(t.getAmount() + t.getGiftAmount());
            order.setPayAmount(t.getAmount());
        } else {
            if (amount == null || amount <= 0) throw new BusinessException(ResultCode.PARAM_ERROR);
            order.setAmount(amount);
            order.setPayAmount(amount);
        }
        order.setStatus(0);
        rechargeOrderMapper.insert(order);
        return order;
    }

    /** 充值模拟支付 */
    @Transactional(rollbackFor = Exception.class)
    public void payRecharge(Long userId, String orderSn) {
        RechargeOrder order = rechargeOrderMapper.selectOne(Wrappers.<RechargeOrder>lambdaQuery()
                .eq(RechargeOrder::getOrderSn, orderSn).eq(RechargeOrder::getUserId, userId));
        if (order == null) throw new BusinessException("充值单不存在");
        if (order.getStatus() != 0) throw new BusinessException("充值单已支付");
        rechargeOrderMapper.update(null, Wrappers.<RechargeOrder>lambdaUpdate()
                .eq(RechargeOrder::getId, order.getId())
                .set(RechargeOrder::getStatus, 1)
                .set(RechargeOrder::getPaySn, "RCPAY" + System.currentTimeMillis())
                .set(RechargeOrder::getPayTime, LocalDateTime.now()));
        changeBalance(userId, order.getAmount(), 1, "充值到账");
    }

    public List<RechargeOrder> myRecharges(Long userId) {
        return rechargeOrderMapper.selectList(Wrappers.<RechargeOrder>lambdaQuery()
                .eq(RechargeOrder::getUserId, userId).orderByDesc(RechargeOrder::getId));
    }

    // ---------------- 提现 ----------------

    @Transactional(rollbackFor = Exception.class)
    public Long withdraw(Long userId, long amount, int type, String accountInfo) {
        if (amount <= 0) throw new BusinessException(ResultCode.PARAM_ERROR);
        User user = userService.getById(userId);
        if (user == null || user.getBalance() == null || user.getBalance() < amount) {
            throw new BusinessException("余额不足");
        }
        changeBalance(userId, -amount, 4, "提现申请，冻结余额");
        WithdrawApply apply = new WithdrawApply();
        apply.setUserId(userId);
        apply.setAmount(amount);
        apply.setType(type);
        apply.setAccountInfo(StrUtil.isBlank(accountInfo) ? "微信零钱" : accountInfo);
        apply.setStatus(0);
        withdrawApplyMapper.insert(apply);
        return apply.getId();
    }

    public List<WithdrawApply> myWithdraws(Long userId) {
        return withdrawApplyMapper.selectList(Wrappers.<WithdrawApply>lambdaQuery()
                .eq(WithdrawApply::getUserId, userId).orderByDesc(WithdrawApply::getId));
    }

    // ---------------- 后台 ----------------

    public List<WithdrawApply> adminWithdraws(Integer status) {
        return withdrawApplyMapper.selectList(Wrappers.<WithdrawApply>lambdaQuery()
                .eq(status != null, WithdrawApply::getStatus, status).orderByDesc(WithdrawApply::getId));
    }

    /** 提现审核：通过->打款(模拟,已完成)；拒绝->退回余额 */
    @Transactional(rollbackFor = Exception.class)
    public void auditWithdraw(Long id, boolean approve, String remark) {
        WithdrawApply apply = withdrawApplyMapper.selectById(id);
        if (apply == null || apply.getStatus() != 0) throw new BusinessException("申请不存在或已处理");
        if (approve) {
            withdrawApplyMapper.update(null, Wrappers.<WithdrawApply>lambdaUpdate()
                    .eq(WithdrawApply::getId, id)
                    .set(WithdrawApply::getStatus, 3)
                    .set(WithdrawApply::getRemark, remark));
        } else {
            withdrawApplyMapper.update(null, Wrappers.<WithdrawApply>lambdaUpdate()
                    .eq(WithdrawApply::getId, id)
                    .set(WithdrawApply::getStatus, 2)
                    .set(WithdrawApply::getRemark, remark));
            changeBalance(apply.getUserId(), apply.getAmount(), 3, "提现被拒，退回余额");
        }
    }
}