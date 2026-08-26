package com.minimall.module.marketing.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.module.marketing.mapper.FullReductionMapper;
import com.minimall.module.marketing.model.entity.FullReduction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FullReductionService extends ServiceImpl<FullReductionMapper, FullReduction> {

    /** 当前生效的满减规则（启用且在有效期内） */
    public Optional<FullReduction> currentActive() {
        LocalDateTime now = LocalDateTime.now();
        return this.lambdaQuery()
                .eq(FullReduction::getStatus, 1)
                .and(q -> q.isNull(FullReduction::getStartTime)
                        .or(w -> w.le(FullReduction::getStartTime, now)))
                .and(q -> q.isNull(FullReduction::getEndTime)
                        .or(w -> w.ge(FullReduction::getEndTime, now)))
                .orderByDesc(FullReduction::getId)
                .last("limit 1")
                .oneOpt();
    }
}