#!/usr/bin/env bash
# ============================================================
#  Minimall 一键启动脚本
#  用途：编译后端镜像 + 构建三个前端 + 启动整套环境 + 打印入口
#  用法：./scripts/start.sh
# ============================================================
set -e
cd "$(dirname "$0")/.."

echo "=================================================="
echo "  Minimall 一键启动（后端 + H5 + PC + 后台）"
echo "=================================================="

echo ""
echo "[1/4] 构建并启动后端(MySQL/Redis/Server/Nginx)..."
docker compose up -d --build server nginx
echo "      ✓ 容器已拉起（首次会初始化数据库）"

echo ""
echo "[2/4] 等待后端就绪 ..."
OK=0
for i in $(seq 1 45); do
  if curl -s -o /dev/null http://localhost:8080/v3/api-docs 2>/dev/null; then OK=1; break; fi
  sleep 2
done
if [ "$OK" != "1" ]; then
  echo "      ✗ 后端启动超时，请查看日志: docker compose logs server"
  exit 1
fi
echo "      ✓ 后端已就绪"

echo ""
echo "[3/4] 构建并部署三个前端（admin / PC / H5）..."
bash scripts/deploy-frontends.sh all

echo ""
echo "[4/4] 重启 nginx 加载最新配置 ..."
docker compose restart nginx >/dev/null 2>&1 || true
sleep 2

echo ""
echo "=================================================="
echo "  ✅ 启动完成，开始测试吧："
echo ""
echo "   🛍️  H5  商城 : http://localhost:8081/"
echo "   🖥️  PC  商城 : http://localhost:8081/pc/"
echo "   ⚙️  后台管理 : http://localhost:8081/admin-web/   (admin / admin123)"
echo "   📚 接口文档 : http://localhost:8080/swagger-ui.html"
echo "   👤 测试用户 : 13100131000 / 123456（也可注册新号）"
echo ""
echo "   常用命令："
echo "     docker compose logs -f server   # 看后端日志"
echo "     ./scripts/setup-demo.sh         # 重建环境后补灌演示数据"
echo "     ./scripts/start.sh              # 以后随时一键启动"
echo "=================================================="