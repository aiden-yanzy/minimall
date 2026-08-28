package com.minimall.module.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class SystemRole extends BaseEntity {
    private String name;
    private String code;
    private String remark;
    private Integer status;
}
