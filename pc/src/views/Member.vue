<template>
  <div class="page">
    <div class="head">
      <h2>会员中心</h2>
    </div>

    <div class="top">
      <div class="level-card">
        <div class="lc-left">
          <div class="lc-label">当前等级</div>
          <div class="lc-name">{{ level.levelName || '普通会员' }}</div>
          <div class="lc-priv">{{ level.privilege }}</div>
        </div>
        <div class="lc-right">
          <div class="lc-amount">累计消费 ￥{{ ((level.totalConsume || 0) / 100).toFixed(2) }}</div>
          <div v-if="level.nextLevelName" class="lc-next">
            再消费 <b>￥{{ ((level.gap || 0) / 100).toFixed(2) }}</b> 升级「{{ level.nextLevelName }}」
          </div>
          <el-progress
            :percentage="level.percent || 0"
            :stroke-width="10"
            color="#ffb400"
            :show-text="false"
            class="lc-bar"
          />
        </div>
      </div>

      <div class="wallet-card">
        <div class="wc-label">账户余额（元）</div>
        <div class="wc-amount">{{ ((balance || 0) / 100).toFixed(2) }}</div>
        <div class="wc-sub">
          积分 {{ me.integral || 0 }} · 优惠券 <router-link to="/coupons/mine" class="lnk">查看</router-link>
        </div>
      </div>
    </div>

    <el-tabs v-model="tab" class="tabs">
      <el-tab-pane label="余额明细" name="logs">
        <el-table :data="logs" border size="small">
          <el-table-column label="类型" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="row.changeAmount >= 0 ? 'success' : 'info'">
                {{ typeText(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="变动" width="120">
            <template #default="{ row }">
              <span :class="row.changeAmount >= 0 ? 'in' : 'out'">
                {{ row.changeAmount >= 0 ? '+' : '' }}{{ (row.changeAmount / 100).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="余额" width="110">
            <template #default="{ row }">{{ ((row.balanceAfter || 0) / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="220" />
          <el-table-column label="时间" width="160">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 16) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!logs.length" description="暂无流水" />
      </el-tab-pane>

      <el-tab-pane :label="`等级权益（${levels.length}）`" name="levels">
        <el-row :gutter="16">
          <el-col v-for="l in levels" :key="l.id" :span="6">
            <div class="lv-card" :class="{ on: l.name === level.levelName }">
              <div class="lv-name">{{ l.name }}</div>
              <div class="lv-req">累计消费满 ￥{{ ((l.upgradeConsume || 0) / 100).toFixed(0) }}</div>
              <div class="lv-disc">{{ (l.discount / 10).toFixed(1).replace('.0', '') }} 折</div>
              <div class="lv-priv">{{ l.privilege }}</div>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane :label="`我的收藏（${collects.length}）`" name="collect">
        <el-row :gutter="16">
          <el-col v-for="g in collects" :key="g.id" :span="6">
            <div class="g-card" @click="$router.push('/product/' + g.id)">
              <img :src="g.image" class="g-img" />
              <div class="g-name">{{ g.name }}</div>
              <div class="g-price">￥{{ ((g.minPrice || g.price || 0) / 100).toFixed(2) }}</div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="!collects.length" description="还没有收藏商品" />
      </el-tab-pane>

      <el-tab-pane :label="`浏览足迹（${footprints.length}）`" name="foot">
        <div class="foot-head">
          <el-button size="small" @click="clearFoot">清空足迹</el-button>
        </div>
        <el-row :gutter="16">
          <el-col v-for="g in footprints" :key="g.id" :span="6">
            <div class="g-card" @click="$router.push('/product/' + g.id)">
              <img :src="g.image" class="g-img" />
              <div class="g-name">{{ g.name }}</div>
              <div class="g-price">￥{{ ((g.minPrice || g.price || 0) / 100).toFixed(2) }}</div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-if="!footprints.length" description="暂无足迹" />
      </el-tab-pane>

      <el-tab-pane :label="`我的售后（${afters.length}）`" name="after">
        <el-table :data="afters" border size="small">
          <el-table-column prop="afterSaleSn" label="售后单号" width="200" />
          <el-table-column prop="goodsName" label="商品" min-width="180" />
          <el-table-column label="退款" width="110">
            <template #default="{ row }">{{ ((row.refundAmount || 0) / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="afterType(row.status)">{{ afterText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" min-width="180" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" link type="danger" @click="cancelAfter(row)">撤销</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!afters.length" description="没有售后记录" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  myLevel,
  levelList,
  walletBalance,
  walletLogs,
  collectList,
  footprintList,
  clearFootprint
} from '@/api/member'
import { afterMine, afterCancel } from '@/api/promo'
import { getMe } from '@/api/user'

const tab = ref('logs')
const level = ref({})
const levels = ref([])
const balance = ref(0)
const logs = ref([])
const collects = ref([])
const footprints = ref([])
const afters = ref([])
const me = ref({})

const typeText = (t) =>
  ({ 1: '充值', 2: '消费', 3: '提现', 4: '售后退款', 5: '分销佣金', 6: '后台调整' }[t] || '其他')
const afterText = (s) => ({ 0: '待处理', 1: '已同意', 2: '已拒绝', 3: '已退款', 4: '已完成' }[s] || '未知')
const afterType = (s) => ({ 0: 'warning', 1: 'primary', 2: 'danger', 3: 'success', 4: 'info' }[s] || 'info')

async function load() {
  const [lv, ls, bal, lg, col, fp, af, m] = await Promise.all([
    myLevel(), levelList(), walletBalance(), walletLogs(),
    collectList(), footprintList(), afterMine(), getMe()
  ])
  level.value = lv.data || {}
  levels.value = ls.data || []
  balance.value = bal.data || 0
  logs.value = lg.data || []
  collects.value = col.data || []
  footprints.value = fp.data || []
  afters.value = af.data || []
  me.value = m.data || {}
}

async function clearFoot() {
  await ElMessageBox.confirm('确定清空浏览足迹？', '提示', { type: 'warning' })
  await clearFootprint()
  ElMessage.success('已清空')
  load()
}

async function cancelAfter(row) {
  await ElMessageBox.confirm('确定撤销这笔售后申请？', '提示', { type: 'warning' })
  await afterCancel(row.id)
  ElMessage.success('已撤销')
  load()
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 20px 0 40px; }
.head h2 { margin: 0 0 18px; font-size: 22px; }
.top { display: grid; grid-template-columns: 1.6fr 1fr; gap: 16px; margin-bottom: 18px; }
.level-card { background: linear-gradient(135deg, #3a3f4b, #1f232b); color: #f6e0a8; border-radius: 12px; padding: 22px; display: flex; gap: 24px; }
.lc-label { font-size: 12px; opacity: .75; }
.lc-name { font-size: 26px; font-weight: 700; margin: 6px 0; }
.lc-priv { font-size: 12px; opacity: .8; }
.lc-right { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.lc-amount { font-size: 14px; }
.lc-next { font-size: 12px; opacity: .85; margin: 8px 0; }
.lc-bar { margin-top: 6px; }
.wallet-card { background: #fff; border-radius: 12px; padding: 22px; }
.wc-label { font-size: 12px; color: #999; }
.wc-amount { font-size: 30px; font-weight: 700; color: #ff2d2d; margin: 8px 0; }
.wc-sub { font-size: 12px; color: #888; }
.lnk { color: #ff2d2d; text-decoration: none; }
.tabs { background: #fff; border-radius: 12px; padding: 16px; }
.in { color: #2fa84f; }
.out { color: #ff2d2d; }
.lv-card { border: 1px solid #eee; border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.lv-card.on { border-color: #e6b450; background: #fffdf5; }
.lv-name { font-size: 16px; font-weight: 600; }
.lv-req { font-size: 12px; color: #999; margin-top: 6px; }
.lv-disc { font-size: 22px; color: #e6a23c; font-weight: 700; margin: 8px 0; }
.lv-priv { font-size: 12px; color: #777; }
.g-card { background: #fafbfc; border-radius: 10px; padding: 8px; margin-bottom: 12px; cursor: pointer; }
.g-img { width: 100%; height: 160px; object-fit: cover; border-radius: 8px; background: #f0f2f5; }
.g-name { font-size: 13px; margin-top: 8px; height: 36px; overflow: hidden; }
.g-price { color: #ff2d2d; font-weight: 700; margin-top: 4px; }
.foot-head { display: flex; justify-content: flex-end; margin-bottom: 10px; }
</style>
