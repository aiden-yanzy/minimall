# Minimall 单商户自营商城

基于 likeshop（PHP）业务设计、用 **Java + Vue3** 从零自研的单商户商城系统（仅借鉴需求与页面风格，不复制其代码）。

## 技术栈
- 后端：Java 17 · Spring Boot 3.3 · MyBatis-Plus · MySQL 8 · Redis 7 · Sa-Token(双体系 JWT) · springdoc
- C 端：Vue3 + uni-app（一套代码编译 **H5 + 微信小程序**）
- 后台：Vue3 + Vite + Element Plus + Pinia + Vue Router
- 部署：Docker Compose 一键启动（mysql + redis + server + nginx）

## 目录结构
```
minimall/
├── server/          # Spring Boot 后端（含 Dockerfile，按业务域分包）
│   └── src/main/resources/sql/minimall-init.sql   # 全量建表+种子数据
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

## 前端
- **后台 admin**：`cd admin && npm i && npm run dev`（默认 http://localhost:5173，代理到 8080）

- **C 端 uniapp（CLI 标准工程，源码在 `src/`）**：
  ```bash
  cd uniapp && npm i
  npm run dev:h5        # H5 开发（http://localhost:5174，同源代理到后端）
  npm run build:h5      # 产物在 dist/build/h5，已自动部署到 nginx
  npm run dev:mp-weixin # 微信小程序（配合微信开发者工具）
  ```
  - 小程序 / 真机调试时，把 `uniapp/src/api/config.js` 的 `baseUrl` 改为后端地址
  - 也可用 HBuilderX：导入 `uniapp/` 目录前，先新建一个 uni-app(Vue3) 空项目让 HBuilderX 正确识别工程类型，再把源码覆盖进去（HBuilderX 对直接导入的普通目录可能识别为 Web 工程）

- **PC 商城端（Vue3 + Element Plus 宽屏版）**：`cd pc && npm i && npm run dev`(5175) / `npm run build` 部署至 nginx
  - 页面：首页/商品列表(分类+排序)/商品详情/购物车/下单/订单；同源走 nginx 或 vite 代理
- **nginx 网关（docker compose 起）**：http://localhost:8081
  - `/` H5 商城（uniapp 构建产物）· `/pc/` PC 商城 · `/admin-web/` 后台 · `/shop /admin /pay` 反代后端 · `/file/` 上传文件
  - 演示数据（10 商品/首页轮播/评价）：`./scripts/setup-demo.sh` 一键灌入

## 功能地图（完整对标，已全部落地）

### C 端（23 个页面）
| 域 | 功能 |
|---|---|
| 交易 | 首页(装修渲染) / 分类 / 商品详情(SKU) / 购物车 / 下单确认(券/会员价/满减明细分) / 订单列表 / 订单详情(支付/取消/确认收货) |
| 营销 | 领券中心 / 我的券 / 限时秒杀(Redis库存) / 拼团 / 砍价 / 预售 / 积分商城 / 每日签到 |
| 钱包分销 | 我的钱包(充值/提现/流水) / 分销绑定推荐人+佣金 |
| 内容 | 文章中心 / 在线客服 |
| 我的 | 个人中心 / 登录注册 |

### 后台（10 个菜单页）
工作台(实时统计+7日趋势) / 商品管理 / 订单管理(发货) / 售后管理(审核退款) / 营销管理(券+满减) / 积分商城(商品+兑换订单+签到配置) / 秒杀管理 / 提现审核 / 内容装修(首页JSON+文章+客服回复)

### 后端（约 90 个接口）
- 四套接口域：`/shop`(C端) `/admin`(后台) `/pay`(支付回调预留)
- 促销管道：商品总额 → 会员等级折扣 → 全场满减 → 优惠券 → 应付
- 秒杀 Redis 原子库存预扣；拼团开团/参团/成团；砍价每人帮砍一次递减至地板价；预售定金/尾款
- 收货后结算一级分销佣金；余额支付/充值/提现审核/账户流水
- 支付回调已按微信/支付宝签名结构预留（当前模拟直通）

## 迭代记录
| 阶段 | 内容 | 状态 |
|---|---|---|
| P0 | 骨架 + 双端认证 + Docker + 核心 DDL | ✅ |
| P1 | 商品/购物车/下单/模拟支付 + 后台商品 | ✅ |
| P2 | 发货/确认收货/售后/退款 | ✅ |
| P3 | 优惠券 + 会员价 + 满减 | ✅ |
| P4 | 秒杀(Redis) + 积分商城 + 签到 | ✅ |
| P5 | 拼团 + 砍价 + 预售 | ✅ |
| P6 | 分销 + 钱包(充值/提现/流水) | ✅ |
| P7 | 装修 + 文章 + 在线客服 | ✅ |
| P8 | 工作台报表 + 支付回调预留 + 收尾 | ✅ |

> 备注：线上运行需补充——短信发送、真实微信/支付宝接入验签、OSS 文件存储、拼团超时自动退款定时任务、内容安全与合规。详见 `docs/01-技术设计方案.md`