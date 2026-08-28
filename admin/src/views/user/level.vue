<template>
  <el-card>
    <div class="toolbar">
      <span class="title">会员等级</span>
      <el-button type="primary" @click="open()" style="float:right">+ 新建等级</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="level" label="级别" width="80" />
      <el-table-column label="折扣" width="120">
        <template #default="{ row }">{{ row.discount }} 折</template>
      </el-table-column>
      <el-table-column prop="upgradeConsume" label="升级门槛(分)" width="130" />
      <el-table-column prop="privilege" label="权益" min-width="140" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="open(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <p style="color:#999;font-size:12px">会员等级折扣会参与下单自动计算（9.5 = 95折）</p>

    <el-dialog v-model="dialog" :title="form.id ? '编辑等级' : '新建等级'" width="440px">
      <el-form label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="级别值"><el-input-number v-model="form.level" :min="1" /></el-form-item>
        <el-form-item label="折扣"><el-input-number v-model="form.discount" :min="1" :max="10" :precision="2" :step="0.5" /></el-form-item>
        <el-form-item label="升级门槛(分)"><el-input-number v-model="form.upgradeConsume" :min="0" /></el-form-item>
        <el-form-item label="权益"><el-input v-model="form.privilege" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { levelListApi, levelSaveApi, levelRemoveApi } from '@/api/user'

const list = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive<any>({})

async function load() {
  loading.value = true
  try {
    const res: any = await levelListApi()
    list.value = res.data
  } finally { loading.value = false }
}
function open(row?: any) {
  Object.assign(form, row ? { ...row } : { name: '', level: 1, discount: 9.5, upgradeConsume: 0, privilege: '', status: 1 })
  dialog.value = true
}
async function save() {
  if (!form.name) return ElMessage.warning('请输入名称')
  await levelSaveApi({ ...form })
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
async function remove(row: any) {
  await ElMessageBox.confirm(`删除「${row.name}」？`, '提示', { type: 'warning' })
  await levelRemoveApi(row.id)
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.title { font-weight: bold; }
</style>