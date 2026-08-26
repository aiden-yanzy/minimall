<template>
  <view class="home">
    <view class="banner">
      <text class="slogan">Minimall</text>
      <text class="sub">P1 商城在线，买买买！</text>
    </view>

    <view class="search-bar" @click="goSearch">
      <text class="search-ph">搜索商品</text>
    </view>

    <view class="goods-grid">
      <view v-for="g in list" :key="g.id" class="goods-card" @click="goDetail(g.id)">
        <image class="goods-img" :src="g.image || defaultImg" mode="aspectFill" />
        <view class="goods-name">{{ g.name }}</view>
        <view class="goods-sub" v-if="g.subtitle">{{ g.subtitle }}</view>
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
import { onMounted, ref } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getGoodsList } from '@/api/goods'
import { fen2yuan } from '@/utils/format'

const list = ref([])
const defaultImg = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII='

async function load() {
  const res = await getGoodsList({ page: 1, size: 20 })
  list.value = res.data.records
}

function goDetail(id) {
  uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
}
function goSearch() {
  uni.switchTab({ url: '/pages/goods/list' })
}

onMounted(load)
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
.goods-sub {
  padding: 4rpx 16rpx 0;
  font-size: 22rpx;
  color: #999;
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
.price-symbol {
  font-size: 24rpx;
}
.price {
  font-size: 34rpx;
  font-weight: bold;
}
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
