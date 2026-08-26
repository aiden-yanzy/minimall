package com.minimall.module.pay.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import com.minimall.common.result.R;
import com.minimall.module.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付回调（预留微信/支付宝真实签名校验结构，P8 阶段为占位实现）
 * 接入真实支付时：微信验签(V3) / 支付宝验签(RSA2)，再调用 orderService.markPaid
 */
@Slf4j
@Tag(name = "支付回调(预留)")
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayNotifyController {

    private final OrderService orderService;

    @Operation(summary = "微信支付回调(预留)")
    @PostMapping("/notify/wechat")
    public String wechatNotify(@RequestBody String body) {
        log.info("微信回调报文: {}", body);
        try {
            // TODO: 微信支付 V3 验签 + 解密，这里按模拟约定直接处理
            // 模拟：body 为 JSON {"orderSn":"MM...","out_trade_no":"..."}
            JSONObject json = new JSONObject(body);
            String orderSn = json.getStr("orderSn");
            if (orderSn != null) {
                orderService.markPaid(orderSn, 1, "WX" + System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("微信回调处理失败", e);
            return "fail";
        }
        return "success";
    }

    @Operation(summary = "支付宝回调(预留)")
    @PostMapping("/notify/alipay")
    public String alipayNotify(@org.springframework.web.bind.annotation.RequestParam Map<String, String> params) {
        log.info("支付宝回调参数: {}", params);
        try {
            // TODO: RSA2 验签
            String orderSn = MapUtil.getStr(params, "out_trade_no");
            String tradeStatus = MapUtil.getStr(params, "trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus) && orderSn != null) {
                orderService.markPaid(orderSn, 2, "ALI" + System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("支付宝回调处理失败", e);
            return "fail";
        }
        return "success";
    }
}