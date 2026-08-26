package com.minimall.module.distribution.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("distribution_order_goods")
public class DistributionOrderGoods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long orderGoodsId;
    private Long buyerUserId;
    private Long sellerUserId;
    private Long goodsAmount;
    private Integer rate;
    private Long commission;
    private Integer status;
    private LocalDateTime createTime;
}
