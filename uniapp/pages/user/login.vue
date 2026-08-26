<template>
  <view class="login">
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
import { post } from '@/api/request'
import { useUserStore } from '@/store/user'

const mobile = ref('')
const password = ref('')
const loading = ref(false)
const userStore = useUserStore()

async function doLogin() {
  if (!mobile.value || !password.value) {
    uni.showToast({ title: '请填写手机号和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await post('/shop/auth/login', {
      mobile: mobile.value,
      password: password.value
    })
    userStore.setToken(res.data.token)
    uni.showToast({ title: '登录成功' })
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 500)
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!mobile.value || !password.value) {
    uni.showToast({ title: '请填写手机号和密码', icon: 'none' })
    return
  }
  try {
    await post('/shop/auth/register', {
      mobile: mobile.value,
      password: password.value,
      channel: 'h5'
    })
    uni.showToast({ title: '注册成功，请登录' })
  } catch (e) {}
}
</script>

<style scoped>
.login {
  padding: 120rpx 40rpx;
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
