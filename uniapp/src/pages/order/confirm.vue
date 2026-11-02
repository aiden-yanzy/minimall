<template>
  <view class="confirm">
    <view class="section">
      <view class="addr-title">收货信息</view>
      <view v-if="selectedAddr" class="addr-card" @click="chooseAddress">
        <view class="addr-line">{{ selectedAddr.contact }} {{ selectedAddr.mobile }} <text v-if="selectedAddr.isDefault === 1" class="def-tag">默认</text></view>
        <view class="addr-line2">{{ selectedAddr.address }}</view>
        <view class="change">切换地址</view>
      </view>
      <template v-else>
        <view class="addr-row">
          <input v-model="contact" class="ipt" placeholder="收货人" />
          <input v-model="mobile" class="ipt" type="number" placeholder="手机号" />
        </view>
        <input v-model="address" class="ipt full" placeholder="详细地址" />
        <view class="change" @click="chooseAddress">从地址簿选择</view>
      </template>
    </view>

    <view class="section">
      <view v-for="g in goodsList" :key="g.key" class="g-item">
        <image class="img" :src="asset(g.image)" mode="aspectFill" />
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
      <view class="pay-row" @click="showCoupon = true">
        <text>优惠券</text>
        <text class="pay-name">{{ couponText }}</text>
      </view>
      <view class="amount-row" v-if="preview">
        <text>商品金额</text><text>￥{{ fen2yuan(preview.totalAmount) }}</text>
      </view>
      <view class="amount-row" v-if="preview?.memberDiscount">
        <text>会员优惠({{ preview.memberName || '' }})</text><text class="red">-￥{{ fen2yuan(preview.memberDiscount) }}</text>
      </view>
      <view class="amount-row" v-if="preview?.fullReduction">
        <text>满减</text><text class="red">-￥{{ fen2yuan(preview.fullReduction) }}</text>
      </view>
      <view class="amount-row" v-if="preview?.couponAmount">
        <text>优惠券</text><text class="red">-￥{{ fen2yuan(preview.couponAmount) }}</text>
      </view>
      <view class="amount-row" v-if="preview?.freightAmount"><text>运费</text><text>￥{{ fen2yuan(preview.freightAmount) }}</text></view>
      <view class="total-row">
        <text>应付</text>
        <text class="total">￥{{ fen2yuan(preview?.payAmount ?? totalPrice) }}</text>
      </view>
    </view>

    <view class="footer">
      <view class="total">合计 ￥{{ fen2yuan(preview?.payAmount ?? totalPrice) }}</view>
      <view class="submit" :class="{ loading: submitting }" @click="submit">提交订单</view>
    </view>

    <!-- 支付方式 -->
    <view v-if="showPay" class="mask" @click="showPay = false"></view>
    <view v-if="showPay" class="sheet">
      <view class="pay-option" @click="selectPay(1)">微信支付<text v-if="payType === 1">✓</text></view>
      <view class="pay-option" @click="selectPay(2)">支付宝<text v-if="payType === 2">✓</text></view>
      <view class="pay-option" @click="selectPay(3)">余额支付<text v-if="payType === 3">✓</text></view>
    </view>

    <!-- 优惠券 -->
    <view v-if="showCoupon" class="mask" @click="showCoupon = false"></view>
    <view v-if="showCoupon" class="sheet">
      <view class="pay-option" @click="selectCoupon(null)">不使用优惠券</view>
      <view v-for="c in available" :key="c.id" class="pay-option" @click="selectCoupon(c)">
        {{ c.couponName }}
        <text v-if="selectedCouponId === c.id">✓</text>
      </view>
      <view v-if="!available.length" class="pay-option none">暂无可用优惠券</view>
    </view>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getCartList } from '@/api/cart'
import { getGoodsDetail } from '@/api/goods'
import { getSeckillList } from '@/api/seckill'
import { getDefaultAddress } from '@/api/member'
import { createOrder, previewOrder } from '@/api/order'
import { fen2yuan } from '@/utils/format'

const buyType = ref('cart')
const goodsId = ref(null)
const goodsItemId = ref(null)
const seckillGoodsId = ref(null)
const num = ref(1)

const contact = ref('')
const mobile = ref('')
const address = ref('')
const selectedAddr = ref(null)
const goodsList = ref([])
const payType = ref(1)
const showPay = ref(false)
const showCoupon = ref(false)
const submitting = ref(false)

const preview = ref(null)
const available = ref([])
const selectedCouponId = ref(null)

const payTypeName = computed(() => ({ 1: '微信支付', 2: '支付宝', 3: '余额支付' }[payType.value]))
const totalPrice = computed(() => goodsList.value.reduce((s, g) => s + g.price * g.num, 0))
const couponText = computed(() => {
  const c = available.value.find((i) => i.id === selectedCouponId.value)
  return c ? c.couponName : '不使用优惠券'
})

