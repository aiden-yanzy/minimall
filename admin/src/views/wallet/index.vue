<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="load">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
        <el-option label="已打款" :value="3" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="amount" label="金额(分)" width="110" />
      <el-table-column label="方式" width="90">
        <template #default="{ row }">{{ { 1: '微信', 2: '银行卡', 3: '余额' }[row.type] }}</template>
      </el-table-column>
      <el-table-column prop="accountInfo" label="收款账户" min-width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="success" @click="approve(row)">同意打款</el-button>
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
import { withdrawListApi, withdrawApproveApi, withdrawRejectApi } from '@/api/wallet'

const list = ref<any[]>([])
const loading = ref(false)
const query = reactive({ status: 0 as number | null })
const statusMap: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已打款' }
const statusType = (s: number) => ({ 0: 'warning', 1: 'primary', 2: 'danger', 3: 'success' }[s] as any)

async function load() {
  loading.value = true
  try {
    const res: any = await withdrawListApi(query.status)
    list.value = res.data
  } finally {
    loading.value = false
  }
}
async function approve(row: any) {
  await ElMessageBox.confirm(`确认向用户${row.userId}打款 ${row.amount} 分？`, '提示', { type: 'warning' })
  await withdrawApproveApi(row.id)
  ElMessage.success('已打款')
  load()
}
async function reject(row: any) {
  const { value } = await ElMessageBox.prompt('拒绝原因', '拒绝提现', { inputPattern: /\S+/, inputErrorMessage: '原因不能为空' })
  await withdrawRejectApi(row.id, value)
  ElMessage.success('已拒绝，余额退回')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
</style>