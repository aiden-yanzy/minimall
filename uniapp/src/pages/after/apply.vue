<template>
  <view class="apply">
    <view v-if="goods" class="goods-card">
      <image class="img" :src="asset(goods.goodsImage)" mode="aspectFill" />
      <view class="info">
        <view class="name">{{ goods.goodsName }}</view>
        <view class="spec">{{ goods.specValueStr }}</view>
        <view class="price">￥{{ fen2yuan(goods.price) }} · x{{ goods.num }}</view>
      </view>
    </view>

    <view class="section">
      <view class="label">售后类型</view>
      <view class="types">
        <view
          v-for="t in types"
          :key="t.value"
          class="type"
          :class="{ on: form.type === t.value }"
          @click="form.type = t.value"
        >{{ t.label }}</view>
      </view>
    </view>

    <view class="section">
      <view class="label">退款金额</view>
      <view class="amount-row">
        <text class="cny">￥</text>
        <input v-model="amountText" class="amount-input" type="digit" placeholder="0.00" />
        <text class="all" @click="fillAll">全额</text>
      </view>
      <view class="tip">最多可退 ￥{{ fen2yuan(maxAmount) }}（该商品实付金额）</view>
    </view>

    <view class="section">
      <view class="label">申请原因</view>
      <textarea v-model="form.reason" class="reason" maxlength="120" placeholder="请描述问题，便于商家快速处理" />
      <view class="quick">
        <text v-for="r in quickReasons" :key="r" class="quick-item" @click="form.reason = r">{{ r }}</text>
      </view>
    </view>

    <view class="notice">
      提交后商家会在 48 小时内处理；同意退款将退回你的账户余额，可在「我的钱包」查看流水。
    </view>

    <button class="submit" :loading="submitting" @click="submit">提交申请</button>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createAfterSale } from '@/api/after'
import { getOrderDetail } from '@/api/order'
import { fen2yuan } from '@/utils/format'

const types = [
  { label: '仅退款', value: 1 },
  { label: '退货退款', value: 2 },
  { label: '换货', value: 3 }
]
const quickReasons = ['不想要了', '商品质量问题', '与描述不符', '少发/漏发', '包装破损']

const orderId = ref(null)
const orderGoodsId = ref(null)
const goods = ref(null)
const form = ref({ type: 1, reason: '' })
const amountText = ref('')
const submitting = ref(false)

const maxAmount = computed(() => (goods.value ? goods.value.totalPrice || goods.value.price * goods.value.num : 0))

function fillAll() {
  amountText.value = (maxAmount.value / 100).toFixed(2)
}

onLoad(async (q) => {
  orderId.value = Number(q.orderId)
  orderGoodsId.value = Number(q.orderGoodsId)
  const res = await getOrderDetail(orderId.value)
  const list = res.data?.goodsList || []
  goods.value = list.find((g) => g.id === orderGoodsId.value) || list[0] || null
  if (goods.value) {
    orderGoodsId.value = goods.value.id
    fillAll()
  }
})

async function submit() {
  if (!form.value.reason.trim()) return uni.showToast({ title: '请填写申请原因', icon: 'none' })
  const yuan = Number(amountText.value || 0)
  if (!(yuan > 0)) return uni.showToast({ title: '请填写退款金额', icon: 'none' })
  submitting.value = true
  try {
    await createAfterSale({
      orderId: orderId.value,
      orderGoodsId: orderGoodsId.value,
      type: form.value.type,
      reason: form.value.reason.trim(),
      refundAmount: Math.round(yuan * 100)
    })
    uni.showToast({ title: '提交成功' })
    setTimeout(() => uni.redirectTo({ url: '/pages/after/list' }), 800)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.apply { padding: 20rpx 24rpx 60rpx; background: #f5f5f5; min-height: 100vh; }
.goods-card { display: flex; gap: 20rpx; background: #fff; border-radius: 16rpx; padding: 20rpx; }
.img { width: 120rpx; height: 120rpx; border-radius: 12rpx; background: #f0f0f0; }
.name { font-size: 28rpx; color: #222; }
.spec { font-size: 24rpx; color: #999; margin-top: 8rpx; }
.price { font-size: 26rpx; color: #ff2d2d; margin-top: 10rpx; }
.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-top: 20rpx; }
.label { font-size: 28rpx; font-weight: 600; color: #222; margin-bottom: 16rpx; }
.types { display: flex; gap: 16rpx; }
.type { flex: 1; text-align: center; padding: 18rpx 0; font-size: 26rpx; border-radius: 12rpx; background: #f5f6f8; color: #555; }
.type.on { background: #fff1f0; color: #ff2d2d; border: 1rpx solid #ff2d2d; }
.amount-row { display: flex; align-items: center; gap: 8rpx; }
.cny { font-size: 32rpx; color: #ff2d2d; }
.amount-input { flex: 1; font-size: 36rpx; color: #222; }
.all { font-size: 24rpx; color: #3370ff; }
.tip { font-size: 22rpx; color: #999; margin-top: 10rpx; }
.reason { width: 100%; height: 160rpx; font-size: 26rpx; background: #f7f8fa; border-radius: 12rpx; padding: 16rpx; box-sizing: border-box; }
.quick { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 14rpx; }
.quick-item { font-size: 22rpx; color: #666; background: #f5f6f8; padding: 8rpx 16rpx; border-radius: 20rpx; }
.notice { font-size: 22rpx; color: #999; margin: 20rpx 8rpx; line-height: 1.6; }
.submit { margin-top: 30rpx; background: #ff2d2d; color: #fff; border-radius: 44rpx; font-size: 30rpx; }
</style>
