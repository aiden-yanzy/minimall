<template>
  <view class="wallet">
    <view class="head">
      <view class="balance-label">余额(分)</view>
      <view class="balance">{{ balance }}</view>
      <view class="actions">
        <view class="act" @click="showRecharge = true">充值</view>
        <view class="act" @click="showWithdraw = true">提现</view>
      </view>
    </view>

    <view class="section">
      <view class="sec-title">收支明细</view>
      <view v-for="l in logs" :key="l.id" class="log">
        <view>
          <view class="l-remark">{{ l.remark }}</view>
          <view class="l-time">{{ (l.createTime || '').slice(0, 16) }}</view>
        </view>
        <view class="l-amount" :class="l.changeAmount > 0 ? 'plus' : 'minus'">
          {{ l.changeAmount > 0 ? '+' : '' }}{{ l.changeAmount }}
        </view>
      </view>
      <view v-if="!logs.length" class="empty">暂无流水</view>
    </view>

    <view class="section" v-if="dist">
      <view class="sec-title">我的分销</view>
      <view class="log">
        <view class="l-remark">累计佣金(分)</view>
        <view class="l-amount plus">{{ dist.totalCommission }}</view>
      </view>
      <view class="bind-row" v-if="!referrerDone">
        <input v-model="referrerId" class="ipt" type="number" placeholder="推荐人用户ID" />
        <button class="bind-btn" @click="bind">绑定</button>
      </view>
    </view>

    <!-- 充值 -->
    <view v-if="showRecharge" class="mask" @click="showRecharge = false"></view>
    <view v-if="showRecharge" class="sheet">
      <view class="sheet-title">选择充值档位</view>
      <view v-for="t in templates" :key="t.id" class="pay-option" @click="recharge(t)">
        充{{ t.amount / 100 }}元 送{{ t.giftAmount / 100 }}元
      </view>
    </view>

    <!-- 提现 -->
    <view v-if="showWithdraw" class="mask" @click="showWithdraw = false"></view>
    <view v-if="showWithdraw" class="sheet">
      <view class="sheet-title">申请提现</view>
      <input v-model="wdAmount" class="ipt" type="number" placeholder="提现金额(分)" />
      <input v-model="wdAccount" class="ipt" placeholder="收款账户(默认微信)" />
      <button class="confirm" @click="doWithdraw">提交</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getBalance, getTemplates, createRecharge, payRecharge, getLogs, withdraw, bindReferrer, getDistOverview } from '@/api/wallet'

const balance = ref(0)
const logs = ref([])
const templates = ref([])
const dist = ref(null)
const showRecharge = ref(false)
const showWithdraw = ref(false)
const wdAmount = ref('')
const wdAccount = ref('')
const referrerId = ref('')
const referrerDone = ref(false)

async function load() {
  const [b, l, t] = await Promise.all([getBalance(), getLogs(), getTemplates()])
  balance.value = b.data
  logs.value = l.data
  templates.value = t.data
  try {
    const d = await getDistOverview()
    dist.value = d.data
  } catch (e) {}
}

async function recharge(t) {
  const res = await createRecharge(t ? { templateId: t.id } : {})
  await payRecharge(res.data.orderSn)
  uni.showToast({ title: '充值成功' })
  showRecharge.value = false
  load()
}
async function doWithdraw() {
  if (!wdAmount.value) return uni.showToast({ title: '输入金额', icon: 'none' })
  try {
    await withdraw({ amount: parseInt(wdAmount.value), accountInfo: wdAccount.value || '微信零钱' })
    uni.showToast({ title: '已提交审核' })
    showWithdraw.value = false
    load()
  } catch (e) {}
}
async function bind() {
  if (!referrerId.value) return
  try {
    await bindReferrer(referrerId.value)
    uni.showToast({ title: '绑定成功' })
    referrerDone.value = true
  } catch (e) {}
}

onShow(load)
</script>

<style scoped>
.wallet { padding: 20rpx; background: #f5f5f5; min-height: 100vh; }
.head { background: linear-gradient(135deg, #ffb800, #ff7a00); border-radius: 16rpx; color: #fff; padding: 40rpx 30rpx; margin-bottom: 20rpx; }
.balance-label { font-size: 24rpx; opacity: .9; }
.balance { font-size: 64rpx; font-weight: bold; margin: 10rpx 0 20rpx; }
.actions { display: flex; gap: 20rpx; }
.act { border: 1rpx solid rgba(255,255,255,.8); border-radius: 30rpx; padding: 12rpx 40rpx; font-size: 26rpx; }
.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.sec-title { font-weight: bold; margin-bottom: 16rpx; }
.log { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.log:last-child { border-bottom: none; }
.l-remark { font-size: 26rpx; }
.l-time { color: #bbb; font-size: 20rpx; margin-top: 6rpx; }
.l-amount { font-size: 30rpx; font-weight: bold; }
.plus { color: #07c160; }
.minus { color: #ff2d2d; }
.empty { text-align: center; color: #999; padding: 40rpx 0; font-size: 24rpx; }
.bind-row { display: flex; gap: 16rpx; margin-top: 16rpx; }
.ipt { flex: 1; height: 72rpx; border: 1rpx solid #eee; border-radius: 10rpx; padding: 0 20rpx; }
.bind-btn { background: #ff7a00; color: #fff; border-radius: 10rpx; padding: 0 40rpx; display: flex; align-items: center; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); z-index: 10; }
.sheet { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; z-index: 11; padding: 30rpx; }
.sheet-title { font-weight: bold; margin-bottom: 20rpx; }
.pay-option { padding: 30rpx; border-bottom: 1rpx solid #f0f0f0; }
.confirm { background: #ff7a00; color: #fff; border-radius: 44rpx; margin-top: 10rpx; }
</style>