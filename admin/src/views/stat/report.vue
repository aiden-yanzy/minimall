<template>
  <div>
    <el-card>
      <div class="head">
        <el-radio-group v-model="days" @change="reload">
          <el-radio-button :value="7">近 7 天</el-radio-button>
          <el-radio-button :value="30">近 30 天</el-radio-button>
          <el-radio-button :value="90">近 90 天</el-radio-button>
        </el-radio-group>
        <span class="muted">数据口径：已支付订单（pay_status=1）</span>
      </div>

      <el-tabs v-model="tab">
        <!-- 概览 -->
        <el-tab-pane label="销售概览" name="overview">
          <div class="cards" v-loading="loadingOverview">
            <div class="metric">
              <div class="label">销售额</div>
              <div class="value">¥{{ yuan(overview?.summary?.amount) }}</div>
              <div class="sub">已支付订单合计</div>
            </div>
            <div class="metric">
              <div class="label">订单数</div>
              <div class="value">{{ overview?.summary?.orders || 0 }}</div>
              <div class="sub">下单 {{ overview?.funnel?.orders || 0 }} 单 / 支付率 {{ payRate }}%</div>
            </div>
            <div class="metric">
              <div class="label">客单价</div>
              <div class="value">¥{{ yuan(overview?.summary?.avgAmount) }}</div>
              <div class="sub">销售额 ÷ 订单数</div>
            </div>
            <div class="metric">
              <div class="label">成交用户</div>
              <div class="value">{{ overview?.summary?.buyers || 0 }}</div>
              <div class="sub">去重买家数</div>
            </div>
          </div>

          <el-divider content-position="left">每日销售额趋势</el-divider>
          <div class="chart" v-loading="loadingOverview">
            <div class="bars">
              <div v-for="d in overview?.daily || []" :key="d.day" class="bar-col" :title="`${d.day}　¥${yuan(d.amount)}　${d.orders} 单`">
                <div class="bar" :style="{ height: barHeight(d.amount) }"></div>
                <div class="bar-x">{{ d.day.slice(5) }}</div>
              </div>
            </div>
            <el-empty v-if="!(overview?.daily || []).length" description="暂无数据" :image-size="60" />
          </div>
        </el-tab-pane>

        <!-- 明细 -->
        <el-tab-pane label="销售明细" name="detail">
          <div class="toolbar">
            <el-date-picker
              v-model="range"
              type="daterange"
              value-format="YYYY-MM-DD"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 260px"
            />
            <el-select v-model="detailQuery.status" placeholder="订单状态" clearable style="width: 130px">
              <el-option label="待支付" :value="0" />
              <el-option label="待发货" :value="1" />
              <el-option label="待收货" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
            </el-select>
            <el-button type="primary" @click="loadDetail(1)">查询</el-button>
          </div>
          <el-table :data="detail.records" v-loading="loadingDetail" border stripe>
            <el-table-column prop="orderSn" label="订单号" min-width="170" />
            <el-table-column prop="userId" label="用户" width="80" />
            <el-table-column label="件数" width="80">
              <template #default="{ row }">{{ row.nums }}</template>
            </el-table-column>
            <el-table-column label="商品总额" width="110">
              <template #default="{ row }">¥{{ yuan(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column label="实付" width="110">
              <template #default="{ row }"><span class="money">¥{{ yuan(row.payAmount) }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ statusMap[row.orderStatus] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="下单时间" min-width="160">
              <template #default="{ row }">{{ fmt(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="支付时间" min-width="160">
              <template #default="{ row }">{{ row.payStatus === 1 ? fmt(row.payTime) : '—' }}</template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            background
            layout="total, prev, pager, next"
            :total="detail.total"
            :page-size="detail.size"
            :current-page="detail.page"
            @current-change="loadDetail"
          />
        </el-tab-pane>

        <!-- 排行 -->
        <el-tab-pane label="商品排行" name="rank">
          <el-table :data="rank" v-loading="loadingRank" border stripe>
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="goodsName" label="商品" min-width="220" />
            <el-table-column label="销量" width="100">
              <template #default="{ row }">{{ row.nums }}</template>
            </el-table-column>
            <el-table-column label="销售额" width="140">
              <template #default="{ row }"><span class="money">¥{{ yuan(row.amount) }}</span></template>
            </el-table-column>
            <el-table-column label="占比" min-width="220">
              <template #default="{ row }">
                <div class="rank-bar">
                  <div class="rank-fill" :style="{ width: ratio(row.amount) }"></div>
                  <span class="rank-pct">{{ ratio(row.amount) }}</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!rank.length && !loadingRank" description="区间内暂无成交" :image-size="70" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { statOverviewApi, statSaleDetailApi, statSaleRankApi } from '@/api/order'

