<template>
  <view class="integral">
    <view class="head">
      <view class="title">积分商城</view>
      <view class="my">我的积分：<text class="num">{{ userInfo?.integral ?? '--' }}</text></view>
      <view class="orders-link" @click="goOrders">我的兑换订单 →</view>
    </view>
    <view v-for="g in list" :key="g.id" class="goods-card">
      <image class="img" :src="asset(g.image)" mode="aspectFill" />
      <view class="info">
        <view class="name">{{ g.name }}</view>
        <view class="desc">{{ g.description || '' }}</view>
        <view class="stock">库存 {{ g.stock }} 件</view>
        <view class="row">
          <view class="integral-price"><text class="num">{{ g.integral }}</text><text class="unit">积分</text></view>
          <view class="btn" @click="goExchange(g)">去兑换</view>
        </view>
      </view>
    </view>
    <view v-if="!list.length" class="empty">暂无可兑换商品</view>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getIntegralGoods } from '@/api/integral'
import { useUserStore } from '@/store/user'

const list = ref([])
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

async function load() {
  const res = await getIntegralGoods()
  list.value = res.data
  if (userStore.token) userStore.fetchMe()
}
function goExchange(g) {
  uni.navigateTo({ url: '/pages/integral/exchange?id=' + g.id })
}
function goOrders() {
  uni.navigateTo({ url: '/pages/integral/orders' })
}

onShow(load)
</script>

<style scoped>
.integral {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.head {
  background: linear-gradient(135deg, #7b2ff7, #f107a3);
  border-radius: 16rpx;
  color: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}
.title { font-size: 34rpx; font-weight: bold; }
.my { margin-top: 10rpx; font-size: 24rpx; }
.num { font-size: 34rpx; font-weight: bold; }
.orders-link { margin-top: 10rpx; font-size: 22rpx; opacity: 0.9; }
.goods-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  display: flex;
}
.img {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  background: #f0f0f0;
}
.info { flex: 1; margin-left: 20rpx; display: flex; flex-direction: column; }
.name { font-size: 30rpx; font-weight: bold; }
.desc { color: #999; font-size: 22rpx; margin-top: 6rpx; }
.stock { color: #bbb; font-size: 20rpx; margin-top: 6rpx; }
.row { margin-top: auto; display: flex; justify-content: space-between; align-items: center; }
.integral-price { color: #f107a3; display: flex; align-items: baseline; }
.num { font-size: 40rpx; font-weight: bold; }
.unit { font-size: 22rpx; margin-left: 4rpx; }
.btn {
  background: #f107a3;
  color: #fff;
  border-radius: 30rpx;
  padding: 12rpx 40rpx;
  font-size: 26rpx;
}
.empty { text-align: center; color: #999; padding: 120rpx 0; }
</style>