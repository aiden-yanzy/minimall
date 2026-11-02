<template>
  <el-card>
    <el-tabs v-model="tab">
      <el-tab-pane label="预售活动" name="acts">
        <div class="toolbar">
          <el-button type="primary" size="small" @click="openCreate">+ 新建预售</el-button>
          <div class="grow"></div>
          <span class="dim">定金 + 尾款模式：付定金锁库存，尾款期内付尾款，逾期视为放弃</span>
        </div>
        <el-table :data="list" v-loading="loading" border stripe size="small">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="活动名" min-width="130" />
          <el-table-column label="商品" min-width="160">
            <template #default="{ row }">
              <div>{{ row.goodsName || ('#' + row.goodsId) }}</div>
              <div class="dim">{{ row.specValueStr }}</div>
            </template>
          </el-table-column>
          <el-table-column label="定金" width="90">
            <template #default="{ row }">￥{{ (row.deposit / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="尾款" width="90">
            <template #default="{ row }">￥{{ (row.finalPrice / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="库存/已售" width="100">
            <template #default="{ row }">{{ row.stock }} / {{ row.sales }}</template>
          </el-table-column>
          <el-table-column label="尾款截止" width="160">
            <template #default="{ row }">{{ (row.finalEndTime || '').slice(0, 16).replace('T', ' ') }}</template>
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

      <el-tab-pane label="预售订单" name="orders">
        <el-table :data="orders" v-loading="loadingO" border stripe size="small">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="orderId" label="订单ID" width="90" />
          <el-table-column label="买家" width="120">
            <template #default="{ row }">{{ row.nickname || row.userId }}</template>
          </el-table-column>
          <el-table-column label="定金" width="90">
            <template #default="{ row }">￥{{ ((row.deposit || 0) / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="尾款" width="90">
            <template #default="{ row }">￥{{ ((row.finalPrice || 0) / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="定金/尾款状态" width="150">
            <template #default="{ row }">
              <el-tag size="small" :type="row.depositPaid === 1 ? 'success' : 'info'">定金{{ row.depositPaid === 1 ? '已付' : '未付' }}</el-tag>
              <el-tag size="small" :type="row.finalPaid === 1 ? 'success' : 'info'" style="margin-left: 4px">尾款{{ row.finalPaid === 1 ? '已付' : '未付' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 2 ? 'success' : 'warning'">{{ row.status === 2 ? '已完成' : '进行中' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                v-if="row.depositPaid === 1 && row.finalPaid === 1 && row.status !== 2"
                link type="primary" size="small" @click="deliver(row)"
              >发货</el-button>
              <span v-else class="dim">—</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!orders.length" description="还没有预售订单" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dlg" :title="form.id ? '编辑预售' : '新建预售'" width="560px">
      <el-form label-width="110px">
        <el-form-item label="活动名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="商品">
          <el-select v-model="form.goodsId" filterable placeholder="选择商品" style="width: 100%" @change="onGoodsChange">
            <el-option v-for="g in goods" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="SKU">
          <el-select v-model="form.goodsItemId" placeholder="选择规格" style="width: 100%" @change="onItemChange">
            <el-option v-for="it in items" :key="it.id" :label="`${it.specValueStr || '默认'}（￥${(it.price / 100).toFixed(2)}）`" :value="it.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="定金(元)"><el-input v-model="form.depositYuan" /></el-form-item>
        <el-form-item label="尾款(元)"><el-input v-model="form.finalPriceYuan" /></el-form-item>
        <el-form-item label="预售库存"><el-input-number v-model="form.stock" :min="1" :max="99999" /></el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker v-model="range" type="datetimerange" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="尾款截止">
          <el-date-picker v-model="finalEnd" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { presellListApi, presellSaveApi, presellOrdersApi, presellDeliverApi, goodsOptionsApi, goodsItemsApi } from '@/api/p5'

const tab = ref('acts')
const list = ref<any[]>([])
const orders = ref<any[]>([])
const goods = ref<any[]>([])
const items = ref<any[]>([])
const loading = ref(false)
const loadingO = ref(false)
const dlg = ref(false)
const range = ref<string[]>([])
const finalEnd = ref('')
const form = reactive<any>({ id: null, name: '', goodsId: null, goodsItemId: null, depositYuan: '', finalPriceYuan: '', stock: 100, status: 1 })

async function load() {
  loading.value = true
  try {
    const res: any = await presellListApi()
    list.value = res.data || []
  } finally { loading.value = false }
}
async function loadOrders() {
  loadingO.value = true
  try {
    const res: any = await presellOrdersApi()
    orders.value = res.data || []
  } finally { loadingO.value = false }
}
function openCreate() {
  Object.assign(form, { id: null, name: '', goodsId: null, goodsItemId: null, depositYuan: '', finalPriceYuan: '', stock: 100, status: 1 })
  items.value = []
  range.value = []
  finalEnd.value = ''
  dlg.value = true
}
function openEdit(row: any) {
  Object.assign(form, { ...row, depositYuan: (row.deposit / 100).toFixed(2), finalPriceYuan: (row.finalPrice / 100).toFixed(2) })
  range.value = [row.startTime, row.endTime]
  finalEnd.value = row.finalEndTime
  loadItems(row.goodsId)
  dlg.value = true
}
async function onGoodsChange(id: number) {
  form.goodsItemId = null
  await loadItems(id)
}
async function loadItems(goodsId: number) {
  const res: any = await goodsItemsApi(goodsId)
  items.value = res.data?.items || []
}
function onItemChange(id: number) {
  const it = items.value.find((x) => x.id === id)
  if (it) form.finalPriceYuan = (it.price / 100).toFixed(2)
}
async function save() {
  if (!form.name || !form.goodsItemId) return ElMessage.warning('请填写活动名并选择 SKU')
  const dep = Number(form.depositYuan)
  const fin = Number(form.finalPriceYuan)
  if (!(dep > 0) || !(fin > 0)) return ElMessage.warning('请填写定金与尾款')
  await presellSaveApi({
    ...form,
    deposit: Math.round(dep * 100),
    finalPrice: Math.round(fin * 100),
    startTime: range.value?.[0],
    endTime: range.value?.[1],
    finalEndTime: finalEnd.value
  })
  ElMessage.success('已保存')
  dlg.value = false
  load()
}
async function deliver(row: any) {
  const { value } = await ElMessageBox.prompt('填写快递单号', '预售发货', { inputPattern: /\S+/, inputErrorMessage: '单号不能为空' })
  await presellDeliverApi(row.id, { expressId: 1, expressName: '顺丰速运', expressNo: value })
  ElMessage.success('已发货')
  loadOrders()
}

onMounted(async () => {
  const g: any = await goodsOptionsApi()
  goods.value = g.data?.records || []
  load()
  loadOrders()
})
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 12px; gap: 10px; }
.grow { flex: 1; }
.dim { color: #909399; font-size: 12px; }
</style>
