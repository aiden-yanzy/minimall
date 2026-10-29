<template>
  <el-container class="layout">
    <el-aside width="210px" class="aside">
      <div class="logo">Minimall 商城</div>
      <el-menu :default-active="route.path" router background-color="#001529" text-color="#fff" active-text-color="#409eff">
        <el-menu-item v-for="m in visibleMenus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ route.meta.title }}</span>
        <el-dropdown @command="onCommand">
          <span class="user">{{ auth.admin?.name || '管理员' }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataBoard, Goods, Tickets, Present, TrendCharts } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { computed } from 'vue'

/** 菜单 → 权限码（与后台 menu 表一致） */
const MENUS = [
  { path: '/dashboard', title: '工作台', icon: DataBoard, perm: '' },
  { path: '/goods', title: '商品管理', icon: Goods, perm: 'goods:list' },
  { path: '/users', title: '用户管理', icon: Goods, perm: 'user:manage' },
  { path: '/level', title: '会员等级', icon: Goods, perm: 'user:level' },
  { path: '/base', title: '品牌/单位', icon: Goods, perm: 'base:manage' },
  { path: '/finance', title: '财务明细', icon: Present, perm: 'finance:log' },
  { path: '/roles', title: '角色权限', icon: Goods, perm: 'system:role' },
  { path: '/settings', title: '设置中心', icon: Goods, perm: 'system:setting' },
  { path: '/extras', title: '抽奖/注册送/免邮', icon: Present, perm: 'lottery:manage' },
  { path: '/freight', title: '运费模板', icon: Goods, perm: 'freight:manage' },
  { path: '/comments', title: '评价管理', icon: Goods, perm: 'goods:comment' },
  { path: '/order', title: '订单管理', icon: Tickets, perm: 'order:list' },
  { path: '/stat', title: '销售统计', icon: TrendCharts, perm: 'order:list' },
  { path: '/after', title: '售后管理', icon: Tickets, perm: 'after:manage' },
  { path: '/marketing', title: '营销管理', icon: Present, perm: 'marketing:manage' },
  { path: '/integral', title: '积分商城', icon: Present, perm: 'integral:manage' },
  { path: '/seckill', title: '秒杀管理', icon: Present, perm: 'seckill:manage' },
  { path: '/wallet', title: '提现审核', icon: Present, perm: 'finance:withdraw' },
  { path: '/content', title: '内容装修', icon: Present, perm: 'content:manage' }
]
const visibleMenus = computed(() => MENUS.filter((m) => auth.can(m.perm)))

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

onMounted(() => {
  if (!auth.admin) auth.fetchMe()
})

function onCommand(cmd: string) {
  if (cmd === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.layout {
  height: 100vh;
}
.aside {
  background: #001529;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.main {
  background: #f0f2f5;
}
.user {
  cursor: pointer;
  color: #333;
}
</style>
