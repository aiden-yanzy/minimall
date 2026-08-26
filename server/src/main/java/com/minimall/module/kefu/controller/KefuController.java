package com.minimall.module.kefu.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.R;
import com.minimall.module.kefu.mapper.KefuMessageMapper;
import com.minimall.module.kefu.model.entity.KefuMessage;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 在线客服（简化：留言 + 客服回复）
 */
@Tag(name = "C端-在线客服")
@RestController
@RequestMapping("/shop/kefu")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class KefuController {

    private final KefuMessageMapper messageMapper;
    private final UserService userService;

    @Operation(summary = "发送留言")
    @PostMapping("/send")
    public R<Long> send(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isBlank()) throw new BusinessException("留言内容不能为空");
        KefuMessage m = new KefuMessage();
        m.setUserId(userService.getCurrentUserId());
        m.setContent(content);
        messageMapper.insert(m);
        return R.ok("已发送", m.getId());
    }

    @Operation(summary = "我的会话记录")
    @GetMapping("/messages")
    public R<List<KefuMessage>> messages() {
        return R.ok(messageMapper.selectList(Wrappers.<KefuMessage>lambdaQuery()
                .eq(KefuMessage::getUserId, userService.getCurrentUserId())
                .orderByDesc(KefuMessage::getId)));
    }
}