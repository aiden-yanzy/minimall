<template>
  <div class="orders">
    <h2>我的订单</h2>
    <el-tabs v-model="status" @tab-change="load">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="待发货" name="1" />
      <el-tab-pane label="待收货" name="2" />
      <el-tab-pane label="已完成" name="3" />
    </el-tabs>
    <div v-for="o in list" :key="o.id" class="card">
      <div class="head">
        <span class="sn">{{ o.orderSn }}</span>
        <el-tag :type="tagType(o.orderStatus)">{{ statusMap[o.orderStatus] }}</el-tag>
      </div>
      <div class="body">
        <div v-for="g in o.goodsList" :key="g.id" class="g">
          <img :src="g.goodsImage" class="img" />
          <div>{{ g.goodsName }} <span class="spec">{{ g.specValueStr }}</span></div>
          <div class="r">￥{{ fen2yuan(g.price) }} × {{ g.num }}</div>
        </div>
        <div class="actions">
          实付 <b class="red">￥{{ fen2yuan(o.payAmount) }}</b>
          <el-button v-if="o.orderStatus === 0" size="small" @click="cancel(o)">取消</el-button>
          <el-button v-if="o.orderStatus === 0" type="danger" size="small" @click="pay(o)">去支付</el-button>
          <el-button v-if="o.orderStatus === 2" type="primary" size="small" @click="confirmReceive(o)">确认收货</el-button>
          <el-button size="small" @click="$router.push('/order/' + o.id)">详情</el-button>
        </div>
      </div>
    </div>
    <el-empty v-if="!list.length" description="暂无订单" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { onMounted } from 'vue'
import { getOrderList, mockPay, cancelOrder, receive } from '@/api/order'

const status = ref('all')
const list = ref([])
const statusMap = { 0: '待支付', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const tagType = (s) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'success', 4: 'info' }[s])
const fen2yuan = (f) => (f / 100).toFixed(2)

async function load() {
  const res = await getOrderList(status.value === 'all' ? null : Number(status.value))
  list.value = res.data
}
async function pay(o) {
  await mockPay(o.orderSn, o.payType || 1)
  load()
}
async function cancel(o) {
  await cancelOrder(o.id, '用户取消')
  load()
}
async function confirmReceive(o) {
  await receive(o.id)
  load()
}
onMounted(load)
</script>

<style scoped>
.orders { max-width: 1100px; margin: 0 auto; background: #fff; border-radius: 12px; padding: 20px 24px 30px; }
.card { border: 1px solid #f0f0f0; border-radius: 10px; margin-bottom: 14px; }
.head { display: flex; justify-content: space-between; padding: 10px 16px; background: #fafafa; border-radius: 10px 10px 0 0; }
.sn { color: #666; font-size: 13px; }
.body { padding: 12px 16px; }
.g { display: flex; align-items: center; gap: 12px; padding: 8px 0; }
.img { width: 56px; height: 56px; border-radius: 6px; }
.spec { color: #999; font-size: 12px; }
.r { margin-left: auto; color: #333; }
.actions { text-align: right; padding-top: 10px; border-top: 1px dashed #eee; margin-top: 6px; }
.red { color: #ff2d2d; }
</style>