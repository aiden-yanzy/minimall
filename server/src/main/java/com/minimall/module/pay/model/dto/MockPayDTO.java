package com.minimall.module.pay.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MockPayDTO {
    @NotBlank(message = "订单号不能为空")
    private String orderSn;
    @NotNull(message = "支付方式不能为空")
    private Integer payType = 1;
}
