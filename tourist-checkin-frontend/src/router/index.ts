import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
  { path: '/', name: 'Home', component: () => import('@/views/HomeView.vue'), meta: { requiresAuth: true } },
  { path: '/explore', name: 'Explore', component: () => import('@/views/ExploreView.vue'), meta: { requiresAuth: true } },
  { path: '/profile', name: 'Profile', component: () => import('@/views/ProfileView.vue'), meta: { requiresAuth: true } },
  { path: '/profile/:userId', name: 'UserProfile', component: () => import('@/views/ProfileView.vue'), meta: { requiresAuth: true } },

  // 通知
  { path: '/notifications', name: 'Notifications', component: () => import('@/views/NotificationListView.vue'), meta: { requiresAuth: true } },

  // 关注
  { path: '/follows/:userId', name: 'FollowList', component: () => import('@/views/FollowListView.vue'), meta: { requiresAuth: true } },

  // 话题
  { path: '/topic/:name', name: 'TopicFeed', component: () => import('@/views/TopicFeedView.vue'), meta: { requiresAuth: true } },

  // 成就
  { path: '/achievements', name: 'Achievements', component: () => import('@/views/AchievementsView.vue'), meta: { requiresAuth: true } },

  // 积分
  { path: '/points', name: 'PointsHistory', component: () => import('@/views/PointsHistoryView.vue'), meta: { requiresAuth: true } },

  // 游记
  { path: '/travel-notes', name: 'TravelNoteList', component: () => import('@/views/TravelNoteListView.vue'), meta: { requiresAuth: true } },
  { path: '/travel-notes/create', name: 'TravelNoteCreate', component: () => import('@/views/TravelNoteCreateView.vue'), meta: { requiresAuth: true } },
  { path: '/travel-notes/:id', name: 'TravelNoteDetail', component: () => import('@/views/TravelNoteDetailView.vue'), meta: { requiresAuth: true } },

  // 行程规划
  { path: '/trip-plans', name: 'TripPlanList', component: () => import('@/views/TripPlanListView.vue'), meta: { requiresAuth: true } },
  { path: '/trip-plans/create', name: 'TripPlanCreate', component: () => import('@/views/TripPlanCreateView.vue'), meta: { requiresAuth: true } },
  { path: '/trip-plans/:id', name: 'TripPlanDetail', component: () => import('@/views/TripPlanDetailView.vue'), meta: { requiresAuth: true } },

  // 统计
  { path: '/stats', name: 'Statistics', component: () => import('@/views/StatisticsDashboard.vue'), meta: { requiresAuth: true } },

  // 管理员
  { path: '/admin', name: 'Admin', component: () => import('@/views/AdminView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },

  // 404 兜底路由
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFoundView.vue'), meta: { public: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/')
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/')
  } else {
    next()
  }
})

export default router
