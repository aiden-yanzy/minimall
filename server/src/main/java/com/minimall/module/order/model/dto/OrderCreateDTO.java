package com.minimall.module.order.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建订单参数
 */
@Data
public class OrderCreateDTO {

    /** 购买方式：cart=购物车结算（用勾选项），buyNow=立即购买，seckill=秒杀 */
    @NotBlank(message = "购买方式不能为空")
    private String buyType;

    /** 秒杀时传入：秒杀商品 id */
    private Long seckillGoodsId;

    /** buyNow 时传入：SKU id */
    private Long goodsItemId;

    /** buyNow 时传入：数量 */
    private Integer num = 1;

    /** 收货信息（create 时必填；preview 可空） */
    private String contact;
    private String mobile;
    private String address;

    private String remark;

    /** 使用的用户优惠券 id（可选） */
    private Long couponListId;

    /** 支付方式：1微信 2支付宝 3余额 */
    private Integer payType = 1;
}
