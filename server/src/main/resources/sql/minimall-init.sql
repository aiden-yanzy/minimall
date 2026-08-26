-- ============================================================
-- Minimall 单商户商城 初始表结构（P0 阶段）
-- 命名规范：snake_case；主键 bigint 自增；含 create_time/update_time/delete_flag
-- 金额统一用 BIGINT 存"分"，避免浮点误差
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------
-- 1. 用户域
-- --------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sn`             VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '用户编号',
  `nickname`       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar`         VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像',
  `mobile`         VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '手机号',
  `password`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '密码(BCrypt)',
  `sex`            TINYINT      NOT NULL DEFAULT 0 COMMENT '性别 0未知1男2女',
  `birthday`       DATETIME     NULL COMMENT '生日',
  `level_id`       BIGINT       NULL COMMENT '会员等级id',
  `balance`        BIGINT       NOT NULL DEFAULT 0 COMMENT '余额(分)',
  `integral`       BIGINT       NOT NULL DEFAULT 0 COMMENT '积分',
  `total_consume`  BIGINT       NOT NULL DEFAULT 0 COMMENT '累计消费(分)',
  `total_order_num` INT         NOT NULL DEFAULT 0 COMMENT '累计订单数',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  `channel`        VARCHAR(16)  NOT NULL DEFAULT 'h5' COMMENT '注册渠道 h5/miniapp/wechat',
  `last_login_time` DATETIME    NULL COMMENT '最后登录时间',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_mobile` (`mobile`),
  KEY `idx_sn` (`sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户';

DROP TABLE IF EXISTS `user_auth`;
CREATE TABLE `user_auth` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT NOT NULL,
  `type`       VARCHAR(16) NOT NULL COMMENT 'wechat/phone/miniapp',
  `openid`     VARCHAR(64) NOT NULL DEFAULT '',
  `unionid`    VARCHAR(64) NOT NULL DEFAULT '',
  `nickname`   VARCHAR(64) NOT NULL DEFAULT '',
  `avatar`     VARCHAR(255) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_openid` (`type`, `openid`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方/多端登录授权';

DROP TABLE IF EXISTS `user_level`;
CREATE TABLE `user_level` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `icon`        VARCHAR(255) NOT NULL DEFAULT '',
  `level`       INT NOT NULL DEFAULT 0 COMMENT '等级值',
  `discount`    DECIMAL(4,2) NOT NULL DEFAULT 10.00 COMMENT '折扣，如9.5折=9.50',
  `upgrade_consume` BIGINT NOT NULL DEFAULT 0 COMMENT '升级所需累计消费(分)',
  `privilege`   VARCHAR(255) NOT NULL DEFAULT '' COMMENT '权益描述',
  `status`      TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级';

DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL,
  `contact`     VARCHAR(32) NOT NULL COMMENT '联系人',
  `mobile`      VARCHAR(20) NOT NULL,
  `province_id` INT NOT NULL DEFAULT 0,
  `city_id`     INT NOT NULL DEFAULT 0,
  `district_id` INT NOT NULL DEFAULT 0,
  `address`     VARCHAR(255) NOT NULL COMMENT '详细地址',
  `longitude`   DECIMAL(10,6) NULL,
  `latitude`    DECIMAL(10,6) NULL,
  `is_default`  TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

-- --------------------------------------------
-- 2. 系统域
-- --------------------------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `account`    VARCHAR(64) NOT NULL,
  `password`   VARCHAR(255) NOT NULL COMMENT 'BCrypt',
  `name`       VARCHAR(64) NOT NULL DEFAULT '',
  `role_name`  VARCHAR(64) NOT NULL DEFAULT 'admin' COMMENT '角色(string,后期换role表)',
  `avatar`     VARCHAR(255) NOT NULL DEFAULT '',
  `status`     TINYINT NOT NULL DEFAULT 1,
  `last_login_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员';

DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL COMMENT '配置键',
  `value`       TEXT COMMENT '配置值(JSON)',
  `remark`      VARCHAR(255) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置(键值)';

-- --------------------------------------------
-- 3. 商品域
-- --------------------------------------------
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `pid`         BIGINT NOT NULL DEFAULT 0 COMMENT '父级id',
  `level`       TINYINT NOT NULL DEFAULT 1,
  `icon`        VARCHAR(255) NOT NULL DEFAULT '',
  `image`       VARCHAR(255) NOT NULL DEFAULT '',
  `sort`        INT NOT NULL DEFAULT 0,
  `is_show`     TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

