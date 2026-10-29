<template>
  <div class="page">
    <div class="head">
      <div>
        <h2>积分商城</h2>
        <div class="sub">用积分免费兑换好物</div>
      </div>
      <div class="my-integral" v-if="user.token">
        我的积分 <b>{{ integral }}</b>
        <router-link to="/member" class="link">积分明细 →</router-link>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col v-for="g in list" :key="g.id" :span="6">
        <div class="card">
          <img :src="g.image" class="img" />
          <div class="name">{{ g.name }}</div>
          <div class="desc">{{ g.description }}</div>
          <div class="row">
            <span class="points">{{ g.integral }} 积分</span>
            <span class="stock">剩 {{ g.stock }} 件</span>
          </div>
          <el-button
            size="small"
            type="danger"
            class="btn"
            :disabled="g.stock <= 0 || (user.token && integral < g.integral)"
            @click="exchange(g)"
          >{{ g.stock <= 0 ? '已兑完' : (user.token && integral < g.integral ? '积分不足' : '立即兑换') }}</el-button>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!list.length" description="暂无积分商品" />

    <el-dialog v-model="dlg" title="确认兑换" width="420px">
      <div class="dlg-goods" v-if="current">
        <img :src="current.image" class="dlg-img" />
        <div>
          <div class="dlg-name">{{ current.name }}</div>
          <div class="dlg-points">{{ current.integral }} 积分 × {{ form.num }}</div>
        </div>
      </div>
      <el-form label-width="80px" class="dlg-form">
        <el-form-item label="数量">
          <el-input-number v-model="form.num" :min="1" :max="current?.stock || 1" />
        </el-form-item>
        <el-form-item label="收货人"><el-input v-model="form.contact" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.mobile" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submit">确认兑换（{{ (current?.integral || 0) * form.num }} 积分）</el-button>
      </template>
    </el-dialog>

    <div class="orders" v-if="user.token">
      <h3>我的兑换记录</h3>
      <el-table :data="orders" border size="small">
        <el-table-column prop="id" label="单号" width="80" />
        <el-table-column prop="goodsName" label="商品" min-width="160" />
        <el-table-column prop="integral" label="消耗积分" width="100" />
        <el-table-column prop="num" label="数量" width="70" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">{{ row.status === 1 ? '已完成' : '处理中' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 16) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { integralGoods, integralExchange, integralOrders } from '@/api/promo'
import { useUserStore } from '@/store/user'
import { getMe } from '@/api/user'

const user = useUserStore()
const list = ref([])
const orders = ref([])
const integral = ref(0)
const dlg = ref(false)
const current = ref(null)
const submitting = ref(false)
const form = reactive({ num: 1, contact: '', mobile: '', address: '' })

async function load() {
  const res = await integralGoods()
  list.value = (res.data || []).filter((g) => g.status === 1)
  if (user.token) {
    const me = await getMe()
    integral.value = me.data?.integral || 0
    const o = await integralOrders()
    orders.value = o.data || []
  }
}

function exchange(g) {
  if (!user.token) return ElMessage.warning('请先登录')
  current.value = g
  form.num = 1
  dlg.value = true
}

async function submit() {
  if (!form.contact || !form.mobile || !form.address) return ElMessage.warning('请填写收货信息')
  submitting.value = true
  try {
    await integralExchange({ goodsId: current.value.id, num: form.num, contact: form.contact, mobile: form.mobile, address: form.address })
    ElMessage.success('兑换成功')
    dlg.value = false
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; padding: 20px 0 40px; }
.head { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 18px; }
.head h2 { margin: 0; font-size: 22px; }
.sub { color: #999; font-size: 13px; margin-top: 6px; }
.my-integral { font-size: 13px; color: #666; }
.my-integral b { color: #ff2d2d; font-size: 18px; margin: 0 6px; }
.link { color: #ff2d2d; text-decoration: none; margin-left: 8px; }
.card { background: #fff; border-radius: 10px; padding: 10px; margin-bottom: 16px; }
.img { width: 100%; height: 200px; object-fit: cover; border-radius: 8px; background: #f0f2f5; }
.name { font-size: 14px; margin-top: 10px; }
.desc { font-size: 12px; color: #999; height: 18px; overflow: hidden; }
.row { display: flex; justify-content: space-between; align-items: baseline; margin: 8px 0; }
.points { color: #ff2d2d; font-weight: 700; font-size: 17px; }
.stock { font-size: 12px; color: #bbb; }
.btn { width: 100%; }
.dlg-goods { display: flex; gap: 12px; align-items: center; margin-bottom: 14px; }
.dlg-img { width: 64px; height: 64px; border-radius: 8px; object-fit: cover; }
.dlg-name { font-size: 14px; }
.dlg-points { color: #ff2d2d; font-size: 13px; margin-top: 6px; }
.orders { margin-top: 30px; background: #fff; border-radius: 12px; padding: 18px; }
.orders h3 { margin: 0 0 14px; font-size: 16px; }
</style>
