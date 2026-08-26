package com.minimall.module.sign.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.module.sign.mapper.SignDailyMapper;
import com.minimall.module.sign.mapper.SignLogMapper;
import com.minimall.module.sign.model.entity.SignDaily;
import com.minimall.module.sign.model.entity.SignLog;
import com.minimall.module.sign.model.vo.SignVO;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignService extends ServiceImpl<SignDailyMapper, SignDaily> {

    private final SignLogMapper signLogMapper;
    private final UserService userService;

    /** 签到配置 */
    public SignDaily config() {
        return this.getOne(Wrappers.<SignDaily>lambdaQuery().eq(SignDaily::getStatus, 1).last("limit 1"), false);
    }

    /** 今日签到状态 */
    public SignVO status(Long userId) {
        SignVO vo = new SignVO();
        SignDaily cfg = config();
        vo.setConfig(cfg);
        vo.setSignedToday(signLogMapper.selectCount(Wrappers.<SignLog>lambdaQuery()
                .eq(SignLog::getUserId, userId).eq(SignLog::getSignDate, LocalDate.now())) > 0);
        vo.setContinuousDays(continuousDays(userId));
        return vo;
    }

    /** 本月签到日期 */
    public List<String> monthDays(Long userId, String month) {
        YearMonth ym = month != null ? YearMonth.parse(month) : YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return signLogMapper.selectList(Wrappers.<SignLog>lambdaQuery()
                        .eq(SignLog::getUserId, userId)
                        .ge(SignLog::getSignDate, start)
                        .le(SignLog::getSignDate, end))
                .stream().map(l -> l.getSignDate().toString()).toList();
    }

    /** 打卡：发放积分（含连续奖励） */
    @Transactional(rollbackFor = Exception.class)
    public SignVO doSign(Long userId) {
        SignDaily cfg = config();
        if (cfg == null) throw new BusinessException("签到功能未开启");
        boolean signed = signLogMapper.selectCount(Wrappers.<SignLog>lambdaQuery()
                .eq(SignLog::getUserId, userId).eq(SignLog::getSignDate, LocalDate.now())) > 0;
        if (signed) throw new BusinessException("今日已签到");

        int integral = cfg.getIntegral() == null ? 0 : cfg.getIntegral();
        int reward = cfg.getContinuousReward() == null ? 0 : cfg.getContinuousReward();
        int days = cfg.getContinuousDays() == null ? 0 : cfg.getContinuousDays();

        // 连续天数（昨天签过则 +1，否则重新开始）
        int continuous = continuousDays(userId) + 1;
        int bonus = 0;
        if (days > 0 && continuous >= days) {
            bonus = reward;
            integral += bonus;
        }

        SignLog log = new SignLog();
        log.setUserId(userId);
        log.setSignDate(LocalDate.now());
        log.setIntegral(integral);
        signLogMapper.insert(log);

        // 加积分
        userService.update(Wrappers.<User>lambdaUpdate()
                .eq(User::getId, userId)
                .setSql("integral = integral + " + integral));

        SignVO vo = new SignVO();
        vo.setConfig(cfg);
        vo.setSignedToday(true);
        vo.setContinuousDays(continuous);
        vo.setRewardIntegral(integral);
        vo.setBonusIntegral(bonus);
        return vo;
    }

    /** 累计连续签到天数（到今天为止，昨天未签即归零） */
    private int continuousDays(Long userId) {
        int n = 0;
        LocalDate day = LocalDate.now().minusDays(1);
        // 今天已签则从今天算起
        boolean todaySigned = signLogMapper.selectCount(Wrappers.<SignLog>lambdaQuery()
                .eq(SignLog::getUserId, userId).eq(SignLog::getSignDate, LocalDate.now())) > 0;
        if (todaySigned) {
            n = 1;
            day = LocalDate.now().minusDays(1);
        }
        while (signLogMapper.selectCount(Wrappers.<SignLog>lambdaQuery()
                .eq(SignLog::getUserId, userId).eq(SignLog::getSignDate, day)) > 0) {
            n++;
            day = day.minusDays(1);
        }
        return n;
    }
}