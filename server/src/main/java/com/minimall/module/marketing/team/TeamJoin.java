package com.minimall.module.marketing.team;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_join")
public class TeamJoin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long foundId;
    private Long activityId;
    private Long userId;
    private Long orderId;
    private LocalDateTime createTime;
}
