<template>
  <el-card>
    <div class="toolbar">
      <span class="title">运费模板</span>
      <el-button type="primary" @click="open()" style="float:right">+ 新建模板</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column label="计费方式" width="100">
        <template #default="{ row }">{{ { 1: '按件', 2: '按重量', 3: '按体积' }[row.chargeType] }}</template>
      </el-table-column>
      <el-table-column label="免运费" width="90">
        <template #default="{ row }">{{ row.isFreeShipping === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="open(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑模板' : '新建模板'" width="560px">
      <el-form label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="计费方式">
          <el-radio-group v-model="form.chargeType">
            <el-radio :value="1">按件</el-radio>
            <el-radio :value="2">按重量</el-radio>
            <el-radio :value="3">按体积</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="免运费">
          <el-switch v-model="form.isFreeShipping" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-divider>计费规则（全国默认）</el-divider>
        <div v-for="(r, i) in form.rules" :key="i" class="rule-row">
          <el-input-number v-model="r.firstUnit" :min="1" size="small" placeholder="首件/首重" />
          <el-input-number v-model="r.firstPrice" :min="0" size="small" placeholder="首费(分)" />
          <el-input-number v-model="r.continueUnit" :min="1" size="small" placeholder="续件/续重" />
          <el-input-number v-model="r.continuePrice" :min="0" size="small" placeholder="续费(分)" />
          <el-button link type="danger" @click="form.rules.splice(i, 1)">删</el-button>
        </div>
        <el-button link type="primary" @click="form.rules.push({ firstUnit: 1, firstPrice: 500, continueUnit: 1, continuePrice: 200 })">+ 规则</el-button>
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
import { freightTemplatesApi, freightSaveApi, freightDeleteApi } from '@/api/freight'

const list = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive<any>({ chargeType: 1, isFreeShipping: 0, rules: [] })

async function load() {
  loading.value = true
  try {
    const res: any = await freightTemplatesApi()
    list.value = res.data
  } finally { loading.value = false }
}
async function open(row?: any) {
  if (row) Object.assign(form, { ...row })
  else Object.assign(form, { id: null, name: '', chargeType: 1, isFreeShipping: 0 })
  form.rules = [{ firstUnit: 1, firstPrice: 500, continueUnit: 1, continuePrice: 200 }]
  dialog.value = true
}
async function save() {
  if (!form.name) return ElMessage.warning('请输入模板名称')
  await freightSaveApi({ template: { ...form, rules: undefined }, rules: form.rules })
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
async function remove(row: any) {
  await ElMessageBox.confirm(`删除模板「${row.name}」？`, '提示', { type: 'warning' })
  await freightDeleteApi(row.id)
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.title { font-weight: bold; }
.rule-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
</style>