package com.minimall.module.marketing.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户已领取的优惠券
 */
@Data
@TableName("coupon_list")
public class CouponList {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long couponId;
    private Long userId;
    private String couponName;
    private Integer type;
    private Integer scope;
    private String scopeIds;
    private Long threshold;
    private Long discountValue;
    /** 0未使用 1已使用 2已过期 */
    private Integer status;
    private LocalDateTime receiveTime;
    private LocalDateTime expireTime;
    private LocalDateTime useTime;
    private Long useOrderId;
}