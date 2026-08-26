<template>
  <view class="mine">
    <view class="tabs">
      <view v-for="t in tabs" :key="t.value" class="tab" :class="{ active: current === t.value }" @click="switchTab(t.value)">
        {{ t.label }}
      </view>
    </view>
    <view v-if="!list.length" class="empty">暂无优惠券</view>
    <view v-for="c in list" :key="c.id" class="coupon-card" :class="{ used: c.status !== 0 }">
      <view class="left">
        <template v-if="c.type === 1">
          <text class="sym">￥</text><text class="num">{{ c.discountValue / 100 }}</text>
        </template>
        <template v-else>
          <text class="num">{{ c.discountValue / 10 }}</text><text class="sym">折</text>
        </template>
      </view>
      <view class="mid">
        <view class="name">{{ c.couponName }}</view>
        <view class="desc">{{ c.threshold ? '满' + c.threshold / 100 + '元可用' : '无门槛' }}</view>
        <view class="time">{{ c.expireTime ? '有效期至 ' + (c.expireTime || '').slice(0, 10) : '' }}</view>
      </view>
      <view class="state">{{ statusMap[c.status] }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyCoupons } from '@/api/coupon'

const tabs = [
  { label: '未使用', value: 0 },
  { label: '已使用', value: 1 },
  { label: '已过期', value: 2 }
]
const statusMap = { 0: '未使用', 1: '已使用', 2: '已过期' }
const current = ref(0)
const list = ref([])

async function load() {
  const res = await getMyCoupons(current.value)
  list.value = res.data
}
function switchTab(v) {
  current.value = v
  load()
}

onShow(load)
</script>

<style scoped>
.mine {
  background: #f5f5f5;
  min-height: 100vh;
}
.tabs {
  display: flex;
  background: #fff;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  border-bottom: 4rpx solid transparent;
}
.tab.active {
  color: #ff2d2d;
  border-bottom-color: #ff2d2d;
  font-weight: bold;
}
.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  margin: 20rpx;
  overflow: hidden;
}
.coupon-card.used {
  opacity: 0.55;
}
.left {
  width: 180rpx;
  background: #ff2d2d;
  color: #fff;
  display: flex;
  align-items: baseline;
  justify-content: center;
  padding: 30rpx 0;
}
.sym { font-size: 24rpx; }
.num { font-size: 48rpx; font-weight: bold; }
.mid {
  flex: 1;
  padding: 24rpx 20rpx;
}
.name { font-size: 28rpx; font-weight: bold; }
.desc { color: #999; font-size: 22rpx; margin-top: 8rpx; }
.time { color: #bbb; font-size: 20rpx; margin-top: 8rpx; }
.state {
  width: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}
.empty {
  text-align: center;
  color: #999;
  padding: 120rpx 0;
}
</style>