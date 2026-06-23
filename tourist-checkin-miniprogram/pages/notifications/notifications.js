const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')
const { formatTime } = require('../../utils/util')

Page({
  data: {
    notifications: [],
    page: 1,
    size: 20,
    loading: false,
    hasMore: true
  },

  onLoad() {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadNotifications(1)
  },

  onPullDownRefresh() {
    this.loadNotifications(1).finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.loading || !this.data.hasMore) return
    this.loadNotifications(this.data.page + 1)
  },

  loadNotifications(page) {
    this.setData({ loading: true })
    return request({
      url: '/notification/page',
      method: 'GET',
      data: { page: page, size: this.data.size }
    })
      .then((res) => {
        const list = (res.data || []).map((item) => ({
          ...item,
          timeText: formatTime(item.createTime)
        }))
        this.setData({
          notifications: page === 1 ? list : this.data.notifications.concat(list),
          page: page,
          hasMore: list.length >= this.data.size
        })
      })
      .catch((err) => {
        console.error('Load notifications failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  onReadAll() {
    wx.showModal({
      title: '提示',
      content: '确定将所有消息标为已读吗？',
      success: (res) => {
        if (res.confirm) {
          request({ url: '/notification/read-all', method: 'PUT' })
            .then(() => {
              const list = this.data.notifications.map((item) => ({
                ...item,
                read: true
              }))
              this.setData({ notifications: list })
              wx.showToast({ title: '已全部标为已读', icon: 'success' })
            })
            .catch((err) => {
              console.error('Read all failed:', err)
              wx.showToast({ title: '操作失败', icon: 'none' })
            })
        }
      }
    })
  },

  onTapNotification(e) {
    const id = e.currentTarget.dataset.id
    const index = e.currentTarget.dataset.index
    const item = this.data.notifications[index]

    request({
      url: '/notification/read/' + id,
      method: 'PUT'
    }).then(() => {
      const key = 'notifications[' + index + '].read'
      this.setData({ [key]: true })
    }).catch((err) => { console.warn(err) })

    // Navigate based on notification type
    if ((item.targetType === 'TRAVEL_NOTE' || item.targetType === 'CHECK_IN') && !item.targetId) {
      wx.showToast({ title: '该内容已删除', icon: 'none' })
      return
    }
    if (item.targetType === 'TRAVEL_NOTE' && item.targetId) {
      wx.navigateTo({ url: '/pages/note-detail/note-detail?id=' + item.targetId })
    } else if (item.targetType === 'CHECK_IN' && item.targetId) {
      wx.navigateTo({ url: '/pages/checkin-detail/checkin-detail?id=' + item.targetId })
    } else if (item.type === 'FOLLOW' && item.fromUserId) {
      wx.navigateTo({ url: '/pages/user-profile/user-profile?userId=' + item.fromUserId })
    } else if (item.type === 'ACHIEVEMENT') {
      wx.navigateTo({ url: '/pages/achievements/achievements' })
    }
  }
})
