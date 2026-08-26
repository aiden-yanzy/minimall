package com.minimall.module.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单
 * 状态机：0待支付 -> 1待发货 -> 2待收货 -> 3已完成；任意态可 4已取消
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {

    private String orderSn;
    private Long userId;
    private Integer orderType;
    private Integer orderStatus;
    private Integer payStatus;
    private Integer payType;
    private LocalDateTime payTime;
    private String paySn;
    private Long totalAmount;
    private Long memberDiscount;
    private Long discountAmount;
    private Long freightAmount;
    private Long payAmount;
    private Long couponId;
    private Long couponAmount;
    private Long integralAmount;
    private String remark;
    // 收货快照
    private String contact;
    private String mobile;
    private String address;
    // 物流
    private Integer deliveryType;
    private Long expressId;
    private String expressName;
    private String expressNo;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private LocalDateTime closeTime;
    private Long commission;
    private Integer isComment;
    private Integer isAfterSale;
}
