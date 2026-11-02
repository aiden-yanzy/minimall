<template>
  <view class="articles">
    <view v-for="a in list" :key="a.id" class="card" @click="open(a)">
      <image v-if="a.image" class="img" :src="asset(a.image)" mode="aspectFill" />
      <view class="info">
        <view class="title">{{ a.title }}</view>
        <view class="time">{{ (a.createTime || '').slice(0, 10) }}</view>
      </view>
    </view>
    <view v-if="!list.length" class="empty">暂无内容</view>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getArticleList } from '@/api/content'

const list = ref([])
onShow(async () => {
  const res = await getArticleList()
  list.value = res.data
})
function open(a) {
  uni.navigateTo({ url: '/pages/article/detail?id=' + a.id })
}
</script>

<style scoped>
.articles { padding: 20rpx; background: #f5f5f5; min-height: 100vh; }
.card { display: flex; background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 16rpx; }
.img { width: 140rpx; height: 120rpx; border-radius: 10rpx; background: #f0f0f0; }
.info { flex: 1; margin-left: 16rpx; }
.title { font-size: 28rpx; font-weight: bold; }
.time { color: #bbb; font-size: 22rpx; margin-top: 10rpx; }
.empty { text-align: center; color: #999; padding: 120rpx 0; }
</style>