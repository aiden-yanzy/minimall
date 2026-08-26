<template>
  <view class="mine">
    <view class="profile">
      <image class="avatar" :src="userInfo?.avatar || defaultAvatar" mode="aspectFill" />
      <view class="u-info">
        <view v-if="userInfo" class="nickname">{{ userInfo.nickname }}</view>
        <view v-else class="nickname" @click="goLogin">点击登录</view>
        <view v-if="userInfo" class="id">ID: {{ userInfo.sn }}</view>
      </view>
    </view>

    <view class="stats">
      <view class="stat">
        <view class="v">￥{{ fen2yuan(userInfo?.balance || 0) }}</view>
        <view class="k">余额</view>
      </view>
      <view class="stat">
        <view class="v">{{ userInfo?.integral || 0 }}</view>
        <view class="k">积分</view>
      </view>
      <view class="stat">
        <view class="v">0</view>
        <view class="k">优惠券</view>
      </view>
    </view>

    <view class="menu">
      <view class="menu-item" @click="go('/pages/order/list')">我的订单</view>
      <view class="menu-item" @click="go('/pages/coupon/mine')">我的优惠券</view>
      <view class="menu-item" @click="go('/pages/coupon/center')">领券中心</view>
      <view class="menu-item" @click="go('/pages/wallet/wallet')">我的钱包</view>
      <view class="menu-item" @click="go('/pages/integral/list')">积分商城</view>
      <view class="menu-item" @click="go('/pages/seckill/list')">限时秒杀</view>
      <view class="menu-item" @click="go('/pages/team/list')">我的拼团</view>
      <view class="menu-item" @click="go('/pages/bargain/list')">我的砍价</view>
      <view class="menu-item" @click="go('/pages/presell/list')">预售专区</view>
      <view class="menu-item" @click="go('/pages/sign/sign')">每日签到</view>
    </view>

    <button v-if="userInfo" class="logout" @click="logout">退出登录</button>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { fen2yuan } from '@/utils/format'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const defaultAvatar = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII='

function goLogin() {
  uni.navigateTo({ url: '/pages/user/login' })
}
function go(url) {
  uni.navigateTo({ url })
}
function logout() {
  userStore.logout()
  uni.showToast({ title: '已退出' })
}

onShow(() => {
  if (userStore.token) userStore.fetchMe()
})
</script>

<style scoped>
.mine {
  background: #f5f5f5;
  min-height: 100vh;
}
.profile {
  background: linear-gradient(135deg, #ff2d2d, #ff8a00);
  padding: 60rpx 40rpx;
  display: flex;
  align-items: center;
}
.avatar {
  width: 130rpx;
  height: 130rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.6);
  background: #fff;
}
.u-info {
  margin-left: 30rpx;
  color: #fff;
}
.nickname {
  font-size: 36rpx;
  font-weight: bold;
}
.id {
  font-size: 24rpx;
  opacity: 0.9;
  margin-top: 8rpx;
}
.stats {
  display: flex;
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 30rpx 0;
}
.stat {
  flex: 1;
  text-align: center;
}
.v {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.k {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}
.menu {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
}
.menu-item {
  padding: 32rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  font-size: 28rpx;
}
.menu-item:last-child {
  border-bottom: none;
}
.logout {
  margin: 40rpx 20rpx;
  background: #fff;
  color: #ff2d2d;
  border-radius: 12rpx;
}
</style>
