package com.minimall.module.marketing.bargain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bargain_activity")
public class BargainActivity extends BaseEntity {
    private String name;
    private Long goodsId;
    private Long goodsItemId;
    private Long basePrice;
    private Long floorPrice;
    private Integer helpNum;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
