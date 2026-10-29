<template>
  <el-tabs v-model="tab">
    <el-tab-pane label="首页装修" name="decorate">
      <div class="dec-layout">
        <!-- 左：编辑区 -->
        <div class="dec-editor">
          <el-card shadow="never">
            <template #header>
              <div class="card-head">
                <span>① 轮播图 <span class="dim">（{{ dec.banners.length }}/8 · 建议 1600×600，8:3）</span></span>
                <el-button size="small" type="primary" plain :disabled="dec.banners.length >= 8" @click="addBanner">
                  + 添加
                </el-button>
              </div>
            </template>
            <div v-for="(b, i) in dec.banners" :key="i" class="banner-row">
              <el-image :src="b.image" fit="cover" class="thumb" preview-teleported :preview-src-list="[b.image]" />
              <div class="row-main">
                <el-input v-model="b.image" size="small" placeholder="图片地址，如 /file/demo/banner1.jpg" />
                <el-input v-model="b.link" size="small" placeholder="跳转路径（可空），如 /pages/seckill/list" />
              </div>
              <div class="row-ops">
                <el-button link :disabled="i === 0" @click="move(dec.banners, i, -1)">上移</el-button>
                <el-button link :disabled="i === dec.banners.length - 1" @click="move(dec.banners, i, 1)">下移</el-button>
                <el-button link type="danger" :disabled="dec.banners.length <= 1" @click="dec.banners.splice(i, 1)">删</el-button>
              </div>
            </div>
            <el-empty v-if="!dec.banners.length" description="至少一张轮播图" :image-size="60" />
          </el-card>

          <el-card shadow="never" style="margin-top: 14px">
            <template #header>
              <div class="card-head">
                <span>② 宫格导航 <span class="dim">（{{ dec.navs.length }}/10）</span></span>
                <el-button size="small" type="primary" plain :disabled="dec.navs.length >= 10" @click="addNav">
                  + 添加
                </el-button>
              </div>
            </template>
            <div v-for="(n, i) in dec.navs" :key="i" class="nav-row">
              <el-input v-model="n.icon" size="small" class="icon-input" placeholder="图标" />
              <el-input v-model="n.name" size="small" placeholder="名称" style="width: 130px" />
              <el-input v-model="n.link" size="small" placeholder="跳转路径，如 /pages/coupon/center" />
              <el-button link :disabled="i === 0" @click="move(dec.navs, i, -1)">←</el-button>
              <el-button link :disabled="i === dec.navs.length - 1" @click="move(dec.navs, i, 1)">→</el-button>
              <el-button link type="danger" @click="dec.navs.splice(i, 1)">删</el-button>
            </div>
          </el-card>

          <el-card shadow="never" style="margin-top: 14px">
            <template #header><span>③ 商品区标题</span></template>
            <el-input v-model="dec.goodsTitle" placeholder="如：精选好物" />
          </el-card>

          <div class="save-bar">
            <el-button type="primary" :loading="saving" @click="saveDecor">保存并生效</el-button>
            <el-button @click="loadDecor">重置读取线上</el-button>
            <el-button @click="toggleJson">{{ showJson ? '收起' : '查看' }} JSON</el-button>
            <span class="dim">保存后 C 端首页刷新即生效</span>
          </div>

          <el-input
            v-if="showJson"
            v-model="jsonPreview"
            type="textarea"
            :rows="10"
            readonly
            style="margin-top: 10px; font-family: monospace"
          />
        </div>

        <!-- 右：实时预览 -->
        <div class="dec-preview">
          <div class="phone">
            <div class="phone-bar">Minimall 商城</div>
            <div class="phone-body">
              <div class="pv-banner">
                <template v-if="dec.banners.length">
                  <img :src="dec.banners[pvIndex % dec.banners.length].image" class="pv-banner-img" />
                  <div v-if="dec.banners.length > 1" class="pv-dots">
                    <i v-for="(_, i) in dec.banners" :key="i" :class="{ on: i === pvIndex % dec.banners.length }" />
                  </div>
                </template>
                <div v-else class="pv-empty">轮播位（建议 1600×600）</div>
              </div>

              <div class="pv-navs" :style="{ gridTemplateColumns: `repeat(${Math.min(dec.navs.length || 5, 5)}, 1fr)` }">
                <div v-for="(n, i) in dec.navs" :key="i" class="pv-nav">
                  <div class="pv-nav-icon">{{ n.icon || n.name?.slice(0, 1) }}</div>
                  <div class="pv-nav-name">{{ n.name }}</div>
                </div>
              </div>

              <div class="pv-section">{{ dec.goodsTitle || '推荐好物' }}</div>
              <div class="pv-goods">
                <div v-for="g in previewGoods" :key="g.id" class="pv-good">
                  <img :src="g.image" class="pv-good-img" />
                  <div class="pv-good-name">{{ g.name }}</div>
                  <div class="pv-good-price">￥{{ ((g.price || 0) / 100).toFixed(2) }}</div>
                </div>
                <template v-if="!previewGoods.length">
                  <div v-for="i in 4" :key="i" class="pv-good">
                    <div class="pv-good-img"></div>
                    <div class="pv-good-line"></div>
                    <div class="pv-good-line short"></div>
                  </div>
                </template>
              </div>
            </div>
          </div>
          <div class="preview-tip">↑ 手机预览（真实图片、实时同步）</div>
        </div>
      </div>
    </el-tab-pane>

    <el-tab-pane label="文章管理" name="article">
      <el-card>
        <el-form inline>
          <el-form-item label="标题"><el-input v-model="articleForm.title" style="width: 220px" /></el-form-item>
          <el-form-item label="内容">
            <el-input v-model="articleForm.content" type="textarea" style="width: 400px" />
          </el-form-item>
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
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { goodsPageApi } from '@/api/goods'
import {
  decorateGetApi,
  decorateSaveApi,
  articleListApi,
  articleSaveApi,
  kefuMessagesApi,
  kefuReplyApi
} from '@/api/content'

