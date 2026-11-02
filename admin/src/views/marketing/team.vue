<template>
  <el-card>
    <el-tabs v-model="tab">
      <!-- 活动配置 -->
      <el-tab-pane label="拼团活动" name="acts">
        <div class="toolbar">
          <el-button type="primary" size="small" @click="openCreate">+ 新建拼团</el-button>
          <div class="grow"></div>
          <el-button size="small" :loading="scanning" @click="scan">手动扫描超时团并退款</el-button>
        </div>
        <el-table :data="list" v-loading="loading" border stripe size="small">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="活动名" min-width="140" />
          <el-table-column label="商品" min-width="180">
            <template #default="{ row }">
              <div>{{ row.goodsName || ('#' + row.goodsId) }}</div>
              <div class="dim">{{ row.specValueStr }}</div>
            </template>
          </el-table-column>
          <el-table-column label="拼团价" width="100">
            <template #default="{ row }"><span class="red">￥{{ (row.teamPrice / 100).toFixed(2) }}</span></template>
          </el-table-column>
          <el-table-column prop="teamNum" label="成团人数" width="90" />
          <el-table-column label="活动时间" width="180">
            <template #default="{ row }">
              <div>{{ (row.startTime || '').slice(0, 16).replace('T', ' ') }}</div>
              <div class="dim">{{ (row.endTime || '').slice(0, 16).replace('T', ' ') }}</div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 团列表 -->
      <el-tab-pane label="开团记录" name="founds">
        <el-table :data="founds" v-loading="loadingF" border stripe size="small">
          <el-table-column prop="id" label="团ID" width="70" />
          <el-table-column prop="activityName" label="活动" min-width="140" />
          <el-table-column prop="leaderName" label="团长" width="120" />
          <el-table-column label="进度" width="120">
            <template #default="{ row }">
              <span :class="{ ok: row.status === 1 }">{{ row.joinNum }}/{{ row.needNum }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : (row.status === 2 ? 'danger' : 'warning')">
                {{ row.status === 1 ? '已成团' : (row.status === 2 ? '失败退款' : '进行中') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="超时时间" width="170">
            <template #default="{ row }">{{ (row.expireTime || '').slice(0, 16).replace('T', ' ') }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!founds.length" description="还没有开团记录" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dlg" :title="form.id ? '编辑拼团' : '新建拼团'" width="560px">
      <el-form label-width="100px">
        <el-form-item label="活动名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="商品">
          <el-select v-model="form.goodsId" filterable placeholder="选择商品" style="width: 100%" @change="onGoodsChange">
            <el-option v-for="g in goods" :key="g.id" :label="`${g.name}（￥${(g.minPrice / 100).toFixed(2)}）`" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="SKU">
          <el-select v-model="form.goodsItemId" placeholder="选择规格" style="width: 100%">
            <el-option v-for="it in items" :key="it.id" :label="`${it.specValueStr || '默认'}（￥${(it.price / 100).toFixed(2)} 库存${it.stock}）`" :value="it.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="拼团价(元)">
          <el-input v-model="form.teamPriceYuan" placeholder="如 88.00" />
        </el-form-item>
        <el-form-item label="成团人数">
          <el-input-number v-model="form.teamNum" :min="2" :max="50" />
        </el-form-item>
        <el-form-item label="每人限购">
          <el-input-number v-model="form.perLimit" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker v-model="range" type="datetimerange" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { teamListApi, teamSaveApi, teamFoundsApi, teamScanApi, goodsOptionsApi, goodsItemsApi } from '@/api/p5'

const tab = ref('acts')
const list = ref<any[]>([])
const founds = ref<any[]>([])
const goods = ref<any[]>([])
const items = ref<any[]>([])
const loading = ref(false)
const loadingF = ref(false)
const scanning = ref(false)
const dlg = ref(false)
const range = ref<string[]>([])
const form = reactive<any>({ id: null, name: '', goodsId: null, goodsItemId: null, teamPriceYuan: '', teamNum: 3, perLimit: 1, status: 1 })

async function load() {
  loading.value = true
  try {
    const res: any = await teamListApi()
    list.value = res.data || []
  } finally { loading.value = false }
}
async function loadFounds() {
  loadingF.value = true
  try {
    const res: any = await teamFoundsApi()
    founds.value = res.data || []
  } finally { loadingF.value = false }
}

function openCreate() {
  Object.assign(form, { id: null, name: '', goodsId: null, goodsItemId: null, teamPriceYuan: '', teamNum: 3, perLimit: 1, status: 1 })
  items.value = []
  range.value = []
  dlg.value = true
}
function openEdit(row: any) {
  Object.assign(form, { ...row, teamPriceYuan: (row.teamPrice / 100).toFixed(2) })
  range.value = [row.startTime, row.endTime]
  loadItems(row.goodsId)
  dlg.value = true
}
async function onGoodsChange(id: number) {
  form.goodsItemId = null
  await loadItems(id)
}
async function loadItems(goodsId: number) {
  if (!goodsId) return
  const res: any = await goodsItemsApi(goodsId)
  items.value = res.data?.items || []
}
async function save() {
  if (!form.name || !form.goodsItemId) return ElMessage.warning('请填写活动名并选择 SKU')
  const yuan = Number(form.teamPriceYuan)
  if (!(yuan > 0)) return ElMessage.warning('请填写拼团价')
  await teamSaveApi({
    ...form,
    teamPrice: Math.round(yuan * 100),
    startTime: range.value?.[0],
    endTime: range.value?.[1]
  })
  ElMessage.success('已保存')
  dlg.value = false
  load()
}
async function scan() {
  scanning.value = true
  try {
    const res: any = await teamScanApi()
    ElMessage.success(`扫描完成，处理 ${res.data} 个超时团`)
    loadFounds()
  } finally { scanning.value = false }
}

onMounted(async () => {
  const g: any = await goodsOptionsApi()
  goods.value = g.data?.records || []
  load()
  loadFounds()
})
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 12px; }
.grow { flex: 1; }
.dim { color: #909399; font-size: 12px; }
.red { color: #f56c6c; font-weight: 600; }
.ok { color: #67c23a; font-weight: 600; }
</style>
