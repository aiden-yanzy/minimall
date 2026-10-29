<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索商品名" style="width: 220px" clearable @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button v-if="auth.can('goods:edit')" type="success" @click="openCreate" style="float: right">+ 新建商品</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="商品" min-width="260">
          <template #default="{ row }">
            <div class="goods-cell">
              <el-image :src="row.image" fit="cover" style="width: 50px; height: 50px" />
              <div>
                <div>{{ row.name }}</div>
                <div class="sub">{{ row.subtitle }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="150">
          <template #default="{ row }">
            <span class="money">¥{{ fen(row.minPrice) }}</span>
            <span v-if="row.maxPrice > row.minPrice" class="sub"> ~ ¥{{ fen(row.maxPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="规格" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.specType === 1" size="small" type="warning">多规格</el-tag>
            <span v-else class="sub">单规格</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="auth.can('goods:edit')" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="auth.can('goods:edit')" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button v-if="auth.can('goods:delete')" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 12px; justify-content: flex-end"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.size"
        v-model:current-page="query.page"
        @current-change="load"
      />
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新建商品'" width="900px" top="6vh">
      <el-tabs v-model="tab">
        <!-- 基础信息 -->
        <el-tab-pane label="基础信息" name="base">
          <el-form :model="form" label-width="94px">
            <el-form-item label="商品名">
              <el-input v-model="form.name" placeholder="商品名称" />
            </el-form-item>
            <el-form-item label="副标题">
              <el-input v-model="form.subtitle" />
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" placeholder="选择分类" clearable>
                <el-option v-for="c in cats" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="主图URL">
              <el-input v-model="form.image" placeholder="/file/demo/xxx.jpg" />
            </el-form-item>
            <el-form-item label="运费模板">
              <el-select v-model="form.deliveryTemplateId" placeholder="选择模板" clearable style="width: 100%">
                <el-option v-for="t in freightTpls" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="内容">
              <el-input v-model="form.content" type="textarea" :rows="3" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 规格与 SKU -->
        <el-tab-pane :label="`规格与SKU（${form.items.length}）`" name="sku">
          <div class="spec-head">
            <span class="tip">留空 = 单规格；至少一个规格组 = 多规格，SKU 按规格值组合生成。</span>
            <el-button size="small" type="primary" plain @click="addSpec">+ 添加规格组</el-button>
          </div>

          <div v-for="(sp, si) in form.specs" :key="si" class="spec-group">
            <div class="spec-row">
              <span class="spec-idx">{{ si + 1 }}</span>
              <el-input v-model="sp.name" placeholder="规格名，如 颜色 / 尺码" style="width: 180px" />
              <el-button link type="danger" @click="removeSpec(si)">删除该组</el-button>
            </div>
            <div class="spec-values">
              <el-tag
                v-for="(v, vi) in sp.values"
                :key="vi"
                closable
                size="large"
                @close="removeValue(si, vi)"
              >
                {{ v.value || '未命名' }}
              </el-tag>
              <el-input
                v-model="newValue[si]"
                size="small"
                style="width: 150px"
                placeholder="输入规格值后回车"
                @keyup.enter="addValue(si)"
              />
              <el-button size="small" link type="primary" @click="addValue(si)">添加</el-button>
            </div>
          </div>

          <div class="gen-bar">
            <el-button type="primary" @click="genMatrix">按规格组合生成 SKU</el-button>
            <span class="tip" v-if="specValueCount">组合数：{{ specValueCount }}</span>
            <div class="fill">
              <el-input v-model="fill.price" size="small" placeholder="统一价格(元)" style="width: 130px" />
              <el-input v-model="fill.stock" size="small" placeholder="统一库存" style="width: 110px" />
              <el-button size="small" @click="applyFill">批量填充</el-button>
              <el-button size="small" link type="primary" @click="addBlankSku">+ 手动加一行</el-button>
            </div>
          </div>

          <el-table :data="form.items" border size="small" max-height="360">
            <el-table-column label="规格" min-width="200">
              <template #default="{ row }">
                <el-input v-model="row.specValueStr" size="small" placeholder="如 黑色 / 白色" />
              </template>
            </el-table-column>
            <el-table-column label="价格(元)" width="130">
              <template #default="{ row }">
                <el-input v-model.number="row.priceYuan" size="small" type="number" @change="syncPrice(row)" />
              </template>
            </el-table-column>
            <el-table-column label="划线价(元)" width="130">
              <template #default="{ row }">
                <el-input v-model.number="row.marketYuan" size="small" type="number" @change="syncMarket(row)" />
              </template>
            </el-table-column>
            <el-table-column label="库存" width="110">
              <template #default="{ row }">
                <el-input v-model.number="row.stock" size="small" type="number" />
              </template>
            </el-table-column>
            <el-table-column label="编码" width="150">
              <template #default="{ row }">
                <el-input v-model="row.itemSn" size="small" placeholder="自动生成" />
              </template>
            </el-table-column>
            <el-table-column width="60">
              <template #default="{ $index }">
                <el-button link type="danger" @click="form.items.splice($index, 1)">删</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  goodsPageApi,
  goodsDetailApi,
  goodsStatusApi,
  goodsDeleteApi,
  goodsCreateApi,
  goodsUpdateApi,
  categoryTreeApi
} from '@/api/goods'
import { freightTemplatesApi } from '@/api/freight'
import { useAuthStore } from '@/store/auth'

const auth = useAuthStore()

const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const cats = ref<any[]>([])
const freightTpls = ref<any[]>([])
const query = reactive({ page: 1, size: 10, keyword: '' })

const dialogVisible = ref(false)
const saving = ref(false)
const tab = ref('base')
const newValue = reactive<Record<number, string>>({})
const fill = reactive({ price: '', stock: '' })

const form = reactive<any>({ items: [], specs: [] })

const fen = (v: any) => ((Number(v) || 0) / 100).toFixed(2)

/** 笛卡尔积组合数 */
const specValueCount = computed(() => {
  const groups = (form.specs || []).filter((s: any) => s.values?.length)
  if (!groups.length) return 0
  return groups.reduce((acc: number, s: any) => acc * s.values.filter((v: any) => v.value?.trim()).length || 0, 1)
})

async function load() {
  loading.value = true
  try {
    const res: any = await goodsPageApi({ ...query })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function loadCats() {
  const res: any = await categoryTreeApi()
  cats.value = res.data
}
async function loadFreight() {
  const res: any = await freightTemplatesApi()
  freightTpls.value = res.data || []
}

function resetForm(payload: any) {
  Object.assign(form, {
    id: payload.id ?? null,
    name: payload.name ?? '',
    subtitle: payload.subtitle ?? '',
    categoryId: payload.categoryId ?? null,
    deliveryTemplateId: payload.deliveryTemplateId ?? null,
    image: payload.image ?? '',
    content: payload.content ?? '',
    specs: (payload.specs || []).map((s: any) => ({
      name: s.name,
      values: (s.values || []).map((v: any) => ({ value: v.value, image: v.image }))
    })),
    items: (payload.items || []).map((it: any) => ({
      specValueStr: it.specValueStr ?? '',
      specValueIds: it.specValueIds ?? '',
      price: it.price ?? 0,
      priceYuan: (it.price ?? 0) / 100,
      marketYuan: (it.marketPrice ?? 0) / 100,
      marketPrice: it.marketPrice ?? 0,
      stock: it.stock ?? 0,
      itemSn: it.itemSn ?? '',
      image: it.image ?? ''
    }))
  })
  Object.keys(newValue).forEach((k) => delete newValue[Number(k)])
  tab.value = 'base'
}

function openCreate() {
  resetForm({ specs: [], items: [{ price: 0, stock: 0, specValueStr: '' }] })
  dialogVisible.value = true
}

async function openEdit(row: any) {
  const res: any = await goodsDetailApi(row.id)
  const d = res.data
  resetForm({
    id: d.id,
    name: d.name,
    subtitle: d.subtitle,
    categoryId: d.categoryId,
    deliveryTemplateId: d.deliveryTemplateId,
    image: d.image,
    content: d.content,
    specs: d.specs || [],
    items: d.items || []
  })
  dialogVisible.value = true
}

function addSpec() {
  if (!form.specs) form.specs = []
  form.specs.push({ name: '', values: [] })
}
function removeSpec(i: number) {
  form.specs.splice(i, 1)
}
function addValue(si: number) {
  const text = (newValue[si] || '').trim()
  if (!text) return
  const sp = form.specs[si]
  if (sp.values.some((v: any) => v.value === text)) return ElMessage.warning('该规格值已存在')
  sp.values.push({ value: text, image: '' })
  newValue[si] = ''
}
function removeValue(si: number, vi: number) {
  form.specs[si].values.splice(vi, 1)
}

/** 按规格值笛卡尔积生成 SKU 矩阵，保留已填过的价格/库存 */
function genMatrix() {
  const groups = (form.specs || [])
    .map((s: any) => (s.values || []).map((v: any) => v.value?.trim()).filter(Boolean))
    .filter((g: string[]) => g.length)
  if (!groups.length) return ElMessage.warning('请先添加规格值')

  let combos: string[][] = [[]]
  for (const g of groups) {
    const next: string[][] = []
    for (const c of combos) for (const v of g) next.push([...c, v])
    combos = next
  }

  const old = new Map<string, any>()
  for (const it of form.items || []) {
    if (it.specValueStr) old.set(it.specValueStr, it)
  }
  form.items = combos.map((c) => {
    const key = c.join(' / ')
    const prev = old.get(key)
    return {
      specValueStr: key,
      specValueIds: '',
      price: prev?.price ?? 0,
      priceYuan: prev?.priceYuan ?? 0,
      marketPrice: prev?.marketPrice ?? 0,
      marketYuan: prev?.marketYuan ?? 0,
      stock: prev?.stock ?? 0,
      itemSn: prev?.itemSn ?? '',
      image: prev?.image ?? ''
    }
  })
  ElMessage.success(`已生成 ${form.items.length} 个 SKU`)
}

function addBlankSku() {
  form.items.push({ specValueStr: '', price: 0, priceYuan: 0, marketPrice: 0, marketYuan: 0, stock: 0, itemSn: '', image: '' })
}

function applyFill() {
  const p = Number(fill.price)
  const s = Number(fill.stock)
  for (const it of form.items) {
    if (Number.isFinite(p) && fill.price !== '') {
      it.priceYuan = p
      it.price = Math.round(p * 100)
    }
    if (Number.isFinite(s) && fill.stock !== '') it.stock = s
  }
  ElMessage.success('已填充')
}

function syncPrice(row: any) {
  row.price = Math.round((Number(row.priceYuan) || 0) * 100)
}
function syncMarket(row: any) {
  row.marketPrice = Math.round((Number(row.marketYuan) || 0) * 100)
}

async function save() {
  if (!form.name?.trim()) return ElMessage.warning('商品名必填')
  if (!form.items?.length) return ElMessage.warning('至少需要一个 SKU')
  for (const it of form.items) {
    if (!Number.isFinite(Number(it.price)) || Number(it.price) <= 0) {
      return ElMessage.warning(`SKU「${it.specValueStr || '默认'}」价格必须大于 0`)
    }
  }
  saving.value = true
  try {
    const data = {
      id: form.id,
      name: form.name,
      subtitle: form.subtitle,
      categoryId: form.categoryId,
      deliveryTemplateId: form.deliveryTemplateId,
      image: form.image,
      content: form.content,
      images: form.image ? [form.image] : [],
      specs: (form.specs || []).map((s: any) => ({
        name: s.name,
        values: (s.values || []).map((v: any) => ({ value: v.value, image: v.image }))
      })),
      items: form.items.map((it: any) => ({
        specValueStr: it.specValueStr,
        specValueIds: it.specValueIds || '',
        price: Math.round(Number(it.price) || 0),
        marketPrice: Math.round(Number(it.marketPrice) || 0),
        stock: Number(it.stock) || 0,
        itemSn: it.itemSn || '',
        image: it.image || ''
      }))
    }
    if (form.id) await goodsUpdateApi(form.id, data)
    else await goodsCreateApi(data)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: any) {
  await goodsStatusApi(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除「${row.name}」？`, '提示', { type: 'warning' })
  await goodsDeleteApi(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => {
  load()
  loadCats()
  loadFreight()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
.goods-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}
.sub {
  color: #999;
  font-size: 12px;
}
.money {
  color: #e4393c;
  font-weight: 600;
}
.spec-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.tip {
  color: #909399;
  font-size: 12px;
}
.spec-group {
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 10px;
}
.spec-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.spec-idx {
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
}
.spec-values {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding-left: 30px;
}
.gen-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 12px 0;
  flex-wrap: wrap;
}
.gen-bar .fill {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-left: auto;
}
</style>
