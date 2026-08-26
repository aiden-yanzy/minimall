package com.minimall.module.integral.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeDTO {

    @NotNull(message = "请选择兑换商品")
    private Long goodsId;

    @Min(value = 1, message = "数量至少为1")
    private Integer num = 1;

    private String contact;
    private String mobile;
    private String address;
}