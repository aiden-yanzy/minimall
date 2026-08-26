package com.minimall.module.goods.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品轮播图/视频
 */
@Data
@TableName("goods_image")
public class GoodsImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId;
    private String url;
    private Integer sort;
}
