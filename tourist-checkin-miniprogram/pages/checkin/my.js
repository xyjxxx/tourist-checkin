const { request, fixImageUrls, fixImageUrl } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')
const { formatTime } = require('../../utils/util')

Page({
  data: {
    checkinList: [],
    page: 1,
    size: 10,
    loading: false,
    hasMore: true
  },

  onLoad() {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadMyCheckins(1)
  },

  onPullDownRefresh() {
    this.loadMyCheckins(1).finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.loading || !this.data.hasMore) return
    this.loadMyCheckins(this.data.page + 1)
  },

  loadMyCheckins(page) {
    this.setData({ loading: true })
    return request({
      url: '/checkin/my',
      method: 'GET',
      data: { page: page, size: this.data.size }
    })
      .then((res) => {
        const list = (res.data || []).map((item) => ({
          ...item,
          avatar: fixImageUrl(item.avatar),
          images: fixImageUrls(item.images),
          timeText: formatTime(item.checkInTime)
        }))
        this.setData({
          checkinList: page === 1 ? list : this.data.checkinList.concat(list),
          page: page,
          hasMore: list.length >= this.data.size
        })
      })
      .catch((err) => {
        console.error('Load my checkins failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  onLongPressCheckin(e) {
    const id = e.currentTarget.dataset.id
    wx.showActionSheet({
      itemList: ['删除此打卡'],
      success: (res) => {
        if (res.tapIndex === 0) {
          wx.showModal({
            title: '提示',
            content: '确定要删除这条打卡吗？',
            success: (modalRes) => {
              if (modalRes.confirm) {
                this.deleteCheckin(id)
              }
            }
          })
        }
      }
    })
  },

  deleteCheckin(id) {
    request({ url: '/checkin/' + id, method: 'DELETE' })
      .then(() => {
        wx.showToast({ title: '删除成功', icon: 'success' })
        const list = this.data.checkinList.filter((item) => item.id !== id)
        this.setData({ checkinList: list })
      })
      .catch((err) => {
        console.error('Delete checkin failed:', err)
        wx.showToast({ title: '删除失败', icon: 'none' })
      })
  },

  onTapCheckin(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/checkin-detail/checkin-detail?id=' + id })
  }
})
