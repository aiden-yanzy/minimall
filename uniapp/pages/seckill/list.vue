<template>
  <view class="seckill">
    <view v-for="a in acts" :key="a.id" class="act">
      <view class="act-head">
        <text class="name">{{ a.name }}</text>
        <text class="tag" :class="{ off: !a.started }">{{ a.started ? '进行中' : '即将开始' }}</text>
      </view>
      <view v-for="g in a.goodsList" :key="g.id" class="goods">
        <image class="img" :src="g.goodsImage" mode="aspectFill" />
        <view class="info">
          <view class="name">{{ g.goodsName }}</view>
          <view class="spec">{{ g.specValueStr }}</view>
          <view class="price-row">
            <view class="price"><text class="sym">￥</text><text class="num">{{ fen2yuan(g.seckillPrice) }}</text></view>
            <text class="origin">￥{{ fen2yuan(g.marketPrice) }}</text>
          </view>
          <view class="stock">余 {{ g.remainStock }} 件 · 限{{ g.perLimit }}件</view>
        </view>
        <view class="btn" :class="{ done: g.remainStock <= 0 || !a.started }" @click="buy(a, g)">
          {{ g.remainStock <= 0 ? '已抢完' : (a.started ? '立即抢购' : '未开始') }}
        </view>
      </view>
    </view>
    <view v-if="!acts.length" class="empty">暂无秒杀活动</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getSeckillList } from '@/api/seckill'
import { fen2yuan } from '@/utils/format'
import { useUserStore } from '@/store/user'

const acts = ref([])
const userStore = useUserStore()

async function load() {
  const res = await getSeckillList()
  acts.value = res.data
}
function buy(a, g) {
  if (!a.started || g.remainStock <= 0) return uni.showToast({ title: '活动未开始或已抢完', icon: 'none' })
  if (!userStore.token) return uni.navigateTo({ url: '/pages/user/login' })
  uni.navigateTo({
    url: '/pages/order/confirm?buyType=seckill&seckillGoodsId=' + g.id + '&goodsItemId=' + g.goodsItemId + '&goodsId=' + g.goodsId
  })
}

onShow(load)
</script>

<style scoped>
.seckill {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}
.act {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}
.act-head {
  background: linear-gradient(90deg, #ff2d2d, #ff6a00);
  color: #fff;
  padding: 20rpx 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.name { font-size: 30rpx; font-weight: bold; }
.tag {
  font-size: 22rpx;
  border: 1rpx solid rgba(255,255,255,.7);
  border-radius: 20rpx;
  padding: 4rpx 16rpx;
}
.tag.off { opacity: .8; }
.goods {
  display: flex;
  padding: 20rpx;
  align-items: center;
}
.img { width: 150rpx; height: 150rpx; border-radius: 10rpx; background: #f0f0f0; }
.info { flex: 1; margin-left: 16rpx; }
.name { font-size: 28rpx; }
.spec { color: #999; font-size: 22rpx; margin-top: 4rpx; }
.price-row { display: flex; align-items: baseline; margin-top: 10rpx; }
.price { color: #ff2d2d; display: flex; align-items: baseline; }
.sym { font-size: 22rpx; }
.num { font-size: 38rpx; font-weight: bold; }
.origin { margin-left: 10rpx; color: #bbb; font-size: 22rpx; text-decoration: line-through; }
.stock { color: #999; font-size: 20rpx; margin-top: 6rpx; }
.btn {
  background: #ff2d2d;
  color: #fff;
  border-radius: 30rpx;
  padding: 14rpx 30rpx;
  font-size: 26rpx;
  margin-left: 10rpx;
}
.btn.done { background: #ccc; }
.empty { text-align: center; color: #999; padding: 120rpx 0; }
</style>