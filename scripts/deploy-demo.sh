#!/usr/bin/env bash
#
# 一键部署 Minimall 演示站（在目标 Linux 服务器上执行）
#
# 用法：
#   bash scripts/deploy-demo.sh                 # 默认端口 18080，部署到 ~/minimall
#   bash scripts/deploy-demo.sh --port 8080     # 换前端端口
#   bash scripts/deploy-demo.sh --dir /opt/minimall
#   bash scripts/deploy-demo.sh --reset         # 清空数据卷后重建（会丢数据）
#
# 要求：Docker + Docker Compose v2（docker compose 子命令形式），内存 >= 2G，磁盘 >= 5G
set -euo pipefail

REPO="${REPO:-https://github.com/aiden-yanzy/minimall.git}"
DIR="$HOME/minimall"
WEB_PORT="${WEB_PORT:-18080}"
SERVER_PORT="${SERVER_PORT:-18082}"
RESET=0

while [ $# -gt 0 ]; do
  case "$1" in
    --port)   WEB_PORT="$2"; SERVER_PORT="$(( $2 + 2 ))"; shift 2 ;;
    --dir)    DIR="$2"; shift 2 ;;
    --repo)   REPO="$2"; shift 2 ;;
    --reset)  RESET=1; shift ;;
    -h|--help) sed -n '2,15p' "$0"; exit 0 ;;
    *) echo "未知参数：$1"; exit 1 ;;
  esac
done

say() { printf "\033[36m==>\033[0m %s\n" "$*"; }
ok()  { printf "\033[32m  ✔\033[0m %s\n" "$*"; }
bad() { printf "\033[31m  ✘\033[0m %s\n" "$*"; }

# ---------- 1. 环境检查 ----------
say "检查环境"
command -v docker >/dev/null 2>&1 || { bad "没装 Docker，先执行：curl -fsSL https://get.docker.com | sh"; exit 1; }
docker compose version >/dev/null 2>&1 || { bad "没装 Docker Compose v2（docker compose 子命令）；Compose v1 已停止维护"; exit 1; }
ok "docker $(docker --version | awk '{print $3}' | tr -d ,)"

if [ -r /proc/meminfo ]; then
  MEM_MB=$(awk '/MemTotal/ {print int($2/1024)}' /proc/meminfo)
elif command -v sysctl >/dev/null 2>&1; then
  # macOS / BSD：sysctl 返回的是字节
  MEM_MB=$(( $(sysctl -n hw.memsize 2>/dev/null || echo 0) / 1024 / 1024 ))
else
  MEM_MB=0
fi
if [ "$MEM_MB" -gt 0 ] && [ "$MEM_MB" -lt 1800 ]; then
  bad "内存只有 ${MEM_MB}MB，mysql + redis + 后端至少需要 ~1.5G，建议 2G 以上"
  exit 1
fi
ok "内存 ${MEM_MB}MB"

AVAIL_KB=$(df -k "$HOME" | awk 'NR==2 {print $4}')
AVAIL_GB=$(( ${AVAIL_KB:-0} / 1024 / 1024 ))
[ "$AVAIL_GB" -lt 4 ] && { bad "磁盘可用 ${AVAIL_GB}G，建议留 5G 以上"; exit 1; }
ok "磁盘可用 ${AVAIL_GB}G"

# ---------- 2. 取代码 ----------
say "获取代码 → $DIR"
if [ -d "$DIR/.git" ]; then
  git -C "$DIR" pull --ff-only && ok "已更新到最新"
else
  git clone --depth 1 "$REPO" "$DIR" && ok "克隆完成"
fi
cd "$DIR"

# ---------- 3. 写环境变量 ----------
say "生成 .env"
if [ -f .env ]; then
  ok ".env 已存在，保留不覆盖"
else
  cp .env.example .env
  sed -i.bak "s/^WEB_PORT=.*/WEB_PORT=$WEB_PORT/; s/^SERVER_PORT=.*/SERVER_PORT=$SERVER_PORT/" .env && rm -f .env.bak
  # 演示站也别用默认 JWT 密钥
  RAND_SECRET=$(head -c 32 /dev/urandom | base64 | tr -d '/+=' | cut -c1-40)
  sed -i.bak "s|^SA_JWT_SECRET=.*|SA_JWT_SECRET=$RAND_SECRET|" .env && rm -f .env.bak
  ok ".env 已生成（JWT 密钥已随机化）"
