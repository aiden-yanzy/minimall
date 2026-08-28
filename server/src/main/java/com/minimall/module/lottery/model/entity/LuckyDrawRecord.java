package com.minimall.module.lottery.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("lucky_draw_record")
public class LuckyDrawRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long drawId;
    private Long userId;
    private String prizeName;
    private Integer prizeType;
    private Long prizeValue;
    private LocalDateTime createTime;
}
