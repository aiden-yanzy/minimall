<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="售后状态" clearable style="width: 160px" @change="load">
        <el-option label="待处理" :value="0" />
        <el-option label="已同意" :value="1" />
        <el-option label="已拒绝" :value="2" />
        <el-option label="已退款" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="afterSaleSn" label="售后单号" min-width="170" />
      <el-table-column prop="orderId" label="订单ID" width="90" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ typeMap[row.type] }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="160" />
      <el-table-column label="退款金额(分)" width="110">
        <template #default="{ row }">{{ row.refundAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="success" @click="approve(row)">同意退款</el-button>
            <el-button link type="danger" @click="reject(row)">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { afterPageApi, afterApproveApi, afterRejectApi } from '@/api/order'

const list = ref<any[]>([])
const loading = ref(false)
const query = reactive({ status: 0 as number | null })

const statusMap: Record<number, string> = { 0: '待处理', 1: '已同意', 2: '已拒绝', 3: '已退款', 4: '已取消' }
const typeMap: Record<number, string> = { 1: '仅退款', 2: '退货退款', 3: '换货' }
const statusType = (s: number) => ({ 0: 'warning', 1: 'primary', 2: 'danger', 3: 'success', 4: 'info' }[s] as any)

async function load() {
  loading.value = true
  try {
    const res: any = await afterPageApi({ ...query })
    list.value = res.data
  } finally {
    loading.value = false
  }
}

async function approve(row: any) {
  await ElMessageBox.confirm(`确定同意退款 ${row.refundAmount} 分？`, '提示', { type: 'warning' })
  await afterApproveApi(row.id)
  ElMessage.success('退款成功')
  load()
}

async function reject(row: any) {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝售后', { inputPattern: /\S+/, inputErrorMessage: '原因不能为空' })
  await afterRejectApi(row.id, value)
  ElMessage.success('已拒绝')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
</style>
