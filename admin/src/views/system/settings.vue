<template>
  <el-card>
    <el-tabs v-model="tab">
      <el-tab-pane label="基础设置" name="site">
        <el-form label-width="110px" style="max-width:480px">
          <el-form-item label="商城名称"><el-input v-model="site.name" /></el-form-item>
          <el-form-item label="Logo URL"><el-input v-model="site.logo" /></el-form-item>
          <el-form-item label="客服电话"><el-input v-model="site.phone" /></el-form-item>
          <el-form-item label="商城公告"><el-input v-model="site.notice" type="textarea" :rows="3" /></el-form-item>
          <el-button type="primary" @click="save('site')">保存</el-button>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="订单设置" name="order">
        <el-form label-width="160px" style="max-width:480px">
          <el-form-item label="超时自动关单(分钟)"><el-input-number v-model="order.close_minutes" :min="1" /></el-form-item>
          <el-form-item label="发货后自动确认(天)"><el-input-number v-model="order.auto_receive_days" :min="1" /></el-form-item>
          <el-button type="primary" @click="save('order')">保存</el-button>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="配送设置" name="delivery">
        <el-form label-width="110px" style="max-width:480px">
          <el-form-item label="默认快递"><el-input v-model="delivery.default_express" /></el-form-item>
          <el-button type="primary" @click="save('delivery')">保存</el-button>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="支付设置" name="pay">
        <el-form label-width="140px" style="max-width:480px">
          <el-form-item label="启用模拟支付"><el-switch v-model="pay.mock_pay" /></el-form-item>
          <el-form-item label="微信商户号"><el-input v-model="pay.wechat_mchid" placeholder="预留，接入真实支付用" /></el-form-item>
          <el-form-item label="支付宝AppId"><el-input v-model="pay.alipay_appid" placeholder="预留" /></el-form-item>
          <el-button type="primary" @click="save('pay')">保存</el-button>
          <p style="color:#999;font-size:12px">C端公开基础设置: GET /settings/public（商城名/公告/电话）</p>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { settingsAllApi, settingsSaveApi } from '@/api/system'

const tab = ref('site')
const site = reactive<any>({})
const order = reactive<any>({})
const delivery = reactive<any>({})
const pay = reactive<any>({})

async function load() {
  const res: any = await settingsAllApi()
  Object.assign(site, res.data.site || {})
  Object.assign(order, res.data.order || {})
  Object.assign(delivery, res.data.delivery || {})
  Object.assign(pay, res.data.pay || {})
}
async function save(group: string) {
  const data = group === 'site' ? site : group === 'order' ? order : group === 'delivery' ? delivery : pay
  await settingsSaveApi(group, data)
  ElMessage.success('保存成功')
}
onMounted(load)
</script>