package com.minimall.module.lottery.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("register_award")
public class RegisterAward {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer awardType;
    private Long awardValue;
    private Long couponId;
    private Integer status;
    private LocalDateTime createTime;
}
