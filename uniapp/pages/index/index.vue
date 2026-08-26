<template>
  <view class="home">
    <!-- 轮播(装修配置) -->
    <swiper v-if="decorate.banners?.length && decorate.banners[0].image" class="banner-swiper" circular indicator-dots autoplay>
      <swiper-item v-for="(b, i) in decorate.banners" :key="i">
        <image class="banner-img" :src="b.image" mode="aspectFill" />
      </swiper-item>
    </swiper>
    <view v-else class="banner">
      <text class="slogan">Minimall</text>
      <text class="sub">多玩法商城，买买买！</text>
    </view>

    <!-- 宫格导航(装修配置) -->
    <view class="nav-grid" v-if="decorate.navs?.length">
      <view v-for="(n, i) in decorate.navs" :key="i" class="nav-cell" @click="go(n.link)">
        <view class="nav-icon">{{ n.name?.slice(0, 1) }}</view>
        <view class="nav-name">{{ n.name }}</view>
      </view>
    </view>

    <view class="search-bar" @click="go('/pages/goods/list')">
      <text class="search-ph">搜索商品</text>
    </view>

    <view class="section-title">{{ decorate.goodsTitle || '推荐好物' }}</view>
    <view class="goods-grid">
      <view v-for="g in list" :key="g.id" class="goods-card" @click="goDetail(g.id)">
        <image class="goods-img" :src="g.image || defaultImg" mode="aspectFill" />
        <view class="goods-name">{{ g.name }}</view>
        <view class="goods-price">
          <text class="price-symbol">￥</text>
          <text class="price">{{ fen2yuan(g.minPrice) }}</text>
          <text class="price-origin" v-if="g.marketPrice > g.minPrice">￥{{ fen2yuan(g.marketPrice) }}</text>
        </view>
      </view>
    </view>
    <view v-if="!list.length" class="empty">加载中...</view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getGoodsList } from '@/api/goods'
import { getHomeDecorate } from '@/api/content'
import { fen2yuan } from '@/utils/format'

const list = ref([])
const decorate = reactive({ banners: [], navs: [], goodsTitle: '' })
const defaultImg = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII='

async function load() {
  const [d, g] = await Promise.all([getHomeDecorate(), getGoodsList({ page: 1, size: 20 })])
  try {
    const j = typeof d.data === 'string' ? JSON.parse(d.data) : d.data
    Object.assign(decorate, j)
  } catch (e) {}
  list.value = g.data.records
}
function go(url) {
  if (!url) return
  if (url.startsWith('/pages')) uni.navigateTo({ url })
}
function goDetail(id) {
  uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
}

onLoad(load)
onPullDownRefresh(async () => {
  await load()
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.home {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.banner-swiper {
  height: 260rpx;
  border-radius: 20rpx;
  overflow: hidden;
}
.banner-img {
  width: 100%;
  height: 260rpx;
}
.banner {
  height: 200rpx;
  background: linear-gradient(135deg, #ff2d2d, #ff8a00);
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.slogan {
  font-size: 44rpx;
  font-weight: bold;
}
.sub {
  margin-top: 12rpx;
  font-size: 24rpx;
  opacity: 0.9;
}
.nav-grid {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 0;
  margin-top: 16rpx;
}
.nav-cell {
  flex: 1;
  text-align: center;
}
.nav-icon {
  width: 90rpx;
  height: 90rpx;
  line-height: 90rpx;
  margin: 0 auto;
  border-radius: 50%;
  background: #fff0f0;
  color: #ff2d2d;
  font-size: 36rpx;
  font-weight: bold;
}
.nav-name {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #333;
}
.search-bar {
  margin: 20rpx 0;
  background: #fff;
  border-radius: 40rpx;
  padding: 18rpx 30rpx;
}
.search-ph {
  color: #bbb;
  font-size: 26rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin: 8rpx 0 16rpx;
}
.goods-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}
.goods-card {
  width: 48.5%;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
}
.goods-img {
  width: 100%;
  height: 340rpx;
  background: #f0f0f0;
}
.goods-name {
  padding: 12rpx 16rpx 0;
  font-size: 26rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-price {
  padding: 12rpx 16rpx 20rpx;
  color: #ff2d2d;
  display: flex;
  align-items: baseline;
}
.price-symbol { font-size: 24rpx; }
.price { font-size: 34rpx; font-weight: bold; }
.price-origin {
  margin-left: 10rpx;
  font-size: 22rpx;
  color: #bbb;
  text-decoration: line-through;
}
.empty {
  text-align: center;
  color: #999;
  padding: 60rpx 0;
}
</style>