<template>
  <view class="center">
    <view v-for="c in list" :key="c.id" class="coupon-card">
      <view class="left">
        <view class="value">
          <template v-if="c.type === 1">
            <text class="sym">￥</text><text class="num">{{ c.discountValue / 100 }}</text>
          </template>
          <template v-else>
            <text class="num">{{ c.discountValue / 10 }}</text><text class="sym">折</text>
          </template>
        </view>
        <view class="cond">{{ c.threshold ? '满' + (c.threshold / 100) + '元可用' : '无门槛' }}</view>
      </view>
      <view class="mid">
        <view class="name">{{ c.name }}</view>
        <view class="desc">{{ c.description || '领后有效' + c.validDays + '天' }}</view>
      </view>
      <view class="right">
        <view class="btn" @click="claim(c)">领取</view>
        <view class="limit" v-if="c.totalCount">余 {{ c.totalCount - c.sendCount }}</view>
      </view>
    </view>
    <view class="mine-link" @click="goMine">我的优惠券 →</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCouponCenter, claimCoupon } from '@/api/coupon'

const list = ref([])

async function load() {
  const res = await getCouponCenter()
  list.value = res.data
}
async function claim(c) {
  try {
    await claimCoupon(c.id)
    uni.showToast({ title: '领取成功' })
  } catch (e) {}
}
function goMine() {
  uni.navigateTo({ url: '/pages/coupon/mine' })
}

onShow(load)
</script>

<style scoped>
.center {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}
.left {
  width: 220rpx;
  background: #ff2d2d;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.value {
  display: flex;
  align-items: baseline;
}
.sym {
  font-size: 24rpx;
}
.num {
  font-size: 56rpx;
  font-weight: bold;
}
.cond {
  font-size: 22rpx;
  opacity: 0.9;
  margin-top: 8rpx;
}
.mid {
  flex: 1;
  padding: 30rpx 20rpx;
}
.name {
  font-size: 30rpx;
  font-weight: bold;
}
.desc {
  color: #999;
  font-size: 22rpx;
  margin-top: 10rpx;
}
.right {
  width: 140rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.btn {
  background: #ff2d2d;
  color: #fff;
  border-radius: 30rpx;
  padding: 12rpx 36rpx;
  font-size: 26rpx;
}
.limit {
  color: #999;
  font-size: 20rpx;
  margin-top: 8rpx;
}
.mine-link {
  text-align: center;
  color: #3370ff;
  padding: 20rpx;
}
</style>