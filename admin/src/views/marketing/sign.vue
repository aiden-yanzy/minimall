<template>
  <el-card>
    <div class="head">
      <h3>每日签到配置</h3>
      <span class="dim">全站只有一份配置，改完立即对 C 端生效</span>
    </div>

    <el-form v-loading="loading" label-width="160px" style="max-width: 560px; margin-top: 16px">
      <el-form-item label="每日签到奖励积分">
        <el-input-number v-model="form.integral" :min="0" :max="10000" />
        <span class="hint">每次签到获得</span>
      </el-form-item>
      <el-form-item label="连续签到天数">
        <el-input-number v-model="form.continuousDays" :min="1" :max="365" />
        <span class="hint">连签达到这个天数发额外奖励</span>
      </el-form-item>
      <el-form-item label="连续签到额外奖励">
        <el-input-number v-model="form.continuousReward" :min="0" :max="100000" />
        <span class="hint">积分</span>
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
        <el-button @click="load">重置</el-button>
      </el-form-item>
    </el-form>

    <el-divider />
    <div class="preview">
      <div class="pv-title">C 端效果预览</div>
      <div class="pv-body">
        每天签到 <b>+{{ form.integral || 0 }}</b> 积分；连续签到 <b>{{ form.continuousDays || 0 }}</b> 天
        额外奖励 <b>+{{ form.continuousReward || 0 }}</b> 积分
        <span v-if="form.status !== 1" class="off">（当前已停用，C 端签到入口不可用）</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { signConfigApi, signConfigSaveApi } from '@/api/p5'

const loading = ref(false)
const saving = ref(false)
const form = reactive<any>({ id: null, integral: 5, continuousDays: 7, continuousReward: 10, status: 1 })

async function load() {
  loading.value = true
  try {
    const res: any = await signConfigApi()
    if (res.data) Object.assign(form, res.data)
  } finally { loading.value = false }
}
async function save() {
  saving.value = true
  try {
    await signConfigSaveApi({ ...form })
    ElMessage.success('已保存，C 端立即生效')
    load()
  } finally { saving.value = false }
}

onMounted(load)
</script>

<style scoped>
.head { display: flex; align-items: baseline; gap: 12px; }
.head h3 { margin: 0; }
.dim { color: #909399; font-size: 12px; }
.hint { color: #909399; font-size: 12px; margin-left: 10px; }
.preview { background: #f7f8fa; border-radius: 8px; padding: 14px 16px; }
.pv-title { font-size: 13px; color: #909399; margin-bottom: 8px; }
.pv-body { font-size: 14px; color: #303133; }
.pv-body b { color: #f56c6c; }
.off { color: #f56c6c; }
</style>
