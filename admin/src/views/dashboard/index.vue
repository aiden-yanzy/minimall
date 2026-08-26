<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card>
          <div class="stat-title">{{ card.title }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 16px">
      <template #header><b>欢迎使用 Minimall 商城后台 🎉</b></template>
      <p>当前登录：{{ auth.admin?.name }}（{{ auth.admin?.roleName }}）</p>
      <p>P0 骨架已打通：登录认证 / 统一响应 / 异常处理 / 双端 JWT 隔离。</p>
      <p>下一步：P1 商品 + 交易主链路。</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/store/auth'
const auth = useAuthStore()
const cards = [
  { title: '今日订单', value: '—' },
  { title: '今日销售额', value: '—' },
  { title: '商品总数', value: '—' },
  { title: '用户总数', value: '—' }
]
</script>

<style scoped lang="scss">
.stat-title {
  color: #888;
  font-size: 13px;
}
.stat-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: bold;
}
</style>