fi
# shellcheck disable=SC1091
set -a; . ./.env; set +a
WEB_PORT="${WEB_PORT:-18080}"

# ---------- 3.5 同名容器预检 ----------
say "检查容器名冲突"
CONFLICT=$(docker ps -a --format '{{.Names}}' | grep -E '^minimall-(mysql|redis|server|nginx)$' | tr '\n' ' ' || true)
if [ -n "$CONFLICT" ]; then
  bad "本机已存在同名容器：${CONFLICT}"
  echo "     docker-compose.yml 里写死了 container_name，同一台机器只能跑一份。三种处理："
  echo "     1) 换一台机器部署（推荐，演示站不该和开发环境挤在一起）"
  echo "     2) 先停掉现有那一份：cd <旧目录> && docker compose down"
  echo "     3) 想换端口共存：把 docker-compose.yml 的 container_name 全部删掉再起"
  exit 1
fi
ok "无同名容器"

# ---------- 4. 启动 ----------
if [ "$RESET" = "1" ]; then
  say "清空数据卷（数据库与上传文件将丢失）"
  docker compose down -v || true
fi

say "启动容器（首次会初始化数据库，需要 1~3 分钟）"
docker compose up -d

# ---------- 5. 等就绪 ----------
say "等待后端就绪"
READY=0
for i in $(seq 1 60); do
  if curl -fsS -m 3 "http://127.0.0.1:${SERVER_PORT}/shop/decorate/home" >/dev/null 2>&1; then
    READY=1; ok "后端已就绪（第 $((i*3)) 秒）"; break
  fi
  sleep 3
done
[ "$READY" = "0" ] && { bad "后端未在 180 秒内就绪，查看日志：docker compose logs --tail 100 server"; exit 1; }

for u in / /pc/ /admin-web/; do
  code=$(curl -s -o /dev/null -m 5 -w "%{http_code}" "http://127.0.0.1:${WEB_PORT}${u}" || echo 000)
  [ "$code" = "200" ] && ok "前端 ${u} 200" || bad "前端 ${u} 返回 ${code}"
done

# ---------- 6. 防火墙 ----------
say "检查防火墙"
if command -v ufw >/dev/null 2>&1 && ufw status 2>/dev/null | grep -q "Status: active"; then
  ufw allow "${WEB_PORT}/tcp" >/dev/null 2>&1 && ok "ufw 已放行 ${WEB_PORT}"
elif command -v firewall-cmd >/dev/null 2>&1 && firewall-cmd --state >/dev/null 2>&1; then
  firewall-cmd --permanent --add-port="${WEB_PORT}/tcp" >/dev/null && firewall-cmd --reload >/dev/null && ok "firewalld 已放行 ${WEB_PORT}"
else
  ok "未检测到启用中的本机防火墙"
fi

# ---------- 7. 输出 ----------
PUBLIC_IP=$(curl -fsS -m 5 https://api.ipify.org 2>/dev/null || echo "<你的服务器IP>")
echo
echo "=============================================================="
echo "  Minimall 演示站已启动"
echo "=============================================================="
echo "  H5 商城     http://${PUBLIC_IP}:${WEB_PORT}/"
echo "  PC 商城     http://${PUBLIC_IP}:${WEB_PORT}/pc/"
echo "  后台管理    http://${PUBLIC_IP}:${WEB_PORT}/admin-web/"
echo "              admin / admin123   （超管）"
echo "              operator / admin123（运营，用来看权限差异）"
echo "  接口文档    http://${PUBLIC_IP}:${SERVER_PORT}/swagger-ui.html"
echo
echo "  如果打不开，先去云厂商控制台的安全组放行 ${WEB_PORT} 端口（入方向 TCP）"
echo
echo "  常用命令："
echo "    docker compose logs -f server     # 看后端日志"
echo "    docker compose ps                 # 看容器状态"
echo "    bash scripts/deploy-demo.sh --reset   # 清库重建"
echo "=============================================================="
echo
echo "  ⚠️  演示站暴露公网时请注意：数据库是演示数据，随时可能被改；"
echo "     不要往里放真实用户信息，重要数据请自行备份。"
echo
