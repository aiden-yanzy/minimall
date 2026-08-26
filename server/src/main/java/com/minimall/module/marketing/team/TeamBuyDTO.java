package com.minimall.module.marketing.team;

import lombok.Data;

@Data
public class TeamBuyDTO {
    private Long activityId;
    private Integer num = 1;
    private String contact;
    private String mobile;
    private String address;
    private Integer payType = 1;
}