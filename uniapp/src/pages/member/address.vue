<template>
  <view class="addr">
    <view v-for="a in list" :key="a.id" class="card" @click="choose(a)">
      <view class="line"><text class="name">{{ a.contact }}</text><text class="mobile">{{ a.mobile }}</text>
        <text v-if="a.isDefault === 1" class="def">默认</text></view>
      <view class="addr-line">{{ a.address }}</view>
      <view class="ops">
        <text class="op" @click.stop="setDefault(a)">{{ a.isDefault === 1 ? '默认地址' : '设为默认' }}</text>
        <text class="op" @click.stop="edit(a)">编辑</text>
        <text class="op del" @click.stop="remove(a)">删除</text>
      </view>
    </view>
    <view v-if="!list.length" class="empty">暂无收货地址</view>
    <button class="add" @click="edit(null)">+ 新增地址</button>

    <!-- 新增/编辑 -->
    <view v-if="showForm" class="mask"></view>
    <view v-if="showForm" class="sheet">
      <view class="sheet-title">{{ form.id ? '编辑地址' : '新增地址' }}</view>
      <input v-model="form.contact" class="ipt" placeholder="收货人" />
      <input v-model="form.mobile" class="ipt" type="number" placeholder="手机号" />
      <input v-model="form.address" class="ipt" placeholder="详细地址" />
      <view class="check-row"><text>设为默认</text><switch :checked="form.isDefault === 1" @change="(e) => form.isDefault = e.detail.value ? 1 : 0" /></view>
      <button class="save" @click="save">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getAddresses, saveAddress, setAddressDefault, removeAddress } from '@/api/member'

const list = ref([])
const chooseMode = ref(false)
const showForm = ref(false)
const form = ref({})

async function load() {
  const res = await getAddresses()
  list.value = res.data
}
function choose(a) {
  if (!chooseMode.value) return
  uni.setStorageSync('selectedAddress', a)
  uni.navigateBack()
}
function edit(a) {
  form.value = a ? { ...a } : { id: null, contact: '', mobile: '', address: '', isDefault: 0 }
  showForm.value = true
}
async function save() {
  if (!form.value.contact || !form.value.mobile || !form.value.address) return uni.showToast({ title: '请填写完整', icon: 'none' })
  await saveAddress(form.value)
  showForm.value = false
  load()
}
async function setDefault(a) {
  if (a.isDefault === 1) return
  await setAddressDefault(a.id)
  load()
}
async function remove(a) {
  uni.showModal({ title: '提示', content: '删除该地址？', success: async (r) => {
    if (r.confirm) { await removeAddress(a.id); load() }
  } })
}

onLoad((o) => { chooseMode.value = o.choose === '1' })
onShow(load)
</script>

<style scoped>
.addr { padding: 20rpx; background: #f5f5f5; min-height: 100vh; padding-bottom: 140rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.line { display: flex; align-items: center; }
.name { font-size: 30rpx; font-weight: bold; }
.mobile { color: #666; margin-left: 16rpx; font-size: 26rpx; }
.def { margin-left: 12rpx; color: #fff; background: #ff2d2d; font-size: 20rpx; border-radius: 8rpx; padding: 2rpx 12rpx; }
.addr-line { color: #666; font-size: 24rpx; margin-top: 10rpx; }
.ops { display: flex; gap: 40rpx; margin-top: 16rpx; border-top: 1rpx solid #f5f5f5; padding-top: 14rpx; }
.op { color: #666; font-size: 24rpx; }
.op.del { color: #ff2d2d; }
.empty { text-align: center; color: #999; padding: 120rpx 0; }
.add { position: fixed; left: 20rpx; right: 20rpx; bottom: 30rpx; background: #ff2d2d; color: #fff; border-radius: 44rpx; }
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); z-index: 10; }
.sheet { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; z-index: 11; padding: 30rpx; }
.sheet-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; }
.ipt { height: 76rpx; border: 1rpx solid #eee; border-radius: 10rpx; padding: 0 20rpx; margin-bottom: 16rpx; }
.check-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.save { background: #ff2d2d; color: #fff; border-radius: 44rpx; }
</style>