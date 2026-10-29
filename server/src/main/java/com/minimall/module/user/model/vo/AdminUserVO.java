package com.minimall.module.user.model.vo;

import com.minimall.module.user.model.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 后台用户列表项（带标签与等级名） */
@Data
public class AdminUserVO {

    private Long id;
    private String sn;
    private String nickname;
    private String avatar;
    private String mobile;
    private Integer sex;
    private Long levelId;
    private String levelName;
    private Long balance;
    private Long integral;
    private Long totalConsume;
    private Integer totalOrderNum;
    private Integer status;
    private String channel;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;

    /** 已打标签 */
    private List<UserTagVO> tags;

    public static AdminUserVO from(User u) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(u.getId());
        vo.setSn(u.getSn());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setMobile(u.getMobile());
        vo.setSex(u.getSex());
        vo.setLevelId(u.getLevelId());
        vo.setBalance(u.getBalance());
        vo.setIntegral(u.getIntegral());
        vo.setTotalConsume(u.getTotalConsume());
        vo.setTotalOrderNum(u.getTotalOrderNum());
        vo.setStatus(u.getStatus());
        vo.setChannel(u.getChannel());
        vo.setLastLoginTime(u.getLastLoginTime());
        vo.setCreateTime(u.getCreateTime());
        return vo;
    }
}
