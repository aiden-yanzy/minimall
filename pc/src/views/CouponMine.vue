<template>
  <div class="page">
    <div class="head">
      <h2>我的优惠券</h2>
      <el-radio-group v-model="tab" size="small" @change="render">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="usable">未使用</el-radio-button>
        <el-radio-button value="used">已使用</el-radio-button>
        <el-radio-button value="expired">已过期</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="16">
      <el-col v-for="c in shown" :key="c.id" :span="8">
        <div class="coupon" :class="stateClass(c)">
          <div class="left">
            <div class="num" v-if="c.type === 1">￥{{ (c.discountValue / 100).toFixed(0) }}</div>
            <div class="num" v-else>{{ (c.discountValue / 10).toFixed(1).replace('.0', '') }}折</div>
            <div class="threshold">{{ c.threshold > 0 ? `满 ￥${(c.threshold / 100).toFixed(0)} 可用` : '无门槛' }}</div>
          </div>
          <div class="right">
            <div class="name">{{ c.couponName }}</div>
            <div class="valid">有效期至 {{ (c.expireTime || '').slice(0, 10) }}</div>
            <div class="state">{{ stateText(c) }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!shown.length" description="还没有优惠券，去领券中心看看" />
    <div class="more">
      <router-link to="/coupons">去领券中心 →</router-link>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { couponMine } from '@/api/promo'

const list = ref([])
const tab = ref('all')

const shown = computed(() => {
  if (tab.value === 'usable') return list.value.filter((c) => c.status === 0 && !expired(c))
  if (tab.value === 'used') return list.value.filter((c) => c.status === 1)
  if (tab.value === 'expired') return list.value.filter((c) => expired(c))
  return list.value
})

function expired(c) {
  return c.expireTime && new Date(c.expireTime).getTime() < Date.now()
}
function stateText(c) {
  if (c.status === 1) return '已使用'
  if (expired(c)) return '已过期'
  return '待使用'
}
function stateClass(c) {
  if (c.status === 1) return 'used'
  if (expired(c)) return 'expired'
  return ''
}
function render() {}

onMounted(async () => {
  const res = await couponMine()
  list.value = res.data || []
})
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 20px 0 40px; }
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.head h2 { margin: 0; font-size: 22px; }
.coupon { display: flex; background: #fff; border-radius: 10px; overflow: hidden; margin-bottom: 16px; border: 1px solid #f0f0f0; }
.coupon.used, .coupon.expired { opacity: 0.55; }
.left { width: 120px; background: linear-gradient(135deg, #ff6b5b, #ff2d2d); color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.coupon.used .left { background: #b9bfc9; }
.coupon.expired .left { background: #c9ced8; }
.num { font-size: 26px; font-weight: 700; }
.threshold { font-size: 12px; opacity: 0.9; margin-top: 6px; }
.right { flex: 1; padding: 16px; }
.name { font-size: 15px; font-weight: 600; }
.valid { font-size: 12px; color: #999; margin-top: 8px; }
.state { font-size: 12px; color: #ff2d2d; margin-top: 6px; }
.more { text-align: right; }
.more a { color: #ff2d2d; font-size: 13px; text-decoration: none; }
</style>
