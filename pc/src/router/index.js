import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/', component: () => import('@/layout/Main.vue'), children: [
    { path: '', component: () => import('@/views/Home.vue') },
    { path: 'products', component: () => import('@/views/ProductList.vue') },
    { path: 'product/:id', component: () => import('@/views/ProductDetail.vue') },
    { path: 'cart', component: () => import('@/views/Cart.vue'), meta: { auth: true } },
    { path: 'checkout', component: () => import('@/views/Checkout.vue'), meta: { auth: true } },
    { path: 'orders', component: () => import('@/views/Orders.vue'), meta: { auth: true } },
    { path: 'order/:id', component: () => import('@/views/OrderDetail.vue'), meta: { auth: true } }
  ]}
]
const router = createRouter({ history: createWebHistory('/pc/'), routes })
router.beforeEach((to) => {
  const auth = useUserStore()
  if (to.meta.auth && !auth.token) return { path: '/login', query: { redirect: to.fullPath } }
})
export default router
