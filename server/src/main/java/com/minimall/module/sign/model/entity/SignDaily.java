package com.minimall.module.sign.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sign_daily")
public class SignDaily extends BaseEntity {
    private Integer integral;
    private Integer continuousDays;
    private Integer continuousReward;
    private Integer status;
}
