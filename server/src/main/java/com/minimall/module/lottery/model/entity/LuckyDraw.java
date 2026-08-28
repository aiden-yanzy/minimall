package com.minimall.module.lottery.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lucky_draw")
public class LuckyDraw extends BaseEntity {
    private String name;
    private Long integralCost;
    private Integer status;
}
