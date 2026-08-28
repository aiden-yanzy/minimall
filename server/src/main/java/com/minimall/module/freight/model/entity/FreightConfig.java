package com.minimall.module.freight.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("freight_config")
public class FreightConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private String regionIds;
    private String regionName;
    private Integer firstUnit;
    private Long firstPrice;
    private Integer continueUnit;
    private Long continuePrice;
}
