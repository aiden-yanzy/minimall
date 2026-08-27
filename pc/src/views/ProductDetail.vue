<template>
  <div v-if="goods.id" class="detail">
    <el-row :gutter="24">
      <el-col :span="10">
        <el-image :src="image" fit="cover" class="big-img" />
        <div class="thumbs">
          <img v-for="(img, i) in images" :key="i" :src="img" class="thumb" :class="{ on: img === image }" @click="image = img" />
        </div>
      </el-col>
      <el-col :span="14">
        <h1 class="name">{{ goods.name }}</h1>
        <div class="sub">{{ goods.subtitle }}</div>
        <div class="price-box">
          价格 <b class="price">￥{{ fen2yuan(selPrice) }}</b>
          <s v-if="goods.marketPrice > selPrice" class="market">￥{{ fen2yuan(goods.marketPrice) }}</s>
          <span class="stock">库存 {{ selStock }} 件 · 已售 {{ goods.sales }}</span>
        </div>
        <div class="spec-row" v-if="goods.items?.length">
          <span class="label">规格</span>
          <el-radio-group v-model="selItemId">
            <el-radio-button v-for="it in goods.items" :key="it.id" :value="it.id" :disabled="it.stock <= 0" @change="onSpec">
              {{ it.specValueStr || '默认' }}
            </el-radio-button>
          </el-radio-group>
        </div>
        <div class="num-row">
          <span class="label">数量</span>
          <el-input-number v-model="num" :min="1" :max="selStock" />
        </div>
        <div class="actions">
          <el-button size="large" @click="addToCart">加入购物车</el-button>
          <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
        </div>
        <div class="content"><div v-html="goods.content"></div></div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetail } from '@/api/goods'
import { addCart } from '@/api/cart'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const goods = ref({})
const images = ref([])
const image = ref('')
const selItemId = ref(null)
const selPrice = ref(0)
const selStock = ref(0)
const num = ref(1)
const fen2yuan = (f) => (f / 100).toFixed(2)

function onSpec() {
  const it = goods.value.items.find((i) => i.id === selItemId.value)
  if (it) { selPrice.value = it.price; selStock.value = it.stock }
}

async function addToCart() {
  if (!user.token) return router.push('/login')
  await addCart({ goodsId: goods.value.id, goodsItemId: selItemId.value, num: num.value })
}
function buyNow() {
  if (!user.token) return router.push('/login')
  router.push({ path: '/checkout', query: { buyType: 'buyNow', goodsId: goods.value.id, goodsItemId: selItemId.value, num: num.value } })
}

onMounted(async () => {
  const res = await getGoodsDetail(route.params.id)
  goods.value = res.data
  images.value = (res.data.images?.length ? res.data.images : [res.data.image])
  image.value = images.value[0]
  if (res.data.items?.length) {
    selItemId.value = res.data.items[0].id
    onSpec()
  }
})
</script>

<style scoped>
.detail { max-width: 1200px; margin: 0 auto; padding: 20px 0; background: #fff; border-radius: 12px; }
.detail :deep(.el-row) { padding: 24px; }
.big-img { width: 100%; height: 460px; border-radius: 10px; background: #f7f7f7; }
.thumbs { display: flex; gap: 8px; margin-top: 10px; }
.thumb { width: 64px; height: 64px; object-fit: cover; border-radius: 6px; border: 2px solid transparent; cursor: pointer; }
.thumb.on { border-color: #ff2d2d; }
.name { margin: 0; font-size: 26px; }
.sub { color: #999; margin: 10px 0 16px; }
.price-box { background: #fff7f7; border-radius: 8px; padding: 14px 16px; }
.price { color: #ff2d2d; font-size: 26px; margin: 0 8px; }
.market { color: #bbb; }
.stock { float: right; color: #999; font-size: 13px; }
.spec-row, .num-row { display: flex; align-items: center; margin-top: 18px; }
.label { width: 60px; color: #999; }
.actions { margin-top: 24px; display: flex; gap: 12px; }
.content { margin-top: 20px; color: #666; line-height: 1.8; }
</style>