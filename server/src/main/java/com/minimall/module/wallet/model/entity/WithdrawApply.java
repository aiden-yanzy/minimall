package com.minimall.module.wallet.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("withdraw_apply")
public class WithdrawApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long amount;
    private Integer type;
    private String accountInfo;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
