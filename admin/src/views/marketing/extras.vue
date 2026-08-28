<template>
  <el-tabs v-model="tab">
    <!-- 幸运抽奖 -->
    <el-tab-pane label="幸运抽奖" name="lucky">
      <el-card>
        <el-form inline>
          <el-form-item label="场次名"><el-input v-model="drawForm.name" style="width:180px" /></el-form-item>
          <el-form-item label="每次花费积分"><el-input-number v-model="drawForm.integralCost" :min="1" /></el-form-item>
          <el-button type="primary" @click="saveDraw">保存场次</el-button>
        </el-form>
        <el-table :data="prizes" border stripe style="margin-top:12px">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="prizeName" label="奖品" min-width="120" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">{{ { 1: '积分', 2: '余额', 3: '优惠券', 4: '未中' }[row.prizeType] }}</template>
          </el-table-column>
          <el-table-column prop="prizeValue" label="数值" width="100" />
          <el-table-column prop="weight" label="权重" width="80" />
          <el-table-column label="库存" width="110"><template #default="{ row }">{{ row.total || '不限' }} (已用{{ row.used }})</template></el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="editPrize(row)">编辑</el-button>
              <el-button link type="primary" @click="addPrize">新增</el-button>
            </template>
          </el-table-column>
        </el-table>
        <h4>中奖记录（{{ records.length }}）</h4>
        <el-table :data="records" border size="small">
          <el-table-column prop="userId" label="用户" width="90" />
          <el-table-column prop="prizeName" label="奖品" min-width="120" />
          <el-table-column label="类型" width="90"><template #default="{ row }">{{ {1:'积分',2:'余额',3:'券',4:'未中'}[row.prizeType] }}</template></el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="150" />
        </el-table>
      </el-card>
    </el-tab-pane>

    <!-- 注册送 -->
    <el-tab-pane label="注册送" name="award">
      <el-card style="width:520px">
        <el-form label-width="110px">
          <el-form-item label="奖励类型">
            <el-select v-model="awardForm.awardType">
              <el-option label="积分" :value="1" />
              <el-option label="余额(分)" :value="2" />
              <el-option label="优惠券(模板ID)" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="数值/券ID"><el-input-number v-model="awardForm.awardValue" :min="0" /></el-form-item>
          <el-form-item label="启用"><el-switch v-model="awardForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-button type="primary" @click="saveAward">保存（新注册用户立即生效）</el-button>
        </el-form>
      </el-card>
    </el-tab-pane>

    <!-- 免邮 -->
    <el-tab-pane label="满额免邮" name="fs">
      <el-card style="width:520px">
        <el-form label-width="110px">
          <el-form-item label="规则名"><el-input v-model="fsForm.name" /></el-form-item>
          <el-form-item label="满(分)"><el-input-number v-model="fsForm.threshold" :min="1" /></el-form-item>
          <el-form-item label="启用"><el-switch v-model="fsForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-button type="primary" @click="saveFs">保存</el-button>
          <p style="color:#999;margin-top:10px">下单时若「商品金额-优惠 ≥ 门槛」则运费为 0</p>
        </el-form>
      </el-card>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { luckyDrawsApi, luckyDrawSaveApi, luckyPrizesApi, luckyPrizeSaveApi, luckyRecordsApi,
  registerAwardApi, registerAwardSaveApi, freeShippingApi, freeShippingSaveApi } from '@/api/lottery'

const tab = ref('lucky')
const prizes = ref<any[]>([])
const records = ref<any[]>([])
const drawForm = reactive<any>({ name: '', integralCost: 100 })
const prizeForm = reactive<any>({})
const awardForm = reactive<any>({ awardType: 1, awardValue: 0, status: 1 })
const fsForm = reactive<any>({ name: '', threshold: 20000, status: 1 })

async function load() {
  const [d, p, r, a, f] = await Promise.all([luckyDrawsApi(), luckyPrizesApi(), luckyRecordsApi(), registerAwardApi(), freeShippingApi()])
  if (d.data?.length) Object.assign(drawForm, d.data[0])
  prizes.value = p.data
  records.value = r.data
  if (a.data) Object.assign(awardForm, a.data)
  if (f.data?.length) Object.assign(fsForm, f.data[0])
}
async function saveDraw() {
  await luckyDrawSaveApi({ ...drawForm, status: 1 })
  ElMessage.success('已保存')
}
function addPrize() {
  Object.assign(prizeForm, { id: null, drawId: drawForm.id || 1, prizeName: '', prizeType: 1, prizeValue: 100, weight: 10, total: 0, used: 0, status: 1 })
  luckyPrizeSaveApi(prizeForm).then(() => ElMessage.success('奖品已添加'))
}
function editPrize(row: any) {
  Object.assign(prizeForm, { ...row })
  luckyPrizeSaveApi(prizeForm).then(() => ElMessage.success('已更新'))
}
async function saveAward() {
  await registerAwardSaveApi({ ...awardForm })
  ElMessage.success('已保存')
}
async function saveFs() {
  await freeShippingSaveApi({ ...fsForm })
  ElMessage.success('已保存')
}
onMounted(load)
</script>