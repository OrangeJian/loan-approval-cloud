import { createRouter, createWebHistory } from 'vue-router'
import store from '../utils/store'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/login/index.vue') },
  {
    path: '/', name: 'Layout', redirect: '/dashboard',
    component: () => import('../views/layout/index.vue'),
    children: [
      { path: '/dashboard', name: 'Dashboard', component: () => import('../views/dashboard/index.vue') },
      { path: '/customer', name: 'Customer', component: () => import('../views/customer/list.vue') },
      { path: '/product', name: 'Product', component: () => import('../views/product/list.vue') },
      { path: '/application', name: 'Application', component: () => import('../views/application/list.vue') },
      { path: '/approval', name: 'Approval', component: () => import('../views/approval/list.vue') },
      { path: '/contract', name: 'Contract', component: () => import('../views/contract/list.vue') },
      { path: '/account', name: 'Account', component: () => import('../views/account/list.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = store.get('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