DROP TABLE IF EXISTS `goods_brand`;
CREATE TABLE `goods_brand` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `logo`        VARCHAR(255) NOT NULL DEFAULT '',
  `sort`        INT NOT NULL DEFAULT 0,
  `is_show`     TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品品牌';

DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(255) NOT NULL COMMENT '商品名',
  `subtitle`    VARCHAR(255) NOT NULL DEFAULT '' COMMENT '副标题/卖点',
  `category_id` BIGINT NULL,
  `brand_id`    BIGINT NULL,
  `unit_id`     BIGINT NULL COMMENT '单位(件/个)',
  `image`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '主图',
  `video`       VARCHAR(255) NOT NULL DEFAULT '',
  `min_price`   BIGINT NOT NULL DEFAULT 0 COMMENT '最低售价(分)',
  `max_price`   BIGINT NOT NULL DEFAULT 0 COMMENT '最高售价(分)',
  `market_price` BIGINT NOT NULL DEFAULT 0 COMMENT '市场价/划线价(分)',
  `stock`       INT NOT NULL DEFAULT 0 COMMENT '总库存',
  `sales`       INT NOT NULL DEFAULT 0 COMMENT '销量',
  `virtual_sales` INT NOT NULL DEFAULT 0 COMMENT '虚拟销量',
  `content`     MEDIUMTEXT COMMENT '富文本详情',
  `delivery_template_id` BIGINT NULL COMMENT '运费模板id',
  `spec_type`   TINYINT NOT NULL DEFAULT 0 COMMENT '0单规格 1多规格',
  `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  `recommend`   TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐',
  `is_show`     TINYINT NOT NULL DEFAULT 1,
  `sort`        INT NOT NULL DEFAULT 0,
  `refund_type` TINYINT NOT NULL DEFAULT 1 COMMENT '是否支持退款',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU';

DROP TABLE IF EXISTS `goods_item`;
CREATE TABLE `goods_item` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `goods_id`    BIGINT NOT NULL,
  `item_sn`     VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'SKU编码',
  `image`       VARCHAR(255) NOT NULL DEFAULT '',
  `price`       BIGINT NOT NULL DEFAULT 0 COMMENT '售价(分)',
  `cost_price`  BIGINT NOT NULL DEFAULT 0 COMMENT '成本价(分)',
  `market_price` BIGINT NOT NULL DEFAULT 0,
  `stock`       INT NOT NULL DEFAULT 0,
  `sales`       INT NOT NULL DEFAULT 0,
  `weight`      DECIMAL(10,2) NOT NULL DEFAULT 0,
  `volume`      DECIMAL(10,2) NOT NULL DEFAULT 0,
  `spec_value_ids` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '规格值id组合,如 1,2,3',
  `spec_value_str` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '规格文字,如 红色/M',
  `status`      TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU';

DROP TABLE IF EXISTS `goods_spec`;
CREATE TABLE `goods_spec` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `goods_id`    BIGINT NOT NULL,
  `name`        VARCHAR(64) NOT NULL COMMENT '规格名,如颜色',
  `sort`        INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格';

DROP TABLE IF EXISTS `goods_spec_value`;
CREATE TABLE `goods_spec_value` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `goods_id`    BIGINT NOT NULL,
  `spec_id`     BIGINT NOT NULL,
  `value`       VARCHAR(64) NOT NULL COMMENT '规格值,如红色',
  `image`       VARCHAR(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_spec_id` (`spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格值';

DROP TABLE IF EXISTS `goods_image`;
CREATE TABLE `goods_image` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `goods_id`    BIGINT NOT NULL,
  `url`         VARCHAR(255) NOT NULL,
  `sort`        INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品轮播图/视频';

DROP TABLE IF EXISTS `goods_comment`;
CREATE TABLE `goods_comment` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`    BIGINT NOT NULL,
  `order_goods_id` BIGINT NOT NULL,
  `user_id`     BIGINT NOT NULL,
  `goods_id`    BIGINT NOT NULL,
  `goods_item_id` BIGINT NOT NULL DEFAULT 0,
  `score`       TINYINT NOT NULL DEFAULT 5 COMMENT '评分1-5',
  `description_score` TINYINT NOT NULL DEFAULT 5,
  `service_score` TINYINT NOT NULL DEFAULT 5,
  `content`     VARCHAR(1000) NOT NULL DEFAULT '',
  `reply`       VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '商家回复',
  `is_show`     TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价';

DROP TABLE IF EXISTS `goods_comment_image`;
CREATE TABLE `goods_comment_image` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `comment_id`  BIGINT NOT NULL,
  `url`         VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价图片';

DROP TABLE IF EXISTS `goods_delivery_template`;
CREATE TABLE `goods_delivery_template` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `charge_type` TINYINT NOT NULL DEFAULT 1 COMMENT '计费:1按件 2按重量 3按体积',
  `is_free_shipping` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运费模板';

DROP TABLE IF EXISTS `freight_config`;
CREATE TABLE `freight_config` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `template_id` BIGINT NOT NULL,
  `region_ids`  VARCHAR(500) NOT NULL COMMENT '地区id集合',
  `region_name` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '地区名称(冗余展示)',
  `first_unit`  INT NOT NULL DEFAULT 0 COMMENT '首件/首重',
  `first_price` BIGINT NOT NULL DEFAULT 0 COMMENT '首件运费(分)',
  `continue_unit` INT NOT NULL DEFAULT 0 COMMENT '续件/续重',
  `continue_price` BIGINT NOT NULL DEFAULT 0 COMMENT '续件运费(分)',
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运费规则';

-- --------------------------------------------
-- 4. 交易域
-- --------------------------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT NOT NULL,
  `goods_id`      BIGINT NOT NULL,
  `goods_item_id` BIGINT NOT NULL,
  `goods_snapshot` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '商品信息快照',
  `num`           INT NOT NULL DEFAULT 1,
  `is_selected`   TINYINT NOT NULL DEFAULT 1,
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag`   TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_sn`       VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id`        BIGINT NOT NULL,
  `order_type`     TINYINT NOT NULL DEFAULT 0 COMMENT '0普通 1拼团 2秒杀 3预售 4积分',
  `order_status`   TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1待发货 2待收货 3已完成 4已取消',
  `pay_status`     TINYINT NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付',
  `pay_type`       TINYINT NOT NULL DEFAULT 0 COMMENT '1微信 2支付宝 3余额',
  `pay_time`       DATETIME NULL,
  `pay_sn`         VARCHAR(64) NOT NULL DEFAULT '' COMMENT '支付流水号',
  `total_amount`   BIGINT NOT NULL DEFAULT 0 COMMENT '商品总额(分)',
  `member_discount` BIGINT NOT NULL DEFAULT 0 COMMENT '会员折扣(分)',
  `discount_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '优惠金额(分)',
  `freight_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '运费(分)',
  `pay_amount`     BIGINT NOT NULL DEFAULT 0 COMMENT '实付(分)',
  `coupon_id`      BIGINT NULL,
  `coupon_amount`  BIGINT NOT NULL DEFAULT 0,
  `integral_amount` BIGINT NOT NULL DEFAULT 0,
  `remark`         VARCHAR(255) NOT NULL DEFAULT '' COMMENT '买家留言',
  -- 收货信息快照
  `contact`        VARCHAR(32) NOT NULL DEFAULT '',
  `mobile`         VARCHAR(20) NOT NULL DEFAULT '',
  `address`        VARCHAR(255) NOT NULL DEFAULT '',
  -- 物流
  `delivery_type`  TINYINT NOT NULL DEFAULT 1 COMMENT '1快递 2自提',
  `express_id`     BIGINT NULL,
  `express_name`   VARCHAR(32) NOT NULL DEFAULT '',
  `express_no`     VARCHAR(64) NOT NULL DEFAULT '',
  `delivery_time`  DATETIME NULL,
  `receive_time`   DATETIME NULL COMMENT '确认收货时间',
  `cancel_time`    DATETIME NULL,
  `cancel_reason`  VARCHAR(255) NOT NULL DEFAULT '',
  `close_time`     DATETIME NULL COMMENT '自动关单时间',
  `commission`     BIGINT NOT NULL DEFAULT 0 COMMENT '已分佣金额',
  `is_comment`     TINYINT NOT NULL DEFAULT 0,
  `is_after_sale`  TINYINT NOT NULL DEFAULT 0,
  `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag`    TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

DROP TABLE IF EXISTS `order_goods`;
CREATE TABLE `order_goods` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT NOT NULL,
  `user_id`       BIGINT NOT NULL,
  `goods_id`      BIGINT NOT NULL,
  `goods_item_id` BIGINT NOT NULL,
  `goods_name`    VARCHAR(255) NOT NULL COMMENT '商品名快照',
  `goods_image`   VARCHAR(255) NOT NULL DEFAULT '',
  `spec_value_str` VARCHAR(128) NOT NULL DEFAULT '',
  `price`         BIGINT NOT NULL COMMENT '成交单价快照(分)',
  `num`           INT NOT NULL DEFAULT 1,
  `total_price`   BIGINT NOT NULL DEFAULT 0 COMMENT '小计',
  `discount_price` BIGINT NOT NULL DEFAULT 0,
  `is_comment`    TINYINT NOT NULL DEFAULT 0,
  `is_after_sale` TINYINT NOT NULL DEFAULT 0,
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag`   TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品';

DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`    BIGINT NOT NULL,
  `type`        TINYINT NOT NULL DEFAULT 0 COMMENT '操作类型',
  `operator`    VARCHAR(32) NOT NULL DEFAULT '' COMMENT '操作人',
  `content`     VARCHAR(255) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单日志';

DROP TABLE IF EXISTS `after_sale`;
CREATE TABLE `after_sale` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `after_sale_sn` VARCHAR(32) NOT NULL,
  `order_id`    BIGINT NOT NULL,
  `order_goods_id` BIGINT NOT NULL,
  `user_id`     BIGINT NOT NULL,
  `type`        TINYINT NOT NULL DEFAULT 1 COMMENT '1仅退款 2退货退款 3换货',
  `reason`      VARCHAR(255) NOT NULL DEFAULT '',
  `refund_amount` BIGINT NOT NULL DEFAULT 0,
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1同意 2拒绝 3已退款 4已完成',
  `refund_sn`   VARCHAR(64) NOT NULL DEFAULT '' COMMENT '退款流水号',
  `refund_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sn` (`after_sale_sn`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后单';

-- --------------------------------------------
-- 5. 基础数据
-- --------------------------------------------
DROP TABLE IF EXISTS `express`;
CREATE TABLE `express` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(32) NOT NULL COMMENT '快递公司名',
  `code`        VARCHAR(32) NOT NULL DEFAULT '' COMMENT '快递编码',
  `sort`        INT NOT NULL DEFAULT 0,
  `is_show`     TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快递公司';

DROP TABLE IF EXISTS `region`;
CREATE TABLE `region` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `pid`         INT NOT NULL DEFAULT 0,
  `name`        VARCHAR(64) NOT NULL,
  `level`       TINYINT NOT NULL DEFAULT 1 COMMENT '1省 2市 3区',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行政区域(基础)';

-- --------------------------------------------
-- 6. 营销域 (P3)
-- --------------------------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`           VARCHAR(64) NOT NULL COMMENT '券名称',
  `type`           TINYINT NOT NULL DEFAULT 1 COMMENT '1满减券 2折扣券',
  `scope`          TINYINT NOT NULL DEFAULT 1 COMMENT '1全场 2分类 3指定商品',
  `scope_ids`      VARCHAR(500) NOT NULL DEFAULT '' COMMENT '分类/商品id集合',
  `threshold`      BIGINT NOT NULL DEFAULT 0 COMMENT '使用门槛(分),0=无门槛',
  `discount_value` BIGINT NOT NULL DEFAULT 0 COMMENT '满减金额(分) 或 折扣(千分比,950=9.5折)',
  `total_count`    INT NOT NULL DEFAULT 0 COMMENT '发放总量,0=不限',
  `send_count`     INT NOT NULL DEFAULT 0 COMMENT '已发放',
  `per_limit`      INT NOT NULL DEFAULT 1 COMMENT '每人限领',
  `valid_type`     TINYINT NOT NULL DEFAULT 1 COMMENT '1固定日期 2领取后N天',
  `start_time`     DATETIME NULL,
  `end_time`       DATETIME NULL,
  `valid_days`     INT NOT NULL DEFAULT 0 COMMENT '领取后有效天数',
  `status`         TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `description`    VARCHAR(255) NOT NULL DEFAULT '',
  `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag`    TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板';

DROP TABLE IF EXISTS `coupon_list`;
CREATE TABLE `coupon_list` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `coupon_id`      BIGINT NOT NULL,
  `user_id`        BIGINT NOT NULL,
  `coupon_name`    VARCHAR(64) NOT NULL,
  `type`           TINYINT NOT NULL DEFAULT 1,
  `scope`          TINYINT NOT NULL DEFAULT 1,
  `scope_ids`      VARCHAR(500) NOT NULL DEFAULT '',
  `threshold`      BIGINT NOT NULL DEFAULT 0,
  `discount_value` BIGINT NOT NULL DEFAULT 0,
  `status`         TINYINT NOT NULL DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
  `receive_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire_time`    DATETIME NULL,
  `use_time`       DATETIME NULL,
  `use_order_id`   BIGINT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `status`),
  KEY `idx_coupon` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券';

