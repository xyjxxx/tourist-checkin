const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

const app = getApp()

Page({
  data: { noteList: [], loading: false },
  onLoad() {
    if (!checkLogin()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    if (app.globalData.userInfo) {
      this.loadCollected()
    } else {
      request({ url: '/user/info', method: 'GET' }).then((res) => {
        app.globalData.userInfo = res.data
        this.loadCollected()
      }).catch(() => {
        wx.showToast({ title: '加载失败，请下拉刷新', icon: 'none' })
      })
    }
  },
  loadCollected() {
    this.setData({ loading: true })
    request({ url: '/travel-note/collected', method: 'GET', data: { page: 1, size: 50 } })
      .then((res) => { this.setData({ noteList: res.data || [] }) })
      .catch((err) => { console.warn(err) })
      .finally(() => { this.setData({ loading: false }) })
  },
  onTapNote(e) { wx.navigateTo({ url: '/pages/note-detail/note-detail?id=' + e.currentTarget.dataset.id }) }
})
