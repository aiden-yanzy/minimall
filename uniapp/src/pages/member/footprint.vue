<template>
  <view class="wrap">
    <view v-for="g in list" :key="g.id" class="card" @click="goDetail(g.id)">
      <image class="img" :src="g.image" mode="aspectFill" />
      <view class="info">
        <view class="name">{{ g.name }}</view>
        <view class="price">￥{{ fen2yuan(g.minPrice) }}</view>
      </view>
    </view>
    <view v-if="!list.length" class="empty">暂无浏览记录</view>
    <view v-if="list.length" class="clear" @click="clear">清空足迹</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getFootprintList, clearFootprint } from '@/api/member'
import { fen2yuan } from '@/utils/format'

const list = ref([])
async function load() {
  const res = await getFootprintList()
  list.value = res.data
}
function clear() {
  clearFootprint().then(() => { uni.showToast({ title: '已清空' }); load() })
}
function goDetail(id) {
  uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
}
onShow(load)
</script>

<style scoped>
.wrap { padding: 20rpx; background: #f5f5f5; min-height: 100vh; padding-bottom: 120rpx; }
.card { display: flex; background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 16rpx; align-items: center; }
.img { width: 140rpx; height: 140rpx; border-radius: 10rpx; background: #f0f0f0; }
.info { flex: 1; margin-left: 16rpx; }
.name { font-size: 28rpx; }
.price { color: #ff2d2d; font-size: 32rpx; margin-top: 10rpx; font-weight: bold; }
.empty { text-align: center; color: #999; padding: 120rpx 0; }
.clear { text-align: center; color: #999; margin-top: 20rpx; font-size: 26rpx; }
</style>