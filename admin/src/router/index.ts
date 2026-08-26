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
          path: 'order',
          name: 'order',
          component: () => import('@/views/order/list.vue'),
          meta: { title: '订单管理' }
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
