package com.minimall.module.aftersale.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 售后单
 * 状态：0待处理 1已同意 2已拒绝 3已退款 4已完成
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("after_sale")
public class AfterSale extends BaseEntity {

    private String afterSaleSn;
    private Long orderId;
    private Long orderGoodsId;
    private Long userId;
    /** 1仅退款 2退货退款 3换货 */
    private Integer type;
    private String reason;
    private Long refundAmount;
    private Integer status;
    private String refundSn;
    private LocalDateTime refundTime;
}
