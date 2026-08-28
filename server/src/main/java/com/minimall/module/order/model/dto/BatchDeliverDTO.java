package com.minimall.module.order.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchDeliverDTO {
    private List<Long> orderIds;
    private Long expressId;
    private String expressName;
    private String expressNo;
}
