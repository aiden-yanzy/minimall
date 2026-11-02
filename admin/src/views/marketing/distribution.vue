<template>
  <el-card>
    <el-tabs v-model="tab">
      <!-- 等级与费率 -->
      <el-tab-pane label="分销等级" name="levels">
        <div class="toolbar">
          <el-button type="primary" size="small" @click="openCreate">+ 新增等级</el-button>
          <div class="grow"></div>
          <span class="dim">费率按千分比存：100 = 10%。升级门槛是「累计佣金」，0 表示无门槛</span>
        </div>
        <el-table :data="levels" v-loading="loading" border stripe size="small">
          <el-table-column prop="level" label="档位" width="70" />
          <el-table-column prop="name" label="等级名称" min-width="140" />
          <el-table-column label="佣金费率" width="110">
            <template #default="{ row }"><span class="red">{{ (row.commissionRate / 10).toFixed(1).replace('.0', '') }}%</span></template>
          </el-table-column>
          <el-table-column label="升级门槛" width="140">
            <template #default="{ row }">
              {{ row.upgradeAmount > 0 ? '累计佣金 ￥' + (row.upgradeAmount / 100).toFixed(2) : '无门槛' }}
            </template>
          </el-table-column>
          <el-table-column prop="upgradeCondition" label="说明" min-width="140" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 分销员看板 -->
      <el-tab-pane label="分销员" name="members">
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="手机号/昵称" style="width: 200px" clearable @keyup.enter="loadMembers" />
          <el-button size="small" @click="loadMembers">查询</el-button>
          <div class="grow"></div>
          <span class="dim">共 {{ members.length }} 位分销员</span>
        </div>
        <el-table :data="members" v-loading="loadingM" border stripe size="small">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column prop="mobile" label="手机号" width="130" />
          <el-table-column prop="levelName" label="等级" width="110" />
          <el-table-column label="费率" width="80">
            <template #default="{ row }">{{ (row.rate / 10).toFixed(0) }}%</template>
          </el-table-column>
          <el-table-column label="累计佣金" width="110">
            <template #default="{ row }"><span class="red">￥{{ ((row.totalCommission || 0) / 100).toFixed(2) }}</span></template>
          </el-table-column>
          <el-table-column label="团队人数" width="90">
            <template #default="{ row }">{{ row.teamCount }}</template>
          </el-table-column>
          <el-table-column prop="inviteCode" label="邀请码" width="110" />
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openOrders(row)">佣金流水</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 佣金流水 -->
      <el-tab-pane label="佣金流水" name="orders">
        <div class="toolbar">
          <span v-if="filterSeller" class="filter">
            仅看：{{ filterSeller.nickname }}
            <el-button link type="primary" size="small" @click="clearFilter">清除</el-button>
          </span>
          <div class="grow"></div>
          <el-button size="small" @click="loadOrders()">刷新</el-button>
        </div>
        <el-table :data="orders" v-loading="loadingO" border stripe size="small">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="orderSn" label="订单号" width="190" />
          <el-table-column prop="buyerName" label="买家" width="110" />
          <el-table-column prop="sellerName" label="分销员" width="110" />
          <el-table-column label="商品金额" width="110">
            <template #default="{ row }">￥{{ ((row.goodsAmount || 0) / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="费率" width="80">
            <template #default="{ row }">{{ (row.rate / 10).toFixed(0) }}%</template>
          </el-table-column>
          <el-table-column label="佣金" width="100">
            <template #default="{ row }"><span class="red">￥{{ ((row.commission || 0) / 100).toFixed(2) }}</span></template>
          </el-table-column>
          <el-table-column label="时间" min-width="160">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 16) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!orders.length" description="暂无佣金流水" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dlg" :title="form.id ? '编辑分销等级' : '新增分销等级'" width="480px">
      <el-form label-width="110px">
        <el-form-item label="等级名称"><el-input v-model="form.name" placeholder="如 高级分销员" /></el-form-item>
        <el-form-item label="档位"><el-input-number v-model="form.level" :min="1" :max="20" /></el-form-item>
        <el-form-item label="佣金费率(‰)">
          <el-input-number v-model="form.commissionRate" :min="0" :max="1000" :step="10" />
          <span class="hint">= {{ ((form.commissionRate || 0) / 10).toFixed(1).replace('.0', '') }}%</span>
        </el-form-item>
        <el-form-item label="升级门槛(元)">
          <el-input v-model="upgradeYuan" placeholder="0 = 无门槛" />
          <span class="hint">累计佣金达到此值自动升级</span>
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.upgradeCondition" placeholder="如 注册即享" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { distLevelListApi, distLevelSaveApi, distLevelRemoveApi, distMembersApi, distOrdersApi } from '@/api/p5'

const tab = ref('levels')
const levels = ref<any[]>([])
const members = ref<any[]>([])
const orders = ref<any[]>([])
const keyword = ref('')
const loading = ref(false)
const loadingM = ref(false)
const loadingO = ref(false)
const dlg = ref(false)
const upgradeYuan = ref('0')
const filterSeller = ref<any>(null)
const form = reactive<any>({ id: null, name: '', level: 1, commissionRate: 100, upgradeAmount: 0, upgradeCondition: '', status: 1 })

async function load() {
  loading.value = true
  try {
    const res: any = await distLevelListApi()
    levels.value = res.data || []
  } finally { loading.value = false }
}
async function loadMembers() {
  loadingM.value = true
  try {
    const res: any = await distMembersApi(keyword.value || undefined)
    members.value = res.data || []
  } finally { loadingM.value = false }
}
async function loadOrders(sellerId?: number) {
  loadingO.value = true
  try {
    const res: any = await distOrdersApi(sellerId)
    orders.value = res.data || []
  } finally { loadingO.value = false }
}

function openCreate() {
  Object.assign(form, { id: null, name: '', level: (levels.value.length || 0) + 1, commissionRate: 100, upgradeAmount: 0, upgradeCondition: '', status: 1 })
  upgradeYuan.value = '0'
  dlg.value = true
}
function openEdit(row: any) {
  Object.assign(form, { ...row })
  upgradeYuan.value = ((row.upgradeAmount || 0) / 100).toFixed(2)
  dlg.value = true
}
async function save() {
  if (!form.name?.trim()) return ElMessage.warning('请填写等级名称')
  await distLevelSaveApi({
    ...form,
    name: form.name.trim(),
    upgradeAmount: Math.round(Number(upgradeYuan.value || 0) * 100)
  })
  ElMessage.success('已保存')
  dlg.value = false
  load()
}
async function remove(row: any) {
  await ElMessageBox.confirm(`删除等级「${row.name}」？该等级的成员会按新的费率重新匹配`, '提示', { type: 'warning' })
  await distLevelRemoveApi(row.id)
  ElMessage.success('已删除')
  load()
}
function openOrders(row: any) {
  filterSeller.value = row
  tab.value = 'orders'
  loadOrders(row.id)
}
function clearFilter() {
  filterSeller.value = null
  loadOrders()
}

onMounted(() => {
  load()
  loadMembers()
  loadOrders()
})
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 12px; gap: 8px; }
.grow { flex: 1; }
.dim { color: #909399; font-size: 12px; }
.red { color: #f56c6c; font-weight: 600; }
.hint { color: #909399; font-size: 12px; margin-left: 10px; }
.filter { font-size: 13px; color: #606266; }
</style>
