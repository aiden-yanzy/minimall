<template>
  <view class="kefu">
    <view v-for="m in msgs" :key="m.id" class="item">
      <view class="mine">我：{{ m.content }}</view>
      <view v-if="m.adminReply" class="reply">客服：{{ m.adminReply }}</view>
    </view>
    <view class="input-bar">
      <input v-model="content" class="ipt" placeholder="说点什么..." />
      <view class="send" @click="send">发送</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { kefuSend, kefuMessages } from '@/api/content'

const msgs = ref([])
const content = ref('')

async function load() {
  const res = await kefuMessages()
  msgs.value = res.data
}
async function send() {
  if (!content.value) return
  await kefuSend(content.value)
  content.value = ''
  load()
}
onShow(load)
</script>

<style scoped>
.kefu { padding: 20rpx; background: #f5f5f5; min-height: 100vh; padding-bottom: 140rpx; }
.item { margin-bottom: 16rpx; }
.mine {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 26rpx;
}
.reply {
  background: #e8f4ff;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 26rpx;
  margin-top: 10rpx;
  color: #2b6cb0;
}
.input-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  display: flex;
  padding: 16rpx 20rpx;
  gap: 16rpx;
}
.ipt {
  flex: 1;
  height: 72rpx;
  border: 1rpx solid #eee;
  border-radius: 36rpx;
  padding: 0 28rpx;
}
.send {
  background: #ff2d2d;
  color: #fff;
  border-radius: 36rpx;
  padding: 14rpx 40rpx;
}
</style>