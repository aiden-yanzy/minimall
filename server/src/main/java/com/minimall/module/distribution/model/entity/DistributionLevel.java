package com.minimall.module.distribution.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("distribution_level")
public class DistributionLevel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer level;
    private Integer commissionRate;

    /** 升级门槛：累计佣金（分），0 = 无门槛 */
    private Long upgradeAmount;
    private String upgradeCondition;
    private Integer status;
    private LocalDateTime createTime;
}
