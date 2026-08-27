<template>
  <div class="login">
    <el-card style="width: 400px">
      <h2>Minimall PC 商城</h2>
      <el-form>
        <el-form-item><el-input v-model="mobile" placeholder="手机号" size="large" /></el-form-item>
        <el-form-item><el-input v-model="password" type="password" placeholder="密码" size="large" show-password @keyup.enter="doLogin" /></el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="doLogin">登录</el-button>
        <p style="margin-top:12px">没有账号？<a style="color:#ff2d2d" @click="doRegister">去注册</a></p>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { register } from '@/api/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const mobile = ref('')
const password = ref('')
const loading = ref(false)

async function doLogin() {
  loading.value = true
  try {
    await user.login(mobile.value, password.value)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } finally { loading.value = false }
}
async function doRegister() {
  await register({ mobile: mobile.value, password: password.value, channel: 'h5' })
  ElMessage.success('注册成功，请登录')
}

onMounted(() => { if (user.token) router.push('/') })
</script>

<style scoped>
.login { height: 60vh; display: flex; align-items: center; justify-content: center; }
h2 { text-align: center; color: #ff2d2d; margin-bottom: 20px; }
</style>