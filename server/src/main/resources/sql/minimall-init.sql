-- ============================================================
-- Minimall 单商户商城 · 全量初始化脚本（结构 + 演示数据）
--
-- 用法：MySQL 容器首次启动时自动执行（docker-entrypoint-initdb.d/01-init.sql）
--       手工执行：mysql -u<user> -p <db> < minimall-init.sql
--
-- 约定：
--   · 命名 snake_case；主键 bigint 自增；公共字段 create_time / update_time / delete_flag
--   · 金额统一 BIGINT 存「分」，避免浮点误差
--   · delete_flag：0 正常 / 1 已删除（MyBatis-Plus 逻辑删除）
--
-- 演示账号：
--   后台  admin / admin123（超管）· operator / admin123（运营，用于演示权限差异）
--   会员  13800138000 / 123456
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1å……å€¼ 2æ¶ˆè´¹ 3é€€æ¬¾ 4æçŽ° 5ä½£é‡‘ 6ç­¾åˆ° 99å…¶ä»–',
  `change_amount` bigint NOT NULL COMMENT 'å˜åŠ¨é¢(åˆ†,è´Ÿæ•°æ”¯å‡º)',
  `balance_after` bigint NOT NULL DEFAULT '0' COMMENT 'å˜åŠ¨åŽä½™é¢',
  `remark` varchar(255) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è´¦æˆ·æµæ°´';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `account_log` WRITE;
/*!40000 ALTER TABLE `account_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_log` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `account` varchar(64) NOT NULL,
  `password` varchar(255) NOT NULL COMMENT 'BCrypt',
  `name` varchar(64) NOT NULL DEFAULT '',
  `role_name` varchar(64) NOT NULL DEFAULT 'admin' COMMENT '角色(string,后期换role表)',
  `role_id` bigint DEFAULT NULL COMMENT '角色id',
  `avatar` varchar(255) NOT NULL DEFAULT '',
  `status` tinyint NOT NULL DEFAULT '1',
  `last_login_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台管理员';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` (`id`, `account`, `password`, `name`, `role_name`, `role_id`, `avatar`, `status`, `last_login_time`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'admin','$2y$10$H7knPw3XoP85wWQAAXxYlOLqBswU0YSXYeultq.vwt9ggF2DZ/p8e','超级管理员','admin',1,'',1,'2026-11-02 13:42:58','2026-08-26 15:34:23','2026-11-02 13:42:57',0);
INSERT INTO `admin` (`id`, `account`, `password`, `name`, `role_name`, `role_id`, `avatar`, `status`, `last_login_time`, `create_time`, `update_time`, `delete_flag`) VALUES (4,'operator','$2y$10$H7knPw3XoP85wWQAAXxYlOLqBswU0YSXYeultq.vwt9ggF2DZ/p8e','运营专员','operator',2,'',1,'2026-10-28 17:07:03','2026-08-28 12:41:19','2026-10-28 17:07:02',0);
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `after_sale` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `after_sale_sn` varchar(32) NOT NULL,
  `order_id` bigint NOT NULL,
  `order_goods_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1仅退款 2退货退款 3换货',
  `reason` varchar(255) NOT NULL DEFAULT '',
  `refund_amount` bigint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待处理 1同意 2拒绝 3已退款 4已完成',
  `refund_sn` varchar(64) NOT NULL DEFAULT '' COMMENT '退款流水号',
  `refund_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sn` (`after_sale_sn`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='售后单';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `after_sale` WRITE;
/*!40000 ALTER TABLE `after_sale` DISABLE KEYS */;
/*!40000 ALTER TABLE `after_sale` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_id` bigint DEFAULT NULL,
  `title` varchar(128) NOT NULL,
  `image` varchar(255) NOT NULL DEFAULT '',
  `content` mediumtext,
  `is_show` tinyint NOT NULL DEFAULT '1',
  `sort` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_cat` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ–‡ç« ';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` (`id`, `category_id`, `title`, `image`, `content`, `is_show`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (1,1,'欢迎来到 Minimall 商城','','<h3>欢迎光临</h3><p>全新商城上线，秒杀、拼团、砍价、积分好礼等你来！</p>',1,1,'2026-08-27 00:51:48','2026-08-27 11:16:15',0);
INSERT INTO `article` (`id`, `category_id`, `title`, `image`, `content`, `is_show`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (2,2,'购物指南','','<h3>购物流程</h3><p>选好商品 → 加入购物车 → 提交订单 → 支付 → 等待收货。</p>',1,2,'2026-08-27 00:51:48','2026-08-27 11:16:15',0);
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_category` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ–‡ç« åˆ†ç±»';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `article_category` WRITE;
/*!40000 ALTER TABLE `article_category` DISABLE KEYS */;
INSERT INTO `article_category` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'商城公告',1,1,'2026-08-27 00:51:48','2026-08-27 11:16:15',0);
INSERT INTO `article_category` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'帮助中心',2,1,'2026-08-27 00:51:48','2026-08-27 11:16:15',0);
/*!40000 ALTER TABLE `article_category` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bargain_activity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_item_id` bigint NOT NULL,
  `base_price` bigint NOT NULL COMMENT 'åŽŸä»·',
  `floor_price` bigint NOT NULL COMMENT 'åº•ä»·',
  `help_num` int NOT NULL DEFAULT '3' COMMENT 'éœ€å¸®ç äººæ•°',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç ä»·æ´»åŠ¨';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `bargain_activity` WRITE;
/*!40000 ALTER TABLE `bargain_activity` DISABLE KEYS */;
INSERT INTO `bargain_activity` (`id`, `name`, `goods_id`, `goods_item_id`, `base_price`, `floor_price`, `help_num`, `start_time`, `end_time`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'砍价到199',1,12,29900,19900,5,'2026-08-27 00:00:00','2026-09-03 00:00:00',1,'2026-08-27 00:30:03','2026-08-28 12:17:43',0);
/*!40000 ALTER TABLE `bargain_activity` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bargain_help` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `initiate_id` bigint NOT NULL,
  `helper_user_id` bigint NOT NULL,
  `reduce_amount` bigint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_initiate_helper` (`initiate_id`,`helper_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç ä»·-å¸®ç ';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `bargain_help` WRITE;
/*!40000 ALTER TABLE `bargain_help` DISABLE KEYS */;
/*!40000 ALTER TABLE `bargain_help` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bargain_initiate` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `current_price` bigint NOT NULL,
  `helped_count` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0è¿›è¡Œä¸­ 1å·²æˆäº¤ 2å·²è¿‡æœŸ',
  `order_id` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç ä»·-å‘èµ·';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `bargain_initiate` WRITE;
/*!40000 ALTER TABLE `bargain_initiate` DISABLE KEYS */;
/*!40000 ALTER TABLE `bargain_initiate` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_item_id` bigint NOT NULL,
  `goods_snapshot` varchar(255) NOT NULL DEFAULT '' COMMENT '商品信息快照',
  `num` int NOT NULL DEFAULT '1',
  `is_selected` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '配置键',
  `value` text COMMENT '配置值(JSON)',
  `remark` varchar(255) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置(键值)';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `config` WRITE;
/*!40000 ALTER TABLE `config` DISABLE KEYS */;
INSERT INTO `config` (`id`, `name`, `value`, `remark`, `create_time`, `update_time`) VALUES (1,'home_decorate','{\n \"banners\":[\n  {\"image\":\"/file/demo/banner2.jpg\",\"link\":\"/pages/seckill/list\"},\n  {\"image\":\"/file/demo/banner1.jpg\",\"link\":\"\"},\n  {\"image\":\"/file/demo/banner3.jpg\",\"link\":\"/pages/integral/list\"}],\n \"navs\":[\n  {\"name\":\"限时秒杀\",\"icon\":\"⚡\",\"link\":\"/pages/seckill/list\"},\n  {\"name\":\"积分商城\",\"icon\":\"🎁\",\"link\":\"/pages/integral/list\"},\n  {\"name\":\"我的拼团\",\"icon\":\"👥\",\"link\":\"/pages/team/list\"},\n  {\"name\":\"领券中心\",\"icon\":\"🎫\",\"link\":\"/pages/coupon/center\"},\n  {\"name\":\"售前预售\",\"icon\":\"🛒\",\"link\":\"/pages/presell/list\"},\n  {\"name\":\"在线客服\",\"icon\":\"💬\",\"link\":\"/pages/kefu/kefu\"}],\n \"goodsTitle\":\"今日精选好物\"}','C端首页装修','2026-08-27 00:57:22','2026-10-28 16:57:57');
INSERT INTO `config` (`id`, `name`, `value`, `remark`, `create_time`, `update_time`) VALUES (2,'settings_site','{\"name\":\"Minimall旗舰店\",\"notice\":\"618大促中！\"}','设置分组:site','2026-08-28 12:46:49','2026-08-28 12:46:49');
/*!40000 ALTER TABLE `config` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `type` tinyint NOT NULL DEFAULT '1',
  `scope` tinyint NOT NULL DEFAULT '1',
  `scope_ids` varchar(500) NOT NULL DEFAULT '',
  `threshold` bigint NOT NULL DEFAULT '0',
  `discount_value` bigint NOT NULL DEFAULT '0',
  `total_count` int NOT NULL DEFAULT '0',
  `send_count` int NOT NULL DEFAULT '0',
  `per_limit` int NOT NULL DEFAULT '1',
  `valid_type` tinyint NOT NULL DEFAULT '1',
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `valid_days` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `description` varchar(255) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä¼˜æƒ åˆ¸æ¨¡æ¿';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `coupon` WRITE;
/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` (`id`, `name`, `type`, `scope`, `scope_ids`, `threshold`, `discount_value`, `total_count`, `send_count`, `per_limit`, `valid_type`, `start_time`, `end_time`, `valid_days`, `status`, `description`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'新人无门槛5元券',1,1,'',0,500,1000,2,1,2,NULL,NULL,7,1,'新用户专享，无门槛立减5元','2026-08-26 17:35:09','2026-08-27 11:16:15',0);
INSERT INTO `coupon` (`id`, `name`, `type`, `scope`, `scope_ids`, `threshold`, `discount_value`, `total_count`, `send_count`, `per_limit`, `valid_type`, `start_time`, `end_time`, `valid_days`, `status`, `description`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'满300减50',1,1,'',30000,5000,500,4,1,2,NULL,NULL,30,1,'全场满300减50','2026-08-26 17:35:09','2026-11-02 13:33:14',0);
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon_list` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `coupon_name` varchar(64) NOT NULL,
  `type` tinyint NOT NULL DEFAULT '1',
  `scope` tinyint NOT NULL DEFAULT '1',
  `scope_ids` varchar(500) NOT NULL DEFAULT '',
  `threshold` bigint NOT NULL DEFAULT '0',
  `discount_value` bigint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire_time` datetime DEFAULT NULL,
  `use_time` datetime DEFAULT NULL,
  `use_order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`,`status`),
  KEY `idx_coupon` (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç”¨æˆ·ä¼˜æƒ åˆ¸';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `coupon_list` WRITE;
/*!40000 ALTER TABLE `coupon_list` DISABLE KEYS */;
/*!40000 ALTER TABLE `coupon_list` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `distribution_level` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `level` int NOT NULL DEFAULT '1',
  `commission_rate` int NOT NULL DEFAULT '0' COMMENT 'ä½£é‡‘æ¯”ä¾‹(åƒåˆ†æ¯”,100=10%)',
  `upgrade_amount` bigint NOT NULL DEFAULT '0' COMMENT '升级门槛:累计佣金(分),0=无门槛',
  `upgrade_condition` varchar(255) NOT NULL DEFAULT '' COMMENT 'å‡çº§æ¡ä»¶',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åˆ†é”€ç­‰çº§';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `distribution_level` WRITE;
/*!40000 ALTER TABLE `distribution_level` DISABLE KEYS */;
INSERT INTO `distribution_level` (`id`, `name`, `level`, `commission_rate`, `upgrade_amount`, `upgrade_condition`, `status`, `create_time`) VALUES (1,'普通分销员',1,100,0,'注册即享',1,'2026-08-27 00:40:45');
INSERT INTO `distribution_level` (`id`, `name`, `level`, `commission_rate`, `upgrade_amount`, `upgrade_condition`, `status`, `create_time`) VALUES (2,'高级分销员',2,150,100000,'累计佣金达标',1,'2026-08-27 00:40:45');
/*!40000 ALTER TABLE `distribution_level` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `distribution_order_goods` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `order_goods_id` bigint NOT NULL,
  `buyer_user_id` bigint NOT NULL COMMENT 'ä¹°å®¶',
  `seller_user_id` bigint NOT NULL COMMENT 'èŽ·ä½£ç”¨æˆ·(æŽ¨èäºº)',
  `goods_amount` bigint NOT NULL COMMENT 'å•†å“å®žä»˜',
  `rate` int NOT NULL DEFAULT '0' COMMENT 'ä½£é‡‘æ¯”ä¾‹',
  `commission` bigint NOT NULL DEFAULT '0' COMMENT 'ä½£é‡‘(åˆ†)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0å¾…ç»“ç®— 1å·²ç»“ç®— 2å·²å–æ¶ˆ',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_seller` (`seller_user_id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åˆ†é”€è®¢å•ä½£é‡‘';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `distribution_order_goods` WRITE;
/*!40000 ALTER TABLE `distribution_order_goods` DISABLE KEYS */;
/*!40000 ALTER TABLE `distribution_order_goods` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `express` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL COMMENT '快递公司名',
  `code` varchar(32) NOT NULL DEFAULT '' COMMENT '快递编码',
  `sort` int NOT NULL DEFAULT '0',
  `is_show` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='快递公司';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `express` WRITE;
/*!40000 ALTER TABLE `express` DISABLE KEYS */;
INSERT INTO `express` (`id`, `name`, `code`, `sort`, `is_show`, `create_time`) VALUES (1,'顺丰速运','SF',1,1,'2026-08-26 15:34:23');
INSERT INTO `express` (`id`, `name`, `code`, `sort`, `is_show`, `create_time`) VALUES (2,'圆通速递','YTO',2,1,'2026-08-26 15:34:23');
INSERT INTO `express` (`id`, `name`, `code`, `sort`, `is_show`, `create_time`) VALUES (3,'中通快递','ZTO',3,1,'2026-08-26 15:34:23');
INSERT INTO `express` (`id`, `name`, `code`, `sort`, `is_show`, `create_time`) VALUES (4,'韵达快递','YD',4,1,'2026-08-26 15:34:23');
/*!40000 ALTER TABLE `express` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `free_shipping` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `threshold` bigint NOT NULL COMMENT '满(分)免邮',
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满额免邮';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `free_shipping` WRITE;
/*!40000 ALTER TABLE `free_shipping` DISABLE KEYS */;
INSERT INTO `free_shipping` (`id`, `name`, `threshold`, `start_time`, `end_time`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'满200免邮',20000,NULL,NULL,1,'2026-08-28 11:14:13','2026-08-28 11:23:01',0);
/*!40000 ALTER TABLE `free_shipping` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `free_shipping_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `discount_amount` bigint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='免邮订单';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `free_shipping_order` WRITE;
/*!40000 ALTER TABLE `free_shipping_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `free_shipping_order` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `freight_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `template_id` bigint NOT NULL,
  `region_ids` varchar(500) NOT NULL COMMENT '地区id集合',
  `region_name` varchar(500) NOT NULL DEFAULT '' COMMENT '地区名称(冗余展示)',
  `first_unit` int NOT NULL DEFAULT '0' COMMENT '首件/首重',
  `first_price` bigint NOT NULL DEFAULT '0' COMMENT '首件运费(分)',
  `continue_unit` int NOT NULL DEFAULT '0' COMMENT '续件/续重',
  `continue_price` bigint NOT NULL DEFAULT '0' COMMENT '续件运费(分)',
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运费规则';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `freight_config` WRITE;
/*!40000 ALTER TABLE `freight_config` DISABLE KEYS */;
INSERT INTO `freight_config` (`id`, `template_id`, `region_ids`, `region_name`, `first_unit`, `first_price`, `continue_unit`, `continue_price`) VALUES (1,1,'0','全国',1,500,1,200);
/*!40000 ALTER TABLE `freight_config` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `full_reduction` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `threshold` bigint NOT NULL,
  `reduce` bigint NOT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å…¨åœºæ»¡å‡è§„åˆ™';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `full_reduction` WRITE;
/*!40000 ALTER TABLE `full_reduction` DISABLE KEYS */;
INSERT INTO `full_reduction` (`id`, `name`, `threshold`, `reduce`, `start_time`, `end_time`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'全场满200减20',20000,2000,NULL,NULL,1,'2026-08-26 17:35:09','2026-08-27 11:16:15',0);
/*!40000 ALTER TABLE `full_reduction` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '商品名',
  `subtitle` varchar(255) NOT NULL DEFAULT '' COMMENT '副标题/卖点',
  `category_id` bigint DEFAULT NULL,
  `brand_id` bigint DEFAULT NULL,
  `unit_id` bigint DEFAULT NULL COMMENT '单位(件/个)',
  `image` varchar(255) NOT NULL DEFAULT '' COMMENT '主图',
  `video` varchar(255) NOT NULL DEFAULT '',
  `min_price` bigint NOT NULL DEFAULT '0' COMMENT '最低售价(分)',
  `max_price` bigint NOT NULL DEFAULT '0' COMMENT '最高售价(分)',
  `market_price` bigint NOT NULL DEFAULT '0' COMMENT '市场价/划线价(分)',
  `stock` int NOT NULL DEFAULT '0' COMMENT '总库存',
  `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
  `virtual_sales` int NOT NULL DEFAULT '0' COMMENT '虚拟销量',
  `content` mediumtext COMMENT '富文本详情',
  `delivery_template_id` bigint DEFAULT NULL COMMENT '运费模板id',
  `spec_type` tinyint NOT NULL DEFAULT '0' COMMENT '0单规格 1多规格',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1上架 0下架',
  `recommend` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐',
  `is_show` tinyint NOT NULL DEFAULT '1',
  `sort` int NOT NULL DEFAULT '0',
  `refund_type` tinyint NOT NULL DEFAULT '1' COMMENT '是否支持退款',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods` WRITE;
/*!40000 ALTER TABLE `goods` DISABLE KEYS */;
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'无线蓝牙耳机 Pro','主动降噪 长续航',1,NULL,NULL,'/file/demo/g1_airpods.jpg','',29900,32900,39900,200,82,0,'<p>高性价比蓝牙耳机</p>',1,0,1,0,1,0,1,'2026-08-26 17:11:19','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'简约纯棉T恤','100%棉 亲肤透气',2,NULL,NULL,'/file/demo/g2_tshirt.jpg','',9900,9900,12900,100,84,300,'<p>新疆长绒棉，经典圆领，多色可选。</p>',NULL,0,1,1,1,1,1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (3,'复古运动休闲鞋','宽松显瘦 经典水洗',2,NULL,NULL,'/file/demo/g3_shoes.jpg','',19900,19900,25900,1500,42,200,'<p>重磅丹宁，直筒剪裁，四季百搭。</p>',NULL,1,1,0,1,0,1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (4,'经典格纹长袖衬衫','90绒 轻若无物',2,NULL,NULL,'/file/demo/g4_shirt.jpg','',29900,29900,39900,100,70,150,'<p>高蓬松度白鸭绒，可收纳便携。</p>',NULL,0,1,1,1,3,1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (5,'天然原蜜礼盒装','30袋装 科学搭配',3,NULL,NULL,'/file/demo/g5_honey.jpg','',3990,3990,5900,100,93,500,'<p>6种坚果3种果干，烘焙非油炸。</p>',NULL,0,1,1,1,4,1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (6,'雀巢经典速溶咖啡','阿拉比卡 中度烘焙',3,NULL,NULL,'/file/demo/g6_coffee.jpg','',6900,6900,9900,100,126,260,'<p>产地直采，现烘发货，香醇回甘。</p>',NULL,0,1,0,1,5,1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (7,'Olay 保湿滋润身体乳','玻尿酸 深层补水',4,NULL,NULL,'/file/demo/g7_lotion.jpg','',12900,12900,18900,100,100,400,'<p>多重玻尿酸，锁水保湿不粘腻。</p>',NULL,0,1,1,1,6,1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (8,'男士保湿面霜 100ml','氨基酸温和洁面',4,NULL,NULL,'/file/demo/g8_face.jpg','',3900,3900,5900,100,112,350,'<p>绵密泡沫，清爽控油，敏感肌可用。</p>',NULL,0,1,0,1,7,1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (9,'北欧风LED台灯','三档调光 护眼无频闪',5,NULL,NULL,'/file/demo/g9_lamp.jpg','',8900,8900,15900,100,11,120,'<p>简约设计，触摸调光，USB充电。</p>',NULL,0,1,0,1,8,1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (10,'北欧实木双人床 1.8m','慢回弹 深度睡眠',5,NULL,NULL,'/file/demo/g10_bed.jpg','',19900,19900,29900,100,70,90,'<p>人体工学曲线，透气枕套可拆洗。</p>',NULL,0,1,0,1,9,1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods` (`id`, `name`, `subtitle`, `category_id`, `brand_id`, `unit_id`, `image`, `video`, `min_price`, `max_price`, `market_price`, `stock`, `sales`, `virtual_sales`, `content`, `delivery_template_id`, `spec_type`, `status`, `recommend`, `is_show`, `sort`, `refund_type`, `create_time`, `update_time`, `delete_flag`) VALUES (12,'纯棉圆领短袖T恤','多色多码 透气亲肤',2,NULL,NULL,'/file/demo/g2_tshirt.jpg','',8900,10900,0,600,69,0,'<p>规格演示</p>',NULL,1,1,0,1,0,1,'2026-10-28 16:51:09','2026-11-02 14:28:07',0);
/*!40000 ALTER TABLE `goods` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_brand` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `logo` varchar(255) NOT NULL DEFAULT '',
  `sort` int NOT NULL DEFAULT '0',
  `is_show` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品品牌';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_brand` WRITE;
/*!40000 ALTER TABLE `goods_brand` DISABLE KEYS */;
INSERT INTO `goods_brand` (`id`, `name`, `logo`, `sort`, `is_show`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'Minimall自有','',1,1,'2026-08-28 10:54:37','2026-08-28 10:58:12',0);
INSERT INTO `goods_brand` (`id`, `name`, `logo`, `sort`, `is_show`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'极客数码','',2,1,'2026-08-28 10:54:37','2026-08-28 10:58:12',0);
/*!40000 ALTER TABLE `goods_brand` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_category` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `pid` bigint NOT NULL DEFAULT '0' COMMENT '父级id',
  `level` tinyint NOT NULL DEFAULT '1',
  `icon` varchar(255) NOT NULL DEFAULT '',
  `image` varchar(255) NOT NULL DEFAULT '',
  `sort` int NOT NULL DEFAULT '0',
  `is_show` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_category` WRITE;
/*!40000 ALTER TABLE `goods_category` DISABLE KEYS */;
INSERT INTO `goods_category` (`id`, `name`, `pid`, `level`, `icon`, `image`, `sort`, `is_show`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'数码',0,1,'/file/demo/c1_digital.jpg','',1,1,'2026-08-26 17:11:19','2026-10-28 15:19:31',0);
INSERT INTO `goods_category` (`id`, `name`, `pid`, `level`, `icon`, `image`, `sort`, `is_show`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'服饰',0,1,'/file/demo/c2_apparel.jpg','',2,1,'2026-08-27 11:41:58','2026-10-28 15:19:31',0);
INSERT INTO `goods_category` (`id`, `name`, `pid`, `level`, `icon`, `image`, `sort`, `is_show`, `create_time`, `update_time`, `delete_flag`) VALUES (3,'食品',0,1,'/file/demo/c3_food.jpg','',3,1,'2026-08-27 11:41:58','2026-10-28 15:19:31',0);
INSERT INTO `goods_category` (`id`, `name`, `pid`, `level`, `icon`, `image`, `sort`, `is_show`, `create_time`, `update_time`, `delete_flag`) VALUES (4,'美妆',0,1,'/file/demo/c4_beauty.jpg','',4,1,'2026-08-27 11:41:58','2026-10-28 15:19:31',0);
INSERT INTO `goods_category` (`id`, `name`, `pid`, `level`, `icon`, `image`, `sort`, `is_show`, `create_time`, `update_time`, `delete_flag`) VALUES (5,'家居',0,1,'/file/demo/c5_home.jpg','',5,1,'2026-08-27 11:41:58','2026-10-28 15:19:31',0);
/*!40000 ALTER TABLE `goods_category` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_comment` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `order_goods_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_item_id` bigint NOT NULL DEFAULT '0',
  `score` tinyint NOT NULL DEFAULT '5' COMMENT '评分1-5',
  `description_score` tinyint NOT NULL DEFAULT '5',
  `service_score` tinyint NOT NULL DEFAULT '5',
  `content` varchar(1000) NOT NULL DEFAULT '',
  `reply` varchar(1000) NOT NULL DEFAULT '' COMMENT '商家回复',
  `is_show` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_comment` WRITE;
/*!40000 ALTER TABLE `goods_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `goods_comment` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_comment_image` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价图片';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_comment_image` WRITE;
/*!40000 ALTER TABLE `goods_comment_image` DISABLE KEYS */;
/*!40000 ALTER TABLE `goods_comment_image` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_delivery_template` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `charge_type` tinyint NOT NULL DEFAULT '1' COMMENT '计费:1按件 2按重量 3按体积',
  `is_free_shipping` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运费模板';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_delivery_template` WRITE;
/*!40000 ALTER TABLE `goods_delivery_template` DISABLE KEYS */;
INSERT INTO `goods_delivery_template` (`id`, `name`, `charge_type`, `is_free_shipping`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'标准按件',1,0,'2026-08-28 10:47:18','2026-08-28 10:47:18',0);
/*!40000 ALTER TABLE `goods_delivery_template` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_image` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `url` varchar(255) NOT NULL,
  `sort` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品轮播图/视频';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_image` WRITE;
/*!40000 ALTER TABLE `goods_image` DISABLE KEYS */;
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (2,2,'/file/demo/g2_tshirt.jpg',0);
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (4,4,'/file/demo/g4_shirt.jpg',0);
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (5,5,'/file/demo/g5_honey.jpg',0);
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (6,6,'/file/demo/g6_coffee.jpg',0);
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (7,7,'/file/demo/g7_lotion.jpg',0);
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (8,8,'/file/demo/g8_face.jpg',0);
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (9,9,'/file/demo/g9_lamp.jpg',0);
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (10,10,'/file/demo/g10_bed.jpg',0);
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (11,1,'/file/demo/g1_airpods.jpg',0);
INSERT INTO `goods_image` (`id`, `goods_id`, `url`, `sort`) VALUES (12,3,'/file/demo/g3_shoes.jpg',0);
/*!40000 ALTER TABLE `goods_image` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `item_sn` varchar(64) NOT NULL DEFAULT '' COMMENT 'SKU编码',
  `image` varchar(255) NOT NULL DEFAULT '',
  `price` bigint NOT NULL DEFAULT '0' COMMENT '售价(分)',
  `cost_price` bigint NOT NULL DEFAULT '0' COMMENT '成本价(分)',
  `market_price` bigint NOT NULL DEFAULT '0',
  `stock` int NOT NULL DEFAULT '0',
  `sales` int NOT NULL DEFAULT '0',
  `weight` decimal(10,2) NOT NULL DEFAULT '0.00',
  `volume` decimal(10,2) NOT NULL DEFAULT '0.00',
  `spec_value_ids` varchar(255) NOT NULL DEFAULT '' COMMENT '规格值id组合,如 1,2,3',
  `spec_value_str` varchar(255) NOT NULL DEFAULT '' COMMENT '规格文字,如 红色/M',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_item` WRITE;
/*!40000 ALTER TABLE `goods_item` DISABLE KEYS */;
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (3,2,'SKU-0201','',9900,5000,12900,100,7,0.00,0.00,'','标准码',1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (5,4,'SKU-0401','',29900,15000,39900,100,52,0.00,0.00,'','S-XL',1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (6,5,'SKU-0501','',3990,1500,5900,100,54,0.00,0.00,'','30袋装',1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (7,6,'SKU-0601','',6900,2500,9900,100,47,0.00,0.00,'','227g',1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (8,7,'SKU-0701','',12900,6000,18900,100,10,0.00,0.00,'','30ml',1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (9,8,'SKU-0801','',3900,1200,5900,100,25,0.00,0.00,'','100g',1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (10,9,'SKU-0901','',8900,4000,15900,100,31,0.00,0.00,'','基础款',1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (11,10,'SKU-1001','',19900,8000,29900,100,13,0.00,0.00,'','标准款',1,'2026-08-27 11:41:58','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (12,1,'1_1787885237986','',29900,0,0,100,30,0.00,0.00,'1','黑色',1,'2026-08-28 10:47:18','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (13,1,'1_1787885237990','',32900,0,0,100,46,0.00,0.00,'2','白色',1,'2026-08-28 10:47:18','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (22,12,'12_1793177558212','',8900,0,12900,100,17,0.00,0.00,'','黑色  /  M',1,'2026-10-28 16:52:38','2026-11-02 14:28:07',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (23,12,'12_1793177558215','',8900,0,12900,100,63,0.00,0.00,'','黑色  /  L',1,'2026-10-28 16:52:38','2026-11-02 14:28:07',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (24,12,'12_1793177558218','',8900,0,12900,100,17,0.00,0.00,'','黑色  /  XL',1,'2026-10-28 16:52:38','2026-11-02 14:28:07',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (25,12,'12_1793177558219','',10900,0,13900,100,11,0.00,0.00,'','白色  /  M',1,'2026-10-28 16:52:38','2026-11-02 14:28:07',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (26,12,'12_1793177558220','',10900,0,13900,100,59,0.00,0.00,'','白色  /  L',1,'2026-10-28 16:52:38','2026-11-02 14:28:07',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (27,12,'12_1793177558221','',10900,0,13900,100,21,0.00,0.00,'','白色  /  XL',1,'2026-10-28 16:52:38','2026-11-02 14:28:07',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (28,3,'3_1793179060440','',19900,0,0,100,40,0.00,0.00,'','蓝色 / 40',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (29,3,'3_1793179060442','',19900,0,0,100,15,0.00,0.00,'','蓝色 / 41',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (30,3,'3_1793179060442','',19900,0,0,100,10,0.00,0.00,'','蓝色 / 42',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (31,3,'3_1793179060443','',19900,0,0,100,59,0.00,0.00,'','蓝色 / 43',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (32,3,'3_1793179060444','',19900,0,0,100,21,0.00,0.00,'','蓝色 / 44',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (33,3,'3_1793179060445','',19900,0,0,100,42,0.00,0.00,'','绿色 / 40',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (34,3,'3_1793179060446','',19900,0,0,100,22,0.00,0.00,'','绿色 / 41',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (35,3,'3_1793179060447','',19900,0,0,100,41,0.00,0.00,'','绿色 / 42',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (36,3,'3_1793179060448','',19900,0,0,100,14,0.00,0.00,'','绿色 / 43',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (37,3,'3_1793179060448','',19900,0,0,100,64,0.00,0.00,'','绿色 / 44',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (38,3,'3_1793179060449','',19900,0,0,100,32,0.00,0.00,'','灰色 / 40',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (39,3,'3_1793179060450','',19900,0,0,100,22,0.00,0.00,'','灰色 / 41',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (40,3,'3_1793179060450','',19900,0,0,100,11,0.00,0.00,'','灰色 / 42',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (41,3,'3_1793179060451','',19900,0,0,100,46,0.00,0.00,'','灰色 / 43',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
INSERT INTO `goods_item` (`id`, `goods_id`, `item_sn`, `image`, `price`, `cost_price`, `market_price`, `stock`, `sales`, `weight`, `volume`, `spec_value_ids`, `spec_value_str`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (42,3,'3_1793179060452','',19900,0,0,100,13,0.00,0.00,'','灰色 / 44',1,'2026-10-28 17:17:40','2026-11-02 14:27:59',0);
/*!40000 ALTER TABLE `goods_item` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_spec` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `name` varchar(64) NOT NULL COMMENT '规格名,如颜色',
  `sort` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_spec` WRITE;
/*!40000 ALTER TABLE `goods_spec` DISABLE KEYS */;
INSERT INTO `goods_spec` (`id`, `goods_id`, `name`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (1,12,'颜色',0,'2026-10-28 16:51:09','2026-10-28 16:52:38',1);
INSERT INTO `goods_spec` (`id`, `goods_id`, `name`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (2,12,'尺码',1,'2026-10-28 16:51:09','2026-10-28 16:52:38',1);
INSERT INTO `goods_spec` (`id`, `goods_id`, `name`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (3,12,'颜色',0,'2026-10-28 16:52:38','2026-10-28 16:52:38',0);
INSERT INTO `goods_spec` (`id`, `goods_id`, `name`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (4,12,'尺码',1,'2026-10-28 16:52:38','2026-10-28 16:52:38',0);
INSERT INTO `goods_spec` (`id`, `goods_id`, `name`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (5,3,'颜色',0,'2026-10-28 17:17:40','2026-10-28 17:17:40',0);
INSERT INTO `goods_spec` (`id`, `goods_id`, `name`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (6,3,'尺码',1,'2026-10-28 17:17:40','2026-10-28 17:17:40',0);
/*!40000 ALTER TABLE `goods_spec` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_spec_value` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `spec_id` bigint NOT NULL,
  `value` varchar(64) NOT NULL COMMENT '规格值,如红色',
  `image` varchar(255) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_spec_id` (`spec_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格值';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_spec_value` WRITE;
/*!40000 ALTER TABLE `goods_spec_value` DISABLE KEYS */;
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (1,12,1,'黑色','','2026-10-28 16:51:09','2026-10-28 16:52:38',1);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (2,12,1,'白色','','2026-10-28 16:51:09','2026-10-28 16:52:38',1);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (3,12,2,'M','','2026-10-28 16:51:09','2026-10-28 16:52:38',1);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (4,12,2,'L','','2026-10-28 16:51:09','2026-10-28 16:52:38',1);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (5,12,3,'黑色','','2026-10-28 16:52:38','2026-10-28 16:52:38',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (6,12,3,'白色','','2026-10-28 16:52:38','2026-10-28 16:52:38',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (7,12,4,'M','','2026-10-28 16:52:38','2026-10-28 16:52:38',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (8,12,4,'L','','2026-10-28 16:52:38','2026-10-28 16:52:38',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (9,12,4,'XL','','2026-10-28 16:52:38','2026-10-28 16:52:38',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (10,3,5,'蓝色','','2026-10-28 17:17:40','2026-10-28 17:17:40',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (11,3,5,'绿色','','2026-10-28 17:17:40','2026-10-28 17:17:40',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (12,3,5,'灰色','','2026-10-28 17:17:40','2026-10-28 17:17:40',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (13,3,6,'40','','2026-10-28 17:17:40','2026-10-28 17:17:40',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (14,3,6,'41','','2026-10-28 17:17:40','2026-10-28 17:17:40',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (15,3,6,'42','','2026-10-28 17:17:40','2026-10-28 17:17:40',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (16,3,6,'43','','2026-10-28 17:17:40','2026-10-28 17:17:40',0);
INSERT INTO `goods_spec_value` (`id`, `goods_id`, `spec_id`, `value`, `image`, `create_time`, `update_time`, `delete_flag`) VALUES (17,3,6,'44','','2026-10-28 17:17:40','2026-10-28 17:17:40',0);
/*!40000 ALTER TABLE `goods_spec_value` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_unit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å•†å“å•ä½';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_unit` WRITE;
/*!40000 ALTER TABLE `goods_unit` DISABLE KEYS */;
INSERT INTO `goods_unit` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'件',1,1,'2026-08-28 10:54:37','2026-08-28 10:58:12',0);
INSERT INTO `goods_unit` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'个',2,1,'2026-08-28 10:54:37','2026-08-28 10:58:12',0);
INSERT INTO `goods_unit` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (3,'盒',3,1,'2026-08-28 10:54:37','2026-08-28 10:58:12',0);
INSERT INTO `goods_unit` (`id`, `name`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (4,'瓶',4,1,'2026-08-28 10:54:37','2026-08-28 10:58:12',0);
/*!40000 ALTER TABLE `goods_unit` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `integral_goods` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `image` varchar(255) NOT NULL DEFAULT '',
  `integral` bigint NOT NULL DEFAULT '0',
  `stock` int NOT NULL DEFAULT '0',
  `sales` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `description` text,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç§¯åˆ†å•†å“';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `integral_goods` WRITE;
/*!40000 ALTER TABLE `integral_goods` DISABLE KEYS */;
INSERT INTO `integral_goods` (`id`, `name`, `image`, `integral`, `stock`, `sales`, `status`, `description`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'不锈钢便携水杯','/file/demo/i1_cup.jpg',500,50,0,1,'环保材质 限量兑换','2026-08-26 23:57:54','2026-10-28 15:19:31',0);
INSERT INTO `integral_goods` (`id`, `name`, `image`, `integral`, `stock`, `sales`, `status`, `description`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'便携榨汁杯','/file/demo/i2_blender.jpg',800,29,1,1,'晴雨两用','2026-08-26 23:57:54','2026-10-28 15:19:31',0);
/*!40000 ALTER TABLE `integral_goods` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `integral_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(32) NOT NULL,
  `user_id` bigint NOT NULL,
  `integral_goods_id` bigint NOT NULL,
  `goods_name` varchar(128) NOT NULL,
  `goods_image` varchar(255) NOT NULL DEFAULT '',
  `integral` bigint NOT NULL DEFAULT '0',
  `num` int NOT NULL DEFAULT '1',
  `total_integral` bigint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0',
  `contact` varchar(32) NOT NULL DEFAULT '',
  `mobile` varchar(20) NOT NULL DEFAULT '',
  `address` varchar(255) NOT NULL DEFAULT '',
  `express_name` varchar(32) NOT NULL DEFAULT '',
  `express_no` varchar(64) NOT NULL DEFAULT '',
  `express_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç§¯åˆ†å…‘æ¢è®¢å•';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `integral_order` WRITE;
/*!40000 ALTER TABLE `integral_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `integral_order` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kefu_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `content` varchar(500) NOT NULL,
  `admin_reply` varchar(500) NOT NULL DEFAULT '',
  `reply_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åœ¨çº¿å®¢æœç•™è¨€';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `kefu_message` WRITE;
/*!40000 ALTER TABLE `kefu_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `kefu_message` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lucky_draw` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `integral_cost` bigint NOT NULL DEFAULT '100',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='幸运抽奖场次';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `lucky_draw` WRITE;
/*!40000 ALTER TABLE `lucky_draw` DISABLE KEYS */;
INSERT INTO `lucky_draw` (`id`, `name`, `integral_cost`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'幸运大转盘',100,1,'2026-08-28 11:14:13','2026-08-28 11:14:13',0);
/*!40000 ALTER TABLE `lucky_draw` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lucky_draw_prize` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `draw_id` bigint NOT NULL,
  `prize_name` varchar(64) NOT NULL,
  `prize_type` tinyint NOT NULL DEFAULT '0' COMMENT '1积分 2余额 3优惠券 4未中',
  `prize_value` bigint NOT NULL DEFAULT '0',
  `weight` int NOT NULL DEFAULT '1',
  `total` int NOT NULL DEFAULT '0' COMMENT '0=不限',
  `used` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_draw` (`draw_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抽奖项';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `lucky_draw_prize` WRITE;
/*!40000 ALTER TABLE `lucky_draw_prize` DISABLE KEYS */;
INSERT INTO `lucky_draw_prize` (`id`, `draw_id`, `prize_name`, `prize_type`, `prize_value`, `weight`, `total`, `used`, `status`) VALUES (1,1,'谢谢参与',4,0,50,0,2,1);
INSERT INTO `lucky_draw_prize` (`id`, `draw_id`, `prize_name`, `prize_type`, `prize_value`, `weight`, `total`, `used`, `status`) VALUES (2,1,'+100积分',1,100,25,0,1,1);
INSERT INTO `lucky_draw_prize` (`id`, `draw_id`, `prize_name`, `prize_type`, `prize_value`, `weight`, `total`, `used`, `status`) VALUES (3,1,'+500积分',1,500,12,0,1,1);
INSERT INTO `lucky_draw_prize` (`id`, `draw_id`, `prize_name`, `prize_type`, `prize_value`, `weight`, `total`, `used`, `status`) VALUES (4,1,'+50元余额',2,5000,8,0,1,1);
INSERT INTO `lucky_draw_prize` (`id`, `draw_id`, `prize_name`, `prize_type`, `prize_value`, `weight`, `total`, `used`, `status`) VALUES (5,1,'+20元余额',2,2000,5,0,0,1);
/*!40000 ALTER TABLE `lucky_draw_prize` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lucky_draw_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `draw_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `prize_name` varchar(64) NOT NULL,
  `prize_type` tinyint NOT NULL DEFAULT '0',
  `prize_value` bigint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抽奖记录';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `lucky_draw_record` WRITE;
/*!40000 ALTER TABLE `lucky_draw_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `lucky_draw_record` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `pid` bigint NOT NULL DEFAULT '0',
  `name` varchar(64) NOT NULL,
  `path` varchar(128) NOT NULL DEFAULT '',
  `permission` varchar(128) NOT NULL DEFAULT '',
  `type` tinyint NOT NULL DEFAULT '1',
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单/权限';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `menu` WRITE;
/*!40000 ALTER TABLE `menu` DISABLE KEYS */;
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (1,0,'商品','','goods',0,1,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (2,0,'订单','','order',0,2,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (3,0,'营销','','marketing',0,3,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (4,0,'用户','','user',0,4,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (5,0,'财务','','finance',0,5,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (6,0,'内容/设置','','system',0,6,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (11,1,'商品列表','/goods','goods:list',1,1,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (12,1,'评价管理','/comments','goods:comment',1,2,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (13,1,'品牌/单位','/base','base:manage',1,3,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (14,1,'运费模板','/freight','freight:manage',1,4,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (15,1,'商品编辑/上下架','','goods:edit',2,5,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (16,1,'删除商品','','goods:delete',2,6,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (21,2,'订单管理','/order','order:list',1,1,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (22,2,'发货','','order:deliver',2,2,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (23,2,'售后管理','/after','after:manage',1,3,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (24,2,'订单改价','','order:edit',2,4,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (31,3,'营销管理','/marketing','marketing:manage',1,1,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (32,3,'积分商城','/integral','integral:manage',1,2,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (33,3,'秒杀管理','/seckill','seckill:manage',1,3,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (34,3,'抽奖/注册送/免邮','/extras','lottery:manage',1,4,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (35,3,'分销管理','/marketing/distribution','distribution:manage',1,5,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (41,4,'用户管理','/users','user:manage',1,1,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (42,4,'会员等级','/level','user:level',1,2,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (51,5,'提现审核','/wallet','finance:withdraw',1,1,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (52,5,'财务明细','/finance','finance:log',1,2,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (61,6,'内容装修','/content','content:manage',1,1,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (62,6,'角色管理','/roles','system:role',1,2,1);
INSERT INTO `menu` (`id`, `pid`, `name`, `path`, `permission`, `type`, `sort`, `status`) VALUES (63,6,'设置中心','/settings','system:setting',1,3,1);
/*!40000 ALTER TABLE `menu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_goods` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_item_id` bigint NOT NULL,
  `goods_name` varchar(255) NOT NULL COMMENT '商品名快照',
  `goods_image` varchar(255) NOT NULL DEFAULT '',
  `spec_value_str` varchar(128) NOT NULL DEFAULT '',
  `price` bigint NOT NULL COMMENT '成交单价快照(分)',
  `num` int NOT NULL DEFAULT '1',
  `total_price` bigint NOT NULL DEFAULT '0' COMMENT '小计',
  `discount_price` bigint NOT NULL DEFAULT '0',
  `is_comment` tinyint NOT NULL DEFAULT '0',
  `is_after_sale` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=129 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单商品';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `order_goods` WRITE;
/*!40000 ALTER TABLE `order_goods` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_goods` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '操作类型',
  `operator` varchar(32) NOT NULL DEFAULT '' COMMENT '操作人',
  `content` varchar(255) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单日志';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `order_log` WRITE;
/*!40000 ALTER TABLE `order_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_log` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_trace` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `trace_time` datetime NOT NULL COMMENT '发生时间',
  `description` varchar(255) NOT NULL COMMENT '轨迹文案',
  `operator` varchar(64) NOT NULL DEFAULT 'system' COMMENT '操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`,`trace_time`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单物流轨迹';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `order_trace` WRITE;
/*!40000 ALTER TABLE `order_trace` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_trace` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL,
  `order_type` tinyint NOT NULL DEFAULT '0' COMMENT '0普通 1拼团 2秒杀 3预售 4积分',
  `order_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待支付 1待发货 2待收货 3已完成 4已取消',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '0未支付 1已支付',
  `pay_type` tinyint NOT NULL DEFAULT '0' COMMENT '1微信 2支付宝 3余额',
  `pay_time` datetime DEFAULT NULL,
  `pay_sn` varchar(64) NOT NULL DEFAULT '' COMMENT '支付流水号',
  `total_amount` bigint NOT NULL DEFAULT '0' COMMENT '商品总额(分)',
  `member_discount` bigint NOT NULL DEFAULT '0' COMMENT 'ä¼šå‘˜æŠ˜æ‰£(åˆ†)',
  `discount_amount` bigint NOT NULL DEFAULT '0' COMMENT '优惠金额(分)',
  `freight_amount` bigint NOT NULL DEFAULT '0' COMMENT '运费(分)',
  `pay_amount` bigint NOT NULL DEFAULT '0' COMMENT '实付(分)',
  `coupon_id` bigint DEFAULT NULL,
  `coupon_amount` bigint NOT NULL DEFAULT '0',
  `integral_amount` bigint NOT NULL DEFAULT '0',
  `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '买家留言',
  `contact` varchar(32) NOT NULL DEFAULT '',
  `mobile` varchar(20) NOT NULL DEFAULT '',
  `address` varchar(255) NOT NULL DEFAULT '',
  `delivery_type` tinyint NOT NULL DEFAULT '1' COMMENT '1快递 2自提',
  `express_id` bigint DEFAULT NULL,
  `express_name` varchar(32) NOT NULL DEFAULT '',
  `express_no` varchar(64) NOT NULL DEFAULT '',
  `delivery_time` datetime DEFAULT NULL,
  `receive_time` datetime DEFAULT NULL COMMENT '确认收货时间',
  `cancel_time` datetime DEFAULT NULL,
  `cancel_reason` varchar(255) NOT NULL DEFAULT '',
  `close_time` datetime DEFAULT NULL COMMENT '自动关单时间',
  `commission` bigint NOT NULL DEFAULT '0' COMMENT '已分佣金额',
  `is_comment` tinyint NOT NULL DEFAULT '0',
  `is_after_sale` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`order_status`)
) ENGINE=InnoDB AUTO_INCREMENT=9110 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presell_activity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_item_id` bigint NOT NULL,
  `deposit` bigint NOT NULL COMMENT 'å®šé‡‘(åˆ†)',
  `final_price` bigint NOT NULL COMMENT 'å°¾æ¬¾(åˆ†)',
  `stock` int NOT NULL DEFAULT '0',
  `sales` int NOT NULL DEFAULT '0',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `final_end_time` datetime NOT NULL COMMENT 'å°¾æ¬¾æ”¯ä»˜æˆªæ­¢',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='é¢„å”®æ´»åŠ¨';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `presell_activity` WRITE;
/*!40000 ALTER TABLE `presell_activity` DISABLE KEYS */;
INSERT INTO `presell_activity` (`id`, `name`, `goods_id`, `goods_item_id`, `deposit`, `final_price`, `stock`, `sales`, `start_time`, `end_time`, `final_end_time`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'新品预售',1,12,1000,25900,29,1,'2026-08-27 00:00:00','2026-09-03 00:00:00','2026-09-10 00:00:00',1,'2026-08-27 00:30:03','2026-08-28 12:17:43',0);
/*!40000 ALTER TABLE `presell_activity` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presell_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(32) NOT NULL,
  `user_id` bigint NOT NULL,
  `activity_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_item_id` bigint NOT NULL,
  `goods_name` varchar(128) NOT NULL,
  `goods_image` varchar(255) NOT NULL DEFAULT '',
  `deposit` bigint NOT NULL COMMENT 'å·²ä»˜å®šé‡‘',
  `final_price` bigint NOT NULL COMMENT 'åº”ä»˜å°¾æ¬¾',
  `pay_sn` varchar(64) NOT NULL DEFAULT '',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0å¾…ä»˜å®šé‡‘ 1å¾…ä»˜å°¾æ¬¾ 2å·²å®Œæˆ 3å·²å…³é—­',
  `contact` varchar(32) NOT NULL DEFAULT '',
  `mobile` varchar(20) NOT NULL DEFAULT '',
  `address` varchar(255) NOT NULL DEFAULT '',
  `express_name` varchar(32) NOT NULL DEFAULT '',
  `express_no` varchar(64) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='é¢„å”®è®¢å•';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `presell_order` WRITE;
/*!40000 ALTER TABLE `presell_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `presell_order` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recharge_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_sn` varchar(32) NOT NULL,
  `user_id` bigint NOT NULL,
  `template_id` bigint DEFAULT NULL,
  `amount` bigint NOT NULL COMMENT 'åˆ°è´¦é‡‘é¢(å«èµ é€)',
  `pay_amount` bigint NOT NULL COMMENT 'å®žä»˜é‡‘é¢',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0å¾…æ”¯ä»˜ 1å·²æ”¯ä»˜',
  `pay_sn` varchar(64) NOT NULL DEFAULT '',
  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å……å€¼è®¢å•';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `recharge_order` WRITE;
/*!40000 ALTER TABLE `recharge_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `recharge_order` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recharge_template` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `amount` bigint NOT NULL COMMENT 'å……å€¼é‡‘é¢(åˆ†)',
  `gift_amount` bigint NOT NULL DEFAULT '0' COMMENT 'èµ é€é‡‘é¢(åˆ†)',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å……å€¼æ¡£ä½';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `recharge_template` WRITE;
/*!40000 ALTER TABLE `recharge_template` DISABLE KEYS */;
INSERT INTO `recharge_template` (`id`, `name`, `amount`, `gift_amount`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'充50送5',5000,500,1,'2026-08-27 00:40:45','2026-08-27 11:16:15',0);
INSERT INTO `recharge_template` (`id`, `name`, `amount`, `gift_amount`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'充100送15',10000,1500,1,'2026-08-27 00:40:45','2026-08-27 11:16:15',0);
INSERT INTO `recharge_template` (`id`, `name`, `amount`, `gift_amount`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (3,'充200送40',20000,4000,1,'2026-08-27 00:40:45','2026-08-27 11:16:15',0);
/*!40000 ALTER TABLE `recharge_template` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `region` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `pid` int NOT NULL DEFAULT '0',
  `name` varchar(64) NOT NULL,
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '1省 2市 3区',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='行政区域(基础)';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `region` WRITE;
/*!40000 ALTER TABLE `region` DISABLE KEYS */;
/*!40000 ALTER TABLE `region` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `register_award` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `award_type` tinyint NOT NULL DEFAULT '1' COMMENT '1积分 2余额 3优惠券',
  `award_value` bigint NOT NULL DEFAULT '0',
  `coupon_id` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='注册送配置';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `register_award` WRITE;
/*!40000 ALTER TABLE `register_award` DISABLE KEYS */;
INSERT INTO `register_award` (`id`, `award_type`, `award_value`, `coupon_id`, `status`, `create_time`) VALUES (1,1,500,NULL,1,'2026-08-28 11:14:13');
/*!40000 ALTER TABLE `register_award` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `code` varchar(64) NOT NULL,
  `remark` varchar(255) NOT NULL DEFAULT '',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台角色';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` (`id`, `name`, `code`, `remark`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'超级管理员','admin','拥有全部权限',1,'2026-08-28 12:36:30','2026-08-28 12:36:30',0);
INSERT INTO `role` (`id`, `name`, `code`, `remark`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'运营专员','operator','商品/订单/营销',1,'2026-08-28 12:36:30','2026-08-28 12:36:30',0);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_menu` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-菜单';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `role_menu` WRITE;
/*!40000 ALTER TABLE `role_menu` DISABLE KEYS */;
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (1,1,1);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (2,1,2);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (3,1,3);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (4,1,4);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (5,1,5);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (6,1,6);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (7,1,11);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (8,1,12);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (9,1,13);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (10,1,14);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (11,1,21);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (12,1,22);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (13,1,23);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (14,1,31);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (15,1,32);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (16,1,33);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (17,1,34);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (18,1,41);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (19,1,42);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (20,1,51);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (21,1,52);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (22,1,61);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (23,1,62);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (24,1,63);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (32,2,1);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (33,2,11);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (34,2,12);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (35,2,2);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (36,2,21);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (37,2,22);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (38,2,23);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (39,2,3);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (40,2,31);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (41,2,32);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (42,2,33);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (43,2,34);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (44,1,15);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (45,1,16);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (46,1,24);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (47,2,15);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (48,2,24);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (49,1,35);
INSERT INTO `role_menu` (`id`, `role_id`, `menu_id`) VALUES (50,2,35);
/*!40000 ALTER TABLE `role_menu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seckill_activity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `sort` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç§’æ€åœºæ¬¡';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `seckill_activity` WRITE;
/*!40000 ALTER TABLE `seckill_activity` DISABLE KEYS */;
INSERT INTO `seckill_activity` (`id`, `name`, `start_time`, `end_time`, `status`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'今日秒杀','2026-10-29 14:37:39','2026-11-04 15:37:39',1,1,'2026-08-27 00:10:12','2026-10-29 15:37:39',0);
/*!40000 ALTER TABLE `seckill_activity` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seckill_goods` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_item_id` bigint NOT NULL,
  `seckill_price` bigint NOT NULL DEFAULT '0',
  `seckill_stock` int NOT NULL DEFAULT '0',
  `seckill_sales` int NOT NULL DEFAULT '0',
  `per_limit` int NOT NULL DEFAULT '1',
  `sort` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_item` (`goods_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç§’æ€å•†å“';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `seckill_goods` WRITE;
/*!40000 ALTER TABLE `seckill_goods` DISABLE KEYS */;
INSERT INTO `seckill_goods` (`id`, `activity_id`, `goods_id`, `goods_item_id`, `seckill_price`, `seckill_stock`, `seckill_sales`, `per_limit`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (1,1,1,1,19900,19,1,1,1,'2026-08-27 00:10:12','2026-08-27 00:22:33',0);
INSERT INTO `seckill_goods` (`id`, `activity_id`, `goods_id`, `goods_item_id`, `seckill_price`, `seckill_stock`, `seckill_sales`, `per_limit`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (2,1,1,1,19900,20,0,1,1,'2026-08-27 00:30:02','2026-08-27 00:30:02',0);
INSERT INTO `seckill_goods` (`id`, `activity_id`, `goods_id`, `goods_item_id`, `seckill_price`, `seckill_stock`, `seckill_sales`, `per_limit`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (105,1,5,6,2594,50,0,2,5,'2026-10-29 15:37:39','2026-10-29 15:37:39',0);
INSERT INTO `seckill_goods` (`id`, `activity_id`, `goods_id`, `goods_item_id`, `seckill_price`, `seckill_stock`, `seckill_sales`, `per_limit`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (108,1,8,9,2535,50,0,2,8,'2026-10-29 15:37:39','2026-10-29 15:37:39',0);
INSERT INTO `seckill_goods` (`id`, `activity_id`, `goods_id`, `goods_item_id`, `seckill_price`, `seckill_stock`, `seckill_sales`, `per_limit`, `sort`, `create_time`, `update_time`, `delete_flag`) VALUES (109,1,9,10,5785,50,0,2,9,'2026-10-29 15:37:39','2026-11-02 08:55:11',0);
/*!40000 ALTER TABLE `seckill_goods` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sign_daily` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `integral` int NOT NULL DEFAULT '5',
  `continuous_days` int NOT NULL DEFAULT '7',
  `continuous_reward` int NOT NULL DEFAULT '10',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç­¾åˆ°é…ç½®';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sign_daily` WRITE;
/*!40000 ALTER TABLE `sign_daily` DISABLE KEYS */;
INSERT INTO `sign_daily` (`id`, `integral`, `continuous_days`, `continuous_reward`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,5,7,10,1,'2026-08-26 23:57:54','2026-08-26 23:57:54',0);
/*!40000 ALTER TABLE `sign_daily` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sign_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `sign_date` date NOT NULL,
  `integral` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`sign_date`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç­¾åˆ°è®°å½•';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sign_log` WRITE;
/*!40000 ALTER TABLE `sign_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sign_log` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_activity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_item_id` bigint NOT NULL,
  `team_price` bigint NOT NULL COMMENT 'æ‹¼å›¢ä»·(åˆ†)',
  `team_num` int NOT NULL DEFAULT '2' COMMENT 'æˆå›¢äººæ•°',
  `per_limit` int NOT NULL DEFAULT '1' COMMENT 'æ¯äººé™è´­',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ‹¼å›¢æ´»åŠ¨';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `team_activity` WRITE;
/*!40000 ALTER TABLE `team_activity` DISABLE KEYS */;
INSERT INTO `team_activity` (`id`, `name`, `goods_id`, `goods_item_id`, `team_price`, `team_num`, `per_limit`, `start_time`, `end_time`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'双人拼团耳机（改）',1,12,25900,2,1,'2026-08-27 00:00:00','2026-09-03 00:00:00',1,'2026-08-27 00:30:03','2026-11-02 11:55:07',0);
/*!40000 ALTER TABLE `team_activity` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_found` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT 'å›¢é•¿',
  `order_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0æ‹¼å›¢ä¸­ 1æˆåŠŸ 2å¤±è´¥',
  `success_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ‹¼å›¢-å›¢';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `team_found` WRITE;
/*!40000 ALTER TABLE `team_found` DISABLE KEYS */;
/*!40000 ALTER TABLE `team_found` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_join` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `found_id` bigint NOT NULL,
  `activity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_id`),
  KEY `idx_found` (`found_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ‹¼å›¢-å‚å›¢è®°å½•';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `team_join` WRITE;
/*!40000 ALTER TABLE `team_join` DISABLE KEYS */;
/*!40000 ALTER TABLE `team_join` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sn` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户编号',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '头像',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '密码(BCrypt)',
  `sex` tinyint NOT NULL DEFAULT '0' COMMENT '性别 0未知1男2女',
  `birthday` datetime DEFAULT NULL COMMENT '生日',
  `level_id` bigint DEFAULT NULL COMMENT '会员等级id',
  `balance` bigint NOT NULL DEFAULT '0' COMMENT '余额(分)',
  `integral` bigint NOT NULL DEFAULT '0' COMMENT '积分',
  `total_consume` bigint NOT NULL DEFAULT '0' COMMENT '累计消费(分)',
  `total_order_num` int NOT NULL DEFAULT '0' COMMENT '累计订单数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0禁用',
  `channel` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'h5' COMMENT '注册渠道 h5/miniapp/wechat',
  `referrer_id` bigint DEFAULT NULL COMMENT 'æŽ¨èäººid',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_mobile` (`mobile`),
  KEY `idx_sn` (`sn`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`id`, `sn`, `nickname`, `avatar`, `mobile`, `password`, `sex`, `birthday`, `level_id`, `balance`, `integral`, `total_consume`, `total_order_num`, `status`, `channel`, `referrer_id`, `last_login_time`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'DEMO-U-0001','演示会员','','13800138000','$2a$10$aYiXjWXVH7wAEflbupeu5OQ65A0mjvJrQVkCpERmW0qrz.1KQR0xK',0,NULL,1,0,500,0,0,1,'h5',NULL,NULL,'2026-11-02 14:27:59','2026-11-02 14:27:59',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `contact` varchar(32) NOT NULL COMMENT '联系人',
  `mobile` varchar(20) NOT NULL,
  `province_id` int NOT NULL DEFAULT '0',
  `city_id` int NOT NULL DEFAULT '0',
  `district_id` int NOT NULL DEFAULT '0',
  `address` varchar(255) NOT NULL COMMENT '详细地址',
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `is_default` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_address` WRITE;
/*!40000 ALTER TABLE `user_address` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_address` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_auth` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` varchar(16) NOT NULL COMMENT 'wechat/phone/miniapp',
  `openid` varchar(64) NOT NULL DEFAULT '',
  `unionid` varchar(64) NOT NULL DEFAULT '',
  `nickname` varchar(64) NOT NULL DEFAULT '',
  `avatar` varchar(255) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_openid` (`type`,`openid`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方/多端登录授权';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_auth` WRITE;
/*!40000 ALTER TABLE `user_auth` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_auth` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_collect` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods` (`user_id`,`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å•†å“æ”¶è—';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_collect` WRITE;
/*!40000 ALTER TABLE `user_collect` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_collect` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_footprint` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods` (`user_id`,`goods_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æµè§ˆè¶³è¿¹';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_footprint` WRITE;
/*!40000 ALTER TABLE `user_footprint` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_footprint` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_level` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `icon` varchar(255) NOT NULL DEFAULT '',
  `level` int NOT NULL DEFAULT '0' COMMENT '等级值',
  `discount` decimal(4,2) NOT NULL DEFAULT '10.00' COMMENT '折扣，如9.5折=9.50',
  `upgrade_consume` bigint NOT NULL DEFAULT '0' COMMENT '升级所需累计消费(分)',
  `privilege` varchar(255) NOT NULL DEFAULT '' COMMENT '权益描述',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员等级';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_level` WRITE;
/*!40000 ALTER TABLE `user_level` DISABLE KEYS */;
INSERT INTO `user_level` (`id`, `name`, `icon`, `level`, `discount`, `upgrade_consume`, `privilege`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'普通会员','',1,10.00,0,'基础权益',1,'2026-08-26 15:34:23','2026-10-29 09:38:49',0);
INSERT INTO `user_level` (`id`, `name`, `icon`, `level`, `discount`, `upgrade_consume`, `privilege`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'银卡会员','',2,9.80,100000,'98折 · 生日礼 · 专属客服',1,'2026-08-26 15:34:23','2026-10-29 09:38:49',0);
INSERT INTO `user_level` (`id`, `name`, `icon`, `level`, `discount`, `upgrade_consume`, `privilege`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (3,'金卡会员','',3,9.50,500000,'95折 · 双倍积分 · 优先发货',1,'2026-08-26 15:34:23','2026-10-29 09:38:49',0);
INSERT INTO `user_level` (`id`, `name`, `icon`, `level`, `discount`, `upgrade_consume`, `privilege`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (4,'黑卡会员','',4,9.00,2000000,'9折 · 免运费 · 1对1顾问',1,'2026-10-29 09:38:49','2026-10-29 09:38:49',0);
/*!40000 ALTER TABLE `user_level` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_tag` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `color` varchar(16) NOT NULL DEFAULT '#409eff',
  `sort` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `delete_flag` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员标签';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_tag` WRITE;
/*!40000 ALTER TABLE `user_tag` DISABLE KEYS */;
INSERT INTO `user_tag` (`id`, `name`, `color`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (1,'高价值','#f56c6c',1,1,'2026-10-29 09:38:49','2026-10-29 09:38:49',0);
INSERT INTO `user_tag` (`id`, `name`, `color`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (2,'复购','#e6a23c',2,1,'2026-10-29 09:38:49','2026-10-29 09:38:49',0);
INSERT INTO `user_tag` (`id`, `name`, `color`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (3,'新客','#67c23a',3,1,'2026-10-29 09:38:49','2026-10-29 09:38:49',0);
INSERT INTO `user_tag` (`id`, `name`, `color`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (4,'沉睡','#909399',4,1,'2026-10-29 09:38:49','2026-10-29 09:38:49',0);
INSERT INTO `user_tag` (`id`, `name`, `color`, `sort`, `status`, `create_time`, `update_time`, `delete_flag`) VALUES (5,'待跟进','#722ed1',5,1,'2026-10-29 09:42:38','2026-10-29 09:42:38',0);
/*!40000 ALTER TABLE `user_tag` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_tag_rel` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `tag_id` bigint unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`,`tag_id`),
  KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员-标签关联';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `user_tag_rel` WRITE;
/*!40000 ALTER TABLE `user_tag_rel` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_tag_rel` ENABLE KEYS */;
UNLOCK TABLES;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `withdraw_apply` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `amount` bigint NOT NULL COMMENT 'æçŽ°é‡‘é¢(åˆ†)',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1å¾®ä¿¡ 2é“¶è¡Œå¡ 3ä½™é¢',
  `account_info` varchar(255) NOT NULL DEFAULT '' COMMENT 'æ”¶æ¬¾è´¦æˆ·',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0å¾…å®¡æ ¸ 1å·²é€šè¿‡ 2å·²æ‹’ç» 3å·²æ‰“æ¬¾',
  `remark` varchar(255) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æçŽ°ç”³è¯·';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `withdraw_apply` WRITE;
/*!40000 ALTER TABLE `withdraw_apply` DISABLE KEYS */;
/*!40000 ALTER TABLE `withdraw_apply` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 演示活动时间平移：保证「任何时候部署，活动都是进行中」
-- dump 里的时间是导出那一刻的字面量，别人 clone 部署时早就过期了
-- ============================================================
UPDATE seckill_activity SET start_time = DATE_SUB(NOW(), INTERVAL 1 DAY), end_time = DATE_ADD(NOW(), INTERVAL 30 DAY) WHERE status = 1;
UPDATE bargain_activity SET start_time = DATE_SUB(NOW(), INTERVAL 1 DAY), end_time = DATE_ADD(NOW(), INTERVAL 30 DAY) WHERE status = 1;
UPDATE presell_activity  SET start_time = DATE_SUB(NOW(), INTERVAL 1 DAY), end_time = DATE_ADD(NOW(), INTERVAL 30 DAY),
                            final_end_time = DATE_ADD(NOW(), INTERVAL 37 DAY) WHERE status = 1;
UPDATE team_activity     SET start_time = DATE_SUB(NOW(), INTERVAL 1 DAY), end_time = DATE_ADD(NOW(), INTERVAL 30 DAY) WHERE status = 1;
UPDATE full_reduction    SET start_time = DATE_SUB(NOW(), INTERVAL 1 DAY), end_time = DATE_ADD(NOW(), INTERVAL 30 DAY) WHERE status = 1;
UPDATE coupon            SET start_time = DATE_SUB(NOW(), INTERVAL 1 DAY), end_time = DATE_ADD(NOW(), INTERVAL 30 DAY) WHERE valid_type = 1;