const tab = ref('decorate')
const saving = ref(false)
const showJson = ref(false)
const pvIndex = ref(0)
let pvTimer = 0
const dec = reactive<any>({ banners: [], navs: [], goodsTitle: '' })
const previewGoods = ref<any[]>([])
const articles = ref<any[]>([])
const msgs = ref<any[]>([])
const articleForm = reactive<any>({})

const jsonPreview = computed(() => JSON.stringify(dec, null, 2))

function applyDecor(raw: any) {
  let data: any = {}
  try {
    data = typeof raw === 'string' ? JSON.parse(raw || '{}') : raw || {}
  } catch {
    data = {}
  }
  dec.banners = (data.banners || []).map((b: any) => ({ image: b.image || '', link: b.link || '' }))
  dec.navs = (data.navs || []).map((n: any) => ({ name: n.name || '', icon: n.icon || '', link: n.link || '' }))
  dec.goodsTitle = data.goodsTitle || ''
}

async function loadPreviewGoods() {
  try {
    const res: any = await goodsPageApi({ page: 1, size: 4 })
    previewGoods.value = (res.data?.records || res.data?.list || []).slice(0, 4).map((g: any) => ({
      id: g.id,
      name: g.name,
      price: g.price,
      image: g.image || g.cover || '/file/demo/g1_shoes.jpg'
    }))
  } catch {
    previewGoods.value = []
  }
}

async function loadDecor() {
  const res: any = await decorateGetApi()
  applyDecor(res.data)
}
async function saveDecor() {
  const banners = dec.banners.filter((b: any) => b.image?.trim())
  if (!banners.length) return ElMessage.warning('至少需要一张轮播图')
  const badNav = dec.navs.find((n: any) => !n.name?.trim())
  if (badNav) return ElMessage.warning('宫格导航名称不能为空')
  saving.value = true
  try {
    await decorateSaveApi(
      JSON.stringify({
        banners: banners.map((b: any) => ({ image: b.image.trim(), link: (b.link || '').trim() })),
        navs: dec.navs
          .filter((n: any) => n.name?.trim())
          .map((n: any) => ({ name: n.name.trim(), icon: n.icon || '', link: (n.link || '').trim() })),
        goodsTitle: dec.goodsTitle || ''
      })
    )
    ElMessage.success('已保存，C 端刷新即生效')
    loadDecor()
  } finally {
    saving.value = false
  }
}

