package com.minimall.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.exception.BusinessException;
import com.minimall.module.system.mapper.SystemRoleMapper;
import com.minimall.module.system.mapper.MenuMapper;
import com.minimall.module.system.mapper.RoleMenuMapper;
import com.minimall.module.system.model.entity.SystemRole;
import com.minimall.module.system.model.entity.Menu;
import com.minimall.module.system.model.entity.RoleMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final SystemRoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    /** 角色列表（含菜单ids） */
    public List<SystemRole> roles() {
        return roleMapper.selectList(Wrappers.<SystemRole>lambdaQuery().orderByAsc(SystemRole::getId));
    }

    public List<Menu> menus() {
        return menuMapper.selectList(Wrappers.<Menu>lambdaQuery().orderByAsc(Menu::getSort));
    }

    public List<RoleMenu> roleMenus(Long roleId) {
        return roleMenuMapper.selectList(Wrappers.<RoleMenu>lambdaQuery().eq(RoleMenu::getRoleId, roleId));
    }

    /** 保存角色 + 重配菜单权限 */
    @Transactional(rollbackFor = Exception.class)
    public Long saveRole(SystemRole role, List<Long> menuIds) {
        if (role.getName() == null || role.getName().isBlank()) throw new BusinessException("角色名不能为空");
        if (role.getId() == null) {
            role.setCode(role.getCode() == null || role.getCode().isBlank()
                    ? "role" + System.currentTimeMillis() : role.getCode());
            roleMapper.insert(role);
        } else {
            roleMapper.updateById(role);
        }
        roleMenuMapper.delete(Wrappers.<RoleMenu>lambdaQuery().eq(RoleMenu::getRoleId, role.getId()));
        if (menuIds != null) {
            for (Long mid : menuIds) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(role.getId());
                rm.setMenuId(mid);
                roleMenuMapper.insert(rm);
            }
        }
        return role.getId();
    }

    /** 权限码列表（接口鉴权用） */
    public List<String> permissionCodes(Long roleId) {
        if (roleId == null) return List.of();
        List<Long> menuIds = roleMenuMapper.selectList(Wrappers.<RoleMenu>lambdaQuery()
                .eq(RoleMenu::getRoleId, roleId)).stream().map(RoleMenu::getMenuId).toList();
        if (menuIds.isEmpty()) return List.of();
        return menuMapper.selectBatchIds(menuIds).stream()
                .filter(m -> m.getPermission() != null && !m.getPermission().isBlank())
                .map(Menu::getPermission).toList();
    }
}