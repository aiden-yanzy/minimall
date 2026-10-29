package com.minimall.module.stat.mapper;

import com.minimall.module.stat.model.vo.StatVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台统计报表查询（直接用聚合 SQL，避免把明细拉到内存里算）
 */
public interface StatMapper {

    /** 每日销售趋势 */
    @Select("""
            SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day,
                   COUNT(*)                            AS orders,
                   COALESCE(SUM(pay_amount), 0)        AS amount
            FROM orders
            WHERE pay_status = 1 AND create_time >= #{start} AND delete_flag = 0
            GROUP BY day
            ORDER BY day
            """)
    List<StatVO.DayAmount> dailySales(@Param("start") LocalDateTime start);

    /** 汇总指标（区间内已支付） */
    @Select("""
            SELECT COUNT(*)                     AS orders,
                   COALESCE(SUM(pay_amount), 0) AS amount,
                   COUNT(DISTINCT user_id)      AS buyers
            FROM orders
            WHERE pay_status = 1 AND create_time >= #{start} AND delete_flag = 0
            """)
    StatVO.Summary summary(@Param("start") LocalDateTime start);

    /** 全部订单口径的汇总（含未支付，用于漏斗展示） */
    @Select("""
            SELECT COUNT(*)                                              AS orders,
                   COALESCE(SUM(CASE WHEN pay_status = 1 THEN 1 ELSE 0 END), 0) AS paid,
                   COALESCE(SUM(pay_amount), 0)                          AS amount
            FROM orders
            WHERE create_time >= #{start} AND delete_flag = 0
            """)
    StatVO.Funnel funnel(@Param("start") LocalDateTime start);

    /** 销售明细（分页） */
    @Select("""
            <script>
            SELECT o.id, o.order_sn, o.user_id, o.total_amount, o.pay_amount, o.freight_amount,
                   o.order_status, o.pay_status, o.pay_type, o.create_time, o.pay_time,
                   (SELECT COALESCE(SUM(g.num), 0) FROM order_goods g WHERE g.order_id = o.id) AS nums,
                   (SELECT COUNT(*) FROM order_goods g WHERE g.order_id = o.id)               AS items
            FROM orders o
            WHERE o.delete_flag = 0
              <if test="status != null"> AND o.order_status = #{status} </if>
              <if test="start != null"> AND o.create_time &gt;= #{start} </if>
              <if test="end != null"> AND o.create_time &lt;= #{end} </if>
            ORDER BY o.id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<StatVO.SaleDetail> saleDetails(@Param("status") Integer status,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        @Param("offset") int offset,
                                        @Param("size") int size);

    /** 明细总条数 */
    @Select("""
            <script>
            SELECT COUNT(*) FROM orders o
            WHERE o.delete_flag = 0
              <if test="status != null"> AND o.order_status = #{status} </if>
              <if test="start != null"> AND o.create_time &gt;= #{start} </if>
              <if test="end != null"> AND o.create_time &lt;= #{end} </if>
            </script>
            """)
    long saleDetailCount(@Param("status") Integer status,
                         @Param("start") LocalDateTime start,
                         @Param("end") LocalDateTime end);

    /** 商品销售排行 */
    @Select("""
            SELECT g.goods_id                                  AS goodsId,
                   g.goods_name                                AS goodsName,
                   COALESCE(SUM(g.num), 0)                     AS nums,
                   COALESCE(SUM(g.total_price), 0)             AS amount,
                   COUNT(DISTINCT g.order_id)                  AS orderCount
            FROM order_goods g
            JOIN orders o ON o.id = g.order_id
            WHERE o.pay_status = 1 AND o.delete_flag = 0 AND o.create_time >= #{start}
            GROUP BY g.goods_id, g.goods_name
            ORDER BY amount DESC
            LIMIT #{limit}
            """)
    List<StatVO.GoodsRank> goodsRank(@Param("start") LocalDateTime start, @Param("limit") int limit);
}
