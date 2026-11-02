<template>
  <view class="wrap">
    <view class="tabs">
      <view class="tab" :class="{ active: tab === 'act' }" @click="tab = 'act'">活动</view>
      <view class="tab" :class="{ active: tab === 'mine' }" @click="tab = 'mine'">我的砍价</view>
    </view>

    <!-- 活动列表 -->
    <view v-if="tab === 'act'">
      <view v-for="a in acts" :key="a.id" class="act">
        <view class="act-head">
          <text class="name">{{ a.name }}</text>
          <view class="btn small" @click="start(a)">发起砍价</view>
        </view>
        <view class="goods">
          <image class="img" :src="asset(a.goodsImage)" mode="aspectFill" />
          <view class="info">
            <view class="name">{{ a.goodsName }}</view>
            <view class="spec">{{ a.specValueStr }}</view>
            <view class="prices">
              <text class="floor">￥{{ fen2yuan(a.floorPrice) }}</text>
              <text class="base">← 原价 ￥{{ fen2yuan(a.basePrice) }}</text>
            </view>
            <view class="rule">需 {{ a.helpNum }} 位好友帮砍</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 我的砍价 -->
    <view v-if="tab === 'mine'">
      <view v-for="i in mine" :key="i.id" class="act mine">
        <view class="goods">
          <image class="img" :src="asset(i.goodsImage)" mode="aspectFill" />
          <view class="info">
            <view class="name">{{ i.activityName }}</view>
            <view class="prices">
              <text class="cur">￥{{ fen2yuan(i.currentPrice) }}</text>
              <text class="base">原价 ￥{{ fen2yuan(i.basePrice) }}</text>
            </view>
            <view class="rule">{{ i.helpedCount }}/{{ i.needHelp }} 人已帮砍{{ i.status === 1 ? ' · 已成交' : '' }}</view>
          </view>
          <view class="right" v-if="i.status === 0">
            <view class="btn small" @click="getHelp(i)">求帮砍</view>
          </view>
          <view class="right" v-else-if="i.status === 1 && !i.orderId">
            <view class="btn small" @click="buyDone(i)">立即购买</view>
          </view>
        </view>
      </view>
      <view v-if="!mine.length" class="empty">还没有发起砍价</view>
    </view>

    <!-- 求帮砍：复制挑衅口令 -->
    <view v-if="showHelp" class="mask" @click="showHelp = false"></view>
    <view v-if="showHelp" class="sheet">
      <view class="sheet-title">把这条消息分享给好友</view>
      <view class="share-txt">帮我砍一刀！输入发起ID: {{ helpIni }} 即可帮我砍价</view>
      <button class="copy" @click="copyText">复制</button>
    </view>
  </view>
</template>

<script setup>
import { asset } from '@/utils/asset'
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getBargainList, bargainStart, bargainHelp, bargainMine, bargainOrder } from '@/api/p5'
import { fen2yuan } from '@/utils/format'

const tab = ref('act')
const acts = ref([])
const mine = ref([])
const showHelp = ref(false)
const helpIni = ref('')

async function loadActs() {
  const res = await getBargainList()
  acts.value = res.data
}
async function loadMine() {
  const res = await bargainMine()
  mine.value = res.data
}
function start(a) {
  bargainStart(a.id).then((r) => {
    uni.showToast({ title: '发起成功' })
    tab.value = 'mine'
    loadMine()
  })
}
function getHelp(i) {
  helpIni.value = i.id
  showHelp.value = true
}
// 好友入口：模拟输入好友发起ID帮砍
function helpFriend(iniId) {
  bargainHelp(iniId).then((r) => {
    uni.showToast({ title: '帮砍成功 减' + (r.data.reduce / 100) + '元' })
  }).catch(() => {})
}
function copyText() {
  uni.setClipboardData({ data: '帮我砍一刀! 发起ID:' + helpIni.value })
  uni.showToast({ title: '已复制' })
}
function buyDone(i) {
  uni.showModal({
    title: '砍到底价，确认下单？',
    content: '将以 ￥' + fen2yuan(i.currentPrice) + ' 成交',
    success: (r) => {
      if (!r.confirm) return
      uni.showModal({ title: '收货信息', editable: true, placeholderText: '收货人+手机+地址 用空格分开', success: async (r2) => {
        const parts = (r2.content || '').split(/[\s，,]+/)
        if (parts.length < 3) return uni.showToast({ title: '格式：姓名 手机 地址', icon: 'none' })
        const oid = await bargainOrder(i.id, parts[0], parts[1], parts[2])
        uni.showToast({ title: '下单成功' })
        setTimeout(() => uni.navigateTo({ url: '/pages/order/detail?id=' + oid.data }), 500)
      } })
    }
  })
}

onShow(() => {
  loadActs()
  loadMine()
})

// 暴露给其他页面(如需) - 此处仅演示入口
</script>

<style scoped>
.wrap { padding: 20rpx; background: #f5f5f5; min-height: 100vh; }
.tabs { display: flex; background: #fff; border-radius: 12rpx; margin-bottom: 20rpx; }
.tab { flex: 1; text-align: center; padding: 22rpx 0; color: #333; }
.tab.active { color: #ff6a00; font-weight: bold; }
.act { background: #fff; border-radius: 16rpx; margin-bottom: 20rpx; overflow: hidden; }
.act-head { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 24rpx; background: #fff7ef; }
.name { font-size: 28rpx; font-weight: bold; color: #ff6a00; }
.goods { display: flex; padding: 20rpx; align-items: center; }
.img { width: 140rpx; height: 140rpx; border-radius: 10rpx; background: #f0f0f0; }
.info { flex: 1; margin-left: 16rpx; }
.spec { color: #999; font-size: 22rpx; margin-top: 4rpx; }
.prices { margin-top: 10rpx; display: flex; align-items: baseline; }
.floor { color: #ff6a00; font-size: 36rpx; font-weight: bold; }
.cur { color: #ff2d55; font-size: 36rpx; font-weight: bold; }
.base { margin-left: 12rpx; color: #bbb; font-size: 22rpx; text-decoration: line-through; }
.rule { color: #999; font-size: 20rpx; margin-top: 6rpx; }
.btn.small { background: #ff6a00; color: #fff; border-radius: 28rpx; padding: 10rpx 26rpx; font-size: 24rpx; }
.right { margin-left: 10rpx; }
.mine .goods { align-items: flex-start; }
.empty { text-align: center; color: #999; padding: 120rpx 0; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); z-index: 10; }
.sheet { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; z-index: 11; padding: 30rpx; }
.sheet-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; }
.share-txt { background: #fff7ef; border-radius: 10rpx; padding: 30rpx; color: #333; margin-bottom: 20rpx; font-size: 26rpx; }
.copy { background: #ff6a00; color: #fff; border-radius: 44rpx; }
</style>