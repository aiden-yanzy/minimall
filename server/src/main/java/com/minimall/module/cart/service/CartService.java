package com.minimall.module.cart.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.cart.mapper.CartMapper;
import com.minimall.module.cart.model.dto.CartAddDTO;
import com.minimall.module.cart.model.entity.Cart;
import com.minimall.module.cart.model.vo.CartItemVO;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsItem;
import com.minimall.module.goods.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService extends ServiceImpl<CartMapper, Cart> {

    private final GoodsService goodsService;

    /** 加购 */
    public Long add(Long userId, CartAddDTO dto) {
        GoodsItem item = goodsService.getGoodsItem(dto.getGoodsItemId());
        if (item == null) throw new BusinessException(ResultCode.GOODS_NOT_EXIST);

        // 已存在则累加数量
        Cart exist = this.lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getGoodsItemId, dto.getGoodsItemId())
                .one();
        if (exist != null) {
            int newNum = exist.getNum() + dto.getNum();
            if (newNum > item.getStock()) throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            this.lambdaUpdate().eq(Cart::getId, exist.getId()).set(Cart::getNum, newNum).update();
            return exist.getId();
        }
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setGoodsId(dto.getGoodsId());
        cart.setGoodsItemId(dto.getGoodsItemId());
        cart.setNum(dto.getNum());
        cart.setIsSelected(1);
        this.save(cart);
        return cart.getId();
    }

    /** 购物车列表 */
    public List<CartItemVO> list(Long userId) {
        List<Cart> carts = this.lambdaQuery()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getUpdateTime)
                .list();
        List<GoodsItem> items = carts.stream().map(Cart::getGoodsItemId)
                .distinct()
                .map(goodsService::getGoodsItem)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, GoodsItem> itemMap = items.stream()
                .collect(Collectors.toMap(GoodsItem::getId, Function.identity(), (a, b) -> a));
        // 商品名/图
        Map<Long, Goods> goodsMap = items.stream()
                .map(it -> goodsService.getById(it.getGoodsId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(Goods::getId, Function.identity(), (a, b) -> a));

        return carts.stream().map(c -> {
            CartItemVO vo = new CartItemVO();
            vo.setId(c.getId());
            vo.setGoodsId(c.getGoodsId());
            vo.setGoodsItemId(c.getGoodsItemId());
            vo.setNum(c.getNum());
            vo.setIsSelected(c.getIsSelected());
            GoodsItem item = itemMap.get(c.getGoodsItemId());
            if (item != null) {
                vo.setSpecValueStr(item.getSpecValueStr());
                vo.setPrice(item.getPrice());
                vo.setStock(item.getStock());
                vo.setTotalPrice(item.getPrice() * c.getNum());
                Goods g = goodsMap.get(item.getGoodsId());
                if (g != null) {
                    vo.setGoodsName(g.getName());
                    vo.setGoodsImage(g.getImage());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /** 修改数量 / 勾选 */
    public void updateNum(Long userId, Long cartId, Integer num) {
        Cart cart = checkOwner(userId, cartId);
        if (num != null) {
            GoodsItem item = goodsService.getGoodsItem(cart.getGoodsItemId());
            if (item != null && num > item.getStock()) throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            this.lambdaUpdate().eq(Cart::getId, cartId).set(Cart::getNum, num).update();
        }
    }

    public void updateSelected(Long userId, Long cartId, Integer selected) {
        checkOwner(userId, cartId);
        this.lambdaUpdate().eq(Cart::getId, cartId).set(Cart::getIsSelected, selected).update();
    }

    public void remove(Long userId, Long cartId) {
        checkOwner(userId, cartId);
        this.removeById(cartId);
    }

    /** 清空已勾选（下单后调用） */
    public void removeSelected(Long userId) {
        this.remove(Wrappers.<Cart>lambdaQuery().eq(Cart::getUserId, userId).eq(Cart::getIsSelected, 1));
    }

    private Cart checkOwner(Long userId, Long cartId) {
        Cart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        return cart;
    }
}