DROP TABLE IF EXISTS `full_reduction`;
CREATE TABLE `full_reduction` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `threshold`   BIGINT NOT NULL COMMENT '满(分)',
  `reduce`      BIGINT NOT NULL COMMENT '减(分)',
  `start_time`  DATETIME NULL,
  `end_time`    DATETIME NULL,
  `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全场满减规则';

-- 演示优惠券与满减
INSERT INTO `coupon` (`name`, `type`, `scope`, `threshold`, `discount_value`, `total_count`, `per_limit`, `valid_type`, `valid_days`, `status`, `description`) VALUES
('新人无门槛5元券', 1, 1, 0, 500, 1000, 1, 2, 7, 1, '新用户专享，无门槛立减5元'),
('满300减50', 1, 1, 30000, 5000, 500, 1, 2, 30, 1, '全场满300减50');

INSERT INTO `full_reduction` (`name`, `threshold`, `reduce`, `status`) VALUES
('全场满200减20', 20000, 2000, 1);

-- --------------------------------------------
-- 7. 积分商城 + 签到 (P4-a)
-- --------------------------------------------
DROP TABLE IF EXISTS `integral_goods`;
CREATE TABLE `integral_goods` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(128) NOT NULL,
  `image`       VARCHAR(255) NOT NULL DEFAULT '',
  `integral`    BIGINT NOT NULL DEFAULT 0 COMMENT '兑换所需积分',
  `stock`       INT NOT NULL DEFAULT 0,
  `sales`       INT NOT NULL DEFAULT 0,
  `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  `description` TEXT,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品';

