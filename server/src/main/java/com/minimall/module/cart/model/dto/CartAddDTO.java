package com.minimall.module.cart.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加购参数
 */
@Data
public class CartAddDTO {

    @NotNull(message = "请选择商品")
    private Long goodsId;

    @NotNull(message = "请选择规格")
    private Long goodsItemId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer num = 1;
}
