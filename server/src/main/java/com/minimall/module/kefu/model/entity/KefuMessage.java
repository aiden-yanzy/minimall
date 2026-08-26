package com.minimall.module.kefu.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("kefu_message")
public class KefuMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private String adminReply;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}
