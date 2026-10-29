<template>
  <div class="page">
    <div class="head"><h2>申请售后</h2></div>

    <el-card shadow="never" class="block">
      <div v-if="goods" class="goods">
        <img :src="goods.goodsImage" class="img" />
        <div class="g-info">
          <div class="g-name">{{ goods.goodsName }}</div>
          <div class="g-spec">{{ goods.specValueStr }}</div>
          <div class="g-price">￥{{ ((goods.price || 0) / 100).toFixed(2) }} × {{ goods.num }}</div>
        </div>
      </div>
      <el-empty v-else description="订单商品加载中…" :image-size="60" />
    </el-card>

    <el-card shadow="never" class="block">
      <el-form label-width="90px">
        <el-form-item label="售后类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">仅退款</el-radio>
            <el-radio :value="2">退货退款</el-radio>
            <el-radio :value="3">换货</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input v-model="amountText" style="width: 180px">
            <template #prepend>￥</template>
          </el-input>
          <el-button link type="primary" style="margin-left: 10px" @click="fillAll">全额</el-button>
          <span class="dim" style="margin-left: 10px">最多可退 ￥{{ ((maxAmount || 0) / 100).toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="申请原因">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请描述问题，便于商家快速处理" style="width: 480px" />
          <div class="quick">
            <el-tag
              v-for="r in quickReasons"
              :key="r"
              size="small"
              class="q-tag"
              @click="form.reason = r"
            >{{ r }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="danger" :loading="submitting" @click="submit">提交申请</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
      <div class="notice">
        提交后商家会在 48 小时内处理；同意退款将退回账户余额，可在「会员中心 → 余额明细」查看流水。
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createAfterSale } from '@/api/promo'
import { getOrderDetail } from '@/api/order'

const route = useRoute()
const router = useRouter()
const orderId = Number(route.query.orderId)
const orderGoodsId = ref(Number(route.query.orderGoodsId))
const goods = ref(null)
const maxAmount = ref(0)
const amountText = ref('')
const submitting = ref(false)
const quickReasons = ['不想要了', '商品质量问题', '与描述不符', '少发/漏发', '包装破损']
const form = reactive({ type: 1, reason: '' })

function fillAll() {
  amountText.value = ((maxAmount.value || 0) / 100).toFixed(2)
}

onMounted(async () => {
  const res = await getOrderDetail(orderId)
  const list = res.data?.goodsList || []
  goods.value = list.find((g) => String(g.id) === String(orderGoodsId.value)) || list[0] || null
  if (goods.value) {
    orderGoodsId.value = goods.value.id
    maxAmount.value = goods.value.totalPrice || (goods.value.price || 0) * (goods.value.num || 1)
    fillAll()
  }
})

async function submit() {
  if (!form.reason.trim()) return ElMessage.warning('请填写申请原因')
  const yuan = Number(amountText.value || 0)
  if (!(yuan > 0)) return ElMessage.warning('请填写退款金额')
  submitting.value = true
  try {
    await createAfterSale({
      orderId,
      orderGoodsId: orderGoodsId.value,
      type: form.type,
      reason: form.reason.trim(),
      refundAmount: Math.round(yuan * 100)
    })
    ElMessage.success('提交成功')
    router.push('/member')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 20px 0 40px; }
.head h2 { margin: 0 0 18px; font-size: 22px; }
.block { margin-bottom: 16px; }
.goods { display: flex; gap: 16px; }
.img { width: 90px; height: 90px; border-radius: 8px; object-fit: cover; background: #f5f7fa; }
.g-name { font-size: 15px; }
.g-spec { font-size: 12px; color: #999; margin-top: 6px; }
.g-price { color: #ff2d2d; font-weight: 600; margin-top: 6px; }
.dim { color: #999; font-size: 12px; }
.quick { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }
.q-tag { cursor: pointer; }
.notice { font-size: 12px; color: #999; line-height: 1.7; }
</style>
