package com.minimall.module.freeshipping.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("free_shipping")
public class FreeShipping extends BaseEntity {
    private String name;
    private Long threshold;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
