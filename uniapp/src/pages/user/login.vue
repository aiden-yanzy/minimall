<template>
  <view class="login">
    <view class="logo">Minimall</view>
    <view v-if="refCode" class="invite-tip">好友邀请你加入（邀请码 {{ refCode }}）· 注册后自动绑定</view>
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
import { onLoad } from '@dcloudio/uni-app'
import { register } from '@/api/user'
import { bindByCode } from '@/api/distribution'
import { useUserStore } from '@/store/user'

const mobile = ref('')
const password = ref('')
const loading = ref(false)
const refCode = ref('')
const userStore = useUserStore()

// 推广链接带 ?ref=邀请码：注册时自动绑定，老用户登录后补绑
onLoad((q) => {
  if (q && q.ref) {
    refCode.value = q.ref
    uni.setStorageSync('ref_code', q.ref)
  } else {
    const cached = uni.getStorageSync('ref_code')
    if (cached) refCode.value = cached
  }
})

function check() {
  if (!mobile.value || !password.value) {
    uni.showToast({ title: '请填写手机号和密码', icon: 'none' })
    return false
  }
  return true
}

/** 老用户带邀请码进来：登录后补绑（已绑定会静默失败） */
async function tryBind() {
  if (!refCode.value) return
  try {
    await bindByCode(refCode.value)
    uni.setStorageSync('ref_code', '')
    uni.showToast({ title: '已绑定推荐关系' })
  } catch (e) {}
}

async function doLogin() {
  if (!check()) return
  loading.value = true
  try {
    await userStore.login(mobile.value, password.value)
    await tryBind()
    uni.showToast({ title: '登录成功' })
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 500)
  } finally {
    loading.value = false
  }
}

async function doRegister() {
  if (!check()) return
  try {
    await register({
      mobile: mobile.value,
      password: password.value,
      channel: 'h5',
      inviteCode: refCode.value || undefined
    })
    uni.setStorageSync('ref_code', '')
    uni.showToast({ title: refCode.value ? '注册成功，已绑定邀请人' : '注册成功，请登录' })
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
.invite-tip {
  text-align: center;
  font-size: 24rpx;
  color: #ff8f6b;
  background: #fff5f0;
  border-radius: 12rpx;
  padding: 14rpx 18rpx;
  margin: -30rpx 0 30rpx;
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
