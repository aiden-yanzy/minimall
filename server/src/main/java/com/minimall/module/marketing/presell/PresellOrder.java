package com.minimall.module.marketing.presell;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("presell_order")
public class PresellOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderSn;
    private Long userId;
    private Long activityId;
    private Long goodsId;
    private Long goodsItemId;
    private String goodsName;
    private String goodsImage;
    private Long deposit;
    private Long finalPrice;
    private String paySn;
    private Integer status;
    private String contact;
    private String mobile;
    private String address;
    private String expressName;
    private String expressNo;
    private LocalDateTime createTime;
}
