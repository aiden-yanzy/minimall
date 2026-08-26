package com.minimall.module.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seckill_activity")
public class SeckillActivity extends BaseEntity {
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer sort;
}
