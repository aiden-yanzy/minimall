import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '工作台' }
        },
        {
          path: 'goods',
          name: 'goods',
          component: () => import('@/views/goods/list.vue'),
          meta: { title: '商品管理' }
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/user/list.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'level',
          name: 'level',
          component: () => import('@/views/user/level.vue'),
          meta: { title: '会员等级' }
        },
        {
          path: 'base',
          name: 'base',
          component: () => import('@/views/goods/base.vue'),
          meta: { title: '品牌/单位' }
        },
        {
          path: 'finance',
          name: 'finance',
          component: () => import('@/views/finance/index.vue'),
          meta: { title: '财务明细' }
        },
        {
          path: 'roles',
          name: 'roles',
          component: () => import('@/views/system/roles.vue'),
          meta: { title: '角色权限' }
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/system/settings.vue'),
          meta: { title: '设置中心' }
        },
        {
          path: 'extras',
          name: 'extras',
          component: () => import('@/views/marketing/extras.vue'),
          meta: { title: '抽奖/注册送/免邮' }
        },
        {
          path: 'freight',
          name: 'freight',
          component: () => import('@/views/setting/freight.vue'),
          meta: { title: '运费模板' }
        },
        {
          path: 'comments',
          name: 'comments',
          component: () => import('@/views/goods/comments.vue'),
          meta: { title: '评价管理' }
        },
        {
          path: 'order',
          name: 'order',
          component: () => import('@/views/order/list.vue'),
          meta: { title: '订单管理' }
        },
        {
          path: 'stat',
          name: 'stat',
          component: () => import('@/views/stat/report.vue'),
          meta: { title: '销售统计' }
        },
        {
          path: 'after',
          name: 'after',
          component: () => import('@/views/order/after.vue'),
          meta: { title: '售后管理' }
        },
        {
          path: 'marketing',
          name: 'marketing',
          component: () => import('@/views/marketing/index.vue'),
          meta: { title: '营销管理' }
        },
        {
          path: 'integral',
          name: 'integral',
          component: () => import('@/views/integral/index.vue'),
          meta: { title: '积分商城' }
        },
        {
          path: 'seckill',
          name: 'seckill',
          component: () => import('@/views/seckill/index.vue'),
          meta: { title: '秒杀管理' }
        },
        {
          path: 'wallet',
          name: 'wallet',
          component: () => import('@/views/wallet/index.vue'),
          meta: { title: '提现审核' }
        },
        {
          path: 'content',
          name: 'content',
          component: () => import('@/views/content/index.vue'),
          meta: { title: '内容装修' }
        }
      ]
    }
  ]
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.name !== 'login' && !auth.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && auth.token) {
    return { name: 'dashboard' }
  }
})

export default router
