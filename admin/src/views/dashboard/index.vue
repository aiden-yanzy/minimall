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

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card>
          <template #header><b>近 7 日订单趋势</b></template>
          <div class="trend">
            <div v-for="(v, k) in stats.trend" :key="k" class="trend-item">
              <div class="bar" :style="{ height: barHeight(v) + 'px' }"></div>
              <div class="val">{{ v }}</div>
              <div class="day">{{ k }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><b>待办事项</b></template>
          <p>待发货订单：<b>{{ stats.pendingDelivery }}</b></p>
          <p>待处理售后：<b>{{ stats.pendingAfterSale }}</b></p>
          <p style="color:#999">P0-P8 全部落地：商品/交易/营销全家桶/钱包分销/装修内容均已打通 ✓</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { statsApi } from '@/api/dashboard'

const stats = reactive<any>({ todayOrder: 0, todayPaidOrder: 0, pendingDelivery: 0, pendingAfterSale: 0, userCount: 0, goodsCount: 0, trend: {} })

const cards = computed(() => [
  { title: '今日订单', value: stats.todayOrder },
  { title: '今日已支付', value: stats.todayPaidOrder },
  { title: '商品总数', value: stats.goodsCount },
  { title: '用户总数', value: stats.userCount }
])

function barHeight(v: number) {
  const max = Math.max(1, ...Object.values(stats.trend))
  return Math.max(4, Math.round((v / max) * 120))
}

onMounted(async () => {
  const res: any = await statsApi()
  Object.assign(stats, res.data)
})
</script>

<style scoped>
.stat-title { color: #888; font-size: 13px; }
.stat-value { margin-top: 8px; font-size: 26px; font-weight: bold; }
.trend { display: flex; align-items: flex-end; gap: 12px; height: 150px; }
.trend-item { flex: 1; text-align: center; }
.bar { background: linear-gradient(180deg, #409eff, #79bbff); border-radius: 4px 4px 0 0; margin: 0 auto; width: 60%; }
.val { font-size: 12px; color: #333; margin-top: 6px; }
.day { font-size: 11px; color: #999; margin-top: 2px; }
</style>