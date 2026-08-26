package com.minimall.module.order.model.vo;

import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.model.entity.OrderGoods;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 VO
 */
@Data
public class OrderVO {

    private Long id;
    private String orderSn;
    private Integer orderType;
    private Integer orderStatus;
    private Integer payStatus;
    private Integer payType;
    private LocalDateTime payTime;
    private Long totalAmount;
    private Long memberDiscount;
    private Long discountAmount;
    private Long couponAmount;
    private Long freightAmount;
    private Long payAmount;
    private String remark;
    private String contact;
    private String mobile;
    private String address;
    private String expressName;
    private String expressNo;
    private LocalDateTime createTime;
    private LocalDateTime receiveTime;
    private List<OrderGoodsVO> goodsList;

    @Data
    public static class OrderGoodsVO {
        private Long id;
        private Long goodsId;
        private Long goodsItemId;
        private String goodsName;
        private String goodsImage;
        private String specValueStr;
        private Long price;
        private Integer num;
        private Long totalPrice;
        private Integer isAfterSale;

        public static OrderGoodsVO from(OrderGoods g) {
            OrderGoodsVO vo = new OrderGoodsVO();
            vo.setId(g.getId());
            vo.setGoodsId(g.getGoodsId());
            vo.setGoodsItemId(g.getGoodsItemId());
            vo.setGoodsName(g.getGoodsName());
            vo.setGoodsImage(g.getGoodsImage());
            vo.setSpecValueStr(g.getSpecValueStr());
            vo.setPrice(g.getPrice());
            vo.setNum(g.getNum());
            vo.setTotalPrice(g.getTotalPrice());
            vo.setIsAfterSale(g.getIsAfterSale());
            return vo;
        }
    }

    public static OrderVO from(Order o) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderSn(o.getOrderSn());
        vo.setOrderType(o.getOrderType());
        vo.setOrderStatus(o.getOrderStatus());
        vo.setPayStatus(o.getPayStatus());
        vo.setPayType(o.getPayType());
        vo.setPayTime(o.getPayTime());
        vo.setTotalAmount(o.getTotalAmount());
        vo.setMemberDiscount(o.getMemberDiscount());
        vo.setDiscountAmount(o.getDiscountAmount());
        vo.setCouponAmount(o.getCouponAmount());
        vo.setFreightAmount(o.getFreightAmount());
        vo.setPayAmount(o.getPayAmount());
        vo.setRemark(o.getRemark());
        vo.setContact(o.getContact());
        vo.setMobile(o.getMobile());
        vo.setAddress(o.getAddress());
        vo.setExpressName(o.getExpressName());
        vo.setExpressNo(o.getExpressNo());
        vo.setCreateTime(o.getCreateTime());
        vo.setReceiveTime(o.getReceiveTime());
        return vo;
    }
}
