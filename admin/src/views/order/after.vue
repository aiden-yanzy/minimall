<template>
  <el-card>
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="0">待处理</el-radio-button>
        <el-radio-button :value="1">已同意</el-radio-button>
        <el-radio-button :value="3">已退款</el-radio-button>
        <el-radio-button :value="2">已拒绝</el-radio-button>
      </el-radio-group>
      <el-button type="primary" plain @click="load">刷新</el-button>
      <div class="grow"></div>
      <span class="dim">
        待处理 <b class="orange">{{ pendingCount }}</b> 单 · 累计退款 <b class="red">￥{{ (refundedAmount / 100).toFixed(2) }}</b>
      </span>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="afterSaleSn" label="售后单号" width="200" />
      <el-table-column label="商品" min-width="240">
        <template #default="{ row }">
          <div class="goods">
            <el-image :src="row.goodsImage" fit="cover" class="g-img" />
            <div class="g-info">
              <div class="g-name">{{ row.goodsName || '—' }}</div>
              <div class="g-spec">{{ row.specValueStr }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="买家" width="140">
        <template #default="{ row }">
          <div>{{ row.nickname || '—' }}</div>
          <div class="dim">{{ row.mobile }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="orderSn" label="订单号" width="190">
        <template #default="{ row }">{{ row.orderSn || ('#' + row.orderId) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.type === 1 ? 'warning' : 'info'">{{ typeText(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="退款金额" width="105">
        <template #default="{ row }">
          <span class="red">￥{{ ((row.refundAmount || 0) / 100).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="95">
        <template #default="{ row }">
          <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" width="155">
        <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 16) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="primary" @click="approve(row)">同意退款</el-button>
            <el-button link type="danger" @click="reject(row)">拒绝</el-button>
          </template>
          <span v-else-if="row.status === 3" class="dim">
            {{ row.refundSn }}<br />已退至买家余额
          </span>
          <span v-else class="dim">—</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { afterPageApi, afterApproveApi, afterRejectApi } from '@/api/order'

const list = ref<any[]>([])
const loading = ref(false)
const status = ref<number | undefined>(undefined)

const pendingCount = computed(() => list.value.filter((r) => r.status === 0).length)
const refundedAmount = computed(() =>
  list.value.filter((r) => r.status === 3).reduce((s, r) => s + (r.refundAmount || 0), 0)
)

// 后端口径：type 1仅退款 2退货退款 3换货；status 0待处理 1同意 2拒绝 3已退款 4已完成
function typeText(t: number) {
  return ({ 1: '仅退款', 2: '退货退款', 3: '换货' } as any)[t] ?? '售后'
}
function statusText(s: number) {
  return ({ 0: '待处理', 1: '已同意', 2: '已拒绝', 3: '已退款', 4: '已完成' } as any)[s] ?? '未知'
}
function statusType(s: number) {
  return ({ 0: 'warning', 1: 'primary', 2: 'danger', 3: 'success', 4: 'info' } as any)[s] ?? 'info'
}

async function load() {
  loading.value = true
  try {
    const res: any = await afterPageApi({ status: status.value })
    list.value = res.data || []
  } finally { loading.value = false }
}

async function approve(row: any) {
  await ElMessageBox.confirm(
    `同意退款 ￥${((row.refundAmount || 0) / 100).toFixed(2)}？将退回买家余额并恢复库存`,
    '确认退款',
    { type: 'warning' }
  )
  await afterApproveApi(row.id)
  ElMessage.success('已退款到买家余额')
  load()
}

async function reject(row: any) {
  const { value } = await ElMessageBox.prompt('请填写拒绝原因（买家可见）', '拒绝售后', {
    inputPlaceholder: '如：商品已拆封使用，不符合退款条件',
    inputPattern: /\S+/,
    inputErrorMessage: '原因不能为空'
  })
  await afterRejectApi(row.id, value)
  ElMessage.success('已拒绝')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 10px; align-items: center; }
.grow { flex: 1; }
.dim { color: #909399; font-size: 12px; }
.red { color: #f56c6c; }
.orange { color: #e6a23c; }
.goods { display: flex; gap: 8px; align-items: center; }
.g-img { width: 46px; height: 46px; border-radius: 4px; background: #f5f7fa; flex: 0 0 auto; }
.g-name { font-size: 13px; }
.g-spec { font-size: 12px; color: #909399; }
</style>
