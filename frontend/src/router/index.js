import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginRegister.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/home/Home.vue') },
      { path: 'book/:id', name: 'BookDetail', component: () => import('@/views/book/BookDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('@/views/cart/Cart.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/profile/Profile.vue') },
      { path: 'orders', name: 'MyOrders', component: () => import('@/views/order/MyOrders.vue') },
      { path: 'seller/orders', name: 'SellerOrders', component: () => import('@/views/order/SellerOrders.vue') },
      { path: 'books/publish', name: 'PublishBook', component: () => import('@/views/book/PublishBook.vue') },
      { path: 'books/my', name: 'MyBooks', component: () => import('@/views/book/MyBooks.vue') },
      { path: 'wallet', name: 'Wallet', component: () => import('@/views/wallet/Wallet.vue') },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: 'books', name: 'AdminBooks', component: () => import('@/views/admin/AdminBooks.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUsers.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/AdminOrders.vue') },
      { path: 'funds', name: 'AdminFunds', component: () => import('@/views/admin/AdminFunds.vue') },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.path === '/login') {
    if (authStore.isLoggedIn) return '/home'
    return true
  }

  if (to.matched.some(r => r.meta.requiresAuth !== false)) {
    if (!authStore.isLoggedIn) {
      try {
        await authStore.fetchUserInfo()
      } catch {
        return '/login'
      }
    }
  }

  if (to.matched.some(r => r.meta.requiresAdmin)) {
    if (!authStore.isAdmin) return '/home'
  }

  return true
})

export default router
