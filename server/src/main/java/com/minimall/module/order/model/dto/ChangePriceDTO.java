package com.minimall.module.order.model.dto;

import lombok.Data;

@Data
public class ChangePriceDTO {
    /** 改价后的应付金额（分） */
    private Long payAmount;
    /** 改价原因（必填，便于审计） */
    private String reason;
}
