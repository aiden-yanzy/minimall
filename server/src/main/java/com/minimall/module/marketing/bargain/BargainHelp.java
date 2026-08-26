package com.minimall.module.marketing.bargain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bargain_help")
public class BargainHelp {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long initiateId;
    private Long helperUserId;
    private Long reduceAmount;
    private LocalDateTime createTime;
}
