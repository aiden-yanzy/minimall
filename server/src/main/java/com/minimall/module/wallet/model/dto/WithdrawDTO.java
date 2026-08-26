package com.minimall.module.wallet.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WithdrawDTO {
    @NotNull(message = "提现金额不能为空")
    @Min(value = 1, message = "提现金额最小1分")
    private Long amount;
    private Integer type = 1;
    private String accountInfo;
}
