<template>
  <view class="after-list">
    <view v-if="!list.length && !loading" class="empty">
      <view class="empty-icon">📦</view>
      <view class="empty-text">还没有售后记录</view>
    </view>

    <view v-for="a in list" :key="a.id" class="card">
      <view class="head">
        <text class="sn">{{ a.afterSaleSn }}</text>
        <text class="status" :class="'s' + a.status">{{ statusText(a.status) }}</text>
      </view>

      <view class="body">
        <image class="img" :src="asset(a.goodsImage)" mode="aspectFill" />
        <view class="info">
          <view class="name">{{ a.goodsName || '商品' }}</view>
          <view class="spec">{{ a.specValueStr }}</view>
          <view class="type">{{ typeText(a.type) }} · 退款 ￥{{ fen2yuan(a.refundAmount) }}</view>
        </view>
      </view>

      <view class="reason">原因：{{ a.reason }}</view>
      <view v-if="a.status === 3" class="refund">
        已退款 {{ a.refundSn }} · {{ (a.refundTime || '').replace('T', ' ').slice(0, 16) }}
      </view>

      <view class="foot">
        <text class="time">{{ (a.createTime || '').replace('T', ' ').slice(0, 16) }}</text>
        <view class="ops">
          <view v-if="a.status === 0" class="btn" @click="cancel(a)">撤销申请</view>
          <view class="btn ghost" @click="goOrder(a.orderId)">查看订单</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { myAfterSales, cancelAfterSale } from '@/api/after'
import { useUserStore } from '@/store/user'
import { fen2yuan } from '@/utils/format'

const list = ref([])
const loading = ref(false)
const userStore = useUserStore()

const statusMap = { 0: '待处理', 1: '已同意', 2: '已拒绝', 3: '已退款', 4: '已完成' }
const typeMap = { 1: '仅退款', 2: '退货退款', 3: '换货' }
const statusText = (s) => statusMap[s] || '未知'
const typeText = (t) => typeMap[t] || '售后'

async function load() {
  if (!userStore.token) return uni.navigateTo({ url: '/pages/user/login' })
  loading.value = true
  try {
    const res = await myAfterSales()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function cancel(a) {
  uni.showModal({
    title: '撤销申请',
    content: '确定撤销这笔售后申请？',
    success: async (r) => {
      if (!r.confirm) return
      await cancelAfterSale(a.id)
      uni.showToast({ title: '已撤销' })
      load()
    }
  })
}

function goOrder(id) {
  uni.navigateTo({ url: '/pages/order/detail?id=' + id })
}

onShow(load)
</script>

<style scoped>
.after-list { padding: 20rpx 24rpx 60rpx; background: #f5f5f5; min-height: 100vh; }
.empty { padding-top: 200rpx; text-align: center; }
.empty-icon { font-size: 80rpx; }
.empty-text { font-size: 26rpx; color: #999; margin-top: 16rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 22rpx; margin-bottom: 20rpx; }
.head { display: flex; justify-content: space-between; align-items: center; }
.sn { font-size: 22rpx; color: #999; }
.status { font-size: 24rpx; color: #e6a23c; }
.status.s2 { color: #f56c6c; }
.status.s3 { color: #67c23a; }
.status.s1 { color: #3370ff; }
.body { display: flex; gap: 18rpx; margin-top: 18rpx; }
.img { width: 120rpx; height: 120rpx; border-radius: 12rpx; background: #f0f0f0; }
.info { flex: 1; }
.name { font-size: 28rpx; color: #222; }
.spec { font-size: 22rpx; color: #999; margin-top: 6rpx; }
.type { font-size: 24rpx; color: #ff2d2d; margin-top: 10rpx; }
.reason { font-size: 24rpx; color: #666; margin-top: 16rpx; background: #fafafa; border-radius: 10rpx; padding: 12rpx 16rpx; }
.refund { font-size: 22rpx; color: #67c23a; margin-top: 12rpx; }
.foot { display: flex; justify-content: space-between; align-items: center; margin-top: 18rpx; }
.time { font-size: 22rpx; color: #bbb; }
.ops { display: flex; gap: 14rpx; }
.btn { font-size: 24rpx; color: #ff2d2d; border: 1rpx solid #ff2d2d; border-radius: 30rpx; padding: 8rpx 22rpx; }
.btn.ghost { color: #666; border-color: #ddd; }
</style>
