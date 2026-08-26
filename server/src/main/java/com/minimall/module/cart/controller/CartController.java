package com.minimall.module.cart.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.cart.model.dto.CartAddDTO;
import com.minimall.module.cart.model.vo.CartItemVO;
import com.minimall.module.cart.service.CartService;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端购物车
 */
@Tag(name = "C端-购物车")
@RestController
@RequestMapping("/shop/cart")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @Operation(summary = "加购")
    @PostMapping
    public R<Long> add(@Valid @RequestBody CartAddDTO dto) {
        return R.ok("已加入购物车", cartService.add(userService.getCurrentUserId(), dto));
    }

    @Operation(summary = "购物车列表")
    @GetMapping
    public R<List<CartItemVO>> list() {
        return R.ok(cartService.list(userService.getCurrentUserId()));
    }

    @Operation(summary = "修改数量")
    @PutMapping("/num/{cartId}")
    public R<Void> updateNum(@PathVariable Long cartId, @RequestParam Integer num) {
        cartService.updateNum(userService.getCurrentUserId(), cartId, num);
        return R.ok();
    }

    @Operation(summary = "勾选/取消勾选")
    @PutMapping("/selected/{cartId}")
    public R<Void> updateSelected(@PathVariable Long cartId, @RequestParam Integer selected) {
        cartService.updateSelected(userService.getCurrentUserId(), cartId, selected);
        return R.ok();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{cartId}")
    public R<Void> remove(@PathVariable Long cartId) {
        cartService.remove(userService.getCurrentUserId(), cartId);
        return R.ok();
    }
}
