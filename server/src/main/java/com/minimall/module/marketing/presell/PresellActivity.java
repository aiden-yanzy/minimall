package com.minimall.module.marketing.presell;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("presell_activity")
public class PresellActivity extends BaseEntity {
    private String name;
    private Long goodsId;
    private Long goodsItemId;
    private Long deposit;
    private Long finalPrice;
    private Integer stock;
    private Integer sales;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime finalEndTime;
    private Integer status;
}
