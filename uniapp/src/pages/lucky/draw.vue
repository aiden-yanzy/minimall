<template>
  <view class="lucky">
    <view class="head">
      <view class="title">🎡 幸运大转盘</view>
      <view class="cost" v-if="draw">每次 {{ draw.integralCost }} 积分</view>
      <view class="my">我的积分：<text class="num">{{ userInfo?.integral ?? '--' }}</text></view>
    </view>

    <!-- 奖品池 -->
    <view class="prizes">
      <view v-for="p in prizes" :key="p.id" class="prize">
        <view class="icon" :class="'t' + p.prizeType">{{ iconText[p.prizeType] }}</view>
        <view class="name">{{ p.prizeName }}</view>
      </view>
    </view>

    <button class="spin" :disabled="spinning" @click="doDraw">抽 一 次</button>

    <view class="records">
      <view class="sec-title">我的抽奖记录</view>
      <view v-for="r in records" :key="r.id" class="rec">
        <text class="r-name">{{ r.prizeName }}</text>
        <text class="r-time">{{ (r.createTime || '').slice(0, 16) }}</text>
      </view>
      <view v-if="!records.length" class="empty">还没有抽过奖</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { get, post } from '@/api/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const iconText = { 1: '分', 2: '币', 3: '券', 4: '✕' }
const draw = ref(null)
const prizes = ref([])
const records = ref([])
const spinning = ref(false)

async function load() {
  if (!userStore.token) return uni.navigateTo({ url: '/pages/user/login' })
  const d = await get('/shop/lucky/draws')
  draw.value = d.data[0]
  if (draw.value) {
    const p = await get('/shop/lucky/draws/' + draw.value.id + '/prizes')
    prizes.value = p.data
  }
  const r = await get('/shop/lucky/records')
  records.value = r.data
  userStore.fetchMe()
}
async function doDraw() {
  if (!draw.value) return
  spinning.value = true
  try {
    const res = await post('/shop/lucky/draw/' + draw.value.id)
    uni.showToast({ title: res.data.prizeType === 4 ? '未中奖，再接再厉' : '中奖: ' + res.data.prizeName })
    load()
  } catch (e) {
  } finally {
    setTimeout(() => (spinning.value = false), 500)
  }
}
onShow(load)
</script>

<style scoped>
.lucky { padding: 20rpx; background: linear-gradient(180deg, #7b2ff7, #f107a3); min-height: 100vh; }
.head { color: #fff; text-align: center; padding: 40rpx 0; }
.title { font-size: 44rpx; font-weight: bold; }
.cost { margin-top: 10rpx; font-size: 24rpx; opacity: .9; }
.my { margin-top: 14rpx; font-size: 26rpx; }
.num { font-size: 40rpx; font-weight: bold; }
.prizes { display: flex; flex-wrap: wrap; gap: 20rpx; padding: 20rpx; background: rgba(255,255,255,.12); border-radius: 20rpx; margin: 0 20rpx; }
.prize { width: calc(33.3% - 14rpx); text-align: center; color: #fff; }
.icon { width: 90rpx; height: 90rpx; line-height: 90rpx; border-radius: 50%; margin: 0 auto; background: rgba(255,255,255,.25); font-size: 36rpx; font-weight: bold; }
.icon.t4 { background: rgba(0,0,0,.15); }
.name { font-size: 22rpx; margin-top: 8rpx; opacity: .95; }
.spin { margin: 40rpx 60rpx 0; background: #fff; color: #f107a3; font-size: 34rpx; font-weight: bold; border-radius: 44rpx; }
.records { margin-top: 30rpx; background: rgba(255,255,255,.12); border-radius: 20rpx; padding: 24rpx; color: #fff; }
.sec-title { font-weight: bold; margin-bottom: 12rpx; }
.rec { display: flex; justify-content: space-between; padding: 12rpx 0; border-bottom: 1rpx solid rgba(255,255,255,.15); font-size: 24rpx; }
.r-time { opacity: .7; font-size: 22rpx; }
.empty { text-align: center; opacity: .7; padding: 30rpx 0; }
</style>