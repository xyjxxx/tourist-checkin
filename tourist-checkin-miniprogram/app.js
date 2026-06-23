App({
  globalData: {
    userInfo: null,
    baseUrl: 'https://你的域名',
    isConnected: true
  },

  onLaunch() {
    // Network status monitor
    wx.onNetworkStatusChange((res) => {
      const wasConnected = this.globalData.isConnected
      this.globalData.isConnected = res.isConnected
      if (!res.isConnected) {
        wx.showToast({ title: '网络已断开', icon: 'none', duration: 2000 })
      } else if (!wasConnected) {
        wx.showToast({ title: '网络已恢复', icon: 'success', duration: 1500 })
      }
    })

    const token = wx.getStorageSync('token')
    if (token) {
      this.fetchUserInfo()
    }
  },

  fetchUserInfo() {
    const api = require('./utils/api')
    api.request({ url: '/user/info', silent401: true }).then(res => {
      this.globalData.userInfo = res.data
    }).catch(() => {
      // token 可能已过期，静默处理，不强制退出
    })
  }
})
