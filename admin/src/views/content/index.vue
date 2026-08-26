<template>
  <el-tabs v-model="tab">
    <el-tab-pane label="首页装修" name="decorate">
      <el-card>
        <p style="color:#999">装修 JSON：轮播(banners[image,link])、宫格(navs[name,link])、商品标题(goodsTitle)，保存后 C 端首页即时生效。</p>
        <el-input v-model="decorateJson" type="textarea" :rows="12" style="margin-top:12px" @blur="formatJson" />
        <el-button type="primary" style="margin-top:12px" @click="saveDecorate">保存装修</el-button>
        <el-button @click="loadDecorate">重置读取</el-button>
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="文章管理" name="article">
      <el-card>
        <el-form inline>
          <el-form-item label="标题"><el-input v-model="articleForm.title" style="width:220px" /></el-form-item>
          <el-form-item label="内容"><el-input v-model="articleForm.content" type="textarea" style="width:400px" /></el-form-item>
          <el-button type="primary" @click="saveArticle">保存文章</el-button>
        </el-form>
        <el-table :data="articles" border stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="isShow" label="显示" width="70">
            <template #default="{ row }">{{ row.isShow === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="primary" @click="editArticle(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="在线客服" name="kefu">
      <el-card>
        <el-table :data="msgs" border stripe>
          <el-table-column prop="userId" label="用户ID" width="90" />
          <el-table-column prop="content" label="留言" min-width="240" />
          <el-table-column prop="adminReply" label="客服回复" min-width="200">
            <template #default="{ row }">
              <el-input v-if="!row.adminReply" v-model="row._reply" placeholder="输入回复" size="small" />
              <span v-else>{{ row.adminReply }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button v-if="!row.adminReply" link type="primary" @click="reply(row)">回复</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { decorateGetApi, decorateSaveApi, articleListApi, articleSaveApi, kefuMessagesApi, kefuReplyApi } from '@/api/content'

const tab = ref('decorate')
const decorateJson = ref('')
const articles = ref<any[]>([])
const msgs = ref<any[]>([])
const articleForm = reactive<any>({})

function formatJson() {
  try { decorateJson.value = JSON.stringify(JSON.parse(decorateJson.value), null, 2) } catch (e) {}
}
async function loadDecorate() {
  const res: any = await decorateGetApi()
  try { decorateJson.value = JSON.stringify(JSON.parse(res.data), null, 2) } catch (e) { decorateJson.value = res.data }
}
async function saveDecorate() {
  try { JSON.parse(decorateJson.value) } catch (e) { return ElMessage.warning('JSON 格式错误') }
  await decorateSaveApi(decorateJson.value)
  ElMessage.success('已保存')
}
async function loadArticles() {
  const res: any = await articleListApi()
  articles.value = res.data
}
async function saveArticle() {
  if (!articleForm.title) return ElMessage.warning('请输入标题')
  await articleSaveApi({ ...articleForm, isShow: articleForm.isShow ?? 1 })
  ElMessage.success('已保存')
  articleForm.id = null
  articleForm.title = ''
  articleForm.content = ''
  loadArticles()
}
function editArticle(row: any) {
  Object.assign(articleForm, { ...row })
}
async function loadKefu() {
  const res: any = await kefuMessagesApi()
  msgs.value = res.data
}
async function reply(row: any) {
  if (!row._reply) return ElMessage.warning('请输入回复')
  await kefuReplyApi(row.id, row._reply)
  ElMessage.success('已回复')
  loadKefu()
}

onMounted(() => {
  loadDecorate()
  loadArticles()
  loadKefu()
})
</script>