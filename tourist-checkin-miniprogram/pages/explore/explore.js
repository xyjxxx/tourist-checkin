const { request, fixImageUrls, fixImageUrl } = require('../../utils/api')
const { checkLogin, requireLogin } = require('../../utils/auth')
const { formatTime } = require('../../utils/util')

const app = getApp()

Page({
  data: {
    checkinList: [],
    page: 1,
    size: 10,
    loading: false,
    refreshing: false,
    hasMore: true,
    currentUserId: null,
    isLoggedIn: false,
    // Hot notes mode
    showNotes: false,
    noteList: [],
    sortTab: 'hot',
    timeTabs: ['全部', '今日', '本周', '本月'],
    activeTime: '本月',
    noteStats: { total: 0, today: 0, week: 0 },
    popup: {
      show: false, opening: false, closing: false, cardStyle: '',
      note: {}, comments: [],
      liked: false, likeCount: 0, likeAnimating: false, commentCount: 0,
      commentText: '', _index: -1, _id: null, _originStyle: ''
    }
  },

  onLoad(options) {
    if (options && options.showNotes) {
      this.setData({ showNotes: true })
      this.loadNotes()
    } else {
      const loggedIn = checkLogin()
      this.setData({ isLoggedIn: loggedIn })
      if (loggedIn) {
        this.loadCurrentUser()
      }
    }
  },

  onShow() {
    if (this.getTabBar()) this.getTabBar().setSelected(1)
    if (!this.data.showNotes) {
      const loggedIn = checkLogin()
      if (loggedIn !== this.data.isLoggedIn) {
        this.setData({ isLoggedIn: loggedIn })
        if (loggedIn) {
          this.loadCurrentUser()
        }
      }
      if (loggedIn) {
        this.loadFollowing(1)
      } else {
        this.setData({ loading: false, checkinList: [] })
      }
    }
  },

  onUnload() {
    if (this._likeAnimTimer) clearTimeout(this._likeAnimTimer)
  },

  onPullDownRefresh() {
    if (this.data.showNotes) {
      this.loadNotes().finally(() => { wx.stopPullDownRefresh() })
    } else {
      this.setData({ refreshing: true })
      this.loadFollowing(1).finally(() => {
        wx.stopPullDownRefresh()
        this.setData({ refreshing: false })
      })
    }
  },

  onReachBottom() {
    if (this.data.showNotes) {
      if (!this._noteLoadMoreUsed && this._allList && this._allList.length > 20) {
        this._noteLoadMoreUsed = true
        this.setData({ noteList: this._allList.slice(0, 50) })
      }
      return
    }
    if (this.data.loading || !this.data.hasMore) return
    this.loadFollowing(this.data.page + 1)
  },

  onGoLogin() {
    wx.reLaunch({ url: '/pages/login/login' })
  },

  loadCurrentUser() {
    request({ url: '/user/info', method: 'GET', silent401: true })
      .then((res) => {
        if (res.data) {
          this.setData({ currentUserId: res.data.id })
        }
      })
      .catch(() => {})
  },

  // ====== Follow Feed ======

  loadFollowing(page) {
    this.setData({ loading: true })
    return request({
      url: '/checkin/following',
      method: 'GET',
      data: { page: page, size: this.data.size },
      silent401: true
    })
      .then((res) => {
        const list = (res.data || []).map((item) => ({
          ...item,
          avatar: fixImageUrl(item.avatar),
          images: fixImageUrls(item.images),
          isLiked: item.hasLiked || false,
          timeText: formatTime(item.checkInTime)
        }))
        this.setData({
          checkinList: page === 1 ? list : this.data.checkinList.concat(list),
          page: page,
          hasMore: list.length >= this.data.size
        })
      })
      .catch((err) => {
        console.error('Load following failed:', err)
        if (err && err.message === '未登录') {
          this.setData({ isLoggedIn: false, checkinList: [] })
        }
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  // ====== 互动操作 ======

  onTapCard(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/checkin-detail/checkin-detail?id=' + id })
  },

  onLikeCheckin(e) {
    if (!requireLogin()) return
    const id = e.currentTarget.dataset.id
    if (this._likingIds && this._likingIds[id]) return
    this._likingIds = this._likingIds || {}
    this._likingIds[id] = true

    const idx = this.data.checkinList.findIndex((item) => item.id === id)
    if (idx === -1) return
    const item = this.data.checkinList[idx]
    const prevLiked = item.isLiked
    const prevCount = item.likeCount
    const liked = !prevLiked
    const newCount = liked ? (prevCount || 0) + 1 : Math.max(0, (prevCount || 1) - 1)

    this.setData({
      ['checkinList[' + idx + '].isLiked']: liked,
      ['checkinList[' + idx + '].likeCount']: newCount,
      ['checkinList[' + idx + '].likeAnimating']: liked
    })

    if (liked) {
      if (this._likeAnimTimer) clearTimeout(this._likeAnimTimer)
      this._likeAnimTimer = setTimeout(() => {
        this.setData({ ['checkinList[' + idx + '].likeAnimating']: false })
        this._likeAnimTimer = null
      }, 400)
    }

    request({ url: '/checkin/' + id + '/like', method: 'POST' })
      .catch(() => {
        this.setData({
          ['checkinList[' + idx + '].isLiked']: prevLiked,
          ['checkinList[' + idx + '].likeCount']: prevCount
        })
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
      .finally(() => { if (this._likingIds) delete this._likingIds[id] })
  },

  onLongPressCheckin(e) {
    if (!requireLogin()) return
    const id = e.currentTarget.dataset.id
    const userId = e.currentTarget.dataset.userid
    if (userId && this.data.currentUserId && userId !== this.data.currentUserId) return

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
      .catch(() => {
        wx.showToast({ title: '删除失败', icon: 'none' })
      })
  },

  onTapUser(e) {
    const userId = e.currentTarget.dataset.userid
    if (userId && userId !== this.data.currentUserId) {
      wx.navigateTo({ url: '/pages/user-profile/user-profile?userId=' + userId })
    }
  },

  // ====== Hot Notes Mode ======

  onTapHotNotes() {
    this.setData({ showNotes: true })
    this.loadNotes()
  },

  onBackToList() {
    this.setData({ showNotes: false, noteList: [], sortTab: 'hot', activeTime: '本月' })
  },

  loadNotes() {
    this.setData({ loading: true })
    return request({ url: '/travel-note/hot', method: 'GET', data: { limit: 50 } })
      .then((res) => {
        const allList = (res.data || []).map((item, idx) => ({
          ...item,
          coverImage: fixImageUrl(item.coverImage),
          images: fixImageUrls(item.images),
          timeText: formatTime(item.createdAt),
          rank: idx + 1
        }))
        this._allList = allList
        this.setData({
          'noteStats.total': allList.length,
          'noteStats.today': allList.filter(i => this._isToday(i.createdAt)).length,
          'noteStats.week': allList.filter(i => this._isThisWeek(i.createdAt)).length
        })
        this._applyFilter()
      })
      .catch(() => {})
      .finally(() => { this.setData({ loading: false }) })
  },

  _applyFilter() {
    const allList = this._allList || []
    const tab = this.data.activeTime
    let filtered = allList
    if (tab === '今日') {
      filtered = allList.filter(i => this._isToday(i.createdAt))
    } else if (tab === '本周') {
      filtered = allList.filter(i => this._isThisWeek(i.createdAt))
    }
    if (this.data.sortTab === 'new') {
      filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    } else {
      filtered.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
    }
    this.setData({ noteList: filtered.slice(0, 20) })
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

  onTapSort(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.sortTab) return
    this.setData({ sortTab: tab })
    this._applyFilter()
  },

  onTapTime(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.activeTime) return
    this.setData({ activeTime: tab })
    this._applyFilter()
  },

  // ====== Popup ======

  noop() {},

  onOpenPopup(e) {
    const idx = e.currentTarget.dataset.index
    const item = this.data.noteList[idx]
    if (!item) return

    wx.createSelectorQuery().select('#card-' + idx).boundingClientRect((rect) => {
      if (!rect) return
      const originStyle = 'top:' + rect.top + 'px;left:' + rect.left + 'px;width:' + rect.width + 'px;height:' + rect.height + 'px;border-radius:24rpx;'

      this.setData({
        'popup.show': true, 'popup.opening': true, 'popup.closing': false,
        'popup.cardStyle': originStyle, 'popup._originStyle': originStyle,
        'popup._index': idx, 'popup._id': item.id,
        'popup.note': item,
        'popup.liked': !!item.hasLiked, 'popup.likeCount': item.likeCount || 0,
        'popup.commentCount': item.commentCount || 0,
        'popup.comments': [], 'popup.commentText': ''
      })

      setTimeout(() => {
        this.setData({ 'popup.cardStyle': 'top:5vh;left:5vw;width:90vw;height:auto;border-radius:24rpx;' })
      }, 30)

      this.loadPopupComments(item.id)
    }).exec()
  },

  onClosePopup() {
    const origin = this.data.popup._originStyle
    this.setData({ 'popup.closing': true, 'popup.cardStyle': origin })
    setTimeout(() => {
      const idx = this.data.popup._index
      const noteList = this.data.noteList.slice()
      if (noteList[idx]) {
        noteList[idx] = { ...noteList[idx], hasLiked: this.data.popup.liked, likeCount: this.data.popup.likeCount }
        this.setData({ noteList: noteList, 'popup.show': false })
      } else {
        this.setData({ 'popup.show': false })
      }
    }, 280)
  },

  loadPopupComments(id) {
    request({ url: '/comment/page', method: 'GET', data: { noteId: id, page: 1, size: 50 } })
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
    request({ url: '/travel-note/' + this.data.popup._id + '/like', method: 'POST' })
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
    request({ url: '/comment', method: 'POST', data: { noteId: this.data.popup._id, content: text } })
      .then(() => {
        this.setData({ 'popup.commentText': '', 'popup.commentCount': this.data.popup.commentCount + 1 })
        this.loadPopupComments(this.data.popup._id)
      })
      .catch(() => { wx.showToast({ title: '评论失败', icon: 'none' }) })
  },

  onPreviewPopupImage(e) {
    wx.previewImage({ current: e.currentTarget.dataset.src, urls: [e.currentTarget.dataset.src] })
  }
})
