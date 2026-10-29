package com.minimall.module.user.model.dto;

import lombok.Data;

import java.util.List;

/** 给用户设置标签（覆盖式） */
@Data
public class UserTagDTO {

    private List<Long> tagIds;
}
