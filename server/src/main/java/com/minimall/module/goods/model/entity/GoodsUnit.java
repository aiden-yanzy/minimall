package com.minimall.module.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_unit")
public class GoodsUnit extends BaseEntity {
    private String name;
    private Integer sort;
    private Integer status;
}
