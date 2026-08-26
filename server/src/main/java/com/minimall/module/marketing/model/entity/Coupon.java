package com.minimall.module.marketing.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 优惠券模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
public class Coupon extends BaseEntity {

    private String name;
    /** 1满减券 2折扣券 */
    private Integer type;
    /** 1全场 2分类 3指定商品 */
    private Integer scope;
    private String scopeIds;
    private Long threshold;
    private Long discountValue;
    private Integer totalCount;
    private Integer sendCount;
    private Integer perLimit;
    private Integer validType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer validDays;
    private Integer status;
    private String description;
}