const days = ref(30)
const tab = ref('overview')
const loadingOverview = ref(false)
const loadingDetail = ref(false)
const loadingRank = ref(false)

const overview = ref<any>(null)
const rank = ref<any[]>([])
const range = ref<[string, string] | null>(null)

const detail = reactive<{ records: any[]; total: number; page: number; size: number }>({
  records: [],
  total: 0,
  page: 1,
  size: 10
})
const detailQuery = reactive<{ status: number | null }>({ status: null })

const statusMap: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const yuan = (fen: any) => ((Number(fen) || 0) / 100).toFixed(2)
const fmt = (v: any) => (v ? String(v).replace('T', ' ').slice(0, 19) : '—')

const maxAmount = computed(() => Math.max(1, ...(overview.value?.daily || []).map((d: any) => Number(d.amount) || 0)))
const barHeight = (amount: any) => {
  const pct = Math.round(((Number(amount) || 0) / maxAmount.value) * 100)
  return Math.max(pct, amount > 0 ? 4 : 1) + '%'
}
const payRate = computed(() => {
  const o = Number(overview.value?.funnel?.orders) || 0
  const p = Number(overview.value?.summary?.orders) || 0
  return o === 0 ? 0 : Math.round((p / o) * 100)
})
const rankMax = computed(() => Math.max(1, ...rank.value.map((r: any) => Number(r.amount) || 0)))
const ratio = (amount: any) => {
  const pct = Math.round(((Number(amount) || 0) / rankMax.value) * 100)
  return Math.max(pct, 2) + '%'
}

async function loadOverview() {
  loadingOverview.value = true
  try {
    const res: any = await statOverviewApi(days.value)
    overview.value = res.data
  } finally {
    loadingOverview.value = false
  }
}
async function loadDetail(page = 1) {
  loadingDetail.value = true
  try {
    const res: any = await statSaleDetailApi({
      page,
      size: detail.size,
      status: detailQuery.status ?? undefined,
      start: range.value?.[0],
      end: range.value?.[1]
    })
    Object.assign(detail, res.data)
  } finally {
    loadingDetail.value = false
  }
}
async function loadRank() {
  loadingRank.value = true
  try {
    const res: any = await statSaleRankApi(days.value, 10)
    rank.value = res.data || []
  } finally {
    loadingRank.value = false
  }
}
function reload() {
  loadOverview()
  loadRank()
  loadDetail(1)
}

watch(tab, (t) => {
  if (t === 'detail' && !detail.records.length) loadDetail(1)
  if (t === 'rank' && !rank.value.length) loadRank()
})

onMounted(() => {
  loadOverview()
  loadRank()
  loadDetail(1)
})
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.metric {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px 18px;
  background: linear-gradient(180deg, #fbfdff 0%, #fff 100%);
}
.metric .label {
  font-size: 13px;
  color: #909399;
}
.metric .value {
  font-size: 26px;
  font-weight: 700;
  color: #1f2d3d;
  margin: 6px 0 4px;
}
.metric .sub {
  font-size: 12px;
  color: #a8abb2;
}
.chart {
  height: 260px;
  padding: 8px 4px 0;
}
.bars {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 210px;
  overflow-x: auto;
}
.bar-col {
  flex: 1 0 18px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  height: 100%;
}
.bar {
  width: 70%;
  min-height: 2px;
  background: linear-gradient(180deg, #5b8ff9 0%, #3d6be0 100%);
  border-radius: 3px 3px 0 0;
  transition: height 0.25s;
}
.bar-x {
  font-size: 11px;
  color: #a8abb2;
  margin-top: 4px;
  white-space: nowrap;
}
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.money {
  color: #e4393c;
  font-weight: 600;
}
.muted {
  color: #909399;
  font-size: 13px;
}
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
.rank-bar {
  position: relative;
  height: 16px;
  background: #f2f4f8;
  border-radius: 8px;
  overflow: hidden;
}
.rank-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffb35c 0%, #ff7a45 100%);
  border-radius: 8px;
}
.rank-pct {
  position: absolute;
  right: 8px;
  top: 0;
  font-size: 11px;
  line-height: 16px;
  color: #6b7280;
}
</style>
