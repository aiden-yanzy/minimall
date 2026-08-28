<template>
  <div>
    <el-tabs v-model="tab">
      <el-tab-pane label="品牌" name="brand">
        <el-card>
          <el-button type="primary" @click="openBrand()" style="margin-bottom:12px">+ 新建品牌</el-button>
          <el-table :data="brands" border stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="名称" min-width="160" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }"><el-tag :type="row.isShow === 1 ? 'success' : 'info'">{{ row.isShow === 1 ? '显示' : '隐藏' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button link type="primary" @click="openBrand(row)">编辑</el-button>
                <el-button link type="danger" @click="removeBrand(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="商品单位" name="unit">
        <el-card>
          <div style="display:flex; gap:8px; margin-bottom:12px">
            <el-input v-model="newUnit" placeholder="单位名，如 双/箱" style="width:220px" />
            <el-button type="primary" @click="addUnit">新增</el-button>
          </div>
          <el-table :data="units" border stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="单位" min-width="160" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }"><el-button link type="danger" @click="removeUnit(row)">删除</el-button></template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="brandDialog" title="品牌" width="400px">
      <el-form label-width="70px">
        <el-form-item label="名称"><el-input v-model="brandForm.name" /></el-form-item>
        <el-form-item label="Logo URL"><el-input v-model="brandForm.logo" placeholder="可留空" /></el-form-item>
        <el-form-item label="显示"><el-switch v-model="brandForm.isShow" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="brandDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBrand">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { brandPageApi, brandSaveApi, brandDelApi, unitListApi, unitSaveApi, unitDelApi } from '@/api/finance'

const tab = ref('brand')
const brands = ref<any[]>([])
const units = ref<any[]>([])
const brandDialog = ref(false)
const brandForm = reactive<any>({ isShow: 1 })
const newUnit = ref('')

async function loadBrands() {
  const res: any = await brandPageApi({ page: 1, size: 50 })
  brands.value = res.data.records
}
async function loadUnits() {
  const res: any = await unitListApi()
  units.value = res.data
}
function openBrand(row?: any) {
  Object.assign(brandForm, row ? { ...row } : { id: null, name: '', logo: '', isShow: 1 })
  brandDialog.value = true
}
async function saveBrand() {
  await brandSaveApi({ ...brandForm })
  ElMessage.success('保存成功')
  brandDialog.value = false
  loadBrands()
}
async function removeBrand(row: any) {
  await ElMessageBox.confirm(`删除「${row.name}」？`, '提示', { type: 'warning' })
  await brandDelApi(row.id)
  loadBrands()
}
async function addUnit() {
  if (!newUnit.value) return ElMessage.warning('请输入单位名')
  await unitSaveApi({ name: newUnit.value })
  newUnit.value = ''
  loadUnits()
}
async function removeUnit(row: any) {
  await unitDelApi(row.id)
  loadUnits()
}
onMounted(() => { loadBrands(); loadUnits() })
</script>