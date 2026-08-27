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
        <div class="collect">
          <el-button :type="collected ? 'danger' : 'default'" size="small" round @click="onToggleCollect">
            {{ collected ? '♥ 已收藏' : '♡ 收藏' }}
          </el-button>
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
        <div class="comments">
          <h3>用户评价（{{ commentTotal }}）</h3>
          <div v-for="c in comments" :key="c.id" class="cm">
            <div class="cm-head"><b>{{ c.nickname }}</b><span class="cm-score">★ {{ c.score }}.0</span></div>
            <div class="cm-content">{{ c.content }}</div>
            <div class="cm-reply" v-if="c.reply">商家回复：{{ c.reply }}</div>
          </div>
          <el-empty v-if="!comments.length" description="暂无评价" :image-size="60" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetail } from '@/api/goods'
import { addCart } from '@/api/cart'
import { collectStatus, toggleCollect, getComments } from '@/api/member'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const cart = useCartStore()
const collected = ref(false)
const comments = ref([])
const commentTotal = ref(0)
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
  if (!user.token) { ElMessage.warning('请先登录'); return router.push('/login') }
  await addCart({ goodsId: goods.value.id, goodsItemId: selItemId.value, num: num.value })
  cart.fetchCount()
  ElMessage.success('已加入购物车')
}
function buyNow() {
  if (!user.token) { ElMessage.warning('请先登录'); return router.push('/login') }
  router.push({ path: '/checkout', query: { buyType: 'buyNow', goodsId: goods.value.id, goodsItemId: selItemId.value, num: num.value } })
}

async function onToggleCollect() {
  if (!user.token) { ElMessage.warning('请先登录'); return router.push('/login') }
  const res = await toggleCollect(goods.value.id)
  collected.value = res.data
  ElMessage.success(res.data ? '收藏成功' : '已取消收藏')
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
  // 评价 + 收藏状态
  const cm = await getComments(route.params.id)
  comments.value = cm.data.records
  commentTotal.value = cm.data.total
  if (user.token) {
    const s = await collectStatus(goods.value.id)
    collected.value = s.data
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
.comments { margin-top: 20px; border-top: 1px solid #f0f0f0; padding-top: 16px; }
.cm { border-bottom: 1px solid #f5f5f5; padding: 12px 0; }
.cm-head { display: flex; justify-content: space-between; }
.cm-score { color: #ffb400; }
.cm-content { color: #666; margin-top: 6px; }
.cm-reply { color: #999; font-size: 13px; margin-top: 6px; }
.collect { margin-top: 14px; }
</style>