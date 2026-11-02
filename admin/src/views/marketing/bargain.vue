<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" size="small" @click="openCreate">+ 新建砍价活动</el-button>
      <div class="grow"></div>
      <span class="dim">用户发起后邀请好友帮砍，砍到底价后可按底价下单</span>
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
      <el-table-column label="原价" width="100">
        <template #default="{ row }">￥{{ (row.basePrice / 100).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="底价" width="100">
        <template #default="{ row }"><span class="red">￥{{ (row.floorPrice / 100).toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column prop="helpNum" label="需帮砍人数" width="110" />
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
    <el-empty v-if="!list.length" description="还没有砍价活动" />

    <el-dialog v-model="dlg" :title="form.id ? '编辑砍价' : '新建砍价'" width="560px">
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
        <el-form-item label="原价(元)"><el-input v-model="form.basePriceYuan" /></el-form-item>
        <el-form-item label="底价(元)"><el-input v-model="form.floorPriceYuan" placeholder="不能高于原价" /></el-form-item>
        <el-form-item label="需帮砍人数">
          <el-input-number v-model="form.helpNum" :min="1" :max="50" />
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
import { bargainListApi, bargainSaveApi, goodsOptionsApi, goodsItemsApi } from '@/api/p5'

const list = ref<any[]>([])
const goods = ref<any[]>([])
const items = ref<any[]>([])
const loading = ref(false)
const dlg = ref(false)
const range = ref<string[]>([])
const form = reactive<any>({ id: null, name: '', goodsId: null, goodsItemId: null, basePriceYuan: '', floorPriceYuan: '', helpNum: 5, status: 1 })

async function load() {
  loading.value = true
  try {
    const res: any = await bargainListApi()
    list.value = res.data || []
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { id: null, name: '', goodsId: null, goodsItemId: null, basePriceYuan: '', floorPriceYuan: '', helpNum: 5, status: 1 })
  items.value = []
  range.value = []
  dlg.value = true
}
function openEdit(row: any) {
  Object.assign(form, { ...row, basePriceYuan: (row.basePrice / 100).toFixed(2), floorPriceYuan: (row.floorPrice / 100).toFixed(2) })
  range.value = [row.startTime, row.endTime]
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
  if (it) form.basePriceYuan = (it.price / 100).toFixed(2)
}
async function save() {
  if (!form.name || !form.goodsItemId) return ElMessage.warning('请填写活动名并选择 SKU')
  const base = Number(form.basePriceYuan)
  const floor = Number(form.floorPriceYuan)
  if (!(base > 0) || !(floor > 0)) return ElMessage.warning('请填写原价与底价')
  if (floor >= base) return ElMessage.warning('底价必须低于原价')
  await bargainSaveApi({
    ...form,
    basePrice: Math.round(base * 100),
    floorPrice: Math.round(floor * 100),
    startTime: range.value?.[0],
    endTime: range.value?.[1]
  })
  ElMessage.success('已保存')
  dlg.value = false
  load()
}

onMounted(async () => {
  const g: any = await goodsOptionsApi()
  goods.value = g.data?.records || []
  load()
})
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 12px; gap: 10px; }
.grow { flex: 1; }
.dim { color: #909399; font-size: 12px; }
.red { color: #f56c6c; font-weight: 600; }
</style>
