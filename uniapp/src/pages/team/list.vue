<template>
  <view class="wrap">
    <view v-for="a in list" :key="a.id" class="act">
      <view class="act-head">
        <text class="name">{{ a.name }}</text>
        <text class="team">N人拼团 ￥{{ fen2yuan(a.teamPrice) }}</text>
      </view>
      <view class="goods">
        <image class="img" :src="asset(a.goodsImage)" mode="aspectFill" />
        <view class="info">
          <view class="name">{{ a.goodsName }}</view>
          <view class="spec">{{ a.specValueStr }}</view>
          <view class="prices">
            <text class="team-price">￥{{ fen2yuan(a.teamPrice) }}</text>
            <text class="market">￥{{ fen2yuan(a.marketPrice) }}</text>
          </view>
          <view class="rule">满{{ a.teamNum }}人成团 · 人限{{ a.perLimit }}件</view>
        </view>
        <view class="btn" @click="openBuy(a)">去拼团</view>
      </view>
    </view>

    <view v-if="!list.length" class="empty">暂无拼团活动</view>

    <!-- 拼团下单表单 -->
    <view v-if="showBuy" class="mask" @click="showBuy = false"></view>
    <view v-if="showBuy" class="sheet">
      <view class="sheet-title">拼团下单 - {{ current?.name }}</view>
      <input v-model="contact" class="ipt" placeholder="收货人" />
      <input v-model="mobile" class="ipt" type="number" placeholder="手机号" />
      <input v-model="address" class="ipt" placeholder="详细地址" />
      <button class="confirm" :loading="submitting" @click="submitBuy">下单({{ fen2yuan(current?.teamPrice) }}元)</button>
    </view>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getTeamList, teamBuy, teamJoin } from '@/api/p5'
import { getOrderDetail, mockPay } from '@/api/order'
import { fen2yuan } from '@/utils/format'

const list = ref([])
const showBuy = ref(false)
const current = ref(null)
const contact = ref('')
const mobile = ref('')
const address = ref('')
const submitting = ref(false)

async function load() {
  const res = await getTeamList()
  list.value = res.data
}
function openBuy(a) {
  current.value = a
  showBuy.value = true
}
async function submitBuy() {
  if (!contact.value || !mobile.value || !address.value) return uni.showToast({ title: '请完善收货信息', icon: 'none' })
  submitting.value = true
  try {
    const res = await teamBuy({
      activityId: current.value.id,
      contact: contact.value,
      mobile: mobile.value,
      address: address.value
    })
    const orderId = res.data
    const d = await getOrderDetail(orderId)
    await mockPay(d.data.orderSn, 1)
    const r = await teamJoin(orderId)
    uni.showToast({ title: r.data.success ? '拼团成功🎉' : '已参团，等待成团' })
    showBuy.value = false
    setTimeout(() => uni.navigateTo({ url: '/pages/order/detail?id=' + orderId }), 600)
  } finally {
    submitting.value = false
  }
}

onShow(load)
</script>

<style scoped>
.wrap { padding: 20rpx; background: #f5f5f5; min-height: 100vh; }
.act { background: #fff; border-radius: 16rpx; margin-bottom: 20rpx; overflow: hidden; }
.act-head { background: linear-gradient(90deg, #ff7a00, #ff2d55); color: #fff; padding: 20rpx 24rpx; display: flex; justify-content: space-between; }
.name { font-size: 30rpx; font-weight: bold; }
.team { font-size: 22rpx; opacity: .9; }
.goods { display: flex; padding: 20rpx; align-items: center; }
.img { width: 150rpx; height: 150rpx; border-radius: 10rpx; background: #f0f0f0; }
.info { flex: 1; margin-left: 16rpx; }
.name { font-size: 28rpx; }
.spec { color: #999; font-size: 22rpx; margin-top: 4rpx; }
.prices { margin-top: 10rpx; display: flex; align-items: baseline; }
.team-price { color: #ff2d55; font-size: 36rpx; font-weight: bold; }
.market { margin-left: 12rpx; color: #bbb; font-size: 22rpx; text-decoration: line-through; }
.rule { color: #999; font-size: 20rpx; margin-top: 6rpx; }
.btn { background: #ff2d55; color: #fff; border-radius: 30rpx; padding: 14rpx 32rpx; }
.empty { text-align: center; color: #999; padding: 120rpx 0; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); z-index: 10; }
.sheet { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; z-index: 11; padding: 30rpx; }
.sheet-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; }
.ipt { height: 76rpx; border: 1rpx solid #eee; border-radius: 10rpx; padding: 0 20rpx; margin-bottom: 16rpx; }
.confirm { background: #ff2d55; color: #fff; border-radius: 44rpx; }
</style>