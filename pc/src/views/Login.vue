<template>
  <div class="login">
    <el-card style="width: 400px">
      <h2>{{ mode === 'login' ? 'Minimall PC 商城' : '注册新账号' }}</h2>
      <el-form>
        <el-form-item>
          <el-input v-model="mobile" placeholder="手机号" size="large" maxlength="11" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" type="password" placeholder="密码(6-20位)" size="large" show-password @keyup.enter="submit" />
        </el-form-item>
        <el-form-item v-if="mode === 'register'">
          <el-input v-model="confirm" type="password" placeholder="确认密码" size="large" show-password @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">
          {{ mode === 'login' ? '登 录' : '注 册' }}
        </el-button>
        <p style="margin-top:14px;text-align:center">
          <template v-if="mode === 'login'">
            没有账号？<a style="color:#ff2d2d;cursor:pointer" @click="switchMode">去注册</a>
          </template>
          <template v-else>
            已有账号？<a style="color:#ff2d2d;cursor:pointer" @click="switchMode">去登录</a>
          </template>
        </p>
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

const mode = ref('login')
const mobile = ref('')
const password = ref('')
const confirm = ref('')
const loading = ref(false)

function switchMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  confirm.value = ''
}

function validate() {
  if (!/^1[3-9]\d{9}$/.test(mobile.value)) return ElMessage.warning('请输入正确的11位手机号')
  if (!password.value || password.value.length < 6) return ElMessage.warning('密码至少6位')
  if (mode.value === 'register' && password.value !== confirm.value) return ElMessage.warning('两次密码不一致')
  return true
}

async function submit() {
  if (!validate()) return
  loading.value = true
  try {
    if (mode.value === 'register') {
      await register({ mobile: mobile.value, password: password.value, channel: 'pc' })
      // 注册成功自动登录
      await user.login(mobile.value, password.value)
      ElMessage.success('注册成功，已自动登录')
    } else {
      await user.login(mobile.value, password.value)
      ElMessage.success('登录成功')
    }
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}

onMounted(() => { if (user.token) router.push('/') })
</script>

<style scoped>
.login { height: 60vh; display: flex; align-items: center; justify-content: center; }
h2 { text-align: center; color: #ff2d2d; margin-bottom: 20px; }
</style>