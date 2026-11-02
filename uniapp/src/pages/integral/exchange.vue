<template>
  <view class="exchange">
    <view class="section">
      <view class="goods">
        <image class="img" :src="asset(goods.image)" mode="aspectFill" />
        <view class="info">
          <view class="name">{{ goods.name }}</view>
          <view class="price"><text class="num">{{ goods.integral }}</text> 积分</view>
        </view>
      </view>
    </view>
    <view class="section">
      <view class="qty-row">
        <text>数量</text>
        <view class="stepper">
          <text class="step" @click="num > 1 && num--">-</text>
          <text class="num">{{ num }}</text>
          <text class="step" @click="num < goods.stock && num++">+</text>
        </view>
      </view>
    </view>
    <view class="section">
      <view class="addr-title">收货信息</view>
      <input v-model="contact" class="ipt" placeholder="收货人" />
      <input v-model="mobile" class="ipt" type="number" placeholder="手机号" />
      <input v-model="address" class="ipt" placeholder="详细地址" />
    </view>
    <view class="section total">
      共需 <text class="red">{{ goods.integral * num }}</text> 积分
    </view>
    <view class="footer">
      <view class="submit" @click="doExchange">立即兑换</view>
    </view>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getIntegralGoods, exchange } from '@/api/integral'

const goods = ref({})
const num = ref(1)
const contact = ref('')
const mobile = ref('')
const address = ref('')

onLoad((o) => load(o.id))

async function load(id) {
  const res = await getIntegralGoods()
  goods.value = res.data.find((g) => g.id == id) || {}
}
async function doExchange() {
  if (!contact.value || !mobile.value || !address.value) {
    return uni.showToast({ title: '请填写完整收货信息', icon: 'none' })
  }
  try {
    const res = await exchange({
      goodsId: goods.value.id,
      num: num.value,
      contact: contact.value,
      mobile: mobile.value,
      address: address.value
    })
    uni.showToast({ title: '兑换成功' })
    setTimeout(() => uni.redirectTo({ url: '/pages/integral/orders' }), 500)
  } catch (e) {}
}
</script>

<style scoped>
.exchange {
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
.goods { display: flex; }
.img { width: 160rpx; height: 160rpx; border-radius: 12rpx; background: #f0f0f0; }
.info { flex: 1; margin-left: 20rpx; }
.name { font-size: 30rpx; }
.price { color: #f107a3; margin-top: 16rpx; }
.num { font-size: 40rpx; font-weight: bold; }
.qty-row { display: flex; justify-content: space-between; align-items: center; }
.stepper { display: flex; align-items: center; }
.step { width: 56rpx; height: 56rpx; border: 1rpx solid #ddd; text-align: center; line-height: 52rpx; }
.num { width: 72rpx; text-align: center; }
.addr-title { font-weight: bold; margin-bottom: 16rpx; }
.ipt {
  height: 76rpx;
  border: 1rpx solid #eee;
  border-radius: 10rpx;
  padding: 0 20rpx;
  margin-bottom: 16rpx;
}
.total { color: #333; }
.red { color: #f107a3; font-size: 36rpx; font-weight: bold; }
.footer {
  position: fixed; left: 0; right: 0; bottom: 0;
  background: #fff; padding: 16rpx 20rpx;
}
.submit {
  background: #f107a3; color: #fff; text-align: center;
  border-radius: 44rpx; padding: 20rpx 0;
}
</style>