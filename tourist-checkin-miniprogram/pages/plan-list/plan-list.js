const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

const app = getApp()

Page({
  data: {
    activeTab: 'my',
    planList: [],
    loading: false
  },

  onLoad() {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadPlans()
  },

  onShow() {
    // Reload only on first visit (empty list) or when returning from plan-create
    if (this._needReload || this.data.planList.length === 0) {
      this._needReload = false
      this.loadPlans()
    }
  },

  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.activeTab) return
    this.setData({ activeTab: tab, planList: [] })
    this.loadPlans()
  },

  loadPlans() {
    const tab = this.data.activeTab
    const url = tab === 'my' ? '/trip-plan/my' : '/trip-plan/public'

    this.setData({ loading: true })
    request({ url: url, method: 'GET' })
      .then((res) => {
        this.setData({ planList: res.data || [] })
      })
      .catch((err) => {
        console.error('Load plans failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  onTapPlan(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/plan-detail/plan-detail?id=' + id })
  },

  onCreatePlan() {
    this._needReload = true
    wx.navigateTo({ url: '/pages/plan-create/plan-create' })
  }
})
