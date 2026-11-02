<template>
  <view class="home">
    <!-- 轮播(装修配置) -->
    <swiper v-if="decorate.banners?.length && decorate.banners[0].image" class="banner-swiper" circular indicator-dots autoplay>
      <swiper-item v-for="(b, i) in decorate.banners" :key="i">
        <image class="banner-img" :src="asset(b.image)" mode="aspectFill" />
      </swiper-item>
    </swiper>
    <view v-else class="banner">
      <text class="slogan">Minimall</text>
      <text class="sub">多玩法商城，买买买！</text>
    </view>

    <!-- 宫格导航(装修配置) -->
    <view class="nav-grid" v-if="decorate.navs?.length">
      <view v-for="(n, i) in decorate.navs" :key="i" class="nav-cell" @click="go(n.link)">
        <image class="nav-icon" :src="asset(navIcon(n.name))" mode="aspectFit" />
        <view class="nav-name">{{ n.name }}</view>
      </view>
    </view>

    <view class="search-bar" @click="go('/pages/search/index')">
      <text class="search-ph">搜索商品</text>
    </view>

    <view class="section-title">{{ decorate.goodsTitle || '推荐好物' }}</view>
    <view class="goods-grid">
      <view v-for="g in list" :key="g.id" class="goods-card" @click="goDetail(g.id)">
        <image class="goods-img" :src="asset(g.image || defaultImg)" mode="aspectFill" />
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
import { asset } from '@/utils/asset'
import { reactive, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getGoodsList } from '@/api/goods'
import { getHomeDecorate } from '@/api/content'
import { fen2yuan } from '@/utils/format'

const list = ref([])
const decorate = reactive({ banners: [], navs: [], goodsTitle: '' })

/** 统一风格的线性图标（内联 SVG，避免混用 emoji 导致风格不统一） */
const NAV_SVG = {
  积分商城: '<rect x="3" y="9" width="18" height="12" rx="2"/><path d="M3 13h18"/><path d="M12 9v12"/><path d="M12 9C10 9 8 8 8 6a2 2 0 0 1 4-.6A2 2 0 0 1 16 6c0 2-2 3-4 3z"/>',
  限时秒杀: '<path d="M13 2 4 14h6l-1 8 9-12h-6l1-8z"/>',
  我的拼团: '<circle cx="9" cy="8" r="3"/><path d="M3 20a6 6 0 0 1 12 0"/><circle cx="17.5" cy="10" r="2.5"/><path d="M15 20a5 5 0 0 1 6-4.6"/>',
  领券中心: '<path d="M3 9V6h18v3a3 3 0 0 0 0 6v3H3v-3a3 3 0 0 0 0-6z"/><path d="M12 7v10" stroke-dasharray="2 2"/>',
  售前预售: '<circle cx="9" cy="20" r="1.4"/><circle cx="18" cy="20" r="1.4"/><path d="M2 3h3l2.6 12h11l2-8H6"/>',
  在线客服: '<path d="M21 12a8 8 0 1 1-3.2-6.4"/><path d="M21 4v6h-6"/><path d="M8 13c1 1.5 2.5 2 4 2s3-.5 4-2"/>',
}
function navIcon(name) {
  const body = NAV_SVG[name] || '<circle cx="12" cy="12" r="8"/>'
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#3b6ef5" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${body}</svg>`
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg)
}

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

// 小程序/分享链接带 ?ref=邀请码 → 存起来，注册时自动带上
onLoad((q) => {
  if (q && q.ref) uni.setStorageSync('ref_code', q.ref)
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
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 8rpx 16rpx;
  margin-top: 16rpx;
}
.nav-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.nav-icon {
  width: 76rpx;
  height: 76rpx;
  margin: 0 auto 8rpx;
  display: block;
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