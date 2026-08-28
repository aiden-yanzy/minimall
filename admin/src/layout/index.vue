<template>
  <el-container class="layout">
    <el-aside width="210px" class="aside">
      <div class="logo">Minimall 商城</div>
      <el-menu :default-active="route.path" router background-color="#001529" text-color="#fff" active-text-color="#409eff">
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/goods">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/freight">
          <el-icon><Goods /></el-icon>
          <span>运费模板</span>
        </el-menu-item>
        <el-menu-item index="/comments">
          <el-icon><Goods /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/after">
          <el-icon><Tickets /></el-icon>
          <span>售后管理</span>
        </el-menu-item>
        <el-menu-item index="/marketing">
          <el-icon><Present /></el-icon>
          <span>营销管理</span>
        </el-menu-item>
        <el-menu-item index="/integral">
          <el-icon><Present /></el-icon>
          <span>积分商城</span>
        </el-menu-item>
        <el-menu-item index="/seckill">
          <el-icon><Present /></el-icon>
          <span>秒杀管理</span>
        </el-menu-item>
        <el-menu-item index="/wallet">
          <el-icon><Present /></el-icon>
          <span>提现审核</span>
        </el-menu-item>
        <el-menu-item index="/content">
          <el-icon><Present /></el-icon>
          <span>内容装修</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ route.meta.title }}</span>
        <el-dropdown @command="onCommand">
          <span class="user">{{ auth.admin?.name || '管理员' }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataBoard, Goods, Tickets, Present } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

onMounted(() => {
  if (!auth.admin) auth.fetchMe()
})

function onCommand(cmd: string) {
  if (cmd === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.layout {
  height: 100vh;
}
.aside {
  background: #001529;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.main {
  background: #f0f2f5;
}
.user {
  cursor: pointer;
  color: #333;
}
</style>
