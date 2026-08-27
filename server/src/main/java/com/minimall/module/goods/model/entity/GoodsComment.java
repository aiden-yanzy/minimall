package com.minimall.module.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_comment")
public class GoodsComment extends BaseEntity {
    private Long orderId;
    private Long orderGoodsId;
    private Long userId;
    private Long goodsId;
    private Long goodsItemId;
    private Integer score;
    private Integer descriptionScore;
    private Integer serviceScore;
    private String content;
    private String reply;
    private Integer isShow;
}
