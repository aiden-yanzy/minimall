<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="手机号/昵称" style="width:200px" clearable @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:110px" @change="load">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="mobile" label="手机号" width="130" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="balance" label="余额(分)" width="100" />
      <el-table-column prop="integral" label="积分" width="90" />
      <el-table-column label="等级" width="110">
        <template #default="{ row }">
          <el-select size="small" :model-value="row.levelId" placeholder="未设置" @change="(v) => setLevel(row, v)">
            <el-option v-for="l in levels" :key="l.id" :label="l.name" :value="l.id" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top:12px; justify-content:flex-end"
      layout="total, prev, pager, next" :total="total" :page-size="query.size"
      v-model:current-page="query.page" @current-change="load" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userPageApi, userUpdateApi, levelListApi } from '@/api/user'

const list = ref<any[]>([])
const levels = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '', status: undefined as number | undefined })

async function load() {
  loading.value = true
  try {
    const res: any = await userPageApi({ ...query })
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}
async function setLevel(row: any, v: number) {
  await userUpdateApi(row.id, { levelId: v })
  ElMessage.success('等级已更新')
  load()
}
async function toggleStatus(row: any) {
  await userUpdateApi(row.id, { status: row.status === 1 ? 0 : 1 })
  load()
}
onMounted(async () => {
  const res: any = await levelListApi()
  levels.value = res.data
  load()
})
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
</style>