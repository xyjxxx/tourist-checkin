const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

Page({
  data: { posterUrl: '', loading: false, checkinId: '' },
  onLoad(options) {
    if (!checkLogin()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    if (options.id) {
      this.setData({ checkinId: options.id })
      this.generatePoster(options.id)
      this.loadShareLink(options.id)
    }
  },
  generatePoster(id) {
    this.setData({ loading: true })
    request({ url: '/share/poster', method: 'POST', data: { checkInId: id } })
      .then((res) => { this.setData({ posterUrl: res.data.url || res.data }) })
      .catch(() => { wx.showToast({ title: '生成海报失败', icon: 'none' }) })
      .finally(() => { this.setData({ loading: false }) })
  },
  loadShareLink(id) {
    request({ url: '/share/link/' + id, method: 'GET' }).then(() => {}).catch((err) => { console.warn(err) })
  },
  onSavePoster() {
    if (!this.data.posterUrl) return
    const token = wx.getStorageSync('token')
    wx.downloadFile({
      url: this.data.posterUrl,
      header: token ? { 'Authorization': 'Bearer ' + token } : {},
      success: (res) => {
        if (res.statusCode !== 200) {
          wx.showToast({ title: '下载失败', icon: 'none' })
          return
        }
        wx.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => wx.showToast({ title: '已保存到相册', icon: 'success' }),
          fail: () => wx.showToast({ title: '保存失败', icon: 'none' })
        })
      }
    })
  },
  onShareAppMessage() {
    return {
      title: '来看看我的打卡！',
      path: '/pages/checkin-detail/checkin-detail?id=' + this.data.checkinId
    }
  }
})
