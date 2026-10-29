package com.minimall.module.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单物流轨迹（真实落库，替代原先按状态拼出来的模拟数据）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_trace")
public class OrderTrace extends BaseEntity {

    private Long orderId;

    /** 发生时间（可人工录入，默认 now） */
    private LocalDateTime traceTime;

    /** 轨迹文案，如「包裹已到达【杭州转运中心】」 */
    private String description;

    /** 操作人：admin:<id> / system */
    private String operator;
}