async function init() {
  try {
    const d = await getDefaultAddress()
    if (d.data && d.data.id) selectedAddr.value = d.data
  } catch (e) {}
  if (buyType.value === 'seckill') {
    const res = await getSeckillList()
    let sg = null
    for (const a of res.data) {
      const f = a.goodsList.find((g) => g.id == seckillGoodsId.value)
      if (f) { sg = f; break }
    }
    if (!sg) return uni.showToast({ title: '秒杀商品已失效', icon: 'none' })
    const gd = await getGoodsDetail(sg.goodsId)
    goodsList.value = [{
      key: 's' + sg.id,
      id: sg.goodsItemId,
      name: sg.goodsName,
      image: sg.goodsImage,
      spec: sg.specValueStr,
      price: sg.seckillPrice,
      num: 1
    }]
    return fetchPreview()
  }
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
  await fetchPreview()
}

function buildDto(couponListId) {
  const dto = { buyType: buyType.value, payType: payType.value }
  if (seckillGoodsId.value) dto.seckillGoodsId = seckillGoodsId.value
  if (couponListId) dto.couponListId = couponListId
  if (buyType.value === 'buyNow') {
    dto.goodsItemId = goodsItemId.value
    dto.num = num.value
  }
  return dto
}

async function fetchPreview(couponListId) {
  const res = await previewOrder(buildDto(couponListId === undefined ? selectedCouponId.value : couponListId))
  preview.value = res.data
  available.value = res.data.availableCoupons || []
}

function selectPay(t) {
  payType.value = t
  showPay.value = false
}

async function selectCoupon(c) {
  selectedCouponId.value = c ? c.id : null
  showCoupon.value = false
  await fetchPreview()
}

async function submit() {
  if (selectedAddr.value) {
    contact.value = selectedAddr.value.contact
    mobile.value = selectedAddr.value.mobile
    address.value = selectedAddr.value.address
  }
  if (!contact.value || !mobile.value || !address.value) {
    return uni.showToast({ title: '请填写完整收货信息', icon: 'none' })
  }
  submitting.value = true
  try {
    const payload = {
      buyType: buyType.value,
      addressId: selectedAddr.value ? selectedAddr.value.id : null,
      contact: contact.value,
      mobile: mobile.value,
      address: address.value,
      payType: payType.value
    }
    if (seckillGoodsId.value) payload.seckillGoodsId = seckillGoodsId.value
    if (selectedCouponId.value) payload.couponListId = selectedCouponId.value
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

function chooseAddress() {
  uni.navigateTo({ url: '/pages/member/address?choose=1' })
}

onLoad((opts) => {
  buyType.value = opts.buyType || 'cart'
  if (buyType.value === 'seckill') {
    seckillGoodsId.value = opts.seckillGoodsId
  }
  if (buyType.value === 'buyNow') {
    goodsId.value = opts.goodsId
    goodsItemId.value = opts.goodsItemId
    num.value = parseInt(opts.num) || 1
  }
  init()
})

onShow(() => {
  // 从地址簿选中返回
  const a = uni.getStorageSync('selectedAddress')
  if (a && a.id) {
    selectedAddr.value = a
    uni.removeStorageSync('selectedAddress')
  }
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
.addr-title { font-weight: bold; margin-bottom: 16rpx; }
.addr-card { background: #fff8f0; border-radius: 10rpx; padding: 20rpx; }
.addr-line { font-weight: bold; }
.addr-line2 { color: #666; font-size: 24rpx; margin-top: 6rpx; }
.def-tag { font-size: 20rpx; color: #ff2d2d; border: 1rpx solid #ff2d2d; border-radius: 6rpx; padding: 0 8rpx; }
.change { color: #3370ff; font-size: 24rpx; margin-top: 12rpx; }
.addr-row { display: flex; gap: 16rpx; }
.ipt {
  flex: 1;
  height: 72rpx;
  border: 1rpx solid #eee;
  border-radius: 10rpx;
  padding: 0 20rpx;
  margin-bottom: 16rpx;
  font-size: 26rpx;
}
.ipt.full { width: 100%; box-sizing: border-box; }
.g-item { display: flex; align-items: center; margin-bottom: 20rpx; }
.img { width: 120rpx; height: 120rpx; border-radius: 10rpx; background: #f0f0f0; }
.g-info { flex: 1; margin-left: 16rpx; }
.g-name { font-size: 26rpx; }
.g-spec { color: #999; font-size: 22rpx; margin-top: 6rpx; }
.g-price { text-align: right; }
.g-num { color: #999; margin-top: 10rpx; }
.pay-row { display: flex; justify-content: space-between; padding: 14rpx 0; }
.amount-row { display: flex; justify-content: space-between; padding: 8rpx 0; color: #333; font-size: 26rpx; }
.red { color: #ff2d2d; }
.total-row { display: flex; justify-content: space-between; padding: 10rpx 0; font-weight: bold; }
.total { color: #ff2d2d; font-size: 36rpx; }
.footer {
  position: fixed; left: 0; right: 0; bottom: 0;
  background: #fff; display: flex; align-items: center; justify-content: flex-end;
  padding: 16rpx 20rpx; gap: 30rpx;
}
.submit { background: #ff2d2d; color: #fff; border-radius: 44rpx; padding: 20rpx 60rpx; }
.mask { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.5); z-index: 10; }
.sheet { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; z-index: 11; padding: 20rpx; max-height: 60vh; overflow-y: auto; }
.pay-option { padding: 30rpx; border-bottom: 1rpx solid #f0f0f0; display: flex; justify-content: space-between; }
.pay-option.none { justify-content: center; color: #999; }
</style>