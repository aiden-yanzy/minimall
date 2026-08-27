#!/usr/bin/env bash
# 一键灌入演示数据（分类/商品/评价/轮播 + 占位图片）
# 用法: ./scripts/setup-demo.sh
set -e
cd "$(dirname "$0")/.."

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

echo "== 1/3 生成占位图片 =="
python3 - "$TMP" <<'PYEOF'
import sys, os
out = sys.argv[1]
def svg(f, name, w=750, h=750, color="#fff0e6", sub=""):
    s = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}">
<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
<stop offset="0" stop-color="{color}"/><stop offset="1" stop-color="#ffffff"/></linearGradient></defs>
<rect width="{w}" height="{h}" fill="url(#g)"/>
<text x="{w/2}" y="{h/2-10}" font-size="{h/9}" text-anchor="middle" fill="#cc5500" font-family="PingFang SC, sans-serif">{name}</text>
<text x="{w/2}" y="{h/2+h/12}" font-size="{h/22}" text-anchor="middle" fill="#aa9990">{sub}</text></svg>'''
    open(f"{out}/{f}", "w").write(s)
for k, n, c in [("demo1","无线蓝牙耳机 Pro","#ffe6cc"),("demo2","简约纯棉T恤","#e6f2ff"),
                ("demo3","复古直筒牛仔裤","#e6ffe6"),("demo4","轻薄羽绒服","#fff2e6"),
                ("demo5","每日坚果礼盒","#fffbe6"),("demo6","精品咖啡豆","#f0e6ff"),
                ("demo7","保湿精华液","#ffe6f2"),("demo8","男士控油洗面奶","#e6f7ff"),
                ("demo9","北欧风LED台灯","#f2ffe6"),("demo10","记忆棉护颈枕","#e6fff2")]:
    svg(f"{k}.svg", n, sub="Minimall 优选")
for k, n, c, sub in [("banner1","夏日大促 全场满200减20","#ff6a00","低至 5 折起"),
                     ("banner2","秒杀专场 每日10点开抢","#7b2ff7","限量开抢"),
                     ("banner3","会员日 双倍积分","#e6007a","积分好礼等你拿")]:
    svg(f"{k}.svg", n, w=1200, h=400, color=c, sub=sub)
print(f"generated {len(os.listdir(out))} files")
PYEOF

echo "== 2/3 复制图片到 nginx 上传卷 =="
docker compose exec -T nginx mkdir -p /usr/share/nginx/upload/demo
for f in "$TMP"/*.svg; do docker compose cp "$f" nginx:/usr/share/nginx/upload/demo/ >/dev/null 2>&1 || true; done
docker compose exec -T nginx sh -c 'ls /usr/share/nginx/upload/demo | wc -l' | tr -d '\r' | xargs echo "uploaded files:"

echo "== 3/3 灌入演示数据(幂等：已存在则跳过) =="
docker compose exec -T mysql env MYSQL_PWD=root123 mysql -uroot --default-character-set=utf8mb4 minimall < server/src/main/resources/sql/demo-data.sql 2>/dev/null || true
docker compose exec -T mysql env MYSQL_PWD=root123 mysql -uroot --default-character-set=utf8mb4 minimall < server/src/main/resources/sql/migrate-2026-08-27-charset-fix.sql 2>/dev/null || true
echo "完成 ✓  访问 http://localhost:8081/ 查看 H5 商城"