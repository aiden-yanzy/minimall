package com.minimall.module.marketing.team;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team_activity")
public class TeamActivity extends BaseEntity {
    private String name;
    private Long goodsId;
    private Long goodsItemId;
    private Long teamPrice;
    private Integer teamNum;
    private Integer perLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
