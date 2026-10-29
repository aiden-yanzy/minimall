package com.minimall.module.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 会员-标签关联 */
@Data
@TableName("user_tag_rel")
public class UserTagRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long tagId;

    private LocalDateTime createTime;
}
