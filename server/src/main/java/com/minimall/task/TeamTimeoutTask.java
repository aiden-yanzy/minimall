package com.minimall.task;

import com.minimall.module.marketing.team.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：拼团超时未成团 → 自动失败退款
 * 每 5 分钟扫描一次
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeamTimeoutTask {

    private final TeamService teamService;

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 30 * 1000)
    public void scan() {
        try {
            int count = teamService.scanExpired();
            if (count > 0) {
                log.info("拼团超时处理完成，共 {} 个团失败退款", count);
            }
        } catch (Exception e) {
            log.error("拼团超时扫描异常", e);
        }
    }
}