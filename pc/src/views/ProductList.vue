<template>
  <div class="list">
    <div class="side">
      <div class="side-title">商品分类</div>
      <div v-for="c in cats" :key="c.id" class="cat" :class="{ active: c.id == categoryId }" @click="select(c.id)">{{ c.name }}</div>
      <div class="cat" :class="{ active: !categoryId }" @click="select(null)">全部</div>
    </div>
    <div class="main">
      <div class="bar">
        <el-input v-model="keyword" placeholder="搜索商品" style="width:220px" @keyup.enter="load" />
        <el-button type="primary" @click="load">搜索</el-button>
        <el-radio-group v-model="sort" size="small" style="margin-left:auto" @change="load">
          <el-radio-button :value="0">默认</el-radio-button>
          <el-radio-button :value="1">价格↑</el-radio-button>
          <el-radio-button :value="2">价格↓</el-radio-button>
        </el-radio-group>
      </div>
      <div class="grid">
        <div v-for="g in goods" :key="g.id" class="card" @click="$router.push('/product/' + g.id)">
          <img :src="g.image" class="img" />
          <div class="name">{{ g.name }}</div>
          <div class="price">￥{{ fen2yuan(g.minPrice) }} <s v-if="g.marketPrice > g.minPrice">￥{{ fen2yuan(g.marketPrice) }}</s></div>
        </div>
      </div>
      <el-pagination style="margin-top:16px" layout="total, prev, pager, next" :total="total" :page-size="size"
        v-model:current-page="page" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getGoodsList, getCategoryTree } from '@/api/goods'

const route = useRoute()
const cats = ref([])
const goods = ref([])
const total = ref(0)
const page = ref(1)
const size = 16
const categoryId = ref(route.query.categoryId || null)
const keyword = ref(route.query.keyword || '')
const sort = ref(0)
const fen2yuan = (f) => (f / 100).toFixed(2)

async function load() {
  const params = { page: page.value, size, categoryId: categoryId.value, keyword: keyword.value }
  if (sort.value === 1) params.sort = 'price_asc'
  if (sort.value === 2) params.sort = 'price_desc'
  const res = await getGoodsList(params)
  goods.value = res.data.records
  total.value = res.data.total
}
function select(id) { categoryId.value = id; page.value = 1; load() }

onMounted(async () => {
  const c = await getCategoryTree()
  cats.value = c.data
  load()
})
</script>

<style scoped>
.list { max-width: 1200px; margin: 0 auto; display: flex; gap: 16px; padding: 20px 0; align-items: flex-start; }
.side { width: 180px; background: #fff; border-radius: 12px; padding: 16px; position: sticky; top: 70px; }
.side-title { font-weight: 700; margin-bottom: 10px; }
.cat { padding: 9px 12px; border-radius: 8px; cursor: pointer; color: #333; }
.cat:hover { background: #f5f5f5; }
.cat.active { background: #fff0f0; color: #ff2d2d; font-weight: 600; }
.main { flex: 1; }
.bar { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; }
.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.card { background: #fff; border-radius: 12px; padding: 12px; cursor: pointer; }
.card:hover { box-shadow: 0 6px 20px rgba(0,0,0,.07); }
.img { width: 100%; height: 190px; object-fit: cover; border-radius: 8px; background: #f7f7f7; }
.name { margin-top: 8px; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.price { color: #ff2d2d; font-weight: 700; margin-top: 6px; }
.price s { color: #bbb; font-size: 12px; }
</style>