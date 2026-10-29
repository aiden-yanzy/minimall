package com.minimall.module.goods.model.vo;

import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情 VO（C 端）
 */
@Data
public class GoodsVO {

    private Long id;
    private String name;
    private String subtitle;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String image;
    private String video;
    private Long minPrice;
    private Long maxPrice;
    private Long marketPrice;
    private Integer stock;
    private Integer sales;
    private String content;
    private Integer specType;
    private Integer status;
    private String unitName;

    /** 轮播图 */
    private List<String> images;

    /** SKU 列表 */
    private List<ItemVO> items;

    /** 规格组（多规格时有值） */
    private List<SpecVO> specs;

    @Data
    public static class SpecVO {
        private Long id;
        private String name;
        private List<SpecValueVO> values;
    }

    @Data
    public static class SpecValueVO {
        private Long id;
        private String value;
        private String image;
    }

    @Data
    public static class ItemVO {
        private Long id;
        private Long goodsId;
        private String itemSn;
        private String image;
        private Long price;
        private Long marketPrice;
        private Integer stock;
        private String specValueStr;

        public static ItemVO from(GoodsItem it) {
            ItemVO vo = new ItemVO();
            vo.setId(it.getId());
            vo.setGoodsId(it.getGoodsId());
            vo.setItemSn(it.getItemSn());
            vo.setImage(it.getImage());
            vo.setPrice(it.getPrice());
            vo.setMarketPrice(it.getMarketPrice());
            vo.setStock(it.getStock());
            vo.setSpecValueStr(it.getSpecValueStr());
            return vo;
        }
    }

    public static GoodsVO from(Goods g) {
        GoodsVO vo = new GoodsVO();
        vo.setId(g.getId());
        vo.setName(g.getName());
        vo.setSubtitle(g.getSubtitle());
        vo.setCategoryId(g.getCategoryId());
        vo.setBrandId(g.getBrandId());
        vo.setImage(g.getImage());
        vo.setVideo(g.getVideo());
        vo.setMinPrice(g.getMinPrice());
        vo.setMaxPrice(g.getMaxPrice());
        vo.setMarketPrice(g.getMarketPrice());
        vo.setStock(g.getStock());
        vo.setSales(g.getSales());
        vo.setContent(g.getContent());
        vo.setSpecType(g.getSpecType());
        vo.setStatus(g.getStatus());
        return vo;
    }
}
