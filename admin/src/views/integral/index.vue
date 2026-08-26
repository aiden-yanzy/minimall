<template>
  <div>
    <el-tabs v-model="tab">
      <!-- 积分商品 -->
      <el-tab-pane label="积分商品" name="goods">
        <el-card>
          <div class="toolbar">
            <el-button type="primary" @click="openGoods">+ 新建积分商品</el-button>
          </div>
          <el-table :data="goodsList" border stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="名称" min-width="160" />
            <el-table-column prop="integral" label="所需积分" width="100" />
            <el-table-column prop="stock" label="库存" width="80" />
            <el-table-column prop="sales" label="销量" width="80" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button link type="primary" @click="openGoods(row)">编辑</el-button>
                <el-button link type="danger" @click="removeGoods(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 兑换订单 -->
      <el-tab-pane label="兑换订单" name="orders">
        <el-card>
          <el-table :data="orderList" border stripe>
            <el-table-column prop="orderSn" label="兑换单号" min-width="180" />
            <el-table-column prop="goodsName" label="商品" min-width="140" />
            <el-table-column prop="totalIntegral" label="积分" width="90" />
            <el-table-column label="收货人" min-width="160">
              <template #default="{ row }">{{ row.contact }} {{ row.mobile }} {{ row.address }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'">{{ orderStatus[row.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" link type="primary" @click="deliver(row)">发货</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 签到配置 -->
      <el-tab-pane label="签到配置" name="sign">
        <el-card style="width: 520px">
          <el-form label-width="140px">
            <el-form-item label="每日签到积分">
              <el-input-number v-model="signConfig.integral" :min="1" />
            </el-form-item>
            <el-form-item label="连续天数阈值">
              <el-input-number v-model="signConfig.continuousDays" :min="1" />
            </el-form-item>
            <el-form-item label="连续达标额外奖励">
              <el-input-number v-model="signConfig.continuousReward" :min="0" />
            </el-form-item>
            <el-button type="primary" @click="saveSign">保存配置</el-button>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 积分商品弹窗 -->
    <el-dialog v-model="goodsDialog" :title="goodsForm.id ? '编辑' : '新建积分商品'" width="480px">
      <el-form label-width="90px">
        <el-form-item label="名称"><el-input v-model="goodsForm.name" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="goodsForm.image" /></el-form-item>
        <el-form-item label="所需积分"><el-input-number v-model="goodsForm.integral" :min="1" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="goodsForm.stock" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="goodsForm.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="goodsForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="goodsDialog = false">取消</el-button>
        <el-button type="primary" @click="saveGoods">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  integralGoodsApi, integralGoodsSaveApi, integralGoodsDelApi,
  integralOrdersApi, integralDeliverApi, signConfigApi, signConfigSaveApi
} from '@/api/integral'

const tab = ref('goods')
const orderStatus: Record<number, string> = { 0: '待发货', 1: '已发货', 2: '已完成' }

const goodsList = ref<any[]>([])
const orderList = ref<any[]>([])
const goodsDialog = ref(false)
const goodsForm = reactive<any>({ status: 1 })
const signConfig = reactive<any>({ integral: 5, continuousDays: 7, continuousReward: 10 })

async function loadGoods() {
  const res: any = await integralGoodsApi()
  goodsList.value = res.data
}
async function loadOrders() {
  const res: any = await integralOrdersApi()
  orderList.value = res.data
}
async function loadSign() {
  const res: any = await signConfigApi()
  Object.assign(signConfig, res.data || {})
}
function openGoods(row?: any) {
  Object.assign(goodsForm, row ? { ...row } : { id: null, name: '', image: '', integral: 100, stock: 0, status: 1, description: '' })
  goodsDialog.value = true
}
async function saveGoods() {
  await integralGoodsSaveApi({ ...goodsForm })
  ElMessage.success('保存成功')
  goodsDialog.value = false
  loadGoods()
}
async function removeGoods(row: any) {
  await ElMessageBox.confirm(`删除「${row.name}」？`, '提示', { type: 'warning' })
  await integralGoodsDelApi(row.id)
  loadGoods()
}
async function deliver(row: any) {
  const { value } = await ElMessageBox.prompt('请输入快递单号', '发货', { inputPattern: /\S+/, inputErrorMessage: '单号不能为空' })
  await integralDeliverApi(row.id, '圆通速递', value)
  ElMessage.success('发货成功')
  loadOrders()
}
async function saveSign() {
  await signConfigSaveApi({ ...signConfig, id: signConfig.id || 1 })
  ElMessage.success('已保存')
}

onMounted(() => {
  loadGoods()
  loadOrders()
  loadSign()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
</style>