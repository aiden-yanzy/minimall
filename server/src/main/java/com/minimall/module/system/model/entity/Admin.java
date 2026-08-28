package com.minimall.module.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 后台管理员
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin")
public class Admin extends BaseEntity {

    /** 登录账号 */
    private String account;

    /** 密码（BCrypt） */
    private String password;

    /** 姓名 */
    private String name;

    /** 角色名称（P0 先用字符串，后续换 role 表） */
    private String roleName;

    /** 角色 id（RBAC） */
    private Long roleId;

    /** 头像 */
    private String avatar;

    /** 1正常 0禁用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
}
