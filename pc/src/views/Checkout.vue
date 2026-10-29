<template>
  <div class="checkout">
    <h2>确认订单</h2>
    <el-form label-width="80px" class="addr">
      <el-form-item label="收货地址">
        <el-select v-model="addressId" style="width:300px" placeholder="选择地址" @change="applyAddress">
          <el-option v-for="a in addressList" :key="a.id" :label="a.contact + ' ' + a.mobile + ' · ' + a.address" :value="a.id" />
        </el-select>
        <el-button link type="primary" style="margin-left:8px" @click="openAdd">+ 新增地址</el-button>
      </el-form-item>
      <el-form-item v-if="addressId">
        <span>{{ contact }} · {{ mobile }} · {{ address }}</span>
      </el-form-item>
    </el-form>

    <el-dialog v-model="addDialog" title="新增收货地址" width="420px">
      <el-form label-width="70px">
        <el-form-item label="收货人"><el-input v-model="newAddr.contact" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="newAddr.mobile" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="newAddr.address" /></el-form-item>
        <el-form-item label="默认"><el-switch v-model="newAddr.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog = false">取消</el-button>
        <el-button type="primary" @click="saveNewAddr">保存</el-button>
      </template>
    </el-dialog>

    <el-table :data="goodsList" style="margin:0 24px">
      <el-table-column label="商品">
        <template #default="{ row }">
          <div class="g"><img :src="row.image" class="img" />{{ row.name }} <span class="spec">{{ row.spec }}</span></div>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="120"><template #default="{ row }">￥{{ fen2yuan(row.price) }}</template></el-table-column>
      <el-table-column label="数量" width="80"><template #default="{ row }">x{{ row.num }}</template></el-table-column>
    </el-table>

    <div class="summary">
      <div class="row"><span>商品金额</span><span>￥{{ fen2yuan(preview?.totalAmount || 0) }}</span></div>
      <div class="row" v-if="preview?.memberDiscount"><span>会员优惠</span><span class="red">-￥{{ fen2yuan(preview.memberDiscount) }}</span></div>
      <div class="row" v-if="preview?.fullReduction"><span>满减</span><span class="red">-￥{{ fen2yuan(preview.fullReduction) }}</span></div>
      <div class="row" v-if="preview?.couponAmount"><span>优惠券</span><span class="red">-￥{{ fen2yuan(preview.couponAmount) }}</span></div>
      <div class="row" v-if="preview?.freightAmount"><span>运费</span><span>￥{{ fen2yuan(preview.freightAmount) }}</span></div>
      <div class="row">
        <span>优惠券</span>
        <el-select v-model="couponListId" placeholder="不使用" clearable size="small" style="width:220px" @change="fetchPreview">
          <el-option v-for="c in available" :key="c.id" :label="c.couponName" :value="c.id" />
        </el-select>
      </div>
      <div class="row total"><span>应付</span><span class="red big">￥{{ fen2yuan(preview?.payAmount || 0) }}</span></div>
      <el-button type="danger" size="large" :loading="submitting" class="submit" @click="submit">提交订单</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetail } from '@/api/goods'
import { getCartList } from '@/api/cart'
import { getAddresses, saveAddress } from '@/api/member'
import { previewOrder, createOrder } from '@/api/order'
import { seckillList } from '@/api/promo'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const buyType = ref(route.query.buyType || 'cart')
const goodsList = ref([])
const preview = ref(null)
const available = ref([])
const couponListId = ref(null)
const contact = ref('')
const mobile = ref('')
const address = ref('')
const submitting = ref(false)
const addressList = ref([])
const addressId = ref(null)
const addDialog = ref(false)
const newAddr = ref({ contact: '', mobile: '', address: '', isDefault: 0 })
const fen2yuan = (f) => (f / 100).toFixed(2)

