<template>
  <view class="sign">
    <view class="card">
      <view class="title">每日签到</view>
      <view class="rules" v-if="config">
        每日 +{{ config.integral }} 积分 · 连续 {{ config.continuousDays }} 天额外 +{{ config.continuousReward }}
      </view>
      <view class="btn" :class="{ done: status?.signedToday }" @click="sign">
        {{ status?.signedToday ? '今日已签到' : '立即签到' }}
      </view>
      <view class="continuous">已连续签到 {{ status?.continuousDays || 0 }} 天</view>
    </view>

    <view class="calendar card">
      <view class="cal-title">本月签到记录</view>
      <view class="grid">
        <view v-for="d in days" :key="d" class="day" :class="{ signed: signedSet.has(d) }">
          <view class="d">{{ d.split('-')[2] }}</view>
          <view class="dot" v-if="signedSet.has(d)">✓</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getSignStatus, doSign, getSignMonth } from '@/api/sign'

const status = ref(null)
const config = ref(null)
const monthDays = ref([])

const signedSet = computed(() => new Set(monthDays.value))
const days = computed(() => {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth() + 1
  const n = new Date(y, m, 0).getDate()
  return Array.from({ length: n }, (_, i) => `${y}-${String(m).padStart(2, '0')}-${String(i + 1).padStart(2, '0')}`)
})

async function load() {
  const now = new Date()
  const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  const [s, m] = await Promise.all([getSignStatus(), getSignMonth(month)])
  status.value = s.data
  config.value = s.data.config
  monthDays.value = m.data
}

async function sign() {
  if (status.value?.signedToday) return
  try {
    const res = await doSign()
    uni.showToast({ title: '签到成功 +' + res.data.rewardIntegral })
    status.value = res.data
    config.value = res.data.config
    await load()
  } catch (e) {}
}

onShow(load)
</script>

<style scoped>
.sign {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}
.title { font-size: 32rpx; font-weight: bold; }
.rules { color: #999; font-size: 22rpx; margin-top: 10rpx; }
.btn {
  margin-top: 30rpx;
  background: linear-gradient(135deg, #7b2ff7, #f107a3);
  color: #fff;
  text-align: center;
  border-radius: 44rpx;
  padding: 22rpx 0;
  font-size: 32rpx;
}
.btn.done { background: #ccc; }
.continuous { text-align: center; color: #999; font-size: 22rpx; margin-top: 16rpx; }
.cal-title { font-size: 28rpx; font-weight: bold; margin-bottom: 20rpx; }
.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 4rpx;
}
.day {
  width: calc((100% - 4rpx * 6) / 7);
  height: 68rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 10rpx;
  font-size: 24rpx;
}
.day.signed { background: #7b2ff7; color: #fff; }
.dot { font-size: 18rpx; }
</style>