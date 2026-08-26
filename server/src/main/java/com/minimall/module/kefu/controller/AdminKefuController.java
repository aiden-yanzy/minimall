package com.minimall.module.kefu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.result.R;
import com.minimall.module.kefu.mapper.KefuMessageMapper;
import com.minimall.module.kefu.model.entity.KefuMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台在线客服（查看留言 + 回复）
 */
@Tag(name = "后台-在线客服")
@RestController
@RequestMapping("/admin/kefu")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminKefuController {

    private final KefuMessageMapper messageMapper;

    @Operation(summary = "留言列表")
    @GetMapping("/messages")
    public R<List<KefuMessage>> messages(@RequestParam(required = false) Long userId) {
        return R.ok(messageMapper.selectList(Wrappers.<KefuMessage>lambdaQuery()
                .eq(userId != null, KefuMessage::getUserId, userId)
                .orderByDesc(KefuMessage::getId)));
    }

    @Operation(summary = "回复留言")
    @PostMapping("/reply/{id}")
    public R<Void> reply(@PathVariable Long id, @RequestParam String reply) {
        messageMapper.update(null, Wrappers.<KefuMessage>lambdaUpdate()
                .eq(KefuMessage::getId, id)
                .set(KefuMessage::getAdminReply, reply)
                .set(KefuMessage::getReplyTime, LocalDateTime.now()));
        return R.ok("已回复", null);
    }
}