package com.minimall.module.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_brand")
public class GoodsBrand extends BaseEntity {
    private String name;
    private String logo;
    private Integer sort;
    private Integer isShow;
}
