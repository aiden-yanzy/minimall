<template>
  <view class="orders">
    <view v-if="!list.length" class="empty">暂无兑换记录</view>
    <view v-for="o in list" :key="o.id" class="card">
      <view class="head">
        <text class="sn">{{ o.orderSn }}</text>
        <text class="status">{{ statusMap[o.status] }}</text>
      </view>
      <view class="body">
        <image class="img" :src="o.goodsImage" mode="aspectFill" />
        <view class="info">
          <view class="name">{{ o.goodsName }}</view>
          <view class="meta">x{{ o.num }} · 共 {{ o.totalIntegral }} 积分</view>
          <view class="meta" v-if="o.expressNo">物流：{{ o.expressName }} {{ o.expressNo }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getIntegralOrders } from '@/api/integral'

const statusMap = { 0: '待发货', 1: '已发货', 2: '已完成' }
const list = ref([])

onShow(async () => {
  const res = await getIntegralOrders()
  list.value = res.data
})
</script>

<style scoped>
.orders {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
}
.head {
  display: flex;
  justify-content: space-between;
  border-bottom: 1rpx solid #f0f0f0;
  padding-bottom: 16rpx;
}
.sn { color: #666; font-size: 22rpx; }
.status { color: #f107a3; }
.body { display: flex; margin-top: 16rpx; }
.img { width: 120rpx; height: 120rpx; border-radius: 10rpx; background: #f0f0f0; }
.info { flex: 1; margin-left: 16rpx; }
.name { font-size: 28rpx; }
.meta { color: #999; font-size: 22rpx; margin-top: 8rpx; }
.empty { text-align: center; color: #999; padding: 120rpx 0; }
</style>