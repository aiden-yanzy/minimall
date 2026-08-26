package com.minimall.module.user.model.vo;

import com.minimall.module.user.model.entity.User;
import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String sn;
    private String nickname;
    private String avatar;
    private String mobile;
    private Integer sex;
    private Long levelId;
    private Long balance;
    private Long integral;

    public static UserVO from(User u) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setSn(u.getSn());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setMobile(u.getMobile());
        vo.setSex(u.getSex());
        vo.setLevelId(u.getLevelId());
        vo.setBalance(u.getBalance());
        vo.setIntegral(u.getIntegral());
        return vo;
    }
}
