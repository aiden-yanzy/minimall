package com.minimall.module.order.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TraceAddDTO {
    /** 轨迹文案，必填 */
    private String description;
    /** 发生时间，留空取当前时间 */
    private LocalDateTime traceTime;
}
