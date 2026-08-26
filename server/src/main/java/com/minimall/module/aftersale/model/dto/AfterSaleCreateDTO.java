package com.minimall.module.aftersale.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 申请售后参数
 */
@Data
public class AfterSaleCreateDTO {

    @NotNull(message = "订单不能为空")
    private Long orderId;

    @NotNull(message = "订单商品不能为空")
    private Long orderGoodsId;

    /** 1仅退款 2退货退款 */
    @NotNull(message = "售后类型不能为空")
    private Integer type;

    @NotBlank(message = "请填写申请原因")
    private String reason;

    /** 退款金额(分)，可根据订单商品计算 */
    private Long refundAmount;
}
