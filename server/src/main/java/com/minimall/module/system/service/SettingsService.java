package com.minimall.module.system.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.module.system.mapper.SystemConfigMapper;
import com.minimall.module.system.model.entity.SystemConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 设置中心：分组键值配置存 config 表（value=JSON）
 */
@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SystemConfigMapper configMapper;
    private static final ObjectMapper OM = new ObjectMapper();

    /** 分组默认值 */
    private static final Map<String, Map<String, Object>> DEFAULTS = Map.of(
            "site", Map.of("name", "Minimall商城", "logo", "", "phone", "400-000-0000", "notice", "欢迎光临 Minimall 商城！"),
            "order", Map.of("close_minutes", 30, "auto_receive_days", 15),
            "delivery", Map.of("default_express", "顺丰速运"),
            "pay", Map.of("mock_pay", true, "wechat_mchid", "", "alipay_appid", ""));

    public Map<String, Map<String, Object>> all() {
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        DEFAULTS.forEach((k, v) -> map.put(k, read(k)));
        return map;
    }

    public Map<String, Object> read(String group) {
        SystemConfig cfg = configMapper.selectOne(Wrappers.<SystemConfig>lambdaQuery()
                .eq(SystemConfig::getName, "settings_" + group).last("limit 1"));
        Map<String, Object> base = new LinkedHashMap<>(DEFAULTS.getOrDefault(group, Map.of()));
        if (cfg != null && cfg.getValue() != null && !cfg.getValue().isBlank()) {
            try {
                base.putAll(OM.readValue(cfg.getValue(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                        }));
            } catch (Exception ignore) {
            }
        }
        return base;
    }

    public void save(String group, Map<String, Object> data) {
        String key = "settings_" + group;
        SystemConfig cfg = configMapper.selectOne(Wrappers.<SystemConfig>lambdaQuery()
                .eq(SystemConfig::getName, key).last("limit 1"));
        String json = JSONUtil.toJsonStr(data == null ? Map.of() : data);
        if (cfg == null) {
            cfg = new SystemConfig();
            cfg.setName(key);
            cfg.setValue(json);
            cfg.setRemark("设置分组:" + group);
            configMapper.insert(cfg);
        } else {
            configMapper.update(null, Wrappers.<SystemConfig>lambdaUpdate()
                    .eq(SystemConfig::getId, cfg.getId()).set(SystemConfig::getValue, json));
        }
    }
}