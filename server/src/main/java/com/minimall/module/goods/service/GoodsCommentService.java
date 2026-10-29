package com.minimall.module.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.goods.mapper.GoodsCommentMapper;
import com.minimall.module.goods.model.dto.CommentCreateDTO;
import com.minimall.module.goods.model.entity.GoodsComment;
import com.minimall.module.goods.model.vo.GoodsCommentVO;
import com.minimall.module.order.mapper.OrderGoodsMapper;
import com.minimall.module.order.mapper.OrderMapper;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.model.entity.OrderGoods;
import com.minimall.module.user.mapper.UserMapper;
import com.minimall.module.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品评价：展示 + 收货后可评价
 */
@Service
@RequiredArgsConstructor
public class GoodsCommentService extends ServiceImpl<GoodsCommentMapper, GoodsComment> {

    private final GoodsCommentMapper commentMapper;
    private final OrderMapper orderMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final UserMapper userMapper;

    /** 商品评价分页（前台展示） */
    public IPage<GoodsCommentVO> pageByGoods(Long goodsId, long page, long size) {
        Page<GoodsComment> pg = commentMapper.selectPage(new Page<>(page, size),
                Wrappers.<GoodsComment>lambdaQuery()
                        .eq(GoodsComment::getGoodsId, goodsId)
                        .eq(GoodsComment::getIsShow, 1)
                        .orderByDesc(GoodsComment::getId));
        // 无评价时别查用户表：MyBatis-Plus 的 selectBatchIds(空集合) 会生成 IN () 直接 SQL 语法错
        Map<Long, User> users = pg.getRecords().isEmpty()
                ? java.util.Map.<Long, User>of()
                : userMapper.selectBatchIds(pg.getRecords().stream()
                        .map(GoodsComment::getUserId).filter(java.util.Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
        return pg.convert(c -> {
            GoodsCommentVO vo = new GoodsCommentVO();
            vo.setId(c.getId());
            vo.setGoodsId(c.getGoodsId());
            vo.setScore(c.getScore());
            vo.setContent(c.getContent());
            vo.setReply(c.getReply());
            vo.setCreateTime(c.getCreateTime());
            User u = users.get(c.getUserId());
            vo.setNickname(u == null ? "匿名用户" : u.getNickname());
            vo.setAvatar(u == null ? "" : u.getAvatar());
            return vo;
        });
    }

    /** 收货后评价 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, CommentCreateDTO dto) {
        OrderGoods og = orderGoodsMapper.selectById(dto.getOrderGoodsId());
        if (og == null || !og.getUserId().equals(userId)) throw new BusinessException("订单商品不存在");
        Order order = orderMapper.selectById(og.getOrderId());
        if (order == null || order.getOrderStatus() != 3) throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        if (og.getIsComment() != null && og.getIsComment() == 1) throw new BusinessException("该商品已评价");
        if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 5) throw new BusinessException("评分需在1-5之间");
        if (dto.getContent() == null || dto.getContent().isBlank()) throw new BusinessException("评价内容不能为空");

        GoodsComment c = new GoodsComment();
        c.setOrderId(og.getOrderId());
        c.setOrderGoodsId(og.getId());
        c.setUserId(userId);
        c.setGoodsId(og.getGoodsId());
        c.setGoodsItemId(og.getGoodsItemId());
        c.setScore(dto.getScore());
        c.setDescriptionScore(dto.getScore());
        c.setServiceScore(dto.getScore());
        c.setContent(dto.getContent());
        c.setIsShow(1);
        commentMapper.insert(c);

        orderGoodsMapper.update(null, Wrappers.<OrderGoods>lambdaUpdate()
                .eq(OrderGoods::getId, og.getId()).set(OrderGoods::getIsComment, 1));
        return c.getId();
    }
}