DROP TABLE IF EXISTS `integral_order`;
CREATE TABLE `integral_order` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_sn`    VARCHAR(32) NOT NULL,
  `user_id`     BIGINT NOT NULL,
  `integral_goods_id` BIGINT NOT NULL,
  `goods_name`  VARCHAR(128) NOT NULL,
  `goods_image` VARCHAR(255) NOT NULL DEFAULT '',
  `integral`    BIGINT NOT NULL DEFAULT 0 COMMENT '单价积分',
  `num`         INT NOT NULL DEFAULT 1,
  `total_integral` BIGINT NOT NULL DEFAULT 0 COMMENT '合计积分',
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0待发货 1已发货 2已完成',
  `contact`     VARCHAR(32) NOT NULL DEFAULT '',
  `mobile`      VARCHAR(20) NOT NULL DEFAULT '',
  `address`     VARCHAR(255) NOT NULL DEFAULT '',
  `express_name` VARCHAR(32) NOT NULL DEFAULT '',
  `express_no`  VARCHAR(64) NOT NULL DEFAULT '',
  `express_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换订单';

DROP TABLE IF EXISTS `sign_daily`;
CREATE TABLE `sign_daily` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `integral`         INT NOT NULL DEFAULT 5 COMMENT '单日签到积分',
  `continuous_days`  INT NOT NULL DEFAULT 7 COMMENT '连续天数阈值',
  `continuous_reward` INT NOT NULL DEFAULT 10 COMMENT '连续达标额外积分',
  `status`           TINYINT NOT NULL DEFAULT 1,
  `create_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag`      TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日签到配置';

