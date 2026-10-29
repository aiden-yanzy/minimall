<template>
  <div class="od">
    <h2>订单详情</h2>
    <div v-if="order.id" class="card">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="订单号">{{ order.orderSn }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="tagType(order.orderStatus)">{{ statusMap[order.orderStatus] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ order.contact }} {{ order.mobile }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ order.address }}</el-descriptions-item>
        <el-descriptions-item label="物流">
          {{ order.expressName }} {{ order.expressNo || '-' }}
          <el-button v-if="order.expressNo" link type="primary" @click="openTraces">查看轨迹</el-button>
        </el-descriptions-item>
        <el-descriptions-item label="商品金额">￥{{ fen2yuan(order.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="优惠">-￥{{ fen2yuan((order.memberDiscount || 0) + (order.couponAmount || 0)) }}</el-descriptions-item>
        <el-descriptions-item label="实付"><b class="red">￥{{ fen2yuan(order.payAmount) }}</b></el-descriptions-item>
      </el-descriptions>

      <div v-for="g in order.goodsList" :key="g.id" class="g">
        <img :src="g.goodsImage" class="img" />
        <div>{{ g.goodsName }} <span class="spec">{{ g.specValueStr }}</span></div>
        <div class="r">
          ￥{{ fen2yuan(g.price) }} × {{ g.num }} = ￥{{ fen2yuan(g.totalPrice) }}
          <div class="g-ops">
            <el-button
              v-if="canAfterSale(g)"
              link
              type="primary"
              size="small"
              @click="$router.push({ path: '/after/apply', query: { orderId: order.id, orderGoodsId: g.id } })"
            >申请售后</el-button>
            <el-button
              v-if="order.orderStatus === 3 && g.isComment !== 1"
              link
              type="warning"
              size="small"
              @click="openComment(g)"
            >评价</el-button>
            <el-tag v-if="g.isAfterSale === 1" size="small" type="info">售后中</el-tag>
          </div>
        </div>
      </div>

      <el-dialog v-model="showTraces" title="物流轨迹" width="480px">
        <el-timeline style="max-height:60vh; overflow:auto">
          <el-timeline-item v-for="t in traces" :key="t.time" :timestamp="(t.time || '').slice(0,16)" placement="top">
            {{ t.desc }}
          </el-timeline-item>
        </el-timeline>
      </el-dialog>

      <el-dialog v-model="showComment" title="评价商品" width="460px">
        <div class="cm-goods">
          <img :src="commentGoods?.goodsImage" class="img" />
          <span>{{ commentGoods?.goodsName }}</span>
        </div>
        <div class="cm-row">
          <span class="cm-label">评分</span>
          <el-rate v-model="score" />
        </div>
        <el-input v-model="commentText" type="textarea" :rows="3" placeholder="说说使用体验吧" />
        <template #footer>
          <el-button @click="showComment = false">取消</el-button>
          <el-button type="danger" :loading="commenting" @click="submitComment">提交评价</el-button>
        </template>
      </el-dialog>

      <div class="actions">
        <el-button v-if="order.orderStatus === 0" @click="cancel">取消订单</el-button>
        <el-button v-if="order.orderStatus === 0" type="danger" @click="pay">立即支付</el-button>
        <el-button v-if="order.orderStatus === 2" type="primary" @click="confirmReceive">确认收货</el-button>
        <el-button @click="$router.push('/orders')">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, getOrderTraces, mockPay, cancelOrder, receive } from '@/api/order'
import { createComment } from '@/api/promo'

const route = useRoute()
const order = ref({})
const statusMap = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const tagType = (s) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' }[s])
const fen2yuan = (f) => (f / 100).toFixed(2)
const showTraces = ref(false)
const traces = ref([])

async function openTraces() {
  const res = await getOrderTraces(route.params.id)
  traces.value = res.data
  showTraces.value = true
}

// ---------- 售后 / 评价 ----------
const showComment = ref(false)
const commentGoods = ref(null)
const score = ref(5)
const commentText = ref('')
const commenting = ref(false)

function canAfterSale(g) {
  const s = order.value.orderStatus
  if (!g || s < 1 || s > 3) return false
  return g.isAfterSale !== 1
}
function openComment(g) {
  commentGoods.value = g
  score.value = 5
  commentText.value = ''
  showComment.value = true
}
async function submitComment() {
  if (!commentText.value.trim()) return ElMessage.warning('请输入评价内容')
  commenting.value = true
  try {
    await createComment({
      orderId: order.value.id,
      orderGoodsId: commentGoods.value.id,
      score: score.value,
      content: commentText.value.trim()
    })
    ElMessage.success('评价成功')
    showComment.value = false
    load()
  } finally {
    commenting.value = false
  }
}

async function load() {
  const res = await getOrderDetail(route.params.id)
  order.value = res.data
}
async function pay() { await mockPay(order.value.orderSn, order.value.payType || 1); load() }
async function cancel() { await cancelOrder(order.value.id, '用户取消'); load() }
async function confirmReceive() { await receive(order.value.id); load() }

onMounted(load)
</script>

<style scoped>
.od { max-width: 1000px; margin: 0 auto; background: #fff; border-radius: 12px; padding: 20px 24px 30px; }
.card { margin-top: 12px; }
.g { display: flex; align-items: center; gap: 14px; padding: 12px 4px; border-bottom: 1px solid #f5f5f5; }
.img { width: 64px; height: 64px; border-radius: 6px; }
.spec { color: #999; font-size: 12px; }
.r { margin-left: auto; }
.actions { text-align: right; padding-top: 16px; }
.red { color: #ff2d2d; }
.g-ops { display: flex; gap: 6px; justify-content: flex-end; margin-top: 6px; align-items: center; }
.cm-goods { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.cm-row { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.cm-label { font-size: 13px; color: #666; }
</style>