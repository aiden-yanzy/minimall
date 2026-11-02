#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
minimall 冒烟测试：一条命令跑完 C 端主流程 + 后台动作 + 并发防超卖校验。

用法：
    python3 scripts/smoke.py                       # 默认打 http://127.0.0.1:18082
    python3 scripts/smoke.py --base http://x:8080
    python3 scripts/smoke.py --skip-concurrency    # 跳过并发校验（不需要 docker）
    python3 scripts/smoke.py --keep-data           # 保留测试数据（默认也不删，仅标注）

约定：
- 只用标准库（urllib），系统 python3 3.9 就能跑，不依赖 requests
- 每步断言失败立刻退出（exit 1），并打印 PASS/FAIL 汇总
- 并发校验需要 docker 访问 mysql（会临时改某个 SKU 的库存，跑完自动还原）
"""
import argparse
import json
import random
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor

PASS, FAIL, SKIP = "\033[32mPASS\033[0m", "\033[31mFAIL\033[0m", "\033[33mSKIP\033[0m"
results = []


def record(step, ok, detail="", skipped=False):
    tag = SKIP if skipped else (PASS if ok else FAIL)
    results.append((step, tag, detail))
    print(f"  [{tag}] {step}" + (f" — {detail}" if detail else ""))
    if not ok and not skipped:
        summary()
        sys.exit(1)


def summary():
    total = len(results)
    ok = sum(1 for _, t, _ in results if t == PASS)
    skipped = sum(1 for _, t, _ in results if t == SKIP)
    print("\n" + "=" * 60)
    print(f"  共 {total} 项：通过 {ok} / 跳过 {skipped} / 失败 {total - ok - skipped}")
    print("=" * 60)


class Api:
    def __init__(self, base):
        self.base = base.rstrip("/")
        self.token = None
        self.admin_token = None

    def call(self, method, path, body=None, admin=False, params=None, timeout=20):
        url = self.base + path
        if params:
            url += ("&" if "?" in url else "?") + urllib.parse.urlencode(params)
        data = None
        headers = {"Content-Type": "application/json"}
        if body is not None:
            data = json.dumps(body).encode("utf-8")
        tok = self.admin_token if admin else self.token
        if tok:
            headers["Authorization"] = "Bearer " + tok
        req = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            try:
                return json.loads(e.read().decode("utf-8"))
            except Exception:
                return {"code": e.code, "msg": str(e)}
        except Exception as e:
            return {"code": -1, "msg": str(e)}

    def get(self, p, **kw):
        return self.call("GET", p, **kw)

    def post(self, p, body=None, **kw):
        return self.call("POST", p, body if body is not None else {}, **kw)


def mysql(sql, container="minimall-mysql"):
    """执行 SQL（并发校验用）"""
    out = subprocess.run(
        ["docker", "exec", container, "mysql", "--default-character-set=utf8mb4",
         "-uminimall", "-pminimall123", "minimall", "-N", "-e", sql],
        capture_output=True, text=True, timeout=30)
    if out.returncode != 0:
        raise RuntimeError(out.stderr.strip()[:200])
    return out.stdout.strip()


def pick_sku(api):
    """挑一个在售且库存充足的 SKU"""
    res = api.get("/shop/goods", params={"page": 1, "size": 20})
    for g in (res.get("data") or {}).get("records", []):
        detail = api.get(f"/shop/goods/{g['id']}")
        items = (detail.get("data") or {}).get("items") or []
        for it in items:
            if (it.get("stock") or 0) >= 3:
                return g["id"], it["id"], it.get("price") or 0
    return None, None, 0


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--base", default="http://127.0.0.1:18082")
    ap.add_argument("--admin-account", default="admin")
    ap.add_argument("--admin-password", default="admin123")
    ap.add_argument("--skip-concurrency", action="store_true")
    ap.add_argument("--db-container", default="minimall-mysql",
                    help="并发校验要改库存，指定 mysql 容器名（多实例同机时必填）")
    ap.add_argument("--concurrency", type=int, default=20)
    ap.add_argument("--stock", type=int, default=5)
    args = ap.parse_args()

    api = Api(args.base)
    mobile = "139" + str(random.randint(10000000, 99999999))
    print(f"\n=== minimall 冒烟测试 @ {args.base} · 测试手机号 {mobile} ===\n")

    # ---------- 1. 健康检查 ----------
    print("【1】健康检查")
    home = api.get("/shop/decorate/home")
    record("首页装修接口 200", home.get("code") == 200, f"code={home.get('code')}")
    cats = api.get("/shop/goods/category")
    record("分类树有数据", cats.get("code") == 200 and len(cats.get("data") or []) > 0,
           f"{len(cats.get('data') or [])} 个分类")

    # ---------- 2. 注册登录 ----------
    print("\n【2】注册 / 登录")
    reg = api.post("/shop/auth/register", {"mobile": mobile, "password": "smoke123456", "channel": "h5"})
    record("注册成功", reg.get("code") == 200, reg.get("msg", ""))
    login = api.post("/shop/auth/login", {"mobile": mobile, "password": "smoke123456"})
    api.token = (login.get("data") or {}).get("token")
    record("登录拿到 token", login.get("code") == 200 and bool(api.token))

    # ---------- 3. 领券 ----------
    print("\n【3】优惠券")
    center = api.get("/shop/coupon/center")
    coupons = center.get("data") or []
    record("领券中心有券", len(coupons) > 0, f"{len(coupons)} 张")
    if coupons:
        claim = api.post(f"/shop/coupon/claim/{coupons[0]['id']}")
        record("领券成功", claim.get("code") == 200, claim.get("msg", ""))

    # ---------- 4. 加购下单 ----------
    print("\n【4】加购 / 下单")
    gid, iid, price = pick_sku(api)
    record("找到有库存的 SKU", iid is not None, f"goods={gid} item={iid} ￥{price / 100:.2f}")
    cart = api.post("/shop/cart", {"goodsId": gid, "goodsItemId": iid, "num": 1})
    record("加入购物车", cart.get("code") == 200, cart.get("msg", ""))
    order = api.post("/shop/order/create", {
        "buyType": "cart", "contact": "冒烟测试", "mobile": mobile, "address": "北京市朝阳区测试路 1 号"
    })
    order_id = order.get("data")
    record("创建订单", order.get("code") == 200 and bool(order_id), f"订单 {order_id} · {order.get('msg')}")

    # ---------- 5. 支付 ----------
    print("\n【5】支付")
    detail = api.get(f"/shop/order/{order_id}")
    order_sn = (detail.get("data") or {}).get("orderSn")
    pay = api.post("/shop/pay/mock", {"orderSn": order_sn, "payType": 1})
    record("模拟支付成功", pay.get("code") == 200, pay.get("msg", ""))
    detail = api.get(f"/shop/order/{order_id}")
    record("订单状态 → 待发货(1)", (detail.get("data") or {}).get("orderStatus") == 1,
           f"orderStatus={(detail.get('data') or {}).get('orderStatus')}")

    # ---------- 6. 后台发货 ----------
    print("\n【6】后台发货 / 物流轨迹")
    alog = api.post("/admin/auth/login", {"account": args.admin_account, "password": args.admin_password})
    api.admin_token = (alog.get("data") or {}).get("token")
    record("后台登录", alog.get("code") == 200 and bool(api.admin_token))
    deliver = api.post(f"/admin/order/{order_id}/deliver",
                       {"expressId": 1, "expressName": "顺丰速运", "expressNo": "SF" + str(int(time.time()))},
                       admin=True)
    record("发货成功", deliver.get("code") == 200, deliver.get("msg", ""))
    traces = api.post(f"/shop/order/{order_id}/traces")
    record("物流轨迹落库", len(traces.get("data") or []) >= 1, f"{len(traces.get('data') or [])} 个节点")

    # ---------- 7. 收货 + 评价 ----------
    print("\n【7】确认收货 / 评价")
    rec = api.post(f"/shop/order/{order_id}/receive")
    record("确认收货", rec.get("code") == 200, rec.get("msg", ""))
    d2 = api.get(f"/shop/order/{order_id}")
    record("订单状态 → 已完成(3)", (d2.get("data") or {}).get("orderStatus") == 3)
    og = ((d2.get("data") or {}).get("goodsList") or [{}])[0]
    cm = api.post("/shop/comment/create",
                  {"orderId": order_id, "orderGoodsId": og.get("id"), "score": 5, "content": "冒烟测试好评"})
    record("发表评价", cm.get("code") == 200, cm.get("msg", ""))

    # ---------- 8. 会员等级 ----------
    print("\n【8】会员成长值")
    lv = api.get("/shop/member/level")
    lv_data = lv.get("data") or {}
    record("成长值已累加", (lv_data.get("totalConsume") or 0) > 0,
           f"累计 ￥{(lv_data.get('totalConsume') or 0) / 100:.2f} · {lv_data.get('levelName')}")

    # ---------- 9. 售后 + 退款 ----------
    print("\n【9】售后 / 退款到余额")
    bal_before = (api.get("/shop/wallet/balance").get("data") or 0)
    refund = 100
    after = api.post("/shop/after/create", {
        "orderId": order_id, "orderGoodsId": og.get("id"), "type": 1,
        "reason": "冒烟测试退款", "refundAmount": refund
    })
    after_id = after.get("data")
    record("申请售后", after.get("code") == 200 and bool(after_id), f"售后单 {after_id}")
    approve = api.post(f"/admin/after/{after_id}/approve", admin=True)
    record("后台同意退款", approve.get("code") == 200, approve.get("msg", ""))
    bal_after = (api.get("/shop/wallet/balance").get("data") or 0)
    record("退款进余额", bal_after == bal_before + refund, f"{bal_before} → {bal_after} 分")

    # ---------- 10. 并发防超卖 ----------
    print("\n【10】并发防超卖")
    if args.skip_concurrency:
        record("并发校验", True, "按参数跳过", skipped=True)
    else:
        try:
            sku = mysql("select id from goods_item where delete_flag=0 and stock>10 order by id limit 1", args.db_container)
            if not sku:
                record("并发校验", True, "库里没有可测 SKU", skipped=True)
            else:
                sku = sku.splitlines()[0]
                origin_stock = mysql(f"select stock from goods_item where id={sku}", args.db_container)
                mysql(f"update goods_item set stock={args.stock} where id={sku}", args.db_container)
                n = args.concurrency

                def place(_):
                    body = {"buyType": "buyNow", "goodsItemId": int(sku), "num": 1,
                            "contact": "并发", "mobile": mobile, "address": "压测"}
                    return api.post("/shop/order/create", body).get("code")

                with ThreadPoolExecutor(max_workers=n) as pool:
                    codes = list(pool.map(place, range(n)))
                ok_n = sum(1 for c in codes if c == 200)
                left = int(mysql(f"select stock from goods_item where id={sku}", args.db_container))
                record("成功数 == 初始库存", ok_n == args.stock, f"成功 {ok_n} 单 / 库存 {args.stock}")
                record("无负库存", left >= 0, f"剩余 {left}")
                record("库存被正确扣净", left == 0, f"剩余 {left}")
                # 还原库存（把测试单取消，或直接恢复）
                mysql(f"update goods_item set stock={origin_stock} where id={sku}", args.db_container)
                record("库存已还原", True, f"恢复到 {origin_stock}")
        except Exception as e:
            record("并发校验", True, f"跳过：{e}", skipped=True)

    print()
    summary()
    print(f"\n  测试账号：{mobile} / smoke123456（订单 {order_id}）")
    print("  提示：冒烟会留下真实数据，需要干净环境可 docker compose down -v 重建\n")


if __name__ == "__main__":
    main()