DROP TABLE IF EXISTS `sign_log`;
CREATE TABLE `sign_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL,
  `sign_date`   DATE NOT NULL,
  `integral`    INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录';

-- 演示数据
INSERT INTO `sign_daily` (`integral`, `continuous_days`, `continuous_reward`) VALUES (5, 7, 10);
INSERT INTO `integral_goods` (`name`, `image`, `integral`, `stock`, `status`, `description`) VALUES
('限定帆布袋', '/file/demo/bag.png', 500, 50, 1, '环保材质 限量兑换'),
('品牌雨伞', '/file/demo/umbrella.png', 800, 30, 1, '晴雨两用');

-- --------------------------------------------
-- 8. 秒杀 (P4-b)
-- --------------------------------------------
DROP TABLE IF EXISTS `seckill_activity`;
CREATE TABLE `seckill_activity` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `start_time`  DATETIME NOT NULL,
  `end_time`    DATETIME NOT NULL,
  `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `sort`        INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀场次';

DROP TABLE IF EXISTS `seckill_goods`;
CREATE TABLE `seckill_goods` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `activity_id`    BIGINT NOT NULL,
  `goods_id`       BIGINT NOT NULL,
  `goods_item_id`  BIGINT NOT NULL,
  `seckill_price`  BIGINT NOT NULL DEFAULT 0 COMMENT '秒杀价(分)',
  `seckill_stock`  INT NOT NULL DEFAULT 0 COMMENT '秒杀库存',
  `seckill_sales`  INT NOT NULL DEFAULT 0 COMMENT '秒杀销量',
  `per_limit`      INT NOT NULL DEFAULT 1 COMMENT '每人限购',
  `sort`           INT NOT NULL DEFAULT 0,
  `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag`    TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_item` (`goods_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品';

