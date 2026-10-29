<template>
  <div class="page">
    <div class="head">
      <h2>限时秒杀</h2>
      <div class="sub">整点开抢，售完即止</div>
    </div>

    <div v-for="act in list" :key="act.id" class="act">
      <div class="act-head">
        <div class="act-name">
          {{ act.name }}
          <el-tag size="small" :type="act.started ? 'danger' : 'info'" effect="dark">
            {{ act.started ? '抢购中' : '未开始' }}
          </el-tag>
        </div>
        <div class="act-time">
          {{ (act.startTime || '').replace('T', ' ').slice(0, 16) }} — {{ (act.endTime || '').replace('T', ' ').slice(0, 16) }}
        </div>
      </div>

      <el-row :gutter="16">
        <el-col v-for="g in act.goodsList" :key="g.id" :span="6">
          <div class="card" @click="go(g)">
            <img :src="g.goodsImage" class="img" />
            <div class="name">{{ g.goodsName }}</div>
            <div class="spec" v-if="g.specValueStr">{{ g.specValueStr }}</div>
            <div class="price-row">
              <span class="price">￥{{ (g.seckillPrice / 100).toFixed(2) }}</span>
              <span class="origin" v-if="g.marketPrice">￥{{ (g.marketPrice / 100).toFixed(2) }}</span>
            </div>
            <el-progress
              :percentage="percent(g)"
              :stroke-width="10"
              :show-text="false"
              color="#ff2d2d"
              class="bar"
            />
            <div class="stock">
              仅剩 {{ g.remainStock }} 件 · 已抢 {{ g.seckillSales }} 件
            </div>
            <el-button
              size="small"
              type="danger"
              class="btn"
              :disabled="!act.started || g.remainStock <= 0"
              @click.stop="buy(g)"
            >{{ g.remainStock <= 0 ? '已抢光' : (act.started ? '立即抢购' : '未开始') }}</el-button>
          </div>
        </el-col>
      </el-row>
    </div>
    <el-empty v-if="!list.length" description="暂无秒杀活动" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { seckillList } from '@/api/promo'
import { useUserStore } from '@/store/user'

const router = useRouter()
const user = useUserStore()
const list = ref([])

const percent = (g) => {
  const total = (g.seckillStock || 0) + (g.seckillSales || 0)
  if (!total) return 0
  return Math.min(100, Math.round(((g.seckillSales || 0) / total) * 100))
}

function go(g) {
  router.push('/product/' + g.goodsId)
}

function buy(g) {
  if (!user.token) return ElMessage.warning('请先登录')
  // 秒杀下单：走确认订单页的秒杀参数
  router.push({ path: '/checkout', query: { buyType: 'seckill', seckillGoodsId: g.id, num: 1 } })
}

onMounted(async () => {
  const res = await seckillList()
  list.value = res.data || []
})
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 20px 0 40px; }
.head { margin-bottom: 18px; }
.head h2 { margin: 0; font-size: 22px; }
.sub { color: #999; font-size: 13px; margin-top: 6px; }
.act { background: #fff; border-radius: 12px; padding: 18px; margin-bottom: 18px; }
.act-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.act-name { font-size: 17px; font-weight: 600; display: flex; align-items: center; gap: 10px; }
.act-time { font-size: 12px; color: #999; }
.card { background: #fafbfc; border-radius: 10px; padding: 10px; margin-bottom: 12px; cursor: pointer; transition: .2s; }
.card:hover { box-shadow: 0 6px 18px rgba(0,0,0,.08); transform: translateY(-2px); }
.img { width: 100%; height: 180px; object-fit: cover; border-radius: 8px; background: #f0f2f5; }
.name { font-size: 14px; margin-top: 10px; height: 40px; overflow: hidden; }
.spec { font-size: 12px; color: #999; }
.price-row { display: flex; align-items: baseline; gap: 8px; margin-top: 6px; }
.price { color: #ff2d2d; font-size: 20px; font-weight: 700; }
.origin { color: #bbb; font-size: 12px; text-decoration: line-through; }
.bar { margin-top: 8px; }
.stock { font-size: 12px; color: #999; margin: 6px 0 10px; }
.btn { width: 100%; }
</style>
