<template>
  <view class="login">
    <view class="logo">Minimall</view>
    <view class="card">
      <input v-model="mobile" class="ipt" type="number" placeholder="请输入手机号" />
      <input v-model="password" class="ipt" type="password" placeholder="请输入密码" />
      <button class="btn" :loading="loading" @click="doLogin">登 录</button>
      <button class="btn plain" @click="doRegister">注 册</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { register } from '@/api/user'
import { useUserStore } from '@/store/user'

const mobile = ref('')
const password = ref('')
const loading = ref(false)
const userStore = useUserStore()

function check() {
  if (!mobile.value || !password.value) {
    uni.showToast({ title: '请填写手机号和密码', icon: 'none' })
    return false
  }
  return true
}

async function doLogin() {
  if (!check()) return
  loading.value = true
  try {
    await userStore.login(mobile.value, password.value)
    uni.showToast({ title: '登录成功' })
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 500)
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!check()) return
  try {
    await register({ mobile: mobile.value, password: password.value, channel: 'h5' })
    uni.showToast({ title: '注册成功，请登录' })
  } catch (e) {}
}
</script>

<style scoped>
.login {
  padding: 120rpx 40rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.logo {
  text-align: center;
  font-size: 60rpx;
  font-weight: bold;
  color: #ff2d2d;
  margin-bottom: 60rpx;
}
.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
}
.ipt {
  height: 88rpx;
  border: 1rpx solid #eee;
  border-radius: 12rpx;
  padding: 0 24rpx;
  margin-bottom: 24rpx;
  font-size: 28rpx;
}
.btn {
  background: #ff2d2d;
  color: #fff;
  border-radius: 44rpx;
  margin-top: 20rpx;
}
.btn.plain {
  background: #fff;
  color: #ff2d2d;
  border: 1rpx solid #ff2d2d;
}
</style>
