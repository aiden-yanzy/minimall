<template>
  <view class="article">
    <view class="title">{{ article.title }}</view>
    <view class="time">{{ (article.createTime || '').slice(0, 10) }}</view>
    <view class="content" v-if="article.content">
      <rich-text :nodes="article.content" />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getArticle } from '@/api/content'

const article = ref({})
onLoad((o) => {
  getArticle(o.id).then((res) => (article.value = res.data))
})
</script>

<style scoped>
.article { padding: 30rpx; background: #fff; min-height: 100vh; }
.title { font-size: 38rpx; font-weight: bold; }
.time { color: #bbb; font-size: 22rpx; margin: 16rpx 0 30rpx; }
.content { font-size: 28rpx; line-height: 1.8; color: #333; }
</style>