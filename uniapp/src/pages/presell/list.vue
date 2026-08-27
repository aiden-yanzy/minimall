<template>
  <view class="wrap">
    <view v-for="a in acts" :key="a.id" class="act">
      <view class="goods">
        <image class="img" :src="a.goodsImage" mode="aspectFill" />
        <view class="info">
          <view class="name">{{ a.name }}</view>
          <view class="desc">付定金 ￥{{ fen2yuan(a.deposit) }}，尾款 ￥{{ fen2yuan(a.finalPrice) }}</view>
          <view class="stock">预售库存 {{ a.stock }} 件</view>
        </view>
        <view class="btn" @click="buy(a)">付定金</view>
      </view>
    </view>

    <view class="title-line">我的预售订单</view>
    <view v-for="o in mine" :key="o.id" class="order">
      <view class="o-head">
        <text class="sn">{{ o.orderSn }}</text>
        <text class="st">{{ stMap[o.status] }}</text>
      </view>
      <view class="o-body">
        <view>
          <view class="name">{{ o.goodsName }}</view>
          <view class="desc">定金 {{ fen2yuan(o.deposit) }} + 尾款 {{ fen2yuan(o.finalPrice) }}{{ o.expressNo ? (' · ' + o.expressNo) : '' }}</view>
        </view>
        <view class="o-actions">
          <view v-if="o.status === 0" class="btn small" @click="pay(o, 'deposit')">付定金</view>
          <view v-if="o.status === 1" class="btn small" @click="pay(o, 'final')">付尾款</view>
        </view>
      </view>
    </view>
    <view v-if="!acts.length" class="empty">暂无预售活动</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getPresellList, presellOrder, presellPayDeposit, presellPayFinal, presellMine } from '@/api/p5'
import { fen2yuan } from '@/utils/format'

const stMap = { 0: '待付定金', 1: '待付尾款', 2: '已完成', 3: '已关闭' }
const acts = ref([])
const mine = ref([])

async function load() {
  const [a, m] = await Promise.all([getPresellList(), presellMine()])
  acts.value = a.data
  mine.value = m.data
}
function buy(a) {
  uni.showModal({
    title: '付定金预订', editable: true, placeholderText: '收货人 手机号 地址(空格分隔)',
    success: async (r) => {
      const parts = (r.content || '').split(/[\s，,]+/)
      if (parts.length < 3) return uni.showToast({ title: '格式：姓名 手机 地址', icon: 'none' })
      try {
        const res = await presellOrder({ activityId: a.id, contact: parts[0], mobile: parts[1], address: parts[2] })
        await presellPayDeposit(res.data)
        uni.showToast({ title: '定金已支付' })
        load()
      } catch (e) {}
    }
  })
}
async function pay(o, kind) {
  try {
    if (kind === 'deposit') await presellPayDeposit(o.id)
    else await presellPayFinal(o.id)
    uni.showToast({ title: '支付成功' })
    load()
  } catch (e) {}
}

onShow(load)
</script>

<style scoped>
.wrap { padding: 20rpx; background: #f5f5f5; min-height: 100vh; }
.act { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.goods { display: flex; align-items: center; }
.img { width: 150rpx; height: 150rpx; border-radius: 10rpx; background: #f0f0f0; }
.info { flex: 1; margin-left: 16rpx; }
.name { font-size: 28rpx; font-weight: bold; }
.desc { color: #3370ff; font-size: 24rpx; margin-top: 8rpx; }
.stock { color: #999; font-size: 22rpx; margin-top: 6rpx; }
.btn { background: #3370ff; color: #fff; border-radius: 30rpx; padding: 14rpx 34rpx; font-size: 26rpx; }
.btn.small { padding: 10rpx 26rpx; font-size: 24rpx; }
.title-line { color: #333; font-weight: bold; margin: 10rpx 0 16rpx; }
.order { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 16rpx; }
.o-head { display: flex; justify-content: space-between; border-bottom: 1rpx solid #f0f0f0; padding-bottom: 14rpx; }
.sn { color: #666; font-size: 22rpx; }
.st { color: #3370ff; }
.o-body { display: flex; justify-content: space-between; align-items: center; margin-top: 14rpx; }
.empty { text-align: center; color: #999; padding: 120rpx 0; }
</style>