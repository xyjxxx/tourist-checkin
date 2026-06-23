const { request, fixImageUrls, fixImageUrl } = require('../../utils/api')
const { checkLogin, requireLogin } = require('../../utils/auth')
const { formatTime } = require('../../utils/util')

Page({
  data: {
    list: [],
    loading: true,
    sortTab: 'hot',
    timeTabs: ['全部', '今日', '本周', '本月'],
    activeTime: '本月',
    hotStats: { total: 0, today: 0, week: 0 },
    popup: {
      show: false, opening: false, closing: false, cardStyle: '',
      checkin: {}, markers: [], comments: [],
      liked: false, likeCount: 0, likeAnimating: false, commentCount: 0,
      commentText: '', replyTo: null, _index: -1, _id: null, _originStyle: ''
    }
  },

  onLoad() {
    this.loadHot()
  },

  onPullDownRefresh() {
    this.loadHot().finally(() => { wx.stopPullDownRefresh() })
  },

  onReachBottom() {
    // 可扩展分页加载
  },

  loadHot() {
    this.setData({ loading: true })
    return request({ url: '/checkin/hot', method: 'GET', data: { limit: 50 } })
      .then((res) => {
        const allList = (res.data || []).map((item, idx) => ({
          ...item,
          avatar: fixImageUrl(item.avatar),
          images: fixImageUrls(item.images),
          timeText: formatTime(item.checkInTime),
          rank: idx + 1
        }))
        this._allList = allList
        this.setData({
          'hotStats.total': allList.length,
          'hotStats.today': allList.filter(i => this._isToday(i.checkInTime)).length,
          'hotStats.week': allList.filter(i => this._isThisWeek(i.checkInTime)).length
        })
        this._applyTimeFilter()
      })
      .catch(() => {})
      .finally(() => { this.setData({ loading: false }) })
  },

  _applyTimeFilter() {
    const allList = this._allList || []
    const tab = this.data.activeTime
    let filtered = allList
    if (tab === '今日') {
      filtered = allList.filter(i => this._isToday(i.checkInTime))
    } else if (tab === '本周') {
      filtered = allList.filter(i => this._isThisWeek(i.checkInTime))
    } else if (tab === '本月') {
      filtered = allList.filter(i => this._isThisMonth(i.checkInTime))
    }
    this.setData({ list: filtered.slice(0, 20) })
  },

  _isToday(dateStr) {
    if (!dateStr) return false
    return dateStr.substring(0, 10) === new Date().toISOString().substring(0, 10)
  },

  _isThisWeek(dateStr) {
    if (!dateStr) return false
    const d = new Date(dateStr)
    const now = new Date()
    const weekAgo = new Date(now.getTime() - 7 * 24 * 3600 * 1000)
    return d >= weekAgo
  },

  _isThisMonth(dateStr) {
    if (!dateStr) return false
    const d = new Date(dateStr)
    const now = new Date()
    return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth()
  },

  onTapSort(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.sortTab) return
    this.setData({ sortTab: tab })
    // 排序切换（前端排序即可）
    const list = this.data.list.slice()
    if (tab === 'hot') {
      list.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
    } else {
      list.sort((a, b) => new Date(b.checkInTime) - new Date(a.checkInTime))
    }
    this.setData({ list: list })
  },

  onTapTime(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.activeTime) return
    this.setData({ activeTime: tab })
    this._applyTimeFilter()
  },

  // ====== 弹窗 ======

  noop() {},

  onOpenPopup(e) {
    const id = e.currentTarget.dataset.id
    const idx = e.currentTarget.dataset.index
    const item = this.data.list[idx]
    if (!item) return

    wx.createSelectorQuery().select('#card-' + idx).boundingClientRect((rect) => {
      if (!rect) return
      const originStyle = 'top:' + rect.top + 'px;left:' + rect.left + 'px;width:' + rect.width + 'px;height:' + rect.height + 'px;border-radius:24rpx;'
      const markers = (item.longitude && item.latitude) ? [{
        id: 1, longitude: item.longitude, latitude: item.latitude, width: 30, height: 30
      }] : []

      this.setData({
        'popup.show': true, 'popup.opening': true, 'popup.closing': false,
        'popup.cardStyle': originStyle, 'popup._originStyle': originStyle,
        'popup._index': idx, 'popup._id': id,
        'popup.checkin': item, 'popup.markers': markers,
        'popup.liked': !!item.hasLiked, 'popup.likeCount': item.likeCount || 0,
        'popup.commentCount': item.commentCount || 0,
        'popup.comments': [], 'popup.commentText': '', 'popup.replyTo': null
      })

      setTimeout(() => {
        this.setData({ 'popup.cardStyle': 'top:5vh;left:5vw;width:90vw;height:auto;border-radius:24rpx;' })
      }, 30)

      this.loadPopupComments(id)
    }).exec()
  },

  onClosePopup() {
    const origin = this.data.popup._originStyle
    this.setData({ 'popup.closing': true, 'popup.cardStyle': origin })
    setTimeout(() => {
      const idx = this.data.popup._index
      const list = this.data.list.slice()
      if (list[idx]) {
        list[idx] = { ...list[idx], hasLiked: this.data.popup.liked, likeCount: this.data.popup.likeCount }
        this.setData({ list: list, 'popup.show': false })
      } else {
        this.setData({ 'popup.show': false })
      }
    }, 280)
  },

  loadPopupComments(id) {
    request({ url: '/comment/page', method: 'GET', data: { checkInId: id, page: 1, size: 50 } })
      .then((res) => {
        const comments = (res.data || []).map((c) => ({ ...c, timeText: formatTime(c.createdAt) }))
        this.setData({ 'popup.comments': comments })
      })
      .catch(() => {})
  },

  onPopupLike() {
    if (!requireLogin()) return
    if (this._popupLiking) return
    this._popupLiking = true
    const liked = !this.data.popup.liked
    const count = liked ? this.data.popup.likeCount + 1 : Math.max(0, this.data.popup.likeCount - 1)
    this.setData({ 'popup.liked': liked, 'popup.likeCount': count, 'popup.likeAnimating': liked })
    if (liked) setTimeout(() => { this.setData({ 'popup.likeAnimating': false }) }, 400)
    request({ url: '/checkin/' + this.data.popup._id + '/like', method: 'POST' })
      .catch(() => {
        this.setData({ 'popup.liked': !liked, 'popup.likeCount': liked ? count - 1 : count + 1 })
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
      .finally(() => { this._popupLiking = false })
  },

  onPopupCommentInput(e) {
    this.setData({ 'popup.commentText': e.detail.value })
  },

  onPopupSendComment() {
    if (!requireLogin()) return
    const text = (this.data.popup.commentText || '').trim()
    if (!text) return
    const payload = { checkInId: this.data.popup._id, content: text }
    request({ url: '/comment', method: 'POST', data: payload })
      .then(() => {
        this.setData({ 'popup.commentText': '', 'popup.commentCount': this.data.popup.commentCount + 1 })
        this.loadPopupComments(this.data.popup._id)
      })
      .catch(() => { wx.showToast({ title: '评论失败', icon: 'none' }) })
  },

  onPreviewPopupImage(e) {
    wx.previewImage({ current: e.currentTarget.dataset.src, urls: e.currentTarget.dataset.list })
  }
})
