package com.minimall.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minimall.common.result.R;
import com.minimall.module.user.mapper.UserLevelMapper;
import com.minimall.module.user.mapper.UserMapper;
import com.minimall.module.user.model.dto.UserTagDTO;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.model.entity.UserLevel;
import com.minimall.module.user.model.entity.UserTag;
import com.minimall.module.user.model.vo.AdminUserVO;
import com.minimall.module.user.service.UserLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台用户 / 会员等级 / 会员标签
 */
@Tag(name = "后台-用户/等级/标签")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminUserController {

    private final UserMapper userMapper;
    private final UserLevelMapper levelMapper;
    private final UserLevelService levelService;

    // ---------- 用户 ----------

    @Operation(summary = "用户分页（支持按标签筛选，回填标签与等级名）")
    @GetMapping("/user")
    public R<IPage<AdminUserVO>> users(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Long tagId) {
        // 标签筛选：先取该标签下的用户 ID，再进分页条件（别在内存里过滤）
        List<Long> idsOfTag = tagId == null ? null : levelService.userIdsOfTag(tagId);
        if (idsOfTag != null && idsOfTag.isEmpty()) {
            return R.ok(new Page<>(page, size));
        }
        IPage<User> raw = userMapper.selectPage(new Page<>(page, size),
                Wrappers.<User>lambdaQuery()
                        .and(StrUtil.isNotBlank(keyword), q -> q.like(User::getMobile, keyword).or().like(User::getNickname, keyword))
                        .eq(status != null, User::getStatus, status)
                        .in(idsOfTag != null, User::getId, idsOfTag)
                        .orderByDesc(User::getId));

        List<User> records = raw.getRecords();
        Map<Long, List<com.minimall.module.user.model.vo.UserTagVO>> tagMap =
                levelService.tagsOfUsers(records.stream().map(User::getId).toList());
        Map<Long, String> levelNames = levelMapper.selectList(null).stream()
                .collect(Collectors.toMap(UserLevel::getId, UserLevel::getName, (a, b) -> a));

        IPage<AdminUserVO> out = raw.convert(u -> {
            AdminUserVO vo = AdminUserVO.from(u);
            vo.setTags(tagMap.getOrDefault(u.getId(), List.of()));
            vo.setLevelName(levelNames.get(u.getLevelId()));
            return vo;
        });
        return R.ok(out);
    }

    @Operation(summary = "用户详情（含已打标签）")
    @GetMapping("/user/{id}")
    public R<AdminUserVO> user(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u == null) return R.ok(null);
        AdminUserVO vo = AdminUserVO.from(u);
        vo.setTags(levelService.tagsOfUsers(List.of(id)).getOrDefault(id, List.of()));
        UserLevel level = u.getLevelId() == null ? null : levelMapper.selectById(u.getLevelId());
        vo.setLevelName(level == null ? null : level.getName());
        return R.ok(vo);
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

    @Operation(summary = "给用户打标签（覆盖式）")
    @SaCheckPermission(value = "user:manage", type = "admin")
    @PostMapping("/user/{id}/tags")
    public R<Void> setUserTags(@PathVariable Long id, @RequestBody UserTagDTO dto) {
        levelService.setUserTags(id, dto.getTagIds());
        return R.ok();
    }

    @Operation(summary = "该用户已打标签 ID")
    @GetMapping("/user/{id}/tags")
    public R<List<Long>> userTags(@PathVariable Long id) {
        return R.ok(levelService.tagIdsOfUser(id));
    }

    // ---------- 会员标签 ----------

    @Operation(summary = "标签列表")
    @GetMapping("/user/tag")
    public R<List<UserTag>> tags() {
        return R.ok(levelService.tags());
    }

    @Operation(summary = "保存标签")
    @SaCheckPermission(value = "user:manage", type = "admin")
    @PostMapping("/user/tag")
    public R<Long> saveTag(@RequestBody UserTag tag) {
        if (StrUtil.isBlank(tag.getName())) return R.fail("标签名称不能为空");
        if (tag.getColor() == null || tag.getColor().isBlank()) tag.setColor("#409eff");
        if (tag.getSort() == null) tag.setSort(0);
        if (tag.getStatus() == null) tag.setStatus(1);
        return R.ok("保存成功", levelService.saveTag(tag));
    }

    @Operation(summary = "删除标签")
    @SaCheckPermission(value = "user:manage", type = "admin")
    @DeleteMapping("/user/tag/{id}")
    public R<Void> removeTag(@PathVariable Long id) {
        levelService.removeTag(id);
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

    @Operation(summary = "按成长值重算单个用户等级")
    @PostMapping("/user/{id}/refresh-level")
    public R<Long> refreshLevel(@PathVariable Long id) {
        return R.ok("已重算", levelService.refreshLevel(id));
    }

    @Operation(summary = "按成长值全量重算等级（返回变动人数）")
    @SaCheckPermission(value = "user:level", type = "admin")
    @PostMapping("/user/refresh-levels")
    public R<Integer> refreshLevels() {
        return R.ok("已重算", levelService.refreshAll());
    }
}
