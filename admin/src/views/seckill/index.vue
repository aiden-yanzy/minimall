<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="title">秒杀场次</span>
        <el-button type="primary" @click="openActivity" style="float: right">+ 新建场次</el-button>
      </div>
      <el-table :data="acts" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="场次" min-width="140" />
        <el-table-column label="时间" min-width="220">
          <template #default="{ row }">{{ (row.startTime || '').slice(0,16) }} ~ {{ (row.endTime || '').slice(0,16) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleAct(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="primary" @click="addGoods(row)">加商品</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建活动 -->
    <el-dialog v-model="actDialog" title="新建场次" width="440px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="actForm.name" /></el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="actForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="actForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAct">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加秒杀商品 -->
    <el-dialog v-model="goodsDialog" :title="'场次 ' + currentActName + ' 添加秒杀商品'" width="520px">
      <el-form label-width="100px">
        <el-form-item label="SKU ID"><el-input-number v-model="goodsForm.goodsItemId" :min="1" /></el-form-item>
        <el-form-item label="秒杀价(分)"><el-input-number v-model="goodsForm.seckillPrice" :min="1" /></el-form-item>
        <el-form-item label="秒杀库存"><el-input-number v-model="goodsForm.seckillStock" :min="1" /></el-form-item>
        <el-form-item label="每人限购"><el-input-number v-model="goodsForm.perLimit" :min="1" /></el-form-item>
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
import { ElMessage } from 'element-plus'
import { activityListApi, activitySaveApi, activityStatusApi, seckillGoodsSaveApi } from '@/api/seckill'

const acts = ref<any[]>([])
const actDialog = ref(false)
const actForm = reactive<any>({})
const goodsDialog = ref(false)
const goodsForm = reactive<any>({ seckillStock: 10, perLimit: 1 })
const currentActName = ref('')
const currentActId = ref<number | null>(null)

async function load() {
  const res: any = await activityListApi()
  acts.value = res.data
}
function openActivity() {
  Object.assign(actForm, { id: null, name: '', status: 1 })
  actDialog.value = true
}
async function saveAct() {
  await activitySaveApi({ ...actForm, status: actForm.status ?? 1 })
  ElMessage.success('保存成功')
  actDialog.value = false
  load()
}
async function toggleAct(row: any) {
  await activityStatusApi(row.id, row.status === 1 ? 0 : 1)
  load()
}
function addGoods(row: any) {
  currentActId.value = row.id
  currentActName.value = row.name
  Object.assign(goodsForm, { id: null, activityId: row.id, goodsItemId: 1, seckillPrice: 1, seckillStock: 10, perLimit: 1 })
  goodsDialog.value = true
}
async function saveGoods() {
  await seckillGoodsSaveApi({ ...goodsForm, activityId: currentActId.value })
  ElMessage.success('保存成功')
  goodsDialog.value = false
}

onMounted(() => load())
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
.title { font-weight: bold; }
</style>