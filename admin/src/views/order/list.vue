<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 140px" @change="load">
          <el-option label="待支付" :value="0" />
          <el-option label="待发货" :value="1" />
          <el-option label="待收货" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-input v-model="query.orderSn" placeholder="订单号" style="width: 220px" clearable @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="orderSn" label="订单号" min-width="170" />
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div v-for="g in row.goodsList" :key="g.id">{{ g.goodsName }} × {{ g.num }}</div>
          </template>
        </el-table-column>
        <el-table-column label="实付(分)" width="100">
          <template #default="{ row }">{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column label="收货人" width="120">
          <template #default="{ row }">{{ row.contact }} {{ row.mobile }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.orderStatus)">{{ statusMap[row.orderStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物流" width="150">
          <template #default="{ row }">{{ row.expressName }} {{ row.expressNo }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button v-if="row.orderStatus === 1" link type="primary" @click="openDeliver(row)">发货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发货弹窗 -->
    <el-dialog v-model="deliverVisible" title="订单发货" width="420px">
      <el-form label-width="80px">
        <el-form-item label="订单号">
          <span>{{ current?.orderSn }}</span>
        </el-form-item>
        <el-form-item label="快递公司">
          <el-select v-model="deliver.expressId" placeholder="选择快递" style="width: 100%">
            <el-option label="顺丰速运" :value="1" />
            <el-option label="圆通速递" :value="2" />
            <el-option label="中通快递" :value="3" />
            <el-option label="韵达快递" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input v-model="deliver.expressNo" placeholder="输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible = false">取消</el-button>
        <el-button type="primary" @click="deliver">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { orderPageApi, orderDeliverApi } from '@/api/order'

const list = ref<any[]>([])
const loading = ref(false)
const query = reactive({ status: 1 as number | null, orderSn: '' })

const statusMap: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const statusType = (s: number) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' }[s] as any)

const deliverVisible = ref(false)
const current = ref<any>(null)
const deliver = reactive({ expressId: 1, expressName: '顺丰速运', expressNo: '' })

async function load() {
  loading.value = true
  try {
    const res: any = await orderPageApi({ ...query })
    list.value = res.data
  } finally {
    loading.value = false
  }
}
function openDeliver(row: any) {
  current.value = row
  deliver.expressId = 1
  deliver.expressName = '顺丰速运'
  deliver.expressNo = ''
  deliverVisible.value = true
}
async function deliverSubmit() {
  const nameMap: Record<number, string> = { 1: '顺丰速运', 2: '圆通速递', 3: '中通快递', 4: '韵达快递' }
  deliver.expressName = nameMap[deliver.expressId as number]
  await orderDeliverApi(current.value.id, { ...deliver })
  ElMessage.success('发货成功')
  deliverVisible.value = false
  load()
}

onMounted(() => load())
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
</style>
