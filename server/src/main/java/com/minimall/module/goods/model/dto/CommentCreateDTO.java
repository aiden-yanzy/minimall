package com.minimall.module.goods.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentCreateDTO {
    @NotNull(message = "订单不能为空")
    private Long orderId;
    @NotNull(message = "订单商品不能为空")
    private Long orderGoodsId;
    private Integer score = 5;
    private String content;
}
