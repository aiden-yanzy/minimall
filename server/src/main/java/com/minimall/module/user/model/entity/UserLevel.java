package com.minimall.module.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 会员等级
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_level")
public class UserLevel extends BaseEntity {

    private String name;
    private String icon;
    private Integer level;
    private BigDecimal discount;
    private Long upgradeConsume;
    private String privilege;
    private Integer status;
}