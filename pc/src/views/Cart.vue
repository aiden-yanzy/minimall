<template>
  <div class="cart">
    <h2>购物车</h2>
    <el-table :data="items" v-loading="loading">
      <el-table-column width="60">
        <template #default="{ row }">
          <el-checkbox :model-value="row.isSelected === 1" @change="(v) => toggle(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="商品">
        <template #default="{ row }">
          <div class="g">
            <img :src="row.goodsImage" class="g-img" />
            <div>
              <div>{{ row.goodsName }}</div>
              <div class="spec">{{ row.specValueStr }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="120">
        <template #default="{ row }">￥{{ fen2yuan(row.price) }}</template>
      </el-table-column>
      <el-table-column label="数量" width="160">
        <template #default="{ row }">
          <el-input-number size="small" :model-value="row.num" :min="1" @change="(v) => changeNum(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="{ row }">￥{{ fen2yuan(row.totalPrice) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }"><el-button link type="danger" @click="remove(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <div class="bar">
      已选 <b>{{ selectedCount }}</b> 件，合计 <b class="red">￥{{ fen2yuan(total) }}</b>
      <el-button type="danger" size="large" @click="goCheckout">去结算</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCartList, updateCartNum, updateSelected, removeCart } from '@/api/cart'

const router = useRouter()
const items = ref([])
const loading = ref(false)
const fen2yuan = (f) => (f / 100).toFixed(2)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getCartList()
    items.value = res.data
  } finally { loading.value = false }
})
async function toggle(row, v) {
  row.isSelected = v ? 1 : 0
  await updateSelected(row.id, row.isSelected)
}
async function changeNum(row, v) {
  row.num = v
  await updateCartNum(row.id, v)
}
async function remove(row) {
  await removeCart(row.id)
  items.value = items.value.filter((i) => i.id !== row.id)
}
const selected = computed(() => items.value.filter((i) => i.isSelected === 1))
const selectedCount = computed(() => selected.value.reduce((s, i) => s + i.num, 0))
const total = computed(() => selected.value.reduce((s, i) => s + i.price * i.num, 0))
function goCheckout() {
  if (!selectedCount.value) return
  router.push('/checkout?buyType=cart')
}
</script>

<style scoped>
.cart { max-width: 1200px; margin: 0 auto; padding: 20px 0; background: #fff; border-radius: 12px; }
.cart h2 { padding: 16px 24px 0; }
.g { display: flex; gap: 12px; align-items: center; }
.g-img { width: 60px; height: 60px; object-fit: cover; border-radius: 6px; }
.spec { color: #999; font-size: 12px; }
.bar { padding: 16px 24px; display: flex; align-items: center; gap: 16px; justify-content: flex-end; border-top: 1px solid #f0f0f0; }
.red { color: #ff2d2d; font-size: 20px; }
</style>