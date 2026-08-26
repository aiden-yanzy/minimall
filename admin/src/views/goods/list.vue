<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索商品名" style="width: 220px" clearable @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button type="success" @click="openCreate" style="float: right">+ 新建商品</el-button>
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
        <el-table-column label="价格(分)" width="110">
          <template #default="{ row }">{{ row.minPrice }} ~ {{ row.maxPrice }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新建商品'" width="720px">
      <el-form :model="form" label-width="90px">
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
          <el-input v-model="form.image" placeholder="http://..." />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>

        <el-divider>SKU</el-divider>
        <div v-for="(item, idx) in form.items" :key="idx" class="sku-row">
          <el-input v-model="item.specValueStr" placeholder="规格(如:黑色)" style="width: 110px" />
          <el-input v-model.number="item.price" placeholder="价格(分)" style="width: 110px" type="number" />
          <el-input v-model.number="item.stock" placeholder="库存" style="width: 110px" type="number" />
          <el-button link type="danger" @click="form.items.splice(idx, 1)">删</el-button>
        </div>
        <el-button link type="primary" @click="form.items.push({ price: 0, stock: 0, specValueStr: '' })">+ 添加SKU</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  goodsPageApi, goodsStatusApi, goodsDeleteApi, goodsCreateApi, goodsUpdateApi, categoryTreeApi
} from '@/api/goods'

const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const cats = ref<any[]>([])
const query = reactive({ page: 1, size: 10, keyword: '' })

const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive<any>({ items: [] })

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

function openCreate() {
  Object.assign(form, { name: '', subtitle: '', categoryId: null, image: '', content: '', items: [{ specValueStr: '', price: 0, stock: 0 }] })
  dialogVisible.value = true
}

function openEdit(row: any) {
  const detail = {
    id: row.id,
    name: row.name,
    subtitle: row.subtitle,
    categoryId: row.categoryId,
    image: row.image,
    content: row.content,
    items: [{ specValueStr: '', price: 0, stock: 0 }]
  }
  Object.assign(form, detail)
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    const data = { ...form, images: form.image ? [form.image] : [] }
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
.sku-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}
</style>
