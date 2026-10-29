package com.minimall.module.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 会员标签 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_tag")
public class UserTag extends BaseEntity {

    private String name;

    /** 展示色，如 #f56c6c */
    private String color;

    private Integer sort;

    /** 1 启用 / 0 停用 */
    private Integer status;
}
