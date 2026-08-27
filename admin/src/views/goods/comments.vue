<template>
  <el-card>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="goodsId" label="商品ID" width="80" />
      <el-table-column prop="score" label="评分" width="70" />
      <el-table-column prop="content" label="内容" min-width="260" />
      <el-table-column prop="reply" label="商家回复" min-width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isShow === 1 ? 'success' : 'info'">{{ row.isShow === 1 ? '显示' : '隐藏' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link :type="row.isShow === 1 ? 'warning' : 'success'" @click="toggle(row)">
            {{ row.isShow === 1 ? '隐藏' : '显示' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { commentListApi, commentShowApi } from '@/api/comment'

const list = ref<any[]>([])
const loading = ref(false)
async function load() {
  loading.value = true
  try {
    const res: any = await commentListApi()
    list.value = res.data
  } finally { loading.value = false }
}
async function toggle(row: any) {
  await commentShowApi(row.id, row.isShow === 1 ? 0 : 1)
  load()
}
onMounted(load)
</script>
