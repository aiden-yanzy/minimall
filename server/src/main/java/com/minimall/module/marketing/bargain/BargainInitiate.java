package com.minimall.module.marketing.bargain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bargain_initiate")
public class BargainInitiate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long userId;
    private Long currentPrice;
    private Integer helpedCount;
    private Integer status;
    private Long orderId;
    private LocalDateTime createTime;
}
