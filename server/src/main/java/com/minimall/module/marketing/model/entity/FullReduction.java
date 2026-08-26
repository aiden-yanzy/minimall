package com.minimall.module.marketing.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 全场满减活动
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("full_reduction")
public class FullReduction extends BaseEntity {

    private String name;
    /** 满(分) */
    private Long threshold;
    /** 减(分) */
    private Long reduce;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}