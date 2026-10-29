package com.minimall.module.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.module.user.mapper.UserLevelMapper;
import com.minimall.module.user.mapper.UserMapper;
import com.minimall.module.user.mapper.UserTagMapper;
import com.minimall.module.user.mapper.UserTagRelMapper;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.model.entity.UserLevel;
import com.minimall.module.user.model.entity.UserTag;
import com.minimall.module.user.model.entity.UserTagRel;
import com.minimall.module.user.model.vo.MemberLevelVO;
import com.minimall.module.user.model.vo.UserTagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会员等级 + 会员标签。
 *
 * <p>等级口径：成长值 = 累计实付金额（user.total_consume，单位分）；
 * 取 upgrade_consume ≤ 成长值 的最高档，不足则落到最低档。
 */
@Service
@RequiredArgsConstructor
public class UserLevelService extends ServiceImpl<UserLevelMapper, UserLevel> {

    private final UserMapper userMapper;
    private final UserTagMapper tagMapper;
    private final UserTagRelMapper tagRelMapper;

    // ---------- 等级 ----------

    /** 会员折扣：返回折扣系数(0.95=95折)，无等级按 1.0 */
    public BigDecimal discountOf(Long levelId) {
        if (levelId == null) return BigDecimal.ONE;
        UserLevel level = this.getById(levelId);
        if (level == null || level.getDiscount() == null || level.getStatus() == null || level.getStatus() != 1) {
            return BigDecimal.ONE;
        }
        return level.getDiscount().divide(BigDecimal.TEN);
    }

    /** 启用中的等级，按档位升序 */
    public List<UserLevel> enabledLevels() {
        return this.list(Wrappers.<UserLevel>lambdaQuery()
                .eq(UserLevel::getStatus, 1)
                .orderByAsc(UserLevel::getLevel));
    }

    /** 按成长值算应得等级 */
    public UserLevel levelOfGrowth(long growth, List<UserLevel> levels) {
        UserLevel matched = null;
        for (UserLevel l : levels) {
            long require = l.getUpgradeConsume() == null ? 0L : l.getUpgradeConsume();
            if (growth >= require) {
                long matchedRequire = matched == null || matched.getUpgradeConsume() == null
                        ? -1L : matched.getUpgradeConsume();
                if (matched == null || require >= matchedRequire) matched = l;
            }
        }
        return matched;
    }

    /** 重算单个用户等级，返回应得等级 ID */
    @Transactional(rollbackFor = Exception.class)
    public Long refreshLevel(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return null;
        List<UserLevel> levels = enabledLevels();
        if (levels.isEmpty()) return user.getLevelId();
        long growth = user.getTotalConsume() == null ? 0L : user.getTotalConsume();
        UserLevel target = levelOfGrowth(growth, levels);
        if (target == null) return user.getLevelId();
        if (!target.getId().equals(user.getLevelId())) {
            userMapper.update(null, Wrappers.<User>lambdaUpdate()
                    .eq(User::getId, userId)
                    .set(User::getLevelId, target.getId()));
        }
        return target.getId();
    }

