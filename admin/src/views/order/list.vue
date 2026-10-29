<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 140px" @change="load">
          <el-option label="待支付" :value="0" />
          <el-option label="待发货" :value="1" />
          <el-option label="待收货" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-input v-model="query.orderSn" placeholder="订单号" style="width: 220px" clearable @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button v-if="auth.can('order:deliver')" :disabled="!selectedIds.length" @click="batchDialog = true">
          批量发货{{ selectedIds.length ? `（${selectedIds.length}）` : '' }}
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe @selection-change="onSelect">
        <el-table-column type="selection" width="46" />
        <el-table-column prop="orderSn" label="订单号" min-width="170" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div v-for="g in row.goodsList" :key="g.id">{{ g.goodsName }} × {{ g.num }}</div>
          </template>
        </el-table-column>
        <el-table-column label="实付" width="110">
          <template #default="{ row }">
            <span class="money">¥{{ ((row.payAmount || 0) / 100).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收货人" width="130">
          <template #default="{ row }">{{ row.contact }} {{ row.mobile }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.orderStatus)">{{ statusMap[row.orderStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物流" width="170">
          <template #default="{ row }">
            <span v-if="row.expressNo">{{ row.expressName }} {{ row.expressNo }}</span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.orderStatus === 1 && auth.can('order:deliver')" link type="primary" @click="openDeliver(row)">发货</el-button>
            <el-button v-if="row.orderStatus === 0 && auth.can('order:edit')" link type="warning" @click="openPrice(row)">改价</el-button>
            <el-button link type="primary" @click="openTraces(row)">物流轨迹</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发货 -->
    <el-dialog v-model="deliverVisible" title="订单发货" width="440px">
      <el-form label-width="86px">
        <el-form-item label="订单号"><span>{{ current?.orderSn }}</span></el-form-item>
        <el-form-item label="快递公司">
          <el-select v-model="deliver.expressId" placeholder="选择快递" style="width: 100%">
            <el-option v-for="e in expresses" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input v-model="deliver.expressNo" placeholder="输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitDeliver">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 批量发货 -->
    <el-dialog v-model="batchDialog" title="批量发货" width="440px">
      <el-form label-width="86px">
        <el-form-item label="快递公司">
          <el-select v-model="batch.expressId" style="width: 100%">
            <el-option v-for="e in expresses" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单号">
          <el-input v-model="batch.expressNo" placeholder="留空则按前缀自动生成（每单一号）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBatch">
          确认发货 {{ selectedIds.length }} 单
        </el-button>
      </template>
    </el-dialog>

    <!-- 改价 -->
    <el-dialog v-model="priceVisible" title="订单改价" width="440px">
      <el-form label-width="86px">
        <el-form-item label="订单号"><span>{{ current?.orderSn }}</span></el-form-item>
        <el-form-item label="原实付">
          <span class="muted">¥{{ ((current?.payAmount || 0) / 100).toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="改后金额">
          <el-input v-model="price.amount" placeholder="单位：元，例如 88.00">
            <template #append>元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="改价原因">
          <el-input v-model="price.reason" type="textarea" :rows="2" placeholder="必填，便于审计（会写入物流轨迹）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="priceVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPrice">确认改价</el-button>
      </template>
    </el-dialog>

    <!-- 物流轨迹 -->
    <el-drawer v-model="traceVisible" title="物流轨迹" size="520px">
      <div class="trace-head">
        <div>
          <div class="sn">{{ current?.orderSn }}</div>
          <div class="muted">
            {{ current?.expressName || '未发货' }}
            <span v-if="current?.expressNo"> · {{ current.expressNo }}</span>
          </div>
        </div>
        <div>
          <el-button size="small" :disabled="!current || current.orderStatus < 2" :loading="submitting" @click="advanceTrace">
            模拟推进一步
          </el-button>
        </div>
      </div>

      <el-divider content-position="left">轨迹记录</el-divider>
      <el-timeline v-if="traces.length">
        <el-timeline-item v-for="(t, i) in traces" :key="i" :timestamp="fmt(t.time)" placement="top">
          {{ t.desc }}
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无轨迹" :image-size="70" />

      <el-divider content-position="left">手动追加</el-divider>
      <div class="trace-add">
        <el-input v-model="newTrace" placeholder="例如：包裹已到达【杭州转运中心】" />
        <el-button type="primary" :loading="submitting" @click="addTrace">添加</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import {
  orderPageApi,
  orderDeliverApi,
  orderBatchDeliverApi,
  orderTracesApi,
  orderAddTraceApi,
  orderAdvanceTraceApi,
  orderChangePriceApi
} from '@/api/order'

const auth = useAuthStore()

const expresses = [
  { id: 1, name: '顺丰速运' },
  { id: 2, name: '圆通速递' },
  { id: 3, name: '中通快递' },
  { id: 4, name: '韵达快递' }
]
const expressName = (id: number) => expresses.find((e) => e.id === id)?.name || '快递'

const list = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)
const selectedIds = ref<number[]>([])
const query = reactive({ status: 1 as number | null, orderSn: '' })

const statusMap: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const statusType = (s: number) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' }[s] as any)
const fmt = (v: any) => (v ? String(v).replace('T', ' ').slice(0, 19) : '')

const current = ref<any>(null)
const deliverVisible = ref(false)
const deliver = reactive({ expressId: 1, expressNo: '' })
const batchDialog = ref(false)
const batch = reactive({ expressId: 1, expressNo: '' })
const priceVisible = ref(false)
const price = reactive({ amount: '', reason: '' })
const traceVisible = ref(false)
const traces = ref<any[]>([])
const newTrace = ref('')

async function load() {
  loading.value = true
  try {
    const res: any = await orderPageApi({ ...query })
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}
function onSelect(rows: any[]) {
  selectedIds.value = rows.filter((r) => r.orderStatus === 1).map((r) => r.id)
}

function openDeliver(row: any) {
  current.value = row
  deliver.expressId = 1
  deliver.expressNo = ''
  deliverVisible.value = true
}
async function submitDeliver() {
  submitting.value = true
  try {
    await orderDeliverApi(current.value.id, {
      expressId: deliver.expressId,
      expressName: expressName(deliver.expressId),
      expressNo: deliver.expressNo
    })
    ElMessage.success('发货成功')
    deliverVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function submitBatch() {
  submitting.value = true
  try {
    const res: any = await orderBatchDeliverApi(
      selectedIds.value,
      batch.expressId,
      expressName(batch.expressId),
      batch.expressNo
    )
    ElMessage.success(res.msg || '批量发货完成')
    batchDialog.value = false
    load()
  } finally {
    submitting.value = false
  }
}

function openPrice(row: any) {
  current.value = row
  price.amount = ((row.payAmount || 0) / 100).toFixed(2)
  price.reason = ''
  priceVisible.value = true
}
async function submitPrice() {
  const yuan = Number(price.amount)
  if (!Number.isFinite(yuan) || yuan < 0) return ElMessage.warning('请输入合法金额')
  if (!price.reason.trim()) return ElMessage.warning('改价原因必填')
  submitting.value = true
  try {
    await orderChangePriceApi(current.value.id, Math.round(yuan * 100), price.reason.trim())
    ElMessage.success('改价成功')
    priceVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function openTraces(row: any) {
  current.value = row
  newTrace.value = ''
  traceVisible.value = true
  await loadTraces()
}
async function loadTraces() {
  const res: any = await orderTracesApi(current.value.id)
  traces.value = (res.data || []).slice().reverse()
}
async function addTrace() {
  if (!newTrace.value.trim()) return ElMessage.warning('请输入轨迹内容')
  submitting.value = true
  try {
    await orderAddTraceApi(current.value.id, newTrace.value.trim())
    newTrace.value = ''
    await loadTraces()
    ElMessage.success('已添加')
  } finally {
    submitting.value = false
  }
}
async function advanceTrace() {
  submitting.value = true
  try {
    const res: any = await orderAdvanceTraceApi(current.value.id)
    ElMessage.success(res.data || '已推进')
    await loadTraces()
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.money {
  font-weight: 600;
  color: #e4393c;
}
.muted {
  color: #909399;
}
.trace-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.trace-head .sn {
  font-weight: 600;
}
.trace-add {
  display: flex;
  gap: 8px;
}
</style>
