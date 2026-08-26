<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="title">优惠券管理</span>
        <el-button type="primary" @click="openCoupon" style="float: right">+ 新建优惠券</el-button>
      </div>
      <el-table :data="coupons" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ row.type === 2 ? '折扣券' : '满减券' }}</template>
        </el-table-column>
        <el-table-column label="门槛(分)" width="100">
          <template #default="{ row }">{{ row.threshold }}</template>
        </el-table-column>
        <el-table-column label="面值" width="120">
          <template #default="{ row }">{{ row.type === 2 ? (row.discountValue / 10) + '折' : row.discountValue + '分' }}</template>
        </el-table-column>
        <el-table-column label="发放" width="120">
          <template #default="{ row }">{{ row.sendCount }} / {{ row.totalCount || '不限' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleCoupon(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="removeCoupon(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top: 16px">
      <div class="toolbar">
        <span class="title">全场满减</span>
        <el-button type="primary" @click="openReduction" style="float: right">+ 新建满减</el-button>
      </div>
      <el-table :data="reductions" border stripe>
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="threshold" label="满(分)" width="100" />
        <el-table-column prop="reduce" label="减(分)" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeReduction(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建优惠券 -->
    <el-dialog v-model="couponDialog" title="新建优惠券" width="520px">
      <el-form label-width="100px">
        <el-form-item label="名称"><el-input v-model="couponForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="couponForm.type">
            <el-radio :value="1">满减券</el-radio>
            <el-radio :value="2">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="门槛(分)"><el-input-number v-model="couponForm.threshold" :min="0" /></el-form-item>
        <el-form-item :label="couponForm.type === 2 ? '折扣(950=9.5折)' : '面值(分)'">
          <el-input-number v-model="couponForm.discountValue" :min="0" />
        </el-form-item>
        <el-form-item label="发放总量"><el-input-number v-model="couponForm.totalCount" :min="0" /></el-form-item>
        <el-form-item label="每人限领"><el-input-number v-model="couponForm.perLimit" :min="1" /></el-form-item>
        <el-form-item label="有效天数"><el-input-number v-model="couponForm.validDays" :min="1" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="couponForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="couponDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCoupon">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新建满减 -->
    <el-dialog v-model="reductionDialog" title="新建满减" width="420px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="reductionForm.name" /></el-form-item>
        <el-form-item label="满(分)"><el-input-number v-model="reductionForm.threshold" :min="1" /></el-form-item>
        <el-form-item label="减(分)"><el-input-number v-model="reductionForm.reduce" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reductionDialog = false">取消</el-button>
        <el-button type="primary" @click="saveReduction">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  couponPageApi, couponSaveApi, couponStatusApi, couponDeleteApi,
  reductionListApi, reductionSaveApi, reductionDeleteApi
} from '@/api/marketing'

const loading = ref(false)
const coupons = ref<any[]>([])
const reductions = ref<any[]>([])

const couponDialog = ref(false)
const couponForm = reactive<any>({ type: 1, threshold: 0, discountValue: 0, totalCount: 0, perLimit: 1, validDays: 7, validType: 2, status: 1, scope: 1 })
const reductionDialog = ref(false)
const reductionForm = reactive<any>({ threshold: 1000, reduce: 100, status: 1 })

async function loadCoupons() {
  loading.value = true
  try {
    const res: any = await couponPageApi({ page: 1, size: 50 })
    coupons.value = res.data.records
  } finally {
    loading.value = false
  }
}
async function loadReductions() {
  const res: any = await reductionListApi()
  reductions.value = res.data
}

function openCoupon() {
  Object.assign(couponForm, { id: null, name: '', type: 1, threshold: 0, discountValue: 0, totalCount: 0, perLimit: 1, validDays: 7, status: 1, scope: 1 })
  couponDialog.value = true
}
async function saveCoupon() {
  await couponSaveApi({ ...couponForm })
  ElMessage.success('保存成功')
  couponDialog.value = false
  loadCoupons()
}
async function toggleCoupon(row: any) {
  await couponStatusApi(row.id, row.status === 1 ? 0 : 1)
  loadCoupons()
}
async function removeCoupon(row: any) {
  await ElMessageBox.confirm(`删除「${row.name}」？`, '提示', { type: 'warning' })
  await couponDeleteApi(row.id)
  loadCoupons()
}

function openReduction() {
  Object.assign(reductionForm, { id: null, name: '', threshold: 1000, reduce: 100, status: 1 })
  reductionDialog.value = true
}
async function saveReduction() {
  await reductionSaveApi({ ...reductionForm })
  ElMessage.success('保存成功')
  reductionDialog.value = false
  loadReductions()
}
async function removeReduction(row: any) {
  await ElMessageBox.confirm(`删除「${row.name}」？`, '提示', { type: 'warning' })
  await reductionDeleteApi(row.id)
  loadReductions()
}

onMounted(() => {
  loadCoupons()
  loadReductions()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.title {
  font-weight: bold;
}
</style>