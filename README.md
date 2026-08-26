# Minimall 单商户自营商城

基于 likeshop（PHP）业务设计、用 **Java + Vue3** 从零自研的单商户商城系统。

> 目标：完整体验商品 / 交易 / 营销 / 财务 / 装修等模块（参考 likeshop 功能与页面风格，不复制其代码）

## 技术栈
- 后端：Java 17 · Spring Boot 3.3 · MyBatis-Plus · MySQL 8 · Redis 7 · Sa-Token(双体系 JWT)
- C 端：Vue3 + uni-app（一套代码编译 H5 + 微信小程序）
- 后台：Vue3 + Vite + Element Plus + Pinia + Vue Router
- 部署：Docker Compose 一键启动

## 目录结构
```
minimall/
├── server/          # Spring Boot 后端（含 Dockerfile）
├── uniapp/          # C 端（Vue3 + uni-app，H5 + 小程序）
├── admin/           # 后台管理（Vue3 + Element Plus）
├── deploy/          # nginx 配置 / www 产物
├── docker-compose.yml
└── docs/            # 技术设计方案
```

## 快速启动（后端 + 数据库）
```bash
docker compose up -d --build       # 启动 mysql / redis / server
# 后端接口文档: http://localhost:8080/swagger-ui.html
# 默认后台账号: admin / admin123
```

## 验证
```bash
# 注册
curl -X POST localhost:8080/shop/auth/register -H 'Content-Type: application/json' \
  -d '{"mobile":"13800138000","password":"123456"}'
# 登录（返回 token）
curl -X POST localhost:8080/shop/auth/login -H 'Content-Type: application/json' \
  -d '{"mobile":"13800138000","password":"123456"}'
# 用 token 访问
curl localhost:8080/shop/auth/me -H "Authorization: Bearer <token>"
```

## 前端
- **后台 admin**：`cd admin && npm i && npm run dev`（默认 http://localhost:5173，代理到 8080）
- **C 端 uniapp**：用 **HBuilderX** 打开，选择 `uniapp/` 目录 → 导入为 uni-app 项目：
  1. HBuilderX → 文件 → 导入 → 从本地目录导入 → 选择 `uniapp/`
  2. 顶部运行 → **运行到浏览器**(H5) 或 **运行到小程序模拟器 → 微信开发者工具**(小程序)
  3. 后端地址在 `uniapp/api/config.js` 的 `baseUrl` 修改（H5 本地默认 http://localhost:8080；真机/小程序需改为局域网 IP）

## 迭代计划
| 阶段 | 内容 |
|---|---|
| P0 ✅ | 骨架 + 双端登录认证 + Docker + 核心 DDL |
| P1 | 商品 + 购物车 + 下单 + 模拟支付 + 后台管理 |
| P2 | 发货/物流/售后/退款/运费/门店自提 |
| P3 | 优惠券 + 会员价 + 满减 |
| P4 | 秒杀 + 积分商城 + 签到/抽奖 |
| P5 | 拼团 + 砍价 + 预售 |
| P6 | 分销 + 钱包 |
| P7 | 装修 + 内容 |
| P8 | 报表 + 小程序发布 + 真实支付接入 |

详见 `docs/01-技术设计方案.md`
