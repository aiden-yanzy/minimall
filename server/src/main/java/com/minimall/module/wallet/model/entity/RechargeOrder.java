package com.minimall.module.wallet.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("recharge_order")
public class RechargeOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderSn;
    private Long userId;
    private Long templateId;
    private Long amount;
    private Long payAmount;
    private Integer status;
    private String paySn;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
