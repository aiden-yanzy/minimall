package com.minimall.module.sign.model.vo;

import com.minimall.module.sign.model.entity.SignDaily;
import lombok.Data;

@Data
public class SignVO {
    private SignDaily config;
    private boolean signedToday;
    private int continuousDays;
    /** 本次签到获得积分（含奖励） */
    private Integer rewardIntegral;
    /** 连续达标额外积分 */
    private Integer bonusIntegral;
}