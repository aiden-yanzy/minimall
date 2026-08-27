<template>
  <view class="wrap">
    <!-- 左侧分类 -->
    <scroll-view scroll-y class="cate-list">
      <view
        v-for="c in cats"
        :key="c.id"
        class="cate-item"
        :class="{ active: currentCat === c.id }"
        @click="selectCat(c.id)"
      >
        {{ c.name }}
      </view>
    </scroll-view>

    <!-- 右侧商品 -->
    <view class="goods-panel">
      <scroll-view scroll-y class="goods-scroll">
        <view class="search-bar">
          <input v-model="keyword" placeholder="搜索商品" @confirm="search" />
          <button size="mini" @click="search">搜索</button>
        </view>
        <view v-for="g in list" :key="g.id" class="goods-row" @click="goDetail(g.id)">
          <image class="img" :src="g.image" mode="aspectFill" />
          <view class="info">
            <view class="name">{{ g.name }}</view>
            <view class="price">￥{{ fen2yuan(g.minPrice) }} <text class="origin" v-if="g.marketPrice > g.minPrice">￥{{ fen2yuan(g.marketPrice) }}</text></view>
          </view>
        </view>
        <view v-if="!list.length" class="empty">暂无商品</view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getCategoryTree, getGoodsList } from '@/api/goods'
import { fen2yuan } from '@/utils/format'

const cats = ref([])
const currentCat = ref(null)
const list = ref([])
const keyword = ref('')

async function loadCat() {
  const res = await getCategoryTree()
  cats.value = res.data
  if (cats.value.length && currentCat.value === null) {
    currentCat.value = cats.value[0].id
    load()
  }
}
async function load() {
  const res = await getGoodsList({ page: 1, size: 50, categoryId: currentCat.value, keyword: keyword.value })
  list.value = res.data.records
}
function selectCat(id) {
  currentCat.value = id
  load()
}
function search() {
  load()
}
function goDetail(id) {
  uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
}

onLoad(loadCat)
onPullDownRefresh(async () => {
  await load()
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.wrap {
  display: flex;
  height: calc(100vh - 50px); /* 减去 tabBar 高度，H5 下微调 */
  background: #f5f5f5;
}
.cate-list {
  width: 180rpx;
  background: #fff;
  height: 100%;
}
.cate-item {
  padding: 32rpx 20rpx;
  font-size: 26rpx;
  text-align: center;
  color: #333;
}
.cate-item.active {
  color: #ff2d2d;
  font-weight: bold;
  background: #f5f5f5;
  border-left: 6rpx solid #ff2d2d;
}
.goods-panel {
  flex: 1;
  height: 100%;
}
.goods-scroll {
  height: 100%;
  padding: 0 20rpx;
}
.search-bar {
  display: flex;
  gap: 10rpx;
  align-items: center;
  background: #fff;
  border-radius: 12rpx;
  padding: 12rpx 20rpx;
  margin-bottom: 16rpx;
}
.search-bar input {
  flex: 1;
  height: 56rpx;
}
.goods-row {
  display: flex;
  background: #fff;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
}
.img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 10rpx;
  background: #f0f0f0;
}
.info {
  flex: 1;
  margin-left: 16rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.name {
  font-size: 27rpx;
}
.price {
  color: #ff2d2d;
  font-size: 30rpx;
  font-weight: bold;
}
.origin {
  color: #bbb;
  font-size: 22rpx;
  text-decoration: line-through;
  font-weight: normal;
}
.empty {
  text-align: center;
  color: #999;
  padding: 60rpx 0;
}
</style>
