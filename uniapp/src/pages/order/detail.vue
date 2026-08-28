<template>
  <view class="wrapper">
    <view class="status-bar" :class="'s' + order.orderStatus">
      <view class="status-text">{{ statusText }}</view>
    </view>

    <view class="section addr">
      <view class="a-line">收货人：{{ order.contact }} {{ order.mobile }}</view>
      <view class="a-line">地址：{{ order.address }}</view>
    </view>

    <view class="section">
      <view v-for="g in order.goodsList" :key="g.goodsItemId" class="g-item">
        <image class="img" :src="g.goodsImage" mode="aspectFill" />
        <view class="g-info">
          <view class="g-name">{{ g.goodsName }}</view>
          <view class="g-spec">{{ g.specValueStr }}</view>
        </view>
        <view class="g-right">
          <view>￥{{ fen2yuan(g.price) }}</view>
          <view class="g-num">x{{ g.num }}</view>
          <view v-if="order.orderStatus === 3 && g.isComment !== 1" class="comment-btn" @click="openComment(g)">评价</view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="amount-row"><text>商品金额</text><text>￥{{ fen2yuan(order.totalAmount) }}</text></view>
      <view class="amount-row"><text>运费</text><text>￥{{ fen2yuan(order.freightAmount) }}</text></view>
      <view class="amount-row"><text>优惠</text><text>-￥{{ fen2yuan(order.discountAmount) }}</text></view>
      <view class="amount-row total"><text>实付金额</text><text class="red">￥{{ fen2yuan(order.payAmount) }}</text></view>
      <view class="amount-row" v-if="order.expressNo"><text>物流</text><text class="copy" @click="openTraces">{{ order.expressName || '' }} {{ order.expressNo }}（轨迹）</text></view>
      <view class="amount-row"><text>订单编号</text><text class="copy" @click="copy(order.orderSn)">{{ order.orderSn }}</text></view>
    </view>

    <view v-if="order.orderStatus === 0" class="action-bar">
      <view class="btn" @click="cancel">取消订单</view>
      <view class="btn primary" @click="pay">立即支付</view>
    </view>
    <view v-if="order.orderStatus === 2" class="action-bar">
      <view class="btn primary" @click="confirmReceive">确认收货</view>
    </view>

    <!-- 评价弹层 -->
    <view v-if="showComment" class="mask"></view>
    <view v-if="showComment" class="sheet">
      <view class="sheet-title">评价商品</view>
      <view class="stars">
        <text v-for="s in 5" :key="s" class="star" :class="{ on: s <= score }" @click="score = s">★</text>
      </view>
      <textarea v-model="content" class="ta" placeholder="说说使用体验吧"></textarea>
      <button class="ok" @click="submitComment">提交评价</button>
    </view>
  </view>

    <!-- 物流轨迹弹层 -->
    <view v-if="showTraces" class="mask"></view>
    <view v-if="showTraces" class="traces-sheet">
      <view class="sheet-title">物流轨迹</view>
      <view v-for="(t, i) in traces" :key="i" class="trace">
        <view class="trace-dot"></view>
        <view class="trace-body">
          <view class="desc">{{ t.desc }}</view>
          <view class="time">{{ (t.time || '').slice(0, 16) }}</view>
        </view>
      </view>
    </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail, cancelOrder, mockPay } from '@/api/order'
import { post } from '@/api/request'
import { createComment } from '@/api/member'
import { fen2yuan } from '@/utils/format'

const order = ref({})
const id = ref(null)
const showComment = ref(false)
const score = ref(5)
const content = ref('')
const currentGoods = ref(null)
const showTraces = ref(false)
const traces = ref([])

const statusMap = {
  0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消'
}
const statusText = computed(() => statusMap[order.value.orderStatus] || '未知')

async function load() {
  const res = await getOrderDetail(id.value)
  order.value = res.data
}

async function cancel() {
  await confirmModal('确定取消该订单？', async () => {
    await cancelOrder(id.value, '用户取消')
    uni.showToast({ title: '已取消' })
    load()
  })
}
async function pay() {
  await mockPay(order.value.orderSn, order.value.payType)
  uni.showToast({ title: '支付成功' })
  load()
}
async function confirmReceive() {
  await confirmModal('确认已收到货？', async () => {
    await post('/shop/order/' + order.value.id + '/receive')
    uni.showToast({ title: '已确认收货' })
    load()
  })
}

function copy(text) {
  uni.setClipboardData({ data: text })
}
function confirmModal(title, fn) {
  uni.showModal({ title: '提示', content: title, success: (r) => r.confirm && fn() })
}

function openTraces() {
  post('/shop/order/' + order.value.id + '/traces').then((res) => {
    traces.value = res.data
    showTraces.value = true
  })
}

function openComment(g) {
  currentGoods.value = g
  score.value = 5
  content.value = ''
  showComment.value = true
}
async function submitComment() {
  if (!content.value) return uni.showToast({ title: '请输入评价内容', icon: 'none' })
  try {
    await createComment({ orderId: order.value.id, orderGoodsId: currentGoods.value.id, score: score.value, content: content.value })
    uni.showToast({ title: '评价成功' })
    showComment.value = false
    load()
  } catch (e) {}
}

onLoad((o) => {
  id.value = o.id
  load()
})
</script>

<style scoped>
.wrapper {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 140rpx;
}
.status-bar {
  border-radius: 16rpx;
  padding: 50rpx 30rpx;
  color: #fff;
  margin-bottom: 16rpx;
}
.status-bar.s0 { background: #ff9900; }
.status-bar.s1 { background: #3370ff; }
.status-bar.s2 { background: #00b578; }
.status-bar.s3 { background: #07c160; }
.status-bar.s4 { background: #bbb; }
.status-text {
  font-size: 40rpx;
  font-weight: bold;
}
.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}
.a-line {
  font-size: 26rpx;
  margin-bottom: 8rpx;
  color: #333;
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
.g-right {
  text-align: right;
}
.g-num {
  color: #999;
  margin-top: 10rpx;
}
.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 10rpx 0;
  color: #333;
  font-size: 26rpx;
}
.amount-row.total {
  font-weight: bold;
  border-top: 1rpx solid #f0f0f0;
  margin-top: 8rpx;
  padding-top: 16rpx;
}
.red {
  color: #ff2d2d;
}
.copy {
  color: #3370ff;
}
.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  padding: 16rpx 20rpx;
}
.btn {
  border: 1rpx solid #ddd;
  border-radius: 40rpx;
  padding: 18rpx 40rpx;
  color: #333;
}
.btn.primary {
  background: #ff2d2d;
  border-color: #ff2d2d;
  color: #fff;
}
.comment-btn { color: #3370ff; font-size: 22rpx; margin-top: 10rpx; }
.traces-sheet { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; z-index: 11; padding: 30rpx; max-height: 70vh; overflow-y: auto; }
.trace { display: flex; gap: 16rpx; padding: 14rpx 0; }
.trace-dot { width: 16rpx; height: 16rpx; border-radius: 50%; background: #07c160; margin-top: 8rpx; flex-shrink: 0; }
.trace-body { flex: 1; }
.desc { font-size: 26rpx; color: #333; }
.time { color: #999; font-size: 22rpx; margin-top: 4rpx; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); z-index: 10; }
.sheet { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; z-index: 11; padding: 30rpx; }
.sheet-title { font-weight: bold; margin-bottom: 16rpx; }
.stars { text-align: center; margin-bottom: 16rpx; }
.star { font-size: 60rpx; color: #ddd; }
.star.on { color: #ffb400; }
.ta { width: 100%; height: 160rpx; border: 1rpx solid #eee; border-radius: 10rpx; padding: 16rpx; box-sizing: border-box; }
.ok { background: #ff2d2d; color: #fff; border-radius: 44rpx; margin-top: 16rpx; }
</style>
