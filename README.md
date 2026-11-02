# Minimall 单商户自营商城

基于 likeshop（PHP）业务设计、用 **Java + Vue3** 自研的单商户商城系统（仅借鉴需求与页面风格，不复制其代码）。

## 技术栈
- 后端：Java 17 · Spring Boot 3.3 · MyBatis-Plus · MySQL 8 · Redis 7 · Sa-Token(双体系) · springdoc
- C 端：Vue3 + uni-app（一套代码编译 **H5 + 微信小程序**）
- PC 端：Vue3 + Vite + Element Plus（宽屏商城）
- 后台：Vue3 + Vite + Element Plus + Pinia + Vue Router
- 部署：Docker Compose 一键启动（mysql + redis + server + nginx）

## 规模

| 项 | 数量 |
|---|---|
| 接口 | **201**（C 端 85 / 后台 83 / 其余健康与文档） |
| 数据库表 | 65 |
| 后端 | 239 个 Java 文件 / 约 10.7k 行 |
| H5 页面 | 31 |
| PC 页面 | 15 |
| 后台页面 | 20（菜单 24 项，含按钮级权限点） |

## 目录结构
```
minimall/
├── server/          # Spring Boot 后端（按业务域分包）
│   └── src/main/resources/sql/minimall-init.sql   # 全量建表+种子数据
├── uniapp/          # C 端（Vue3 + uni-app，H5 + 小程序）
├── pc/              # PC 商城端
├── admin/           # 后台管理
├── deploy/          # nginx 配置 / www 产物
├── scripts/         # 启动 / 演示数据 / 冒烟测试
├── docker-compose.yml
└── docs/            # 技术设计方案 · 小程序端说明
```

## 快速启动

```bash
# 后端 + 中间件（mysql / redis / server）
docker compose up -d

# 前端三端产物
cd admin  && npm i && npm run build && rsync -a --delete dist/ ../deploy/www/admin/
cd pc     && npm i && npm run build && rsync -a --delete dist/ ../deploy/www/pc/
cd uniapp && npm i && npm run build:h5
rsync -a --delete --exclude 'pc' --exclude 'admin' --exclude 'landing.html' \
      uniapp/dist/build/h5/ ../deploy/www/
```

访问（端口可改 `docker-compose.yml`）：

| 入口 | 地址 |
|---|---|
| H5 商城 | http://localhost:18080/ |
| PC 商城 | http://localhost:18080/pc/ |
| 后台 | http://localhost:18080/admin-web/ （admin / admin123，另有 operator / admin123 演示权限差异）|
| 接口文档 | http://localhost:18082/swagger-ui.html |

后端改动后的构建链路（容器内 Maven 拉依赖不稳，走本机打包 + 薄运行镜像）：

```bash
cd server && JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo /opt/homebrew/opt/openjdk@21) \
  mvn -q -DskipTests package
cd .. && docker build -f server/Dockerfile.runtime -t minimall-server:latest ./server
docker compose up -d --no-build server
```

## 冒烟测试（一条命令验主流程）

```bash
python3 scripts/smoke.py            # 打 http://127.0.0.1:18082
python3 scripts/smoke.py --base http://localhost:18082
python3 scripts/smoke.py --skip-concurrency   # 不需要 docker
```

覆盖 **25 项断言**：健康检查 → 注册登录 → 领券 → 加购下单 → 支付 → 后台发货 → 物流轨迹 → 收货 → 评价 → 成长值累加 → 申请售后 → 后台同意退款 → 退款进余额 → **并发 20 请求打 5 件库存（必须恰好 5 成功、无负库存）**。
任一步失败立即非 0 退出，可直接接 CI。

## 功能地图

### C 端（H5 31 页 / PC 15 页）
| 域 | 功能 |
|---|---|
| 交易 | 首页(可视化装修) / 搜索(历史+热词) / 分类 / 商品详情(SKU) / 购物车 / 下单确认(券+会员价+满减明细分) / 订单列表 / 订单详情(支付/取消/收货/轨迹) |
| 售后 | 申请售后(仅退款/退货退款/换货) / 我的售后(撤销/退款单号) |
| 营销 | 领券中心 / 我的券 / 限时秒杀(Redis 库存) / 拼团 / 砍价 / 预售 / 积分商城 / 每日签到 / 幸运抽奖 |
| 会员 | 会员中心(等级+成长值进度) / 余额明细 / 收藏 / 足迹 / 分销中心(邀请码/团队/佣金) |
| 内容 | 文章中心 / 在线客服 |

### 后台（20 页）
工作台 / 商品(多规格 SKU) / 订单(发货·改价·轨迹) / 售后审核 / 销售统计 / 用户(标签·等级) / 会员等级 / 品牌单位 / 运费模板 / 评价管理 / 营销(券+满减) / 拼团 / 砍价 / 预售 / 签到配置 / 积分商城 / 秒杀 / 提现审核 / 财务明细 / 内容装修 / 角色权限 / 设置中心

### 后端要点
- 促销管道：商品总额 → 会员等级折扣 → 满减 → 优惠券 → 应付
- **库存原子扣减**（`stock >= num` 条件更新，扣不动即失败），并发不超卖
- **未支付订单超时自动关单**（普通 30min / 秒杀 5min，CAS 抢占状态，恢复库存并退回优惠券）
- 秒杀 Redis 预扣 + DB 双写，key 缺失/漂移自动回源校正
- 售后同意即退款到余额并记流水，同时恢复库存（秒杀单回补秒杀库存）
- 分销：邀请码加盐编码、注册自动绑定、按卖家累计佣金匹配等级费率、收货后结算
- 会员成长值 = 累计实付，支付后自动累加并重算等级
- RBAC：`/admin/auth/me` 返回权限码，前端菜单与按钮按权限过滤，后端 `@SaCheckPermission` 兜底

## 迭代记录

| 阶段 | 内容 | 状态 |
|---|---|---|
| P0–P8 | 骨架 / 交易主链路 / 售后 / 营销 / 秒杀 / 拼团砍价预售 / 分销钱包 / 装修内容 / 报表 | ✅ |
| P9 | 多规格 SKU · 首页可视化装修 · RBAC 菜单按钮级 | ✅ |
| P10 | 会员标签与等级成长值 | ✅ |
| P11 | 售后闭环（退款落余额）· 分销中心 · C 端搜索 | ✅ |
| P12 | PC 端营销与会员页 · 微信小程序端可构建 | ✅ |
| P13 | 防超卖 · 未支付超时关单 · 后台 5 个营销域补页面 · 冒烟测试 | ✅ |

## 上线前仍需补（都需要外部资质或资源）

- **真实支付**：微信/支付宝 SDK + 商户号 + 回调验签（当前 `/shop/pay/mock` 模拟直通）
- **小程序登录**：`wx.login` → `code2Session` 换 openid（当前手机号+密码）
- **短信**：注册/找回密码验证码
- **对象存储**：OSS/COS 替换本地 upload 卷
- **域名**：小程序要求 https + 备案域名 + 后台白名单（详见 `docs/小程序端说明.md`）
