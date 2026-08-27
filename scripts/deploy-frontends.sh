#!/usr/bin/env bash
# 重新构建全部前端并部署到 nginx（改完代码后一键上线）
# 用法: ./scripts/deploy-frontends.sh [admin|pc|uniapp|all]
set -e
cd "$(dirname "$0")/.."
TARGET="${1:-all}"

build_admin() {
  echo "==> 构建 admin 后台 ..."
  (cd admin && npm run build >/dev/null 2>&1)
  mkdir -p deploy/www/admin && cp -r admin/dist/* deploy/www/admin/
  echo "    已部署 deploy/www/admin/"
}

build_pc() {
  echo "==> 构建 PC 商城 ..."
  (cd pc && npm run build >/dev/null 2>&1)
  mkdir -p deploy/www/pc && cp -r pc/dist/* deploy/www/pc/
  echo "    已部署 deploy/www/pc/"
}

build_uniapp() {
  echo "==> 构建 uniapp H5 ..."
  (cd uniapp && npm run build:h5 >/dev/null 2>&1)
  # H5 产物在 dist/build/h5；部署到 nginx 根目录，替换旧 H5 产物
  rm -rf deploy/www/assets deploy/www/index.html
  cp -r uniapp/dist/build/h5/index.html deploy/www/
  cp -r uniapp/dist/build/h5/assets deploy/www/
  echo "    已部署到 deploy/www/（H5 位于站点根）"
}

case "$TARGET" in
  admin)  build_admin ;;
  pc)     build_pc ;;
  uniapp) build_uniapp ;;
  all)
    build_admin
    build_pc
    build_uniapp
    ;;
esac

echo ""
echo "✅ 前端构建部署完成"
echo "   H5 商城:  http://localhost:8081/"
echo "   PC 商城:  http://localhost:8081/pc/"
echo "   后台:     http://localhost:8081/admin-web/"
echo ""
echo "若还改了 Java 后端代码，追加执行一次："
echo "   docker compose up -d --build server"