-- 分类
INSERT INTO goods_category (name,pid,level,sort,is_show) VALUES
('服饰',0,1,2,1),('食品',0,1,3,1),('美妆',0,1,4,1),('家居',0,1,5,1);

-- 商品 SPU (id 2..10)
INSERT INTO goods (id,name,subtitle,category_id,image,market_price,min_price,max_price,stock,sales,virtual_sales,content,spec_type,status,recommend,is_show,sort,refund_type) VALUES
(2,'简约纯棉T恤','100%棉 亲肤透气',2,'/file/demo/demo2.svg',12900,9900,9900,200,86,300,'<p>新疆长绒棉，经典圆领，多色可选。</p>',0,1,1,1,1,1),
(3,'复古直筒牛仔裤','宽松显瘦 经典水洗',2,'/file/demo/demo3.svg',25900,19900,19900,120,45,200,'<p>重磅丹宁，直筒剪裁，四季百搭。</p>',0,1,0,1,2,1),
(4,'轻薄羽绒服','90绒 轻若无物',2,'/file/demo/demo4.svg',39900,29900,29900,80,23,150,'<p>高蓬松度白鸭绒，可收纳便携。</p>',0,1,1,1,3,1),
(5,'每日坚果礼盒','30袋装 科学搭配',3,'/file/demo/demo5.svg',5900,3990,3990,300,210,500,'<p>6种坚果3种果干，烘焙非油炸。</p>',0,1,1,1,4,1),
(6,'精品咖啡豆','阿拉比卡 中度烘焙',3,'/file/demo/demo6.svg',9900,6900,6900,150,78,260,'<p>产地直采，现烘发货，香醇回甘。</p>',0,1,0,1,5,1),
(7,'保湿精华液','玻尿酸 深层补水',4,'/file/demo/demo7.svg',18900,12900,12900,160,96,400,'<p>多重玻尿酸，锁水保湿不粘腻。</p>',0,1,1,1,6,1),
(8,'男士控油洗面奶','氨基酸温和洁面',4,'/file/demo/demo8.svg',5900,3900,3900,200,130,350,'<p>绵密泡沫，清爽控油，敏感肌可用。</p>',0,1,0,1,7,1),
(9,'北欧风LED台灯','三档调光 护眼无频闪',5,'/file/demo/demo9.svg',15900,8900,8900,90,34,120,'<p>简约设计，触摸调光，USB充电。</p>',0,1,0,1,8,1),
(10,'记忆棉护颈枕','慢回弹 深度睡眠',5,'/file/demo/demo10.svg',29900,19900,19900,70,19,90,'<p>人体工学曲线，透气枕套可拆洗。</p>',0,1,0,1,9,1);

-- SKU（单规格）
INSERT INTO goods_item (goods_id,item_sn,image,price,cost_price,market_price,stock,sales,spec_value_ids,spec_value_str,status) VALUES
(2,'SKU-0201','',9900,5000,12900,200,86,'','标准码',1),
(3,'SKU-0301','',19900,9000,25900,120,45,'','28-34码',1),
(4,'SKU-0401','',29900,15000,39900,80,23,'','S-XL',1),
(5,'SKU-0501','',3990,1500,5900,300,210,'','30袋装',1),
(6,'SKU-0601','',6900,2500,9900,150,78,'','227g',1),
(7,'SKU-0701','',12900,6000,18900,160,96,'','30ml',1),
(8,'SKU-0801','',3900,1200,5900,200,130,'','100g',1),
(9,'SKU-0901','',8900,4000,15900,90,34,'','基础款',1),
(10,'SKU-1001','',19900,8000,29900,70,19,'','标准款',1);

-- 轮播图
INSERT INTO goods_image (goods_id,url,sort) VALUES
(2,'/file/demo/demo2.svg',0),(3,'/file/demo/demo3.svg',0),(4,'/file/demo/demo4.svg',0),
(5,'/file/demo/demo5.svg',0),(6,'/file/demo/demo6.svg',0),(7,'/file/demo/demo7.svg',0),
(8,'/file/demo/demo8.svg',0),(9,'/file/demo/demo9.svg',0),(10,'/file/demo/demo10.svg',0);

-- 评价
INSERT INTO goods_comment (order_id,order_goods_id,user_id,goods_id,goods_item_id,score,description_score,service_score,content,is_show) VALUES
(0,0,1,1,1,5,5,5,'音质很棒，降噪效果满意，续航给力！',1),
(0,0,2,1,1,4,4,5,'东西不错，物流也挺快，就是白色不耐脏哈哈。',1),
(0,0,4,2,2,5,5,5,'面料很舒服，尺码标准，推荐！',1),
(0,0,5,5,5,5,4,5,'坚果很新鲜，每日一包刚刚好。',1),
(0,0,8,7,7,4,4,4,'补水效果不错，质地清爽。',1);

-- 首页装修：轮播+导航
UPDATE config SET value = '{"banners":[{"image":"/file/demo/banner1.svg","link":""},{"image":"/file/demo/banner2.svg","link":"/pages/seckill/list"},{"image":"/file/demo/banner3.svg","link":"/pages/integral/list"}],"navs":[{"name":"积分商城","icon":"","link":"/pages/integral/list"},{"name":"限时秒杀","icon":"","link":"/pages/seckill/list"},{"name":"我的拼团","icon":"","link":"/pages/team/list"},{"name":"领券中心","icon":"","link":"/pages/coupon/center"},{"name":"售前预售","icon":"","link":"/pages/presell/list"},{"name":"在线客服","icon":"","link":"/pages/kefu/kefu"}],"goodsTitle":"精选好物"}' WHERE name='home_decorate';
