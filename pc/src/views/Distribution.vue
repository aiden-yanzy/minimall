<template>
  <div class="page">
    <div class="head"><h2>分销中心</h2></div>

    <div class="cards">
      <div class="card hero">
        <div class="hero-label">累计佣金（元）</div>
        <div class="hero-amount">{{ ((info.totalCommission || 0) / 100).toFixed(2) }}</div>
        <div class="hero-sub">
          <span>本月 {{ ((info.monthCommission || 0) / 100).toFixed(2) }}</span>
          <span>今日 {{ ((info.todayCommission || 0) / 100).toFixed(2) }}</span>
          <span>团队 {{ info.teamCount || 0 }} 人</span>
        </div>
      </div>
      <div class="card">
        <div class="card-label">可提现余额</div>
        <div class="card-amount">￥{{ ((info.balance || 0) / 100).toFixed(2) }}</div>
        <router-link to="/member" class="lnk">查看钱包流水 →</router-link>
      </div>
      <div class="card">
        <div class="card-label">分销等级</div>
        <div class="card-level">{{ info.levelName }}</div>
        <div class="rate">费率 {{ ((info.rate || 0) / 10).toFixed(0) }}%
          <span v-if="info.nextLevelName" class="next">→ {{ ((info.nextRate || 0) / 10).toFixed(0) }}%</span>
        </div>
      </div>
    </div>

    <el-card shadow="never" class="block">
      <div class="row-between">
        <span class="block-title">升级进度</span>
        <span class="dim" v-if="info.nextLevelName">
          再赚 ￥{{ ((info.gap || 0) / 100).toFixed(2) }} 升「{{ info.nextLevelName }}」
        </span>
        <span class="dim" v-else>已是最高等级</span>
      </div>
      <el-progress :percentage="info.percent || 0" :stroke-width="12" color="#ff2d2d" :show-text="false" class="bar" />
    </el-card>

    <el-card shadow="never" class="block">
      <div class="row-between">
        <span class="block-title">邀请好友</span>
        <div class="ops">
          <el-button size="small" @click="copy(invite.inviteCode, '邀请码已复制')">复制邀请码</el-button>
          <el-button size="small" type="danger" @click="copyLink">复制推广链接</el-button>
        </div>
      </div>
      <div class="invite">
        <div class="code">{{ invite.inviteCode || '—' }}</div>
        <div class="tip">好友通过你的链接注册并下单，确认收货后佣金自动进余额</div>
      </div>
    </el-card>

    <el-tabs v-model="tab" class="block tabs">
      <el-tab-pane :label="`佣金明细（${commissions.length}）`" name="c">
        <el-table :data="commissions" border size="small">
          <el-table-column prop="orderId" label="订单" width="110" />
          <el-table-column label="商品金额" width="120">
            <template #default="{ row }">￥{{ ((row.goodsAmount || 0) / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="费率" width="90">
            <template #default="{ row }">{{ (row.rate / 10).toFixed(0) }}%</template>
          </el-table-column>
          <el-table-column label="佣金" width="110">
            <template #default="{ row }">
              <span class="red">+￥{{ ((row.commission || 0) / 100).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="时间" min-width="160">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 16) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!commissions.length" description="还没有佣金记录" />
      </el-tab-pane>

      <el-tab-pane :label="`我的团队（${team.length}）`" name="t">
        <el-table :data="team" border size="small">
          <el-table-column prop="nickname" label="昵称" min-width="140" />
          <el-table-column prop="mobile" label="手机号" width="150" />
          <el-table-column label="加入时间" width="130">
            <template #default="{ row }">{{ (row.createTime || '').slice(0, 10) }}</template>
          </el-table-column>
          <el-table-column label="贡献佣金" min-width="120">
            <template #default="{ row }">
              <span class="red">￥{{ ((row.commission || 0) / 100).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!team.length" description="还没有下级成员，分享链接邀请好友" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { distOverview, distInvite, distCommissions, distTeam } from '@/api/promo'

const info = ref({})
const invite = ref({})
const commissions = ref([])
const team = ref([])
const tab = ref('c')

function copy(text, tip) {
  if (!text) return
  navigator.clipboard.writeText(String(text)).then(() => ElMessage.success(tip || '已复制'))
}
function copyLink() {
  if (!invite.value.inviteCode) return
  const link = `${location.origin}/#/pages/user/login?ref=${invite.value.inviteCode}`
  copy(link, '推广链接已复制')
}

onMounted(async () => {
  const [ov, iv, cs, tm] = await Promise.all([distOverview(), distInvite(), distCommissions(), distTeam()])
  info.value = ov.data || {}
  invite.value = iv.data || {}
  commissions.value = cs.data || []
  team.value = tm.data || []
})
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 20px 0 40px; }
.head h2 { margin: 0 0 18px; font-size: 22px; }
.cards { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.card { border-radius: 12px; padding: 20px; background: #fff; }
.hero { background: linear-gradient(135deg, #ff5f45, #ff2d2d); color: #fff; }
.hero-label { font-size: 13px; opacity: .9; }
.hero-amount { font-size: 34px; font-weight: 700; margin: 8px 0 12px; }
.hero-sub { display: flex; gap: 20px; font-size: 12px; opacity: .92; }
.card-label { font-size: 12px; color: #999; }
.card-amount { font-size: 26px; font-weight: 700; color: #ff2d2d; margin: 10px 0; }
.card-level { font-size: 20px; font-weight: 600; margin: 10px 0 6px; }
.rate { font-size: 13px; color: #666; }
.next { color: #ff2d2d; }
.lnk { font-size: 12px; color: #ff2d2d; text-decoration: none; }
.block { margin-bottom: 16px; }
.block-title { font-size: 15px; font-weight: 600; }
.row-between { display: flex; justify-content: space-between; align-items: center; }
.dim { color: #999; font-size: 13px; }
.bar { margin-top: 14px; }
.invite { display: flex; align-items: center; gap: 20px; margin-top: 14px; }
.code { font-size: 30px; font-weight: 700; letter-spacing: 6px; }
.tip { font-size: 12px; color: #999; }
.tabs { background: #fff; border-radius: 12px; padding: 16px; }
.red { color: #ff2d2d; font-weight: 600; }
.ops { display: flex; gap: 8px; }
</style>