async function loadAddresses() {
  const res = await getAddresses()
  addressList.value = res.data
  if (addressList.value.length) {
    addressId.value = (addressList.value.find((a) => a.isDefault === 1) || addressList.value[0]).id
    applyAddress()
  }
}
function applyAddress() {
  const a = addressList.value.find((x) => x.id === addressId.value)
  if (a) { contact.value = a.contact; mobile.value = a.mobile; address.value = a.address }
}
function openAdd() {
  Object.assign(newAddr, { contact: '', mobile: '', address: '', isDefault: 0 })
  addDialog.value = true
}
async function saveNewAddr() {
  if (!newAddr.value.contact || !newAddr.value.mobile || !newAddr.value.address) return ElMessage.warning('请填写完整')
  const res = await saveAddress({ ...newAddr.value, isDefault: newAddr.value.isDefault ? 1 : 0 })
  addDialog.value = false
  await loadAddresses()
  addressId.value = res.data
  applyAddress()
  fetchPreview()
}

function buildDto() {
  const dto = {
    buyType: buyType.value,
    addressId: addressId.value,
    contact: contact.value,
    mobile: mobile.value,
    address: address.value
  }
  if (couponListId.value) dto.couponListId = couponListId.value
  if (buyType.value === 'buyNow') {
    dto.goodsItemId = Number(route.query.goodsItemId)
    dto.num = Number(route.query.num || 1)
  }
  if (buyType.value === 'seckill') {
    // 秒杀单：后端按 seckillGoodsId 取价，不用 goodsItemId
    dto.seckillGoodsId = Number(route.query.seckillGoodsId)
    dto.num = Number(route.query.num || 1)
  }
  return dto
}

async function init() {
  await loadAddresses()
  if (buyType.value === 'cart') {
    const res = await getCartList()
    goodsList.value = res.data.filter((i) => i.isSelected === 1)
  } else if (buyType.value === 'seckill') {
    // 秒杀商品展示：从秒杀列表里找出当前场次商品
    const sl = await seckillList()
    const acts = sl.data || []
    let found = null
    acts.forEach((a) => (a.goodsList || []).forEach((g) => {
      if (String(g.id) === String(route.query.seckillGoodsId)) found = g
    }))
    if (found) {
      goodsList.value = [{
        key: 1,
        image: found.goodsImage,
        name: found.goodsName,
        spec: found.specValueStr || '秒杀',
        price: found.seckillPrice,
        num: Number(route.query.num || 1)
      }]
    }
  } else {
    const res = await getGoodsDetail(route.query.goodsId)
    const it = res.data.items.find((i) => i.id == route.query.goodsItemId)
    goodsList.value = [{ key: 1, image: res.data.image, name: res.data.name, spec: it.specValueStr, price: it.price, num: Number(route.query.num || 1) }]
  }
  await fetchPreview()
}

async function fetchPreview() {
  const res = await previewOrder(buildDto())
  preview.value = res.data
  available.value = res.data.availableCoupons || []
}

async function submit() {
  if (!addressId.value) return ElMessage.warning('请先选择收货地址')
  submitting.value = true
  try {
    const dto = buildDto()
    const res = await createOrder(dto)
    router.push('/order/' + res.data)
  } finally { submitting.value = false }
}

onMounted(init)
</script>

<style scoped>
.checkout { max-width: 1000px; margin: 0 auto; background: #fff; border-radius: 12px; padding: 20px 0 30px; }
.checkout h2, .addr { padding: 0 24px; }
.addr { padding: 12px 24px 0; }
.g { display: flex; align-items: center; gap: 10px; }
.img { width: 50px; height: 50px; border-radius: 6px; }
.spec { color: #999; font-size: 12px; }
.summary { padding: 20px 24px; border-top: 1px solid #f0f0f0; }
.row { display: flex; justify-content: space-between; padding: 6px 0; }
.row.total { font-size: 16px; font-weight: 700; }
.red { color: #ff2d2d; }
.big { font-size: 22px; }
.submit { margin-top: 16px; float: right; }
</style>