package com.minimall.module.aftersale.model.vo;

import com.minimall.module.aftersale.model.entity.AfterSale;
import lombok.Data;

import java.time.LocalDateTime;

/** 售后单 + 订单/商品信息（列表展示用，避免前端二次请求） */
@Data
public class AfterSaleVO {

    private Long id;
    private String afterSaleSn;
    private Long orderId;
    private String orderSn;
    private Long orderGoodsId;
    private Long userId;
    private String nickname;
    private String mobile;
    private Integer type;
    private String reason;
    private Long refundAmount;
    private Integer status;
    private String refundSn;
    private LocalDateTime refundTime;
    private LocalDateTime createTime;
    private String goodsName;
    private String goodsImage;
    private String specValueStr;

    public static AfterSaleVO from(AfterSale a) {
        AfterSaleVO vo = new AfterSaleVO();
        vo.setId(a.getId());
        vo.setAfterSaleSn(a.getAfterSaleSn());
        vo.setOrderId(a.getOrderId());
        vo.setOrderGoodsId(a.getOrderGoodsId());
        vo.setUserId(a.getUserId());
        vo.setType(a.getType());
        vo.setReason(a.getReason());
        vo.setRefundAmount(a.getRefundAmount());
        vo.setStatus(a.getStatus());
        vo.setRefundSn(a.getRefundSn());
        vo.setRefundTime(a.getRefundTime());
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }
}
