package com.minimall.module.integral.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("integral_order")
public class IntegralOrder extends BaseEntity {
    private String orderSn;
    private Long userId;
    private Long integralGoodsId;
    private String goodsName;
    private String goodsImage;
    private Long integral;
    private Integer num;
    private Long totalIntegral;
    private Integer status;
    private String contact;
    private String mobile;
    private String address;
    private String expressName;
    private String expressNo;
    private LocalDateTime expressTime;
}
