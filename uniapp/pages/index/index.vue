<template>
  <view class="home">
    <view class="banner">
      <text class="slogan">Minimall 商城</text>
      <text class="sub">P0 骨架已就绪</text>
    </view>
    <view class="grid">
      <view class="cell" @click="go('/pages/user/login')">
        <text>登录/注册</text>
      </view>
    </view>
  </view>
</template>

<script setup>
function go(url) {
  uni.navigateTo({ url })
}
</script>

<style scoped>
.home {
  padding: 20rpx;
}
.banner {
  height: 300rpx;
  background: linear-gradient(135deg, #ff2d2d, #ff8a00);
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.slogan {
  font-size: 44rpx;
  font-weight: bold;
}
.sub {
  margin-top: 16rpx;
  font-size: 26rpx;
  opacity: 0.9;
}
.grid {
  margin-top: 20rpx;
  display: flex;
}
.cell {
  flex: 1;
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 0;
  text-align: center;
}
</style>
