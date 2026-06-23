const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')
const { formatTime } = require('../../utils/util')

Page({
  data: {
    totalPoints: 0,
    currentPoints: 0,
    level: 1,
    records: [],
    loading: false
  },

  onLoad() {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadPoints()
  },

  loadPoints() {
    this.setData({ loading: true })
    const overviewReq = request({ url: '/point/my', method: 'GET' }).then((res) => {
      const d = res.data || {}
      this.setData({
        totalPoints: d.totalPoints || 0,
        currentPoints: d.currentPoints || 0,
        level: d.level || 0
      })
    })
    const recordsReq = request({ url: '/point/records', method: 'GET' }).then((res) => {
      const records = (res.data || []).map((item) => ({
        ...item,
        timeText: formatTime(item.createdAt)
      }))
      this.setData({ records: records })
    })
    Promise.all([overviewReq, recordsReq])
      .catch((err) => {
        console.error('Load points failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})
