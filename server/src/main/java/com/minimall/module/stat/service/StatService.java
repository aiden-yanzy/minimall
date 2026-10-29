package com.minimall.module.stat.service;

import com.minimall.module.stat.mapper.StatMapper;
import com.minimall.module.stat.model.vo.StatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatService {

    private final StatMapper statMapper;

    /** 销售概览：区间汇总 + 每日趋势 + 漏斗 */
    public StatVO.Overview overview(int days) {
        int d = Math.min(Math.max(days, 1), 365);
        LocalDateTime start = LocalDate.now().minusDays(d - 1L).atStartOfDay();

        StatVO.Summary summary = statMapper.summary(start);
        if (summary == null) summary = new StatVO.Summary();
        long orders = summary.getOrders() == null ? 0 : summary.getOrders();
        long amount = summary.getAmount() == null ? 0 : summary.getAmount();
        summary.setAvgAmount(orders == 0 ? 0 : amount / orders);

        StatVO.Funnel funnel = statMapper.funnel(start);
        if (funnel == null) funnel = new StatVO.Funnel();

        List<StatVO.DayAmount> daily = new ArrayList<>(statMapper.dailySales(start));
        // 补齐空白日期，前端画折线不用自己补点
        java.util.Map<String, StatVO.DayAmount> byDay = new java.util.HashMap<>();
        for (StatVO.DayAmount a : daily) byDay.put(a.getDay(), a);
        List<StatVO.DayAmount> filled = new ArrayList<>();
        for (int i = 0; i < d; i++) {
            String key = LocalDate.now().minusDays(d - 1L - i).toString();
            StatVO.DayAmount a = byDay.get(key);
            if (a == null) {
                a = new StatVO.DayAmount();
                a.setDay(key);
                a.setOrders(0L);
                a.setAmount(0L);
            }
            filled.add(a);
        }

        StatVO.Overview vo = new StatVO.Overview();
        vo.setDays(d);
        vo.setSummary(summary);
        vo.setFunnel(funnel);
        vo.setDaily(filled);
        return vo;
    }

    /** 销售明细分页 */
    public StatVO.DetailPage detail(int page, int size, Integer status, LocalDate startDate, LocalDate endDate) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime end = endDate == null ? null : endDate.atTime(23, 59, 59);

        StatVO.DetailPage vo = new StatVO.DetailPage();
        vo.setRecords(statMapper.saleDetails(status, start, end, (p - 1) * s, s));
        vo.setTotal(statMapper.saleDetailCount(status, start, end));
        vo.setPage(p);
        vo.setSize(s);
        return vo;
    }

    /** 商品销售排行 */
    public List<StatVO.GoodsRank> rank(int days, int limit) {
        int d = Math.min(Math.max(days, 1), 365);
        int l = Math.min(Math.max(limit, 1), 100);
        LocalDateTime start = LocalDate.now().minusDays(d - 1L).atStartOfDay();
        return statMapper.goodsRank(start, l);
    }
}