function addBanner() {
  dec.banners.push({ image: '', link: '' })
}
function addNav() {
  dec.navs.push({ name: '', icon: '', link: '' })
}
function move(arr: any[], i: number, delta: number) {
  const j = i + delta
  if (j < 0 || j >= arr.length) return
  const [x] = arr.splice(i, 1)
  arr.splice(j, 0, x)
}
function toggleJson() {
  showJson.value = !showJson.value
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
  pvTimer = window.setInterval(() => { pvIndex.value++ }, 3000)
  loadDecor()
  loadPreviewGoods()
  loadArticles()
  loadKefu()
})
onUnmounted(() => {
  if (pvTimer) window.clearInterval(pvTimer)
})
</script>

<style scoped>
.dec-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 16px;
  align-items: start;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dim {
  color: #909399;
  font-size: 12px;
}
.banner-row,
.nav-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
.row-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.row-ops {
  display: flex;
  gap: 2px;
}
.thumb {
  width: 120px;
  height: 45px;
  border-radius: 4px;
  background: #f5f7fa;
  flex: 0 0 auto;
}
.icon-input {
  width: 64px;
  flex: 0 0 auto;
}
.save-bar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 14px;
}
.dec-preview {
  position: sticky;
  top: 12px;
}
.phone {
  width: 320px;
  border: 8px solid #1f2d3d;
  border-radius: 26px;
  overflow: hidden;
  background: #f6f7f9;
  margin: 0 auto;
}
.phone-bar {
  height: 34px;
  line-height: 34px;
  text-align: center;
  background: #fff;
  font-size: 13px;
  font-weight: 600;
  border-bottom: 1px solid #eef0f4;
}
.phone-body {
  height: 520px;
  overflow-y: auto;
  padding-bottom: 10px;
}
.pv-banner {
  position: relative;
  background: #e9edf5;
  line-height: 0;
}
.pv-banner-img {
  width: 100%;
  display: block;
}
.pv-dots {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 6px;
  display: flex;
  justify-content: center;
  gap: 4px;
  line-height: 1;
}
.pv-dots i {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
}
.pv-dots i.on {
  background: #fff;
  width: 12px;
  border-radius: 3px;
}
.pv-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9aa4b8;
  font-size: 12px;
}
.pv-navs {
  display: grid;
  gap: 6px;
  background: #fff;
  padding: 10px 6px;
  margin-bottom: 8px;
}
.pv-nav {
  text-align: center;
}
.pv-nav-icon {
  width: 34px;
  height: 34px;
  line-height: 34px;
  margin: 0 auto 4px;
  border-radius: 50%;
  background: #eef3ff;
  font-size: 17px;
}
.pv-nav-name {
  font-size: 10px;
  color: #4b5563;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.pv-section {
  background: #fff;
  padding: 10px 12px;
  font-size: 14px;
  font-weight: 600;
}
.pv-goods {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 0 8px;
}
.pv-good {
  background: #fff;
  border-radius: 8px;
  padding: 6px;
}
.pv-good-img {
  width: 100%;
  height: 84px;
  object-fit: cover;
  border-radius: 6px;
  background: linear-gradient(135deg, #e8eef9, #dfe7f5);
  display: block;
}
.pv-good-name {
  font-size: 10px;
  color: #374151;
  margin-top: 5px;
  line-height: 1.3;
  max-height: 26px;
  overflow: hidden;
}
.pv-good-price {
  font-size: 11px;
  color: #ef4444;
  font-weight: 600;
  margin-top: 2px;
}
.pv-good-line {
  height: 8px;
  margin-top: 6px;
  border-radius: 4px;
  background: #eef1f6;
}
.pv-good-line.short {
  width: 55%;
}
.preview-tip {
  text-align: center;
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
</style>
