package com.minimall.module.order.model.vo;

import com.minimall.module.marketing.model.entity.CouponList;
import lombok.Data;

import java.util.List;

/**
 * 下单预览（价格明细 + 可用优惠券）
 */
@Data
public class OrderPreviewVO {

    private Long totalAmount;
    private Long memberDiscount;
    private Long fullReduction;
    private Long couponAmount;
    private Long discountAmount;
    private Long payAmount;

    /** 会员等级名（无则普通） */
    private String memberName;

    /** 当前金额可用的优惠券 */
    private List<CouponList> availableCoupons;
}