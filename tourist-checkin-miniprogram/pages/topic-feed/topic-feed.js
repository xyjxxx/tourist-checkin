const { request, fixImageUrls, fixImageUrl } = require('../../utils/api')
const { formatTime } = require('../../utils/util')

Page({
  data: {
    topicName: '',
    checkinList: [],
    page: 1,
    size: 10,
    loading: false,
    hasMore: true
  },

  onLoad(options) {
    let topicName = ''
    try {
      topicName = options.name ? decodeURIComponent(options.name) : ''
    } catch (e) {
      topicName = options.name || ''
    }
    this.setData({ topicName: topicName })
    wx.setNavigationBarTitle({ title: '#' + topicName })
    this.loadCheckins(1)
  },

  onReachBottom() {
    if (this.data.loading || !this.data.hasMore) return
    this.loadCheckins(this.data.page + 1)
  },

  loadCheckins(page) {
    this.setData({ loading: true })
    request({
      url: '/checkin/recent',
      method: 'GET',
      data: {
        page: page,
        size: this.data.size,
        topic: this.data.topicName
      }
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
        console.error('Load topic checkins failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})
