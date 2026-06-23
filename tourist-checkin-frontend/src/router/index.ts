import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
  { path: '/', redirect: '/admin/dashboard' },
  {
    path: '/admin',
    component: () => import('@/components/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/admin/DashboardView.vue') },
      { path: 'users', name: 'UserManage', component: () => import('@/views/admin/UserManageView.vue') },
      { path: 'checkins', name: 'CheckInManage', component: () => import('@/views/admin/CheckInManageView.vue') },
      { path: 'travel-notes', name: 'TravelNoteManage', component: () => import('@/views/admin/TravelNoteManageView.vue') },
      { path: 'trip-plans', name: 'TripPlanManage', component: () => import('@/views/admin/TripPlanManageView.vue') },
      { path: 'comments', name: 'CommentManage', component: () => import('@/views/admin/CommentManageView.vue') },
      { path: 'topics', name: 'TopicManage', component: () => import('@/views/admin/TopicManageView.vue') },
      { path: 'achievements', name: 'AchievementManage', component: () => import('@/views/admin/AchievementManageView.vue') },
      { path: 'merchants', name: 'MerchantManage', component: () => import('@/views/admin/MerchantManageView.vue') },
      { path: 'shop-recommends', name: 'ShopRecommendManage', component: () => import('@/views/admin/ShopRecommendManageView.vue') },
      { path: 'locations', name: 'LocationManage', component: () => import('@/views/admin/LocationManageView.vue') },
      { path: 'reports', name: 'ReportManage', component: () => import('@/views/admin/ReportManageView.vue') },
      { path: 'image-audits', name: 'ImageAuditManage', component: () => import('@/views/admin/ImageAuditManageView.vue') },
      { path: 'sensitive-words', name: 'SensitiveWordManage', component: () => import('@/views/admin/SensitiveWordManageView.vue') },
      { path: 'notifications', name: 'NotificationManage', component: () => import('@/views/admin/NotificationManageView.vue') },
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFoundView.vue'), meta: { public: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/admin/dashboard')
  } else if (to.meta.requiresAdmin) {
    if (!userStore.userInfo && userStore.token) {
      try {
        await userStore.fetchUserInfo()
      } catch {
        userStore.clearToken()
        next('/login')
        return
      }
    }
    if (!userStore.isAdmin) {
      next('/404')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
