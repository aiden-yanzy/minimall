package com.minimall.module.user.model.vo;

import lombok.Data;

/** 会员中心：当前等级 + 下一档进度 */
@Data
public class MemberLevelVO {

    private String levelName;

    /** 当前累计消费（分） */
    private Long totalConsume;

    /** 当前等级所需成长值（分） */
    private Long currentRequire;

    /** 下一档名称，已是最高档时为空 */
    private String nextLevelName;

    /** 下一档所需成长值（分） */
    private Long nextRequire;

    /** 距下一档还差多少（分） */
    private Long gap;

    /** 升级进度 0-100 */
    private Integer percent;

    /** 当前折扣（10=不打折） */
    private java.math.BigDecimal discount;

    private String privilege;
}
