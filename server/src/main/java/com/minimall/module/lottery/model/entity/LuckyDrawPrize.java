package com.minimall.module.lottery.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("lucky_draw_prize")
public class LuckyDrawPrize {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long drawId;
    private String prizeName;
    private Integer prizeType;
    private Long prizeValue;
    private Integer weight;
    private Integer total;
    private Integer used;
    private Integer status;
}
