<template>
  <view class="confirm">
    <!-- 收货地址 -->
    <view class="section">
      <view class="addr-title">收货信息</view>
      <view class="addr-row">
        <input v-model="contact" class="ipt" placeholder="收货人" />
        <input v-model="mobile" class="ipt" type="number" placeholder="手机号" />
      </view>
      <input v-model="address" class="ipt full" placeholder="详细地址" />
    </view>

    <!-- 商品 -->
    <view class="section">
      <view v-for="g in goodsList" :key="g.key" class="g-item">
        <image class="img" :src="g.image" mode="aspectFill" />
        <view class="g-info">
          <view class="g-name">{{ g.name }}</view>
          <view class="g-spec">{{ g.spec }}</view>
        </view>
        <view class="g-price">
          <view>￥{{ fen2yuan(g.price) }}</view>
          <view class="g-num">x{{ g.num }}</view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="pay-row" @click="showPay = !showPay">
        <text>支付方式</text>
        <text class="pay-name">{{ payTypeName }}</text>
      </view>
      <view class="total-row">
        <text>应付</text>
        <text class="total">￥{{ fen2yuan(totalPrice) }}</text>
      </view>
    </view>

    <!-- 底部提交 -->
    <view class="footer">
      <view class="total">合计 ￥{{ fen2yuan(totalPrice) }}</view>
      <view class="submit" @click="submit" :class="{ loading: submitting }">提交订单</view>
    </view>

    <!-- 支付方式弹层 -->
    <view v-if="showPay" class="mask" @click="showPay = false"></view>
    <view v-if="showPay" class="sheet">
      <view class="pay-option" @click="selectPay(1)">微信支付<text v-if="payType === 1">✓</text></view>
      <view class="pay-option" @click="selectPay(2)">支付宝<text v-if="payType === 2">✓</text></view>
      <view class="pay-option" @click="selectPay(3)">余额支付<text v-if="payType === 3">✓</text></view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCartList } from '@/api/cart'
import { getGoodsDetail } from '@/api/goods'
import { createOrder } from '@/api/order'
import { fen2yuan } from '@/utils/format'

const buyType = ref('cart')
const goodsId = ref(null)
const goodsItemId = ref(null)
const num = ref(1)

const contact = ref('')
const mobile = ref('')
const address = ref('')
const goodsList = ref([])
const payType = ref(1)
const showPay = ref(false)
const submitting = ref(false)

const payTypeName = computed(() => ({ 1: '微信支付', 2: '支付宝', 3: '余额支付' }[payType.value]))

const totalPrice = computed(() => goodsList.value.reduce((s, g) => s + g.price * g.num, 0))

async function init() {
  if (buyType.value === 'cart') {
    const res = await getCartList()
    goodsList.value = res.data
      .filter((i) => i.isSelected === 1)
      .map((i) => ({ key: 'c' + i.id, id: i.goodsItemId, name: i.goodsName, image: i.goodsImage, spec: i.specValueStr, price: i.price, num: i.num }))
  } else {
    const res = await getGoodsDetail(goodsId.value)
    const item = res.data.items.find((i) => i.id == goodsItemId.value)
    if (!item) return uni.showToast({ title: '商品已失效', icon: 'none' })
    goodsList.value = [{
      key: 'd' + goodsItemId.value,
      id: goodsItemId.value,
      name: res.data.name,
      image: res.data.image,
      spec: item.specValueStr,
      price: item.price,
      num: num.value
    }]
  }
}

function selectPay(t) {
  payType.value = t
  showPay.value = false
}

async function submit() {
  if (!contact.value || !mobile.value || !address.value) {
    return uni.showToast({ title: '请填写完整收货信息', icon: 'none' })
  }
  submitting.value = true
  try {
    const payload = {
      buyType: buyType.value,
      contact: contact.value,
      mobile: mobile.value,
      address: address.value,
      remark: '',
      payType: payType.value
    }
    if (buyType.value === 'buyNow') {
      payload.goodsItemId = goodsItemId.value
      payload.num = num.value
    }
    const res = await createOrder(payload)
    uni.showToast({ title: '下单成功' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/order/detail?id=' + res.data })
    }, 500)
  } finally {
    submitting.value = false
  }
}

onLoad((opts) => {
  buyType.value = opts.buyType || 'cart'
  if (buyType.value === 'buyNow') {
    goodsId.value = opts.goodsId
    goodsItemId.value = opts.goodsItemId
    num.value = parseInt(opts.num) || 1
  }
  init()
})
</script>

<style scoped>
.confirm {
  padding: 20rpx;
  padding-bottom: 120rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}
.addr-title {
  font-weight: bold;
  margin-bottom: 16rpx;
}
.addr-row {
  display: flex;
  gap: 16rpx;
}
.ipt {
  flex: 1;
  height: 72rpx;
  border: 1rpx solid #eee;
  border-radius: 10rpx;
  padding: 0 20rpx;
  margin-bottom: 16rpx;
  font-size: 26rpx;
}
.ipt.full {
  width: 100%;
  box-sizing: border-box;
}
.g-item {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}
.img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 10rpx;
  background: #f0f0f0;
}
.g-info {
  flex: 1;
  margin-left: 16rpx;
}
.g-name {
  font-size: 26rpx;
}
.g-spec {
  color: #999;
  font-size: 22rpx;
  margin-top: 6rpx;
}
.g-price {
  text-align: right;
  color: #333;
}
.g-num {
  color: #999;
  margin-top: 10rpx;
}
.pay-row {
  display: flex;
  justify-content: space-between;
  padding: 10rpx 0;
}
.pay-name {
  color: #333;
}
.total-row {
  display: flex;
  justify-content: space-between;
  padding: 10rpx 0;
  font-weight: bold;
}
.total {
  color: #ff2d2d;
  font-size: 36rpx;
}
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 16rpx 20rpx;
  gap: 30rpx;
}
.submit {
  background: #ff2d2d;
  color: #fff;
  border-radius: 44rpx;
  padding: 20rpx 60rpx;
}
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 10;
}
.sheet {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  z-index: 11;
  padding: 20rpx;
}
.pay-option {
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  color: #333;
}
</style>
