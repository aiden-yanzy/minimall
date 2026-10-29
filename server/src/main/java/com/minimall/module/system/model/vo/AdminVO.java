package com.minimall.module.system.model.vo;

import com.minimall.module.system.model.entity.Admin;
import lombok.Data;

@Data
public class AdminVO {
    private Long id;
    private String account;
    private String name;
    private String roleName;
    private String roleCode;
    private String avatar;

    /** 权限码集合（* 表示全部） */
    private java.util.List<String> permissions;

    public static AdminVO from(Admin a) {
        AdminVO vo = new AdminVO();
        vo.setId(a.getId());
        vo.setAccount(a.getAccount());
        vo.setName(a.getName());
        vo.setRoleName(a.getRoleName());
        vo.setAvatar(a.getAvatar());
        return vo;
    }
}
