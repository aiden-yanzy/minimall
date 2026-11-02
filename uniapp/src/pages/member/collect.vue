<template>
  <view class="wrap">
    <view v-for="g in list" :key="g.id" class="card" @click="goDetail(g.id)">
      <image class="img" :src="asset(g.image)" mode="aspectFill" />
      <view class="info">
        <view class="name">{{ g.name }}</view>
        <view class="price">￥{{ fen2yuan(g.minPrice) }}</view>
      </view>
      <view class="heart" @click.stop="uncollect(g)">♥</view>
    </view>
    <view v-if="!list.length" class="empty">还没有收藏的商品</view>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCollectList, toggleCollect } from '@/api/member'
import { fen2yuan } from '@/utils/format'

const list = ref([])
async function load() {
  const res = await getCollectList()
  list.value = res.data
}
async function uncollect(g) {
  await toggleCollect(g.id)
  uni.showToast({ title: '已取消收藏' })
  load()
}
function goDetail(id) {
  uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
}
onShow(load)
</script>

<style scoped>
.wrap { padding: 20rpx; background: #f5f5f5; min-height: 100vh; }
.card { display: flex; background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 16rpx; align-items: center; }
.img { width: 140rpx; height: 140rpx; border-radius: 10rpx; background: #f0f0f0; }
.info { flex: 1; margin-left: 16rpx; }
.name { font-size: 28rpx; }
.price { color: #ff2d2d; font-size: 32rpx; margin-top: 10rpx; font-weight: bold; }
.heart { color: #ff2d2d; font-size: 40rpx; padding: 10rpx 20rpx; }
.empty { text-align: center; color: #999; padding: 120rpx 0; }
</style>