    /** 全量重算，返回等级变动人数 */
    @Transactional(rollbackFor = Exception.class)
    public int refreshAll() {
        List<UserLevel> levels = enabledLevels();
        if (levels.isEmpty()) return 0;
        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery());
        int changed = 0;
        for (User u : users) {
            long growth = u.getTotalConsume() == null ? 0L : u.getTotalConsume();
            UserLevel target = levelOfGrowth(growth, levels);
            if (target == null) continue;
            if (!target.getId().equals(u.getLevelId())) {
                userMapper.update(null, Wrappers.<User>lambdaUpdate()
                        .eq(User::getId, u.getId())
                        .set(User::getLevelId, target.getId()));
                changed++;
            }
        }
        return changed;
    }

    /** 会员中心：当前等级 + 下一档进度 */
    public MemberLevelVO memberLevel(Long userId) {
        MemberLevelVO vo = new MemberLevelVO();
        User user = userMapper.selectById(userId);
        if (user == null) return vo;
        long growth = user.getTotalConsume() == null ? 0L : user.getTotalConsume();
        vo.setTotalConsume(growth);
        List<UserLevel> levels = enabledLevels();
        if (levels.isEmpty()) return vo;

        List<UserLevel> sorted = new ArrayList<>(levels);
        sorted.sort(Comparator.comparing(l -> l.getUpgradeConsume() == null ? 0L : l.getUpgradeConsume()));

        UserLevel current = levelOfGrowth(growth, sorted);
        if (current == null) current = sorted.get(0);
        long curRequire = current.getUpgradeConsume() == null ? 0L : current.getUpgradeConsume();
        vo.setLevelName(current.getName());
        vo.setCurrentRequire(curRequire);
        vo.setDiscount(current.getDiscount());
        vo.setPrivilege(current.getPrivilege());

        UserLevel next = null;
        for (UserLevel l : sorted) {
            long require = l.getUpgradeConsume() == null ? 0L : l.getUpgradeConsume();
            if (require > curRequire) { next = l; break; }
        }
        if (next == null) {
            vo.setPercent(100);
            vo.setGap(0L);
            return vo;
        }
        long nextRequire = next.getUpgradeConsume() == null ? 0L : next.getUpgradeConsume();
        vo.setNextLevelName(next.getName());
        vo.setNextRequire(nextRequire);
        vo.setGap(Math.max(0L, nextRequire - growth));
        long span = nextRequire - curRequire;
        int percent = span <= 0 ? 100 : (int) Math.min(100, Math.max(0, (growth - curRequire) * 100 / span));
        vo.setPercent(percent);
        return vo;
    }

    // ---------- 标签 ----------

    public List<UserTag> tags() {
        return tagMapper.selectList(Wrappers.<UserTag>lambdaQuery()
                .orderByAsc(UserTag::getSort).orderByAsc(UserTag::getId));
    }

    public Long saveTag(UserTag tag) {
        if (tag.getId() == null) tagMapper.insert(tag);
        else tagMapper.updateById(tag);
        return tag.getId();
    }

    /** 删除标签（连带清理关联） */
    @Transactional(rollbackFor = Exception.class)
    public void removeTag(Long tagId) {
        tagMapper.deleteById(tagId);
        tagRelMapper.delete(Wrappers.<UserTagRel>lambdaQuery().eq(UserTagRel::getTagId, tagId));
    }

    /** 覆盖式设置某用户的标签 */
    @Transactional(rollbackFor = Exception.class)
    public void setUserTags(Long userId, List<Long> tagIds) {
        tagRelMapper.delete(Wrappers.<UserTagRel>lambdaQuery().eq(UserTagRel::getUserId, userId));
        if (tagIds == null || tagIds.isEmpty()) return;
        for (Long tagId : tagIds.stream().filter(java.util.Objects::nonNull).distinct().toList()) {
            UserTagRel rel = new UserTagRel();
            rel.setUserId(userId);
            rel.setTagId(tagId);
            tagRelMapper.insert(rel);
        }
    }

    public List<Long> tagIdsOfUser(Long userId) {
        return tagRelMapper.selectList(Wrappers.<UserTagRel>lambdaQuery()
                        .eq(UserTagRel::getUserId, userId)).stream()
                .map(UserTagRel::getTagId).toList();
    }

    /** 某标签下的用户 ID（列表筛选用） */
    public List<Long> userIdsOfTag(Long tagId) {
        return tagRelMapper.selectList(Wrappers.<UserTagRel>lambdaQuery()
                        .eq(UserTagRel::getTagId, tagId)).stream()
                .map(UserTagRel::getUserId).distinct().toList();
    }

    /** 批量取标签并按 userId 分组（列表回填，避免 N+1） */
    public Map<Long, List<UserTagVO>> tagsOfUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        List<UserTagRel> rels = tagRelMapper.selectList(Wrappers.<UserTagRel>lambdaQuery()
                .in(UserTagRel::getUserId, userIds));
        if (rels.isEmpty()) return Map.of();
        Map<Long, UserTag> tagMap = tagMapper.selectBatchIds(
                        rels.stream().map(UserTagRel::getTagId).distinct().toList()).stream()
                .collect(Collectors.toMap(UserTag::getId, t -> t, (a, b) -> a));
        Map<Long, List<UserTagVO>> result = new HashMap<>();
        for (UserTagRel rel : rels) {
            UserTag t = tagMap.get(rel.getTagId());
            if (t == null) continue;
            UserTagVO vo = new UserTagVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            vo.setColor(t.getColor());
            result.computeIfAbsent(rel.getUserId(), k -> new ArrayList<>()).add(vo);
        }
        return result;
    }
}
