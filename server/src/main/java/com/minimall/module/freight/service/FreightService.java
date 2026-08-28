package com.minimall.module.freight.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.module.freight.mapper.FreightConfigMapper;
import com.minimall.module.freight.mapper.GoodsDeliveryTemplateMapper;
import com.minimall.module.freight.model.entity.FreightConfig;
import com.minimall.module.freight.model.entity.GoodsDeliveryTemplate;
import com.minimall.module.goods.model.entity.Goods;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 运费模板：后台 CRUD + 下单计费
 * 计费说明：当前按模板"全国"默认规则计费（region_ids='0'）；
 * 地区细分需配合行政区域数据，后续可扩展为按收货地址匹配 region_ids。
 */
@Service
@RequiredArgsConstructor
public class FreightService extends ServiceImpl<GoodsDeliveryTemplateMapper, GoodsDeliveryTemplate> {

    private final FreightConfigMapper configMapper;

    // ---------------- 后台管理 ----------------

    /** 模板列表（含规则） */
    public List<GoodsDeliveryTemplate> templates() {
        List<GoodsDeliveryTemplate> list = this.list(Wrappers.<GoodsDeliveryTemplate>lambdaQuery()
                .orderByDesc(GoodsDeliveryTemplate::getId));
        return list;
    }

    public List<FreightConfig> configs(Long templateId) {
        return configMapper.selectList(Wrappers.<FreightConfig>lambdaQuery()
                .eq(FreightConfig::getTemplateId, templateId));
    }

    /** 保存模板 + 重建规则 */
    @Transactional(rollbackFor = Exception.class)
    public Long saveTemplate(GoodsDeliveryTemplate t, List<FreightConfig> rules) {
        if (t.getName() == null || t.getName().isBlank()) throw new BusinessException("模板名称不能为空");
        if (t.getId() == null) {
            this.save(t);
        } else {
            this.updateById(t);
        }
        // 重建规则
        configMapper.delete(Wrappers.<FreightConfig>lambdaQuery().eq(FreightConfig::getTemplateId, t.getId()));
        if (rules != null) {
            for (FreightConfig r : rules) {
                if (r.getFirstPrice() == null) continue;
                r.setId(null);
                r.setTemplateId(t.getId());
                if (r.getRegionIds() == null || r.getRegionIds().isBlank()) r.setRegionIds("0");
                if (r.getRegionName() == null || r.getRegionName().isBlank()) r.setRegionName("全国");
                configMapper.insert(r);
            }
        }
        return t.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeTemplate(Long id) {
        configMapper.delete(Wrappers.<FreightConfig>lambdaQuery().eq(FreightConfig::getTemplateId, id));
        this.removeById(id);
    }

    // ---------------- 下单计费 ----------------

    /**
     * 计算单件商品的运费（按件/按重量/按体积，取该商品模板的默认规则）
     * @param goods 商品
     * @param num 数量
     * @param totalWeight 总重量(克，用于按重量计费)
     */
    public long calculate(Goods goods, int num, BigDecimal totalWeight) {
        if (goods.getDeliveryTemplateId() == null) return 0;
        GoodsDeliveryTemplate t = this.getById(goods.getDeliveryTemplateId());
        if (t == null) return 0;
        if (t.getIsFreeShipping() != null && t.getIsFreeShipping() == 1) return 0;

        FreightConfig config = configMapper.selectOne(Wrappers.<FreightConfig>lambdaQuery()
                .eq(FreightConfig::getTemplateId, t.getId())
                .orderByAsc(FreightConfig::getId).last("limit 1"));
        if (config == null) return 0;

        int chargeType = t.getChargeType() == null ? 1 : t.getChargeType();
        // 计费单位：按件=件数；按重量/体积=总重量(克→按配置单位处理)
        BigDecimal unit = BigDecimal.valueOf(num);
        if ((chargeType == 2 || chargeType == 3) && totalWeight != null && totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            unit = totalWeight;
        }
        int first = config.getFirstUnit() == null ? 0 : config.getFirstUnit();
        long firstPrice = config.getFirstPrice() == null ? 0 : config.getFirstPrice();
        int cont = config.getContinueUnit() == null ? Math.max(1, first) : config.getContinueUnit();
        long contPrice = config.getContinuePrice() == null ? 0 : config.getContinuePrice();

        if (unit.compareTo(BigDecimal.valueOf(first)) <= 0) return firstPrice;
        BigDecimal extra = unit.subtract(BigDecimal.valueOf(first));
        long steps = extra.divide(BigDecimal.valueOf(Math.max(1, cont)), 0, RoundingMode.CEILING).longValue();
        return firstPrice + steps * contPrice;
    }
}