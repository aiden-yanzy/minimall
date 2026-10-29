package com.minimall.module.stat.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计报表返回结构（概览 / 明细 / 排行）
 */
public class StatVO {

    /** 每日金额/单量 */
    @Data
    public static class DayAmount {
        private String day;
        private Long orders;
        private Long amount;
    }

    /** 已支付汇总 */
    @Data
    public static class Summary {
        private Long orders;
        private Long amount;
        private Long buyers;
        /** 客单价（分） */
        private Long avgAmount;
    }

    /** 下单漏斗 */
    @Data
    public static class Funnel {
        private Long orders;
        private Long paid;
        private Long amount;
    }

    /** 销售明细行 */
    @Data
    public static class SaleDetail {
        private Long id;
        private String orderSn;
        private Long userId;
        private Long totalAmount;
        private Long payAmount;
        private Long freightAmount;
        private Integer orderStatus;
        private Integer payStatus;
        private Integer payType;
        private LocalDateTime createTime;
        private LocalDateTime payTime;
        private Long nums;
        private Long items;
    }

    /** 商品排行行 */
    @Data
    public static class GoodsRank {
        private Long goodsId;
        private String goodsName;
        private Long nums;
        private Long amount;
        private Long orderCount;
    }

    /** 概览响应 */
    @Data
    public static class Overview {
        private int days;
        private Summary summary;
        private Funnel funnel;
        private List<DayAmount> daily;
    }

    /** 明细分页响应 */
    @Data
    public static class DetailPage {
        private List<SaleDetail> records;
        private long total;
        private int page;
        private int size;
    }
}
