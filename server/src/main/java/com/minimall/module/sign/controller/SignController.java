package com.minimall.module.sign.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.sign.model.entity.SignDaily;
import com.minimall.module.sign.model.vo.SignVO;
import com.minimall.module.sign.service.SignService;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端签到
 */
@Tag(name = "C端-签到")
@RestController
@RequestMapping("/shop/sign")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class SignController {

    private final SignService signService;
    private final UserService userService;

    @Operation(summary = "签到状态")
    @GetMapping("/status")
    public R<SignVO> status() {
        return R.ok(signService.status(userService.getCurrentUserId()));
    }

    @Operation(summary = "每日打卡")
    @PostMapping("/do")
    public R<SignVO> doSign() {
        SignVO vo = signService.doSign(userService.getCurrentUserId());
        return R.ok("签到成功 +" + vo.getRewardIntegral() + "积分", vo);
    }

    @Operation(summary = "本月签到日历")
    @GetMapping("/month")
    public R<List<String>> month(@RequestParam(required = false) String month) {
        return R.ok(signService.monthDays(userService.getCurrentUserId(), month));
    }
}