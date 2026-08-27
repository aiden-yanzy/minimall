<template>
  <div class="home">
    <el-carousel v-if="decorate.banners?.filter(b => b.image)?.length" height="320px" class="banner">
      <el-carousel-item v-for="(b, i) in decorate.banners.filter(x => x.image)" :key="i">
        <img :src="b.image" style="width:100%;height:100%;object-fit:cover" />
      </el-carousel-item>
    </el-carousel>

    <div class="cats" v-if="cats.length">
      <span class="t">分类：</span>
      <router-link v-for="c in cats" :key="c.id" class="cat" :to="{ path: '/products', query: { categoryId: c.id } }">{{ c.name }}</router-link>
      <router-link class="cat" to="/products">全部</router-link>
    </div>

    <h3 class="sec-title">{{ decorate.goodsTitle || '精选好物' }}</h3>
    <div class="grid">
      <div v-for="g in goods" :key="g.id" class="card" @click="router.push('/product/' + g.id)">
        <img :src="g.image" class="img" />
        <div class="name">{{ g.name }}</div>
        <div class="sub">{{ g.subtitle }}</div>
        <div class="price">￥{{ fen2yuan(g.minPrice) }} <s v-if="g.marketPrice > g.minPrice">￥{{ fen2yuan(g.marketPrice) }}</s></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getGoodsList, getCategoryTree, getHomeDecorate } from '@/api/goods'

const router = useRouter()
const goods = ref([])
const cats = ref([])
const decorate = reactive({ banners: [], navs: [], goodsTitle: '' })
const fen2yuan = (f) => (f / 100).toFixed(2)

onMounted(async () => {
  const [d, c, g] = await Promise.all([getHomeDecorate(), getCategoryTree(), getGoodsList({ page: 1, size: 12 })])
  try { Object.assign(decorate, JSON.parse(d.data)) } catch (e) { Object.assign(decorate, d.data) }
  cats.push(...c.data)
  goods.push(...g.data.records)
})
</script>

<style scoped>
.home { max-width: 1200px; margin: 0 auto; padding: 20px 0; }
.banner { border-radius: 12px; overflow: hidden; }
.cats { background: #fff; border-radius: 12px; padding: 16px 20px; margin-top: 16px; }
.cats .t { color: #999; }
.cat { display: inline-block; margin: 0 14px 6px 0; padding: 6px 18px; border-radius: 16px; background: #f5f5f5; color: #333; }
.cat:hover { color: #ff2d2d; background: #fff0f0; }
.sec-title { margin: 24px 0 14px; }
.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.card { background: #fff; border-radius: 12px; padding: 14px; cursor: pointer; transition: .2s; }
.card:hover { box-shadow: 0 8px 24px rgba(0,0,0,.08); transform: translateY(-2px); }
.img { width: 100%; height: 220px; object-fit: cover; border-radius: 8px; background: #f7f7f7; }
.name { margin-top: 10px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sub { color: #999; font-size: 12px; margin-top: 4px; }
.price { color: #ff2d2d; font-size: 18px; font-weight: 700; margin-top: 8px; }
.price s { color: #bbb; font-size: 12px; font-weight: 400; }
</style>