package com.minimall.module.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.exception.BusinessException;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.service.GoodsService;
import com.minimall.module.user.mapper.UserAddressMapper;
import com.minimall.module.user.mapper.UserCollectMapper;
import com.minimall.module.user.mapper.UserFootprintMapper;
import com.minimall.module.user.model.entity.UserAddress;
import com.minimall.module.user.model.entity.UserCollect;
import com.minimall.module.user.model.entity.UserFootprint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员中心：收货地址 / 收藏 / 足迹
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserAddressMapper addressMapper;
    private final UserCollectMapper collectMapper;
    private final UserFootprintMapper footprintMapper;
    private final GoodsService goodsService;

    // ---------------- 收货地址 ----------------

    public List<UserAddress> addresses(Long userId) {
        return addressMapper.selectList(Wrappers.<UserAddress>lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault).orderByDesc(UserAddress::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveAddress(Long userId, UserAddress a) {
        if (a.getContact() == null || a.getContact().isBlank()
                || a.getMobile() == null || a.getMobile().isBlank()
                || a.getAddress() == null || a.getAddress().isBlank()) {
            throw new BusinessException("请填写完整的收货信息");
        }
        if (a.getId() == null) {
            a.setUserId(userId);
            addressMapper.insert(a);
        } else {
            UserAddress exist = addressMapper.selectById(a.getId());
            if (exist == null || !exist.getUserId().equals(userId)) throw new BusinessException("地址不存在");
            a.setUserId(userId);
            addressMapper.updateById(a);
        }
        // 设为默认则清除其他默认
        if (a.getIsDefault() != null && a.getIsDefault() == 1) {
            addressMapper.update(null, Wrappers.<UserAddress>lambdaUpdate()
                    .eq(UserAddress::getUserId, userId)
                    .ne(UserAddress::getId, a.getId())
                    .set(UserAddress::getIsDefault, 0));
        } else if (a.getIsDefault() == null) {
            // 第一条默认
            if (addressMapper.selectCount(Wrappers.<UserAddress>lambdaQuery().eq(UserAddress::getUserId, userId)) == 1) {
                addressMapper.update(null, Wrappers.<UserAddress>lambdaUpdate()
                        .eq(UserAddress::getId, a.getId()).set(UserAddress::getIsDefault, 1));
            }
        }
        return a.getId();
    }

    public void setDefault(Long userId, Long id) {
        UserAddress a = addressMapper.selectById(id);
        if (a == null || !a.getUserId().equals(userId)) throw new BusinessException("地址不存在");
        addressMapper.update(null, Wrappers.<UserAddress>lambdaUpdate()
                .eq(UserAddress::getUserId, userId).set(UserAddress::getIsDefault, 0));
        addressMapper.update(null, Wrappers.<UserAddress>lambdaUpdate()
                .eq(UserAddress::getId, id).set(UserAddress::getIsDefault, 1));
    }

    public void removeAddress(Long userId, Long id) {
        UserAddress a = addressMapper.selectById(id);
        if (a == null || !a.getUserId().equals(userId)) throw new BusinessException("地址不存在");
        addressMapper.deleteById(id);
    }

    /** 取默认地址，无则取第一条 */
    public UserAddress defaultAddress(Long userId) {
        List<UserAddress> list = addresses(userId);
        if (list.isEmpty()) return null;
        return list.stream().filter(a -> a.getIsDefault() != null && a.getIsDefault() == 1)
                .findFirst().orElse(list.get(0));
    }

    // ---------------- 收藏 ----------------

    /** 收藏/取消收藏，返回是否收藏 */
    public boolean toggleCollect(Long userId, Long goodsId) {
        if (!goodsExists(goodsId)) throw new BusinessException("商品不存在");
        UserCollect exist = collectMapper.selectOne(Wrappers.<UserCollect>lambdaQuery()
                .eq(UserCollect::getUserId, userId).eq(UserCollect::getGoodsId, goodsId));
        if (exist != null) {
            collectMapper.deleteById(exist.getId());
            return false;
        }
        UserCollect c = new UserCollect();
        c.setUserId(userId);
        c.setGoodsId(goodsId);
        collectMapper.insert(c);
        return true;
    }

    public boolean isCollected(Long userId, Long goodsId) {
        return collectMapper.selectCount(Wrappers.<UserCollect>lambdaQuery()
                .eq(UserCollect::getUserId, userId).eq(UserCollect::getGoodsId, goodsId)) > 0;
    }

    /** 我的收藏（含商品信息） */
    public List<Goods> collectList(Long userId) {
        return collectMapper.selectList(Wrappers.<UserCollect>lambdaQuery()
                        .eq(UserCollect::getUserId, userId).orderByDesc(UserCollect::getId))
                .stream().map(c -> goodsService.getById(c.getGoodsId()))
                .filter(g -> g != null).toList();
    }

    // ---------------- 足迹 ----------------

    /** 记录浏览（详情页调用，重复浏览更新时间） */
    public void recordFootprint(Long userId, Long goodsId) {
        UserFootprint exist = footprintMapper.selectOne(Wrappers.<UserFootprint>lambdaQuery()
                .eq(UserFootprint::getUserId, userId).eq(UserFootprint::getGoodsId, goodsId));
        if (exist != null) {
            footprintMapper.update(null, Wrappers.<UserFootprint>lambdaUpdate()
                    .eq(UserFootprint::getId, exist.getId()).set(UserFootprint::getCreateTime, LocalDateTime.now()));
        } else {
            UserFootprint f = new UserFootprint();
            f.setUserId(userId);
            f.setGoodsId(goodsId);
            footprintMapper.insert(f);
        }
    }

    /** 我的足迹 */
    public List<Goods> footprintList(Long userId) {
        return footprintMapper.selectList(Wrappers.<UserFootprint>lambdaQuery()
                        .eq(UserFootprint::getUserId, userId).orderByDesc(UserFootprint::getCreateTime).last("limit 50"))
                .stream().map(f -> goodsService.getById(f.getGoodsId()))
                .filter(g -> g != null).toList();
    }

    public void clearFootprint(Long userId) {
        footprintMapper.delete(Wrappers.<UserFootprint>lambdaQuery().eq(UserFootprint::getUserId, userId));
    }

    private boolean goodsExists(Long goodsId) {
        return goodsService.getById(goodsId) != null;
    }
}