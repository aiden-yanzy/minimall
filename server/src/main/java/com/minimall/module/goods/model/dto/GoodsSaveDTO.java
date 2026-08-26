package com.minimall.module.goods.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 后台商品保存参数（创建/编辑）
 */
@Data
public class GoodsSaveDTO {

    private Long id;

    @NotBlank(message = "商品名不能为空")
    private String name;

    private String subtitle;
    private Long categoryId;
    private Long brandId;
    private Long unitId;
    private String image;
    private String video;
    private Long marketPrice;
    private Integer virtualSales;
    private String content;
    private Long deliveryTemplateId;
    private Integer specType = 0;
    private Integer recommend = 0;
    private Integer sort = 0;
    private Integer refundType = 1;

    /** 轮播图/视频 */
    private List<String> images;

    /** SKU 列表 */
    @NotNull(message = "至少需要一个SKU")
    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        private Long id;
        private String itemSn;
        private String image;
        @NotNull(message = "SKU价格不能为空")
        @Min(value = 0, message = "SKU价格不能为负")
        private Long price;
        private Long costPrice;
        private Long marketPrice;
        @NotNull(message = "SKU库存不能为空")
        @Min(value = 0, message = "SKU库存不能为负")
        private Integer stock;
        private BigDecimal weight = BigDecimal.ZERO;
        private BigDecimal volume = BigDecimal.ZERO;
        private String specValueIds;
        private String specValueStr;
        private Integer status = 1;
    }
}
