package com.minimall.module.goods.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.goods.mapper.GoodsImageMapper;
import com.minimall.module.goods.mapper.GoodsItemMapper;
import com.minimall.module.goods.mapper.GoodsMapper;
import com.minimall.module.goods.mapper.GoodsSpecMapper;
import com.minimall.module.goods.mapper.GoodsSpecValueMapper;
import com.minimall.module.goods.model.dto.GoodsSaveDTO;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsCategory;
import com.minimall.module.goods.model.entity.GoodsImage;
import com.minimall.module.goods.model.entity.GoodsItem;
import com.minimall.module.goods.model.entity.GoodsSpec;
import com.minimall.module.goods.model.entity.GoodsSpecValue;
import com.minimall.module.goods.model.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsService extends ServiceImpl<GoodsMapper, Goods> {

    private final GoodsItemMapper goodsItemMapper;
    private final GoodsImageMapper goodsImageMapper;
    private final GoodsSpecMapper goodsSpecMapper;
    private final GoodsSpecValueMapper goodsSpecValueMapper;
    private final GoodsCategoryService categoryService;

    // ---------------- C 端 ----------------

    /** 前台商品分页（仅上架） */
    public IPage<GoodsVO> pageForShop(long page, long size, Long categoryId, String keyword) {
        LambdaQueryWrapper<Goods> qw = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, 1)
                .eq(Goods::getIsShow, 1)
                .like(StrUtil.isNotBlank(keyword), Goods::getName, keyword)
                .eq(categoryId != null, Goods::getCategoryId, categoryId)
                .orderByDesc(Goods::getSort)
                .orderByDesc(Goods::getId);
        Page<Goods> pg = this.page(new Page<>(page, size), qw);
        // 组装分类名
        Map<Long, String> catMap = categoryService.list()
                .stream().collect(Collectors.toMap(GoodsCategory::getId, GoodsCategory::getName, (a, b) -> a));
        return pg.convert(g -> {
            GoodsVO vo = GoodsVO.from(g);
            vo.setCategoryName(catMap.get(g.getCategoryId()));
            return vo;
        });
    }

    /** 商品详情 */
    public GoodsVO detail(Long id) {
        Goods goods = this.getById(id);
        if (goods == null || goods.getDeleteFlag() != null && goods.getDeleteFlag() == 1) {
            throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        }
        GoodsVO vo = GoodsVO.from(goods);
        if (goods.getCategoryId() != null) {
            GoodsCategory c = categoryService.getById(goods.getCategoryId());
            if (c != null) vo.setCategoryName(c.getName());
        }
        // 轮播图
        List<String> images = goodsImageMapper.selectList(
                        new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, id).orderByAsc(GoodsImage::getSort))
                .stream().map(GoodsImage::getUrl).toList();
        vo.setImages(images);
        // SKU
        List<GoodsVO.ItemVO> items = goodsItemMapper.selectList(
                        new LambdaQueryWrapper<GoodsItem>().eq(GoodsItem::getGoodsId, id))
                .stream().map(GoodsVO.ItemVO::from).toList();
        vo.setItems(items);
        // 规格组
        List<GoodsSpec> specs = goodsSpecMapper.selectList(
                new LambdaQueryWrapper<GoodsSpec>().eq(GoodsSpec::getGoodsId, id).orderByAsc(GoodsSpec::getSort));
        if (!specs.isEmpty()) {
            List<GoodsSpecValue> allValues = goodsSpecValueMapper.selectList(
                    new LambdaQueryWrapper<GoodsSpecValue>().eq(GoodsSpecValue::getGoodsId, id));
            List<GoodsVO.SpecVO> specVOs = new ArrayList<>();
            for (GoodsSpec sp : specs) {
                GoodsVO.SpecVO sv = new GoodsVO.SpecVO();
                sv.setId(sp.getId());
                sv.setName(sp.getName());
                List<GoodsVO.SpecValueVO> vlist = new ArrayList<>();
                for (GoodsSpecValue v : allValues) {
                    if (!sp.getId().equals(v.getSpecId())) continue;
                    GoodsVO.SpecValueVO vv = new GoodsVO.SpecValueVO();
                    vv.setId(v.getId());
                    vv.setValue(v.getValue());
                    vv.setImage(v.getImage());
                    vlist.add(vv);
                }
                sv.setValues(vlist);
                specVOs.add(sv);
            }
            vo.setSpecs(specVOs);
        }
        return vo;
    }

    // ---------------- 后台 ----------------

    /** 单个 SKU */
    public GoodsItem getGoodsItem(Long itemId) {
        return goodsItemMapper.selectById(itemId);
    }

    /** 扣 SKU 库存、加销量 */
    public void deductStock(Long itemId, int num) {
        goodsItemMapper.update(null, new LambdaUpdateWrapper<GoodsItem>()
                .eq(GoodsItem::getId, itemId)
                .setSql("stock = stock - " + num)
                .setSql("sales = sales + " + num));
    }

    /** 恢复 SKU 库存 */
    public void restoreStock(Long itemId, int num) {
        goodsItemMapper.update(null, new LambdaUpdateWrapper<GoodsItem>()
                .eq(GoodsItem::getId, itemId)
                .setSql("stock = stock + " + num)
                .setSql("sales = sales - " + num));
    }

    /** 加 SPU 销量 */
    public void addSales(Long goodsId, int num) {
        this.update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goodsId)
                .setSql("stock = stock - " + num)
                .setSql("sales = sales + " + num));
    }

    /** 减少 SPU 销量（取消订单恢复） */
    public void minusSales(Long goodsId, int num) {
        this.update(new LambdaUpdateWrapper<Goods>()
                .eq(Goods::getId, goodsId)
                .setSql("stock = stock + " + num)
                .setSql("sales = sales - " + num));
    }

    /** 后台商品分页 */
    public IPage<Goods> adminPage(long page, long size, String keyword, Integer status) {
        LambdaQueryWrapper<Goods> qw = new LambdaQueryWrapper<Goods>()
                .like(StrUtil.isNotBlank(keyword), Goods::getName, keyword)
                .eq(status != null, Goods::getStatus, status)
                .orderByDesc(Goods::getId);
        return this.page(new Page<>(page, size), qw);
    }

    /** 创建 / 更新商品（含 SKU 与轮播图） */
    @Transactional(rollbackFor = Exception.class)
    public Long save(GoodsSaveDTO dto) {
        Goods goods = new Goods();
        if (dto.getId() != null) {
            goods = this.getById(dto.getId());
            if (goods == null) throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        }
        BeanUtils.copyProperties(dto, goods);

        // 汇总 SKU 计算价格区间与总库存
        long minP = Long.MAX_VALUE, maxP = 0;
        int totalStock = 0;
        for (GoodsSaveDTO.ItemDTO it : dto.getItems()) {
            minP = Math.min(minP, it.getPrice());
            maxP = Math.max(maxP, it.getPrice());
            totalStock += it.getStock() == null ? 0 : it.getStock();
        }
        goods.setMinPrice(dto.getItems().isEmpty() ? 0 : minP);
        goods.setMaxPrice(maxP);
        goods.setStock(totalStock);
        if (goods.getStatus() == null) goods.setStatus(0);

        this.saveOrUpdate(goods);
        Long goodsId = goods.getId();

        // 保存轮播图
        goodsImageMapper.delete(new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, goodsId));
        if (dto.getImages() != null) {
            int i = 0;
            for (String url : dto.getImages()) {
                if (StrUtil.isBlank(url)) continue;
                GoodsImage img = new GoodsImage();
                img.setGoodsId(goodsId);
                img.setUrl(url);
                img.setSort(i++);
                goodsImageMapper.insert(img);
            }
        }

        // 保存 SKU
        goodsItemMapper.delete(new LambdaQueryWrapper<GoodsItem>().eq(GoodsItem::getGoodsId, goodsId));
        for (GoodsSaveDTO.ItemDTO it : dto.getItems()) {
            if (it.getPrice() == null || it.getStock() == null) continue;
            GoodsItem item = new GoodsItem();
            BeanUtils.copyProperties(it, item);
            item.setId(null);
            item.setGoodsId(goodsId);
            if (StrUtil.isBlank(item.getItemSn())) {
                item.setItemSn(goodsId + "_" + System.currentTimeMillis());
            }
            goodsItemMapper.insert(item);
        }

        // 保存规格组（先删后插，保证与前端编辑结果一致）
        goodsSpecValueMapper.delete(new LambdaQueryWrapper<GoodsSpecValue>().eq(GoodsSpecValue::getGoodsId, goodsId));
        goodsSpecMapper.delete(new LambdaQueryWrapper<GoodsSpec>().eq(GoodsSpec::getGoodsId, goodsId));
        List<GoodsSaveDTO.SpecDTO> specs = dto.getSpecs();
        if (specs != null && !specs.isEmpty()) {
            int si = 0;
            for (GoodsSaveDTO.SpecDTO sp : specs) {
                if (StrUtil.isBlank(sp.getName())) continue;
                GoodsSpec spec = new GoodsSpec();
                spec.setGoodsId(goodsId);
                spec.setName(sp.getName());
                spec.setSort(si++);
                goodsSpecMapper.insert(spec);
                if (sp.getValues() == null) continue;
                for (GoodsSaveDTO.SpecValueDTO v : sp.getValues()) {
                    if (StrUtil.isBlank(v.getValue())) continue;
                    GoodsSpecValue sv = new GoodsSpecValue();
                    sv.setGoodsId(goodsId);
                    sv.setSpecId(spec.getId());
                    sv.setValue(v.getValue());
                    sv.setImage(v.getImage());
                    goodsSpecValueMapper.insert(sv);
                }
            }
            goods.setSpecType(1);
            this.lambdaUpdate().eq(Goods::getId, goodsId).set(Goods::getSpecType, 1).update();
        } else {
            this.lambdaUpdate().eq(Goods::getId, goodsId).set(Goods::getSpecType, 0).update();
        }
        return goodsId;
    }

    /** 上下架 */
    public void updateStatus(Long id, Integer status) {
        Goods g = this.getById(id);
        if (g == null) throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        this.lambdaUpdate().eq(Goods::getId, id).set(Goods::getStatus, status).update();
    }

    /** 删除 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        this.removeById(id);
        goodsItemMapper.delete(new LambdaQueryWrapper<GoodsItem>().eq(GoodsItem::getGoodsId, id));
        goodsImageMapper.delete(new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, id));
    }
}
