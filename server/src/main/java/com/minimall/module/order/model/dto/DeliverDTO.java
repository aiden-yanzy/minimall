package com.minimall.module.order.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发货参数
 */
@Data
public class DeliverDTO {

    @NotNull(message = "请选择快递公司")
    private Long expressId;

    private String expressName;

    @NotBlank(message = "快递单号不能为空")
    private String expressNo;
}
