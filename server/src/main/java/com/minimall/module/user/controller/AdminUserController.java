package com.minimall.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minimall.common.result.R;
import com.minimall.module.user.mapper.UserMapper;
import com.minimall.module.user.mapper.UserLevelMapper;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.model.entity.UserLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台用户与会员等级管理
 */
@Tag(name = "后台-用户/等级")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminUserController {

    private final UserMapper userMapper;
    private final UserLevelMapper levelMapper;

    // ---------- 用户 ----------

    @Operation(summary = "用户分页")
    @GetMapping("/user")
    public R<IPage<User>> users(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "10") long size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer status) {
        return R.ok(userMapper.selectPage(new Page<>(page, size),
                Wrappers.<User>lambdaQuery()
                        .and(StrUtil.isNotBlank(keyword), q -> q.like(User::getMobile, keyword).or().like(User::getNickname, keyword))
                        .eq(status != null, User::getStatus, status)
                        .orderByDesc(User::getId)));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/user/{id}")
    public R<User> user(@PathVariable Long id) {
        return R.ok(userMapper.selectById(id));
    }

    @Operation(summary = "调整等级/状态")
    @PutMapping("/user/{id}")
    public R<Void> updateUser(@PathVariable Long id,
                              @RequestParam(required = false) Long levelId,
                              @RequestParam(required = false) Integer status) {
        userMapper.update(null, Wrappers.<User>lambdaUpdate()
                .eq(User::getId, id)
                .set(levelId != null, User::getLevelId, levelId)
                .set(status != null, User::getStatus, status));
        return R.ok();
    }

    // ---------- 会员等级 ----------

    @Operation(summary = "等级列表")
    @GetMapping("/level")
    public R<List<UserLevel>> levels() {
        return R.ok(levelMapper.selectList(Wrappers.<UserLevel>lambdaQuery().orderByAsc(UserLevel::getLevel)));
    }

    @Operation(summary = "保存等级")
    @PostMapping("/level")
    public R<Long> saveLevel(@RequestBody UserLevel level) {
        if (level.getId() == null) levelMapper.insert(level);
        else levelMapper.updateById(level);
        return R.ok("保存成功", level.getId());
    }

    @Operation(summary = "删除等级")
    @PostMapping("/level/remove/{id}")
    public R<Void> removeLevel(@PathVariable Long id) {
        levelMapper.deleteById(id);
        return R.ok();
    }
}