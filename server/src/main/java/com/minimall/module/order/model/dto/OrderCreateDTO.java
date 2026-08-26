package com.minimall.module.order.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建订单参数
 */
@Data
public class OrderCreateDTO {

    /** 购买方式：cart=购物车结算（用勾选项），buyNow=立即购买 */
    @NotBlank(message = "购买方式不能为空")
    private String buyType;

    /** buyNow 时传入：SKU id */
    private Long goodsItemId;

    /** buyNow 时传入：数量 */
    private Integer num = 1;

    /** 收货信息（P1 直接传入，后续接地址簿） */
    @NotBlank(message = "收货人不能为空")
    private String contact;
    @NotBlank(message = "手机号不能为空")
    private String mobile;
    @NotBlank(message = "收货地址不能为空")
    private String address;

    private String remark;

    /** 支付方式：1微信 2支付宝 3余额 */
    private Integer payType = 1;
}
