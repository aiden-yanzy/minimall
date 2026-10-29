package com.minimall.module.user.model.vo;

import lombok.Data;

/** 用户列表里的标签（含颜色） */
@Data
public class UserTagVO {

    private Long id;

    private String name;

    private String color;
}