-- 演示秒杀：全天场（今天 00:00 ~ 23:59）绑定现有商品SKU
INSERT INTO `seckill_activity` (`name`, `start_time`, `end_time`, `status`, `sort`)
VALUES ('今日秒杀', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 1);
INSERT INTO `seckill_goods` (`activity_id`, `goods_id`, `goods_item_id`, `seckill_price`, `seckill_stock`, `per_limit`, `sort`)
VALUES (1, 1, 1, 19900, 20, 1, 1);

-- --------------------------------------------
-- 9. 拼团 (P5)
-- --------------------------------------------
DROP TABLE IF EXISTS `team_activity`;
CREATE TABLE `team_activity` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `goods_id`    BIGINT NOT NULL,
  `goods_item_id` BIGINT NOT NULL,
  `team_price`  BIGINT NOT NULL COMMENT '拼团价(分)',
  `team_num`    INT NOT NULL DEFAULT 2 COMMENT '成团人数',
  `per_limit`   INT NOT NULL DEFAULT 1 COMMENT '每人限购',
  `start_time`  DATETIME NOT NULL,
  `end_time`    DATETIME NOT NULL,
  `status`      TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团活动';

DROP TABLE IF EXISTS `team_found`;
CREATE TABLE `team_found` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `user_id`     BIGINT NOT NULL COMMENT '团长',
  `order_id`    BIGINT NOT NULL,
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0拼团中 1成功 2失败',
  `success_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团-团';

DROP TABLE IF EXISTS `team_join`;
CREATE TABLE `team_join` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `found_id`    BIGINT NOT NULL,
  `activity_id` BIGINT NOT NULL,
  `user_id`     BIGINT NOT NULL,
  `order_id`    BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_id`),
  KEY `idx_found` (`found_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团-参团记录';

-- --------------------------------------------
-- 10. 砍价 (P5)
-- --------------------------------------------
DROP TABLE IF EXISTS `bargain_activity`;
CREATE TABLE `bargain_activity` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `goods_id`    BIGINT NOT NULL,
  `goods_item_id` BIGINT NOT NULL,
  `base_price`  BIGINT NOT NULL COMMENT '原价',
  `floor_price` BIGINT NOT NULL COMMENT '底价',
  `help_num`    INT NOT NULL DEFAULT 3 COMMENT '需帮砍人数',
  `start_time`  DATETIME NOT NULL,
  `end_time`    DATETIME NOT NULL,
  `status`      TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='砍价活动';

DROP TABLE IF EXISTS `bargain_initiate`;
CREATE TABLE `bargain_initiate` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `user_id`     BIGINT NOT NULL,
  `current_price` BIGINT NOT NULL,
  `helped_count` INT NOT NULL DEFAULT 0,
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0进行中 1已成交 2已过期',
  `order_id`    BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='砍价-发起';

DROP TABLE IF EXISTS `bargain_help`;
CREATE TABLE `bargain_help` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `initiate_id`     BIGINT NOT NULL,
  `helper_user_id`  BIGINT NOT NULL,
  `reduce_amount`   BIGINT NOT NULL DEFAULT 0,
  `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_initiate_helper` (`initiate_id`, `helper_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='砍价-帮砍';

-- --------------------------------------------
-- 11. 预售 (P5)
-- --------------------------------------------
DROP TABLE IF EXISTS `presell_activity`;
CREATE TABLE `presell_activity` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `goods_id`    BIGINT NOT NULL,
  `goods_item_id` BIGINT NOT NULL,
  `deposit`     BIGINT NOT NULL COMMENT '定金(分)',
  `final_price` BIGINT NOT NULL COMMENT '尾款(分)',
  `stock`       INT NOT NULL DEFAULT 0,
  `sales`       INT NOT NULL DEFAULT 0,
  `start_time`  DATETIME NOT NULL,
  `end_time`    DATETIME NOT NULL,
  `final_end_time` DATETIME NOT NULL COMMENT '尾款支付截止',
  `status`      TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预售活动';

DROP TABLE IF EXISTS `presell_order`;
CREATE TABLE `presell_order` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_sn`       VARCHAR(32) NOT NULL,
  `user_id`        BIGINT NOT NULL,
  `activity_id`    BIGINT NOT NULL,
  `goods_id`       BIGINT NOT NULL,
  `goods_item_id`  BIGINT NOT NULL,
  `goods_name`     VARCHAR(128) NOT NULL,
  `goods_image`    VARCHAR(255) NOT NULL DEFAULT '',
  `deposit`        BIGINT NOT NULL COMMENT '已付定金',
  `final_price`    BIGINT NOT NULL COMMENT '应付尾款',
  `pay_sn`         VARCHAR(64) NOT NULL DEFAULT '',
  `status`         TINYINT NOT NULL DEFAULT 0 COMMENT '0待付定金 1待付尾款 2已完成 3已关闭',
  `contact`        VARCHAR(32) NOT NULL DEFAULT '',
  `mobile`         VARCHAR(20) NOT NULL DEFAULT '',
  `address`        VARCHAR(255) NOT NULL DEFAULT '',
  `express_name`   VARCHAR(32) NOT NULL DEFAULT '',
  `express_no`     VARCHAR(64) NOT NULL DEFAULT '',
  `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag`    TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预售订单';

-- 演示数据（绑定商品1/SKU1）
INSERT INTO `team_activity` (`name`, `goods_id`, `goods_item_id`, `team_price`, `team_num`, `per_limit`, `start_time`, `end_time`, `status`)
SELECT '双人拼团耳机', 1, 1, 25900, 2, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 1
WHERE NOT EXISTS (SELECT 1 FROM team_activity);
INSERT INTO `bargain_activity` (`name`, `goods_id`, `goods_item_id`, `base_price`, `floor_price`, `help_num`, `start_time`, `end_time`, `status`)
SELECT '砍价到199', 1, 1, 29900, 19900, 5, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 1
WHERE NOT EXISTS (SELECT 1 FROM bargain_activity);
INSERT INTO `presell_activity` (`name`, `goods_id`, `goods_item_id`, `deposit`, `final_price`, `stock`, `start_time`, `end_time`, `final_end_time`, `status`)
SELECT '新品预售', 1, 2, 1000, 25900, 30, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 1
WHERE NOT EXISTS (SELECT 1 FROM presell_activity);

-- --------------------------------------------
-- 12. 钱包 + 分销 (P6)
-- --------------------------------------------
ALTER TABLE `user` ADD COLUMN `referrer_id` BIGINT NULL COMMENT '推荐人id' AFTER `channel`;

DROP TABLE IF EXISTS `account_log`;
CREATE TABLE `account_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL,
  `type`        TINYINT NOT NULL COMMENT '1充值 2消费 3退款 4提现 5佣金 6签到 99其他',
  `change_amount` BIGINT NOT NULL COMMENT '变动额(分,负数支出)',
  `balance_after` BIGINT NOT NULL DEFAULT 0 COMMENT '变动后余额',
  `remark`      VARCHAR(255) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户流水';

DROP TABLE IF EXISTS `recharge_template`;
CREATE TABLE `recharge_template` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `amount`      BIGINT NOT NULL COMMENT '充值金额(分)',
  `gift_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '赠送金额(分)',
  `status`      TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值档位';

DROP TABLE IF EXISTS `recharge_order`;
CREATE TABLE `recharge_order` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_sn`    VARCHAR(32) NOT NULL,
  `user_id`     BIGINT NOT NULL,
  `template_id` BIGINT NULL,
  `amount`      BIGINT NOT NULL COMMENT '到账金额(含赠送)',
  `pay_amount`  BIGINT NOT NULL COMMENT '实付金额',
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付',
  `pay_sn`      VARCHAR(64) NOT NULL DEFAULT '',
  `pay_time`    DATETIME NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值订单';

DROP TABLE IF EXISTS `withdraw_apply`;
CREATE TABLE `withdraw_apply` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL,
  `amount`      BIGINT NOT NULL COMMENT '提现金额(分)',
  `type`        TINYINT NOT NULL DEFAULT 1 COMMENT '1微信 2银行卡 3余额',
  `account_info` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '收款账户',
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝 3已打款',
  `remark`      VARCHAR(255) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现申请';

DROP TABLE IF EXISTS `distribution_level`;
CREATE TABLE `distribution_level` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL,
  `level`       INT NOT NULL DEFAULT 1,
  `commission_rate` INT NOT NULL DEFAULT 0 COMMENT '佣金比例(千分比,100=10%)',
  `upgrade_condition` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '升级条件',
  `status`      TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销等级';

DROP TABLE IF EXISTS `distribution_order_goods`;
CREATE TABLE `distribution_order_goods` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id`    BIGINT NOT NULL,
  `order_goods_id` BIGINT NOT NULL,
  `buyer_user_id` BIGINT NOT NULL COMMENT '买家',
  `seller_user_id` BIGINT NOT NULL COMMENT '获佣用户(推荐人)',
  `goods_amount` BIGINT NOT NULL COMMENT '商品实付',
  `rate`        INT NOT NULL DEFAULT 0 COMMENT '佣金比例',
  `commission`  BIGINT NOT NULL DEFAULT 0 COMMENT '佣金(分)',
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0待结算 1已结算 2已取消',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_seller` (`seller_user_id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销订单佣金';

INSERT INTO `recharge_template` (`name`, `amount`, `gift_amount`, `status`) VALUES
('充50送5', 5000, 500, 1),
('充100送15', 10000, 1500, 1),
('充200送40', 20000, 4000, 1);
INSERT INTO `distribution_level` (`name`, `level`, `commission_rate`, `status`) VALUES
('普通分销员', 1, 100, 1),
('高级分销员', 2, 150, 1);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 初始数据
-- ============================================================
-- 默认管理员 admin / admin123 （hash 为 BCrypt）
INSERT INTO `admin` (`account`, `password`, `name`, `role_name`) VALUES
('admin', '$2y$10$H7knPw3XoP85wWQAAXxYlOLqBswU0YSXYeultq.vwt9ggF2DZ/p8e', '超级管理员', 'admin');

-- 默认会员等级
INSERT INTO `user_level` (`name`, `level`, `discount`, `upgrade_consume`, `status`) VALUES
('普通会员', 1, 10.00, 0, 1),
('黄金会员', 2, 9.50, 1000000, 1),
('铂金会员', 3, 9.00, 5000000, 1);

-- 演示快递
INSERT INTO `express` (`name`, `code`, `sort`, `is_show`) VALUES
('顺丰速运', 'SF', 1, 1),
('圆通速递', 'YTO', 2, 1),
('中通快递', 'ZTO', 3, 1),
('韵达快递', 'YD', 4, 1);

-- 演示分类/商品（P1 验收数据，可选）
INSERT INTO `goods_category` (`name`, `pid`, `level`, `sort`, `is_show`) VALUES
('数码', 0, 1, 1, 1),
('服饰', 0, 1, 2, 1);

INSERT INTO `goods`
(`id`, `name`, `subtitle`, `category_id`, `image`, `market_price`, `min_price`, `max_price`, `stock`, `sales`, `content`, `spec_type`, `status`, `is_show`, `sort`, `refund_type`)
VALUES
(1, '无线蓝牙耳机 Pro', '主动降噪 长续航', 1, '/file/demo/hp.png', 39900, 29900, 32900, 150, 0, '<p>高性价比蓝牙耳机</p>', 0, 1, 1, 1, 1);

INSERT INTO `goods_item`
(`goods_id`, `item_sn`, `price`, `cost_price`, `market_price`, `stock`, `spec_value_ids`, `spec_value_str`, `status`)
VALUES
(1, 'DEMO-001', 29900, 15000, 39900, 100, '1', '黑色', 1),
(1, 'DEMO-002', 32900, 16000, 42900, 50, '2', '白色', 1);

INSERT INTO `goods_image` (`goods_id`, `url`, `sort`) VALUES
(1, '/file/demo/hp1.png', 0),
(1, '/file/demo/hp2.png', 1);

SET FOREIGN_KEY_CHECKS = 1;
