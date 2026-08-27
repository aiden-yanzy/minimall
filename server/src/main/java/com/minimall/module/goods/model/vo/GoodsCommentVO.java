package com.minimall.module.goods.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GoodsCommentVO {
    private Long id;
    private Long goodsId;
    private Integer score;
    private String content;
    private String reply;
    private String nickname;
    private String avatar;
    private LocalDateTime createTime;
}
