<template>
  <view class="detail">
    <swiper class="swiper" circular indicator-dots autoplay>
      <swiper-item v-for="(img, i) in images" :key="i">
        <image class="swiper-img" :src="img" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <view class="section">
      <view class="price-row">
        <text class="price">￥{{ fen2yuan(selPrice) }}</text>
        <text class="origin" v-if="goods.marketPrice > selPrice">￥{{ fen2yuan(goods.marketPrice) }}</text>
        <text class="sales">已售 {{ goods.sales }}</text>
      </view>
      <view class="name">{{ goods.name }}</view>
      <view class="subtitle" v-if="goods.subtitle">{{ goods.subtitle }}</view>
    </view>

    <view class="section" v-if="goods.content">
      <rich-text :nodes="goods.content" />
    </view>

    <!-- 底部操作栏 -->
    <view class="footer">
      <view class="footer-btn cart" @click="openSpec('cart')">加入购物车</view>
      <view class="footer-btn buy" @click="openSpec('buy')">立即购买</view>
    </view>

    <!-- 规格弹窗 -->
    <view v-if="showSpec" class="mask" @click="showSpec = false"></view>
    <view v-if="showSpec" class="spec-panel">
      <view class="spec-head">
        <view>
          <view class="spec-price">￥{{ fen2yuan(selPrice) }}</view>
          <view class="spec-stock">库存 {{ selStock }} 件</view>
        </view>
        <text class="close" @click="showSpec = false">×</text>
      </view>
      <view class="spec-label">选择规格</view>
      <scroll-view scroll-x class="spec-items">
        <view
          v-for="it in goods.items"
          :key="it.id"
          class="spec-item"
          :class="{ active: selItemId === it.id, disabled: it.stock <= 0 }"
          @click="selectItem(it)"
        >
          {{ it.specValueStr || '默认' }}
        </view>
      </scroll-view>
      <view class="qty-row">
        <text>数量</text>
        <view class="stepper">
          <text class="step" @click="qty > 1 && qty--">-</text>
          <text class="num">{{ qty }}</text>
          <text class="step" @click="qty < selStock && qty++">+</text>
        </view>
      </view>
      <button class="confirm-btn" @click="confirmSpec">确定</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getGoodsDetail } from '@/api/goods'
import { addCart } from '@/api/cart'
import { fen2yuan } from '@/utils/format'

const goods = ref({})
const images = ref([])
const selItemId = ref(null)
const selPrice = ref(0)
const selStock = ref(0)
const qty = ref(1)
const showSpec = ref(false)
const action = ref('cart')

async function load(id) {
  const res = await getGoodsDetail(id)
  goods.value = res.data
  images.value = (res.data.images && res.data.images.length) ? res.data.images : [res.data.image]
  if (res.data.items && res.data.items.length) {
    selectItem(res.data.items[0])
  }
}

function selectItem(it) {
  selItemId.value = it.id
  selPrice.value = it.price
  selStock.value = it.stock
  if (qty.value > it.stock) qty.value = Math.max(1, it.stock)
}

function openSpec(a) {
  action.value = a
  qty.value = 1
  showSpec.value = true
}

async function confirmSpec() {
  if (!selItemId.value) return
  showSpec.value = false
  if (action.value === 'cart') {
    await addCart({ goodsId: goods.value.id, goodsItemId: selItemId.value, num: qty.value })
    uni.showToast({ title: '已加入购物车' })
  } else {
    uni.navigateTo({
      url: `/pages/order/confirm?buyType=buyNow&goodsId=${goods.value.id}&goodsItemId=${selItemId.value}&num=${qty.value}`
    })
  }
}

onLoad((opts) => load(opts.id))
</script>

<style scoped>
.detail {
  padding-bottom: 120rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.swiper {
  height: 750rpx;
  background: #fff;
}
.swiper-img {
  width: 100%;
  height: 750rpx;
}
.section {
  background: #fff;
  padding: 24rpx;
  border-radius: 16rpx;
  margin: 16rpx;
}
.price-row {
  display: flex;
  align-items: baseline;
}
.price {
  color: #ff2d2d;
  font-size: 44rpx;
  font-weight: bold;
}
.origin {
  margin-left: 14rpx;
  color: #bbb;
  font-size: 24rpx;
  text-decoration: line-through;
}
.sales {
  margin-left: auto;
  color: #999;
  font-size: 24rpx;
}
.name {
  margin-top: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
}
.subtitle {
  margin-top: 8rpx;
  color: #999;
  font-size: 26rpx;
}
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  padding: 16rpx;
  background: #fff;
  gap: 16rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}
.footer-btn {
  flex: 1;
  text-align: center;
  border-radius: 44rpx;
  padding: 20rpx 0;
  color: #fff;
  font-size: 30rpx;
}
.cart {
  background: linear-gradient(90deg, #ff9900, #ff6a00);
}
.buy {
  background: linear-gradient(90deg, #ff2d2d, #ff1a1a);
}
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 10;
}
.spec-panel {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  padding: 30rpx;
  z-index: 11;
}
.spec-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.spec-price {
  color: #ff2d2d;
  font-size: 40rpx;
  font-weight: bold;
}
.spec-stock {
  color: #999;
  font-size: 24rpx;
  margin-top: 8rpx;
}
.close {
  font-size: 48rpx;
  color: #999;
}
.spec-label {
  margin: 24rpx 0 16rpx;
  font-size: 28rpx;
  font-weight: bold;
}
.spec-items {
  white-space: nowrap;
}
.spec-item {
  display: inline-block;
  padding: 14rpx 30rpx;
  margin-right: 16rpx;
  background: #f5f5f5;
  border-radius: 10rpx;
  font-size: 26rpx;
  border: 2rpx solid transparent;
}
.spec-item.active {
  border-color: #ff2d2d;
  color: #ff2d2d;
}
.spec-item.disabled {
  color: #bbb;
}
.qty-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 30rpx 0;
}
.stepper {
  display: flex;
  align-items: center;
}
.step {
  width: 56rpx;
  height: 56rpx;
  border: 1rpx solid #ddd;
  text-align: center;
  line-height: 52rpx;
  font-size: 32rpx;
}
.num {
  width: 72rpx;
  text-align: center;
  font-size: 30rpx;
}
.confirm-btn {
  background: #ff2d2d;
  color: #fff;
  border-radius: 44rpx;
}
</style>
