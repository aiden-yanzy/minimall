package com.minimall.module.wallet.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recharge_template")
public class RechargeTemplate extends BaseEntity {
    private String name;
    private Long amount;
    private Long giftAmount;
    private Integer status;
}
