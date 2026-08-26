<template>
  <view class="cart">
    <view v-if="!items.length" class="empty">
      <text>购物车空空如也</text>
      <button class="go-shop" @click="goShop">去逛逛</button>
    </view>

    <view v-for="it in items" :key="it.id" class="cart-item">
      <view class="check" :class="{ on: it.isSelected === 1 }" @click="toggle(it)">
        <text v-if="it.isSelected === 1">✓</text>
      </view>
      <image class="img" :src="it.goodsImage" mode="aspectFill" @click="goDetail(it.goodsId)" />
      <view class="info">
        <view class="name" @click="goDetail(it.goodsId)">{{ it.goodsName }}</view>
        <view class="spec">{{ it.specValueStr }}</view>
        <view class="row">
          <view class="price">￥{{ fen2yuan(it.price) }}</view>
          <view class="stepper">
            <text class="step" @click="changeNum(it, it.num - 1)">-</text>
            <text class="num">{{ it.num }}</text>
            <text class="step" @click="changeNum(it, it.num + 1)">+</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="items.length" class="footer">
      <view class="all-check" @click="toggleAll">
        <view class="check" :class="{ on: allSelected }"><text v-if="allSelected">✓</text></view>
        <text>全选</text>
      </view>
      <view class="total">
        合计：<text class="total-price">￥{{ fen2yuan(totalPrice) }}</text>
      </view>
      <view class="settle" @click="goConfirm">去结算({{ selectedCount }})</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCartList, updateCartNum, updateSelected, removeCart } from '@/api/cart'
import { useUserStore } from '@/store/user'
import { fen2yuan } from '@/utils/format'

const items = ref([])
const userStore = useUserStore()

async function load() {
  if (!userStore.token) {
    items.value = []
    return uni.navigateTo({ url: '/pages/user/login' })
  }
  const res = await getCartList()
  items.value = res.data
}

async function toggle(it) {
  it.isSelected = it.isSelected === 1 ? 0 : 1
  await updateSelected(it.id, it.isSelected)
}
async function toggleAll() {
  const target = !allSelected.value ? 1 : 0
  items.value.forEach((i) => (i.isSelected = target))
  await Promise.all(items.value.map((i) => updateSelected(i.id, target)))
}
async function changeNum(it, num) {
  if (num < 1) {
    const r = await confirm('删除该商品？')
    if (r) {
      await removeCart(it.id)
      load()
    }
    return
  }
  it.num = num
  await updateCartNum(it.id, num)
}

const selectedItems = computed(() => items.value.filter((i) => i.isSelected === 1))
const allSelected = computed(() => items.value.length > 0 && selectedItems.value.length === items.value.length)
const selectedCount = computed(() => selectedItems.value.length)
const totalPrice = computed(() => selectedItems.value.reduce((s, i) => s + i.price * i.num, 0))

function goConfirm() {
  if (!selectedItems.value.length) return uni.showToast({ title: '请先勾选商品', icon: 'none' })
  uni.navigateTo({ url: '/pages/order/confirm?buyType=cart' })
}
function goDetail(id) {
  uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
}
function goShop() {
  uni.switchTab({ url: '/pages/index/index' })
}
function confirm(title) {
  return new Promise((resolve) => {
    uni.showModal({ title: '提示', content: title, success: (r) => resolve(r.confirm) })
  })
}

onShow(load)
</script>

<style scoped>
.cart {
  padding: 20rpx;
  padding-bottom: 120rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.empty {
  text-align: center;
  padding-top: 200rpx;
  color: #999;
}
.go-shop {
  margin-top: 30rpx;
  background: #ff2d2d;
  color: #fff;
  border-radius: 44rpx;
  width: 300rpx;
}
.cart-item {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  align-items: center;
}
.check {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ccc;
  border-radius: 50%;
  margin-right: 16rpx;
  text-align: center;
  line-height: 36rpx;
  color: #fff;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.check.on {
  background: #ff2d2d;
  border-color: #ff2d2d;
}
.img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 10rpx;
  background: #f0f0f0;
}
.info {
  flex: 1;
  margin-left: 16rpx;
}
.name {
  font-size: 28rpx;
}
.spec {
  color: #999;
  font-size: 22rpx;
  margin-top: 6rpx;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}
.price {
  color: #ff2d2d;
  font-size: 30rpx;
  font-weight: bold;
}
.stepper {
  display: flex;
  align-items: center;
}
.step {
  width: 48rpx;
  height: 48rpx;
  border: 1rpx solid #ddd;
  text-align: center;
  line-height: 44rpx;
}
.num {
  width: 64rpx;
  text-align: center;
}
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 50px; /* H5 tabBar 高度，小程序需微调 */
  background: #fff;
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  gap: 20rpx;
}
.all-check {
  display: flex;
  align-items: center;
}
.total-price {
  color: #ff2d2d;
  font-size: 34rpx;
  font-weight: bold;
}
.settle {
  margin-left: auto;
  background: #ff2d2d;
  color: #fff;
  border-radius: 44rpx;
  padding: 18rpx 40rpx;
}
</style>
