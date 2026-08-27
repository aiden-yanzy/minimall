<template>
  <div>
    <header class="header">
      <div class="inner">
        <router-link to="/" class="logo">Minimall<small>PC商城</small></router-link>
        <div class="search">
          <el-input v-model="kw" placeholder="搜索商品" @keyup.enter="search" clearable />
          <el-button type="primary" @click="search">搜索</el-button>
        </div>
        <nav class="nav">
          <router-link to="/">首页</router-link>
          <router-link to="/products">全部商品</router-link>
          <router-link to="/cart"><el-badge :value="0" hide>购物车</el-badge></router-link>
          <router-link to="/orders">我的订单</router-link>
          <template v-if="user.token">
            <span class="uname">{{ user.userInfo?.nickname }}</span>
            <a style="cursor:pointer" @click="logout">退出</a>
          </template>
          <router-link v-else to="/login">登录</router-link>
        </nav>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">Minimall 单商户自营商城 · Spring Boot 3 + Vue3 · PC 端</footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const user = useUserStore()
const kw = ref('')
function search() {
  router.push({ path: '/products', query: { keyword: kw.value } })
}
function logout() {
  user.logout()
  router.push('/')
}
</script>

<style scoped>
.header { background: #fff; border-bottom: 1px solid #eee; }
.inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; gap: 30px; padding: 14px 0; }
.logo { font-size: 24px; font-weight: 800; color: #ff2d2d; }
.logo small { font-size: 12px; color: #999; margin-left: 6px; font-weight: 400; }
.search { flex: 1; display: flex; gap: 8px; max-width: 460px; }
.nav { display: flex; align-items: center; gap: 20px; font-size: 14px; color: #333; }
.nav .uname { color: #ff2d2d; }
.main { min-height: calc(100vh - 150px); }
.footer { text-align: center; color: #999; font-size: 13px; padding: 30px 0; }
</style>