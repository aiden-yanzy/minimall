package com.minimall.module.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.module.user.mapper.UserLevelMapper;
import com.minimall.module.user.model.entity.UserLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserLevelService extends ServiceImpl<UserLevelMapper, UserLevel> {

    /** 会员折扣：返回折扣系数(0.95=95折)，无等级按 1.0 */
    public BigDecimal discountOf(Long levelId) {
        if (levelId == null) return BigDecimal.ONE;
        UserLevel level = this.getById(levelId);
        if (level == null || level.getDiscount() == null || level.getStatus() == null || level.getStatus() != 1) {
            return BigDecimal.ONE;
        }
        return level.getDiscount().divide(BigDecimal.TEN);
    }
}