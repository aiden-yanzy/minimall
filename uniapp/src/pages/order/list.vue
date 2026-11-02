<template>
  <view class="order-list">
    <view class="tabs">
      <view v-for="t in tabs" :key="t.value" class="tab" :class="{ active: current === t.value }" @click="switchTab(t.value)">
        {{ t.label }}
      </view>
    </view>

    <view v-if="!list.length" class="empty">暂无订单</view>
    <view v-for="o in list" :key="o.id" class="order-card" @click="goDetail(o.id)">
      <view class="order-head">
        <text class="sn">{{ o.orderSn }}</text>
        <text class="status">{{ statusMap[o.orderStatus] }}</text>
      </view>
      <view v-for="g in o.goodsList" :key="g.goodsItemId" class="g-item">
        <image class="img" :src="asset(g.goodsImage)" mode="aspectFill" />
        <view class="g-info">
          <view class="g-name">{{ g.goodsName }}</view>
          <view class="g-spec">{{ g.specValueStr }}</view>
        </view>
        <view class="g-right">
          <view>￥{{ fen2yuan(g.price) }}</view>
          <view class="g-num">x{{ g.num }}</view>
        </view>
      </view>
      <view class="order-foot">
        <text>共{{ countGoods(o) }}件 实付 <text class="red">￥{{ fen2yuan(o.payAmount) }}</text></text>
        <view class="btn small" @click.stop="goDetail(o.id)">{{ statusMap[o.orderStatus] }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOrderList } from '@/api/order'
import { useUserStore } from '@/store/user'
import { fen2yuan } from '@/utils/format'

const tabs = [
  { label: '全部', value: null },
  { label: '待支付', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 }
]
const statusMap = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const current = ref(null)
const list = ref([])
const userStore = useUserStore()

async function load() {
  if (!userStore.token) return uni.navigateTo({ url: '/pages/user/login' })
  const res = await getOrderList(current.value)
  list.value = res.data
}
function switchTab(v) {
  current.value = v
  load()
}
function countGoods(o) {
  return (o.goodsList || []).reduce((s, g) => s + g.num, 0)
}
function goDetail(id) {
  uni.navigateTo({ url: '/pages/order/detail?id=' + id })
}

onShow(load)
</script>

<style scoped>
.order-list {
  background: #f5f5f5;
  min-height: 100vh;
}
.tabs {
  display: flex;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 5;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #333;
  border-bottom: 4rpx solid transparent;
}
.tab.active {
  color: #ff2d2d;
  border-bottom-color: #ff2d2d;
  font-weight: bold;
}
.order-card {
  background: #fff;
  border-radius: 16rpx;
  margin: 20rpx;
  padding: 20rpx;
}
.order-head {
  display: flex;
  justify-content: space-between;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 16rpx;
}
.sn { color: #666; font-size: 24rpx; }
.status { color: #ff2d2d; }
.g-item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}
.img {
  width: 110rpx;
  height: 110rpx;
  border-radius: 10rpx;
  background: #f0f0f0;
}
.g-info {
  flex: 1;
  margin-left: 16rpx;
}
.g-name { font-size: 26rpx; }
.g-spec { color: #999; font-size: 22rpx; margin-top: 4rpx; }
.g-right { text-align: right; }
.g-num { color: #999; }
.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #333;
}
.red { color: #ff2d2d; font-weight: bold; }
.btn.small {
  border: 1rpx solid #ff2d2d;
  color: #ff2d2d;
  border-radius: 30rpx;
  padding: 8rpx 28rpx;
  font-size: 24rpx;
}
.empty {
  text-align: center;
  color: #999;
  padding: 120rpx 0;
}
</style>
