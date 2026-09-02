<template>
  <el-card>
    <div class="toolbar">
      <span class="title">角色与权限</span>
      <el-button type="primary" @click="open()" style="float:right">+ 新建角色</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="角色" min-width="120" />
      <el-table-column prop="code" label="编码" width="120" />
      <el-table-column prop="remark" label="备注" min-width="160" />
      <el-table-column label="菜单权限数" width="100">
        <template #default="{ row }">{{ row.menuIds.length }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }"><el-button link type="primary" @click="open(row)">编辑权限</el-button></template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="角色编辑" width="420px">
      <el-form label-width="80px">
        <el-form-item label="角色名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="菜单权限">
          <el-tree ref="treeRef" :data="treeData" show-checkbox node-key="id"
            :props="{ children: 'children', label: 'name' }" style="max-height:300px; overflow:auto" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'
import { roleListApi, menuTreeApi, roleSaveApi } from '@/api/system'

const list = ref<any[]>([])
const treeData = ref<any[]>([])
const loading = ref(false)
const dialog = ref(false)
const treeRef = ref<InstanceType<typeof ElTree>>()
const form = reactive<any>({ status: 1, menuIds: [] })

async function load() {
  loading.value = true
  try {
    const res: any = await roleListApi()
    list.value = res.data
  } finally { loading.value = false }
}
async function loadMenus() {
  const res: any = await menuTreeApi()
  const menus = res.data
  const byId: Record<number, any> = {}
  menus.forEach((m: any) => (byId[m.id] = { ...m, children: [] }))
  const roots: any[] = []
  menus.forEach((m: any) => {
    if (m.pid === 0) roots.push(byId[m.id])
    else if (byId[m.pid]) byId[m.pid].children.push(byId[m.id])
  })
  treeData.value = roots
}
async function open(row?: any) {
  Object.assign(form, row ? { ...row } : { id: null, name: '', code: '', remark: '', status: 1, menuIds: [] })
  dialog.value = true
  await nextTick()
  treeRef.value?.setCheckedKeys(row?.menuIds || [])
}
async function save() {
  const checked = treeRef.value?.getCheckedKeys() as number[]
  const half = treeRef.value?.getHalfCheckedKeys() as number[]
  await roleSaveApi({ role: { ...form }, menuIds: [...checked, ...half] })
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}
onMounted(() => { load(); loadMenus() })
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.title { font-weight: bold; }
</style>