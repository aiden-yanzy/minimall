<template>
  <view class="dist">
    <!-- 佣金概览 -->
    <view class="hero">
      <view class="hero-top">
        <text class="hero-label">累计佣金（元）</text>
        <text class="hero-level">{{ info.levelName || '普通分销员' }} · {{ rateText }}</text>
      </view>
      <view class="hero-amount">{{ fen2yuan(info.totalCommission) }}</view>
      <view class="hero-sub">
        <view class="hs-item">
          <text class="hs-num">{{ fen2yuan(info.monthCommission) }}</text>
          <text class="hs-label">本月</text>
        </view>
        <view class="hs-item">
          <text class="hs-num">{{ fen2yuan(info.todayCommission) }}</text>
          <text class="hs-label">今日</text>
        </view>
        <view class="hs-item">
          <text class="hs-num">{{ fen2yuan(info.balance) }}</text>
          <text class="hs-label">可提现余额</text>
        </view>
      </view>
    </view>

    <!-- 等级进度 -->
    <view class="card">
      <view class="row-between">
        <text class="card-title">分销等级</text>
        <text class="grade">{{ info.levelName }}</text>
      </view>
      <view v-if="info.nextLevelName" class="progress-wrap">
        <view class="bar"><view class="bar-in" :style="{ width: (info.percent || 0) + '%' }"></view></view>
        <view class="progress-text">
          再赚 <text class="red">￥{{ fen2yuan(info.gap) }}</text> 升「{{ info.nextLevelName }}」，费率
          {{ pct(info.rate) }} → <text class="red">{{ pct(info.nextRate) }}</text>
        </view>
      </view>
      <view v-else class="progress-text">已是最高等级，当前费率 {{ pct(info.rate) }}</view>
    </view>

    <!-- 邀请 -->
    <view class="card">
      <text class="card-title">邀请好友赚佣金</text>
      <view class="invite">
        <view class="code-box">
          <text class="code">{{ invite.inviteCode || '—' }}</text>
          <text class="code-label">我的邀请码</text>
        </view>
        <view class="invite-ops">
          <view class="btn-mini" @click="copy(invite.inviteCode, '邀请码已复制')">复制邀请码</view>
          <view class="btn-mini primary" @click="copyLink">复制推广链接</view>
        </view>
      </view>
      <view class="tip">好友通过你的链接注册并下单，确认收货后佣金自动进余额</view>
    </view>

    <!-- tabs -->
    <view class="tabs">
      <view class="tab" :class="{ on: tab === 'commission' }" @click="tab = 'commission'">佣金明细</view>
      <view class="tab" :class="{ on: tab === 'team' }" @click="tab = 'team'">我的团队（{{ info.teamCount || 0 }}）</view>
    </view>

    <!-- 佣金明细 -->
    <template v-if="tab === 'commission'">
      <view v-if="!commissions.length" class="empty">还没有佣金记录，先邀请好友下单吧</view>
      <view v-for="c in commissions" :key="c.id" class="item">
        <view class="item-main">
          <view class="item-title">订单 #{{ c.orderId }}</view>
          <view class="item-sub">
            商品金额 ￥{{ fen2yuan(c.goodsAmount) }} · 费率 {{ pct(c.rate) }}
          </view>
        </view>
        <view class="item-right">
          <text class="earn">+￥{{ fen2yuan(c.commission) }}</text>
          <text class="time">{{ (c.createTime || '').replace('T', ' ').slice(5, 16) }}</text>
        </view>
      </view>
    </template>

    <!-- 我的团队 -->
    <template v-else>
      <view v-if="!team.length" class="empty">还没有下级成员，分享链接邀请好友注册</view>
      <view v-for="m in team" :key="m.id" class="item">
        <view class="avatar">{{ (m.nickname || 'U').slice(0, 1) }}</view>
        <view class="item-main">
          <view class="item-title">{{ m.nickname }}</view>
          <view class="item-sub">{{ m.mobile }} · {{ (m.createTime || '').slice(0, 10) }} 加入</view>
        </view>
        <view class="item-right">
          <text class="earn">贡献 ￥{{ fen2yuan(m.commission) }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { distOverview, distInvite, distCommissions, distTeam } from '@/api/distribution'
import { useUserStore } from '@/store/user'
import { fen2yuan } from '@/utils/format'

const userStore = useUserStore()
const info = ref({})
const invite = ref({})
const commissions = ref([])
const team = ref([])
const tab = ref('commission')

const rateText = computed(() => (info.value.rate ? '费率 ' + pct(info.value.rate) : ''))
const pct = (r) => ((r || 0) / 10).toFixed(1).replace('.0', '') + '%'

async function load() {
  if (!userStore.token) return uni.navigateTo({ url: '/pages/user/login' })
  const [ov, iv, cs, tm] = await Promise.all([distOverview(), distInvite(), distCommissions(), distTeam()])
  info.value = ov.data || {}
  invite.value = iv.data || {}
  commissions.value = cs.data || []
  team.value = tm.data || []
}

function copy(text, tip) {
  if (!text) return
  uni.setClipboardData({
    data: String(text),
    success: () => uni.showToast({ title: tip || '已复制' })
  })
}

function copyLink() {
  const code = invite.value.inviteCode
  if (!code) return
  // 当前域名 + 页面路径（H5 hash 路由）
  const origin = location.origin
  const link = `${origin}/#/pages/user/login?ref=${code}`
  copy(link, '推广链接已复制')
}

onShow(load)
</script>

<style scoped>
.dist { padding: 20rpx 24rpx 60rpx; background: #f5f5f5; min-height: 100vh; }
.hero { background: linear-gradient(135deg, #ff5f45, #ff2d2d); border-radius: 20rpx; padding: 28rpx; color: #fff; }
.hero-top { display: flex; justify-content: space-between; align-items: center; }
.hero-label { font-size: 24rpx; opacity: 0.9; }
.hero-level { font-size: 22rpx; background: rgba(255, 255, 255, 0.22); padding: 6rpx 16rpx; border-radius: 20rpx; }
.hero-amount { font-size: 62rpx; font-weight: 700; margin: 12rpx 0 18rpx; }
.hero-sub { display: flex; }
.hs-item { flex: 1; display: flex; flex-direction: column; }
.hs-num { font-size: 28rpx; font-weight: 600; }
.hs-label { font-size: 20rpx; opacity: 0.85; margin-top: 4rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-top: 20rpx; }
.card-title { font-size: 28rpx; font-weight: 600; color: #222; }
.row-between { display: flex; justify-content: space-between; align-items: center; }
.grade { font-size: 24rpx; color: #ff2d2d; }
.progress-wrap { margin-top: 18rpx; }
.bar { height: 12rpx; background: #f2f3f5; border-radius: 6rpx; overflow: hidden; }
.bar-in { height: 100%; background: linear-gradient(90deg, #ff8f6b, #ff2d2d); border-radius: 6rpx; }
.progress-text { font-size: 22rpx; color: #999; margin-top: 12rpx; }
.red { color: #ff2d2d; }
.invite { display: flex; justify-content: space-between; align-items: center; margin-top: 16rpx; }
.code-box { display: flex; flex-direction: column; }
.code { font-size: 40rpx; font-weight: 700; letter-spacing: 4rpx; color: #222; }
.code-label { font-size: 20rpx; color: #999; margin-top: 4rpx; }
.invite-ops { display: flex; flex-direction: column; gap: 12rpx; }
.btn-mini { font-size: 22rpx; color: #ff2d2d; border: 1rpx solid #ff2d2d; border-radius: 28rpx; padding: 10rpx 20rpx; text-align: center; }
.btn-mini.primary { background: #ff2d2d; color: #fff; }
.tip { font-size: 20rpx; color: #bbb; margin-top: 16rpx; }
.tabs { display: flex; margin: 22rpx 0 6rpx; }
.tab { flex: 1; text-align: center; font-size: 26rpx; color: #666; padding: 16rpx 0; }
.tab.on { color: #ff2d2d; font-weight: 600; border-bottom: 3rpx solid #ff2d2d; }
.item { background: #fff; border-radius: 14rpx; padding: 20rpx; margin-top: 16rpx; display: flex; align-items: center; gap: 16rpx; }
.item-main { flex: 1; }
.item-title { font-size: 26rpx; color: #222; }
.item-sub { font-size: 22rpx; color: #999; margin-top: 6rpx; }
.item-right { display: flex; flex-direction: column; align-items: flex-end; }
.earn { font-size: 28rpx; color: #ff2d2d; font-weight: 600; }
.time { font-size: 20rpx; color: #bbb; margin-top: 6rpx; }
.avatar { width: 72rpx; height: 72rpx; border-radius: 50%; background: #eef3ff; color: #3370ff; font-size: 28rpx; display: flex; align-items: center; justify-content: center; }
.empty { text-align: center; font-size: 24rpx; color: #999; padding: 60rpx 0; }
</style>
