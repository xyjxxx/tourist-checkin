const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')
const { animateCounter } = require('../../utils/animations')

Page({
  data: {
    report: null,
    animatedReport: {},
    loading: false
  },

  onLoad() {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadReport()
  },

  onUnload() {
    if (this._animCleanups) {
      this._animCleanups.forEach(function (fn) { if (fn) fn() })
      this._animCleanups = null
    }
  },

  loadReport() {
    this.setData({ loading: true })
    request({ url: '/statistics/report', method: 'GET' })
      .then((res) => {
        const data = res.data
        this.setData({
          report: data,
          animatedReport: {}
        })
        this._animCleanups = [
          animateCounter(this, 'animatedReport.totalCheckIns', data.totalCheckIns || 0, 1000),
          animateCounter(this, 'animatedReport.totalCities', data.totalCities || 0, 1000),
          animateCounter(this, 'animatedReport.totalLikes', data.totalLikes || 0, 1000),
          animateCounter(this, 'animatedReport.longestStreak', data.longestStreak || 0, 1000)
        ].filter(Boolean)
      })
      .catch((err) => {
        console.error('Load report failed:', err)
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})
