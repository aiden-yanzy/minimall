<template>
  <el-card>
    <el-tabs v-model="tab">
      <el-tab-pane label="账户流水" name="logs">
        <div class="toolbar">
          <el-select v-model="logQuery.type" placeholder="类型" clearable style="width:130px" @change="loadLogs">
            <el-option label="充值" :value="1" />
            <el-option label="消费" :value="2" />
            <el-option label="退款" :value="3" />
            <el-option label="提现" :value="4" />
            <el-option label="佣金" :value="5" />
            <el-option label="签到" :value="6" />
          </el-select>
          <el-button type="primary" @click="loadLogs">查询</el-button>
        </div>
        <el-table :data="logs" v-loading="loadingLogs" border stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userId" label="用户ID" width="90" />
          <el-table-column label="类型" width="90">
            <template #default="{ row }">{{ typeMap[row.type] }}</template>
          </el-table-column>
          <el-table-column label="变动(分)" width="110">
            <template #default="{ row }">
              <span :class="row.changeAmount > 0 ? 'plus' : 'minus'">{{ row.changeAmount > 0 ? '+' : '' }}{{ row.changeAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="balanceAfter" label="变动后余额" width="110" />
          <el-table-column prop="remark" label="备注" min-width="180" />
          <el-table-column prop="createTime" label="时间" min-width="150" />
        </el-table>
        <el-pagination style="margin-top:12px; justify-content:flex-end"
          layout="total, prev, pager, next" :total="logTotal" :page-size="logQuery.size"
          v-model:current-page="logQuery.page" @current-change="loadLogs" />
      </el-tab-pane>

      <el-tab-pane label="分销佣金" name="comm">
        <el-table :data="comms" v-loading="loadingComms" border stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="buyerUserId" label="买家ID" width="90" />
          <el-table-column prop="sellerUserId" label="获佣用户" width="100" />
          <el-table-column prop="goodsAmount" label="商品实付(分)" width="110" />
          <el-table-column label="费率" width="80"><template #default="{ row }">{{ (row.rate / 10) }}%</template></el-table-column>
          <el-table-column prop="commission" label="佣金(分)" width="100" />
          <el-table-column prop="orderId" label="订单ID" width="90" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已结算' : '未结算' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="150" />
        </el-table>
        <el-pagination style="margin-top:12px; justify-content:flex-end"
          layout="total, prev, pager, next" :total="commTotal" :page-size="commQuery.size"
          v-model:current-page="commQuery.page" @current-change="loadComms" />
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { financeLogsApi, financeCommissionsApi } from '@/api/finance'

const tab = ref('logs')
const typeMap: Record<number, string> = { 1: '充值', 2: '消费', 3: '退款', 4: '提现', 5: '佣金', 6: '签到', 99: '其他' }
const logs = ref<any[]>([])
const comms = ref<any[]>([])
const logTotal = ref(0)
const commTotal = ref(0)
const loadingLogs = ref(false)
const loadingComms = ref(false)
const logQuery = reactive({ page: 1, size: 15, type: undefined as number | undefined })
const commQuery = reactive({ page: 1, size: 15 })

async function loadLogs() {
  loadingLogs.value = true
  try {
    const res: any = await financeLogsApi({ ...logQuery })
    logs.value = res.data.records
    logTotal.value = res.data.total
  } finally { loadingLogs.value = false }
}
async function loadComms() {
  loadingComms.value = true
  try {
    const res: any = await financeCommissionsApi({ ...commQuery })
    comms.value = res.data.records
    commTotal.value = res.data.total
  } finally { loadingComms.value = false }
}
onMounted(() => { loadLogs(); loadComms() })
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
.plus { color: #07c160; }
.minus { color: #ff2d2d; }
</style>