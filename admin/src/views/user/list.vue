<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="手机号/昵称" style="width:200px" clearable @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:110px" @change="load">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-select v-model="query.tagId" placeholder="按标签筛选" clearable style="width:150px" @change="load">
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <div class="grow"></div>
      <el-button @click="openTagManage">标签管理</el-button>
      <el-button :loading="refreshing" @click="refreshAllLevels">按成长值重算等级</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="mobile" label="手机号" width="130" />
      <el-table-column prop="nickname" label="昵称" min-width="110" />
      <el-table-column label="标签" min-width="170">
        <template #default="{ row }">
          <div class="tag-cell">
            <el-tag
              v-for="t in row.tags || []"
              :key="t.id"
              size="small"
              :style="{ background: t.color, borderColor: t.color, color: '#fff' }"
            >{{ t.name }}</el-tag>
            <el-button link type="primary" size="small" @click="openTagPick(row)">
              {{ (row.tags || []).length ? '改' : '+ 打标' }}
            </el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="累计消费(元)" width="110">
        <template #default="{ row }">{{ ((row.totalConsume || 0) / 100).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="totalOrderNum" label="订单数" width="80" />
      <el-table-column prop="balance" label="余额(分)" width="95" />
      <el-table-column prop="integral" label="积分" width="80" />
      <el-table-column label="等级" width="150">
        <template #default="{ row }">
          <el-select size="small" :model-value="row.levelId" placeholder="未设置" @change="(v) => setLevel(row, v)">
            <el-option v-for="l in levels" :key="l.id" :label="l.name" :value="l.id" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110">
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

    <!-- 给用户打标签 -->
    <el-dialog v-model="tagDlg" :title="`打标签 · ${current?.nickname || ''}`" width="420px">
      <el-checkbox-group v-model="pickedTags">
        <el-checkbox v-for="t in tags" :key="t.id" :value="t.id" :label="t.id">
          <el-tag size="small" :style="{ background: t.color, borderColor: t.color, color: '#fff' }">{{ t.name }}</el-tag>
        </el-checkbox>
      </el-checkbox-group>
      <el-empty v-if="!tags.length" description="还没有标签，先建一个" :image-size="50" />
      <template #footer>
        <el-button @click="tagDlg = false">取消</el-button>
        <el-button type="primary" @click="saveUserTags">保存</el-button>
      </template>
    </el-dialog>

    <!-- 标签管理 -->
    <el-dialog v-model="tagMgrDlg" title="标签管理" width="560px">
      <el-form inline>
        <el-form-item label="名称">
          <el-input v-model="tagForm.name" style="width:120px" placeholder="如 高价值" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="tagForm.color" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="tagForm.sort" :min="0" :max="99" size="small" />
        </el-form-item>
        <el-button type="primary" @click="saveTag">{{ tagForm.id ? '更新' : '新增' }}</el-button>
        <el-button v-if="tagForm.id" @click="resetTagForm">取消编辑</el-button>
      </el-form>
      <el-table :data="tags" border size="small">
        <el-table-column label="标签" width="140">
          <template #default="{ row }">
            <el-tag size="small" :style="{ background: row.color, borderColor: row.color, color: '#fff' }">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button link type="primary" @click="editTag(row)">编辑</el-button>
            <el-button link type="danger" @click="removeTag(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  userPageApi,
  userUpdateApi,
  levelListApi,
  tagListApi,
  tagSaveApi,
  tagRemoveApi,
  userTagsApi,
  setUserTagsApi,
  refreshLevelsApi
} from '@/api/user'

const list = ref<any[]>([])
const levels = ref<any[]>([])
const tags = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const refreshing = ref(false)
const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined,
  tagId: undefined as number | undefined
})

const tagDlg = ref(false)
const current = ref<any>(null)
const pickedTags = ref<number[]>([])

const tagMgrDlg = ref(false)
const tagForm = reactive<any>({ id: null, name: '', color: '#409eff', sort: 0, status: 1 })

async function load() {
  loading.value = true
  try {
    const res: any = await userPageApi({ ...query })
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}
async function loadTags() {
  const res: any = await tagListApi()
  tags.value = res.data || []
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
async function refreshAllLevels() {
  refreshing.value = true
  try {
    const res: any = await refreshLevelsApi()
    ElMessage.success(`重算完成，等级变动 ${res.data} 人`)
    load()
  } finally { refreshing.value = false }
}

async function openTagPick(row: any) {
  current.value = row
  const res: any = await userTagsApi(row.id)
  pickedTags.value = res.data || []
  tagDlg.value = true
}
async function saveUserTags() {
  await setUserTagsApi(current.value.id, pickedTags.value)
  ElMessage.success('标签已更新')
  tagDlg.value = false
  load()
}

function openTagManage() {
  resetTagForm()
  tagMgrDlg.value = true
}
function resetTagForm() {
  Object.assign(tagForm, { id: null, name: '', color: '#409eff', sort: 0, status: 1 })
}
function editTag(row: any) {
  Object.assign(tagForm, { ...row })
}
async function saveTag() {
  if (!tagForm.name?.trim()) return ElMessage.warning('请填写标签名称')
  await tagSaveApi({ ...tagForm, name: tagForm.name.trim() })
  ElMessage.success('已保存')
  resetTagForm()
  loadTags()
}
async function removeTag(row: any) {
  await ElMessageBox.confirm(`删除标签「${row.name}」？该标签会从所有会员身上移除`, '提示', { type: 'warning' })
  await tagRemoveApi(row.id)
  ElMessage.success('已删除')
  loadTags()
  load()
}

onMounted(async () => {
  const res: any = await levelListApi()
  levels.value = res.data
  await loadTags()
  load()
})
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; align-items: center; }
.grow { flex: 1; }
.tag-cell { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
</style>
