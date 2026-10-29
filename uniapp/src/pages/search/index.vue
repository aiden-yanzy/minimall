<template>
  <view class="search">
    <view class="bar">
      <view class="input-wrap">
        <text class="ico">🔍</text>
        <input
          v-model="keyword"
          class="ipt"
          confirm-type="search"
          placeholder="搜索商品名称"
          focus
          @confirm="doSearch"
        />
        <text v-if="keyword" class="clear" @click="keyword = ''">✕</text>
      </view>
      <text class="go" @click="doSearch">搜索</text>
    </view>

    <!-- 历史 -->
    <view v-if="history.length" class="block">
      <view class="block-head">
        <text class="block-title">搜索历史</text>
        <text class="block-op" @click="clearHistory">清空</text>
      </view>
      <view class="tags">
        <text v-for="(h, i) in history" :key="i" class="tag" @click="searchWord(h)">{{ h }}</text>
      </view>
    </view>

    <!-- 热词 -->
    <view class="block">
      <view class="block-head">
        <text class="block-title">热门搜索</text>
      </view>
      <view class="tags">
        <text v-for="(h, i) in hotWords" :key="i" class="tag hot" @click="searchWord(h)">{{ h }}</text>
      </view>
    </view>

    <!-- 分类快捷 -->
    <view class="block">
      <view class="block-head"><text class="block-title">按分类逛</text></view>
      <view class="tags">
        <text v-for="c in cats" :key="c.id" class="tag" @click="goCategory(c.id)">{{ c.name }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onLoad, onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { getCategoryTree } from '@/api/goods'

const KEY = 'search_history'
const keyword = ref('')
const history = ref([])
const cats = ref([])
// 热词必须是站内真能搜到的词（已逐个验证命中数 > 0），别写"蜂蜜"这种商品名里没有的字
const hotWords = ref(['耳机', '衬衫', '原蜜', '台灯', '面霜', '咖啡', '身体乳', 'T恤'])

function loadHistory() {
  history.value = uni.getStorageSync(KEY) || []
}

function pushHistory(w) {
  const list = [w, ...history.value.filter((x) => x !== w)].slice(0, 10)
  history.value = list
  uni.setStorageSync(KEY, list)
}

function doSearch() {
  const w = keyword.value.trim()
  if (!w) return uni.showToast({ title: '请输入搜索词', icon: 'none' })
  pushHistory(w)
  uni.navigateTo({ url: '/pages/goods/list?keyword=' + encodeURIComponent(w) })
}

function searchWord(w) {
  keyword.value = w
  pushHistory(w)
  uni.navigateTo({ url: '/pages/goods/list?keyword=' + encodeURIComponent(w) })
}

function goCategory(id) {
  uni.navigateTo({ url: '/pages/goods/list?categoryId=' + id })
}

function clearHistory() {
  uni.showModal({
    title: '提示',
    content: '清空搜索历史？',
    success: (r) => {
      if (!r.confirm) return
      uni.removeStorageSync(KEY)
      history.value = []
    }
  })
}

onLoad(async () => {
  const res = await getCategoryTree()
  cats.value = (res.data || []).slice(0, 8)
})
onShow(loadHistory)
</script>

<style scoped>
.search { padding: 20rpx 24rpx 60rpx; background: #f5f5f5; min-height: 100vh; }
.bar { display: flex; align-items: center; gap: 16rpx; }
.input-wrap { flex: 1; display: flex; align-items: center; background: #fff; border-radius: 40rpx; padding: 0 24rpx; height: 72rpx; }
.ico { font-size: 26rpx; margin-right: 10rpx; }
.ipt { flex: 1; font-size: 28rpx; }
.clear { color: #bbb; font-size: 26rpx; padding: 0 6rpx; }
.go { font-size: 28rpx; color: #ff2d2d; }
.block { margin-top: 36rpx; }
.block-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18rpx; }
.block-title { font-size: 26rpx; font-weight: 600; color: #333; }
.block-op { font-size: 24rpx; color: #999; }
.tags { display: flex; flex-wrap: wrap; gap: 16rpx; }
.tag { font-size: 24rpx; color: #555; background: #fff; padding: 12rpx 26rpx; border-radius: 30rpx; }
.tag.hot { color: #ff2d2d; background: #fff3f0; }
</style>
