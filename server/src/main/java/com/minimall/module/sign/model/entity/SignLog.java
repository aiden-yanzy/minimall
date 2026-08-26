package com.minimall.module.sign.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("sign_log")
public class SignLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate signDate;
    private Integer integral;
}
