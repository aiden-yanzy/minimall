<template>
  <div class="page">
    <div class="head">
      <h2>领券中心</h2>
      <div class="sub">先领券再下单，满减自动抵扣</div>
    </div>

    <el-row :gutter="16">
      <el-col v-for="c in list" :key="c.id" :span="8">
        <div class="coupon" :class="{ taken: mineIds.includes(c.id) }">
          <div class="left">
            <div class="amount">
              <template v-if="c.type === 1">
                <span class="num">￥{{ (c.discountValue / 100).toFixed(0) }}</span>
              </template>
              <template v-else>
                <span class="num">{{ (c.discountValue / 10).toFixed(1).replace('.0', '') }}折</span>
              </template>
            </div>
            <div class="threshold">
              {{ c.threshold > 0 ? `满 ￥${(c.threshold / 100).toFixed(0)} 可用` : '无门槛' }}
            </div>
          </div>
          <div class="right">
            <div class="name">{{ c.name }}</div>
            <div class="desc">{{ c.description || '全场通用' }}</div>
            <div class="valid">有效 {{ c.validDays || 30 }} 天 · 每人限 {{ c.perLimit || 1 }} 张</div>
            <el-button
              size="small"
              :type="mineIds.includes(c.id) ? 'info' : 'danger'"
              :disabled="mineIds.includes(c.id)"
              @click="claim(c)"
            >{{ mineIds.includes(c.id) ? '已领取' : '立即领取' }}</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!list.length" description="暂无可领优惠券" />

    <div class="more">
      <router-link to="/coupons/mine">查看我的优惠券 →</router-link>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { couponCenter, couponMine, couponClaim } from '@/api/promo'
import { useUserStore } from '@/store/user'

const list = ref([])
const mineIds = ref([])
const user = useUserStore()

async function load() {
  const res = await couponCenter()
  list.value = res.data || []
  if (user.token) {
    const mine = await couponMine()
    mineIds.value = (mine.data || []).map((m) => m.couponId || m.id)
  }
}

async function claim(c) {
  if (!user.token) return ElMessage.warning('请先登录')
  try {
    await couponClaim(c.id)
    ElMessage.success('领取成功')
    load()
  } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 20px 0 40px; }
.head { margin-bottom: 18px; }
.head h2 { margin: 0; font-size: 22px; }
.sub { color: #999; font-size: 13px; margin-top: 6px; }
.coupon { display: flex; background: #fff; border-radius: 10px; overflow: hidden; margin-bottom: 16px; border: 1px solid #f0f0f0; }
.coupon.taken { opacity: 0.6; }
.left { width: 120px; background: linear-gradient(135deg, #ff6b5b, #ff2d2d); color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 18px 0; }
.num { font-size: 26px; font-weight: 700; }
.threshold { font-size: 12px; opacity: 0.9; margin-top: 6px; }
.right { flex: 1; padding: 16px; display: flex; flex-direction: column; gap: 6px; align-items: flex-start; }
.name { font-size: 15px; font-weight: 600; }
.desc { font-size: 12px; color: #888; }
.valid { font-size: 12px; color: #bbb; }
.more { text-align: right; }
.more a { color: #ff2d2d; font-size: 13px; text-decoration: none; }
</style>
