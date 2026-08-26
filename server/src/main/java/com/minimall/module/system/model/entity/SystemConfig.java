package com.minimall.module.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("config")
public class SystemConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String value;
    private String remark;
    private LocalDateTime createTime;
}
