const { request, fixImageUrls, fixImageUrl } = require('../../utils/api')
const { checkLogin, requireLogin } = require('../../utils/auth')
const { formatTime } = require('../../utils/util')

const app = getApp()

Page({
  data: {
    checkinList: [],
    page: 1,
    size: 10,
    loading: true,
    refreshing: false,
    hasMore: true,
    currentUserId: null,
    isLoggedIn: false,
    greetingEmoji: '🌅',
    greetingText: '早上好',
    heroQuote: '每一段旅程，都值得被记录'
  },

  onLoad() {
    this._updateGreeting()
    const loggedIn = checkLogin()
    this.setData({ isLoggedIn: loggedIn })
    if (loggedIn) {
      this.loadCurrentUser()
    }
  },

  onShow() {
    if (this.getTabBar()) this.getTabBar().setSelected(0)
    const loggedIn = checkLogin()
    if (loggedIn !== this.data.isLoggedIn) {
      this.setData({ isLoggedIn: loggedIn })
      if (loggedIn) {
        this.loadCurrentUser()
      }
    }
    if (loggedIn && app.globalData.needRefreshFeed) {
      app.globalData.needRefreshFeed = false
      this.loadCheckins(1)
    } else if (!this._loaded) {
      this._loaded = true
      this.loadCheckins(1)
    }
  },

  onGoLogin() {
    wx.reLaunch({ url: '/pages/login/login' })
  },

  _updateGreeting() {
    var hour = new Date().getHours()
    var quotes = [
      '每一段旅程，都值得被记录',
      '世界那么大，一起去看看',
      '生活的诗意，藏在山川湖海间',
      '出发，就是最好的治愈',
      '用脚步丈量世界，用镜头定格美好'
    ]
    var emoji, text
    if (hour < 6) { emoji = '🌙'; text = '夜深了' }
    else if (hour < 9) { emoji = '🌅'; text = '早上好' }
    else if (hour < 12) { emoji = '☀️'; text = '上午好' }
    else if (hour < 14) { emoji = '🌤️'; text = '中午好' }
    else if (hour < 18) { emoji = '⛅'; text = '下午好' }
    else if (hour < 22) { emoji = '🌆'; text = '晚上好' }
    else { emoji = '🌙'; text = '夜深了' }
    var quote = quotes[Math.floor(Math.random() * quotes.length)]
    this.setData({ greetingEmoji: emoji, greetingText: text, heroQuote: quote })
  },

  onUnload() {
    if (this._likeAnimTimer) clearTimeout(this._likeAnimTimer)
  },

  onPullDownRefresh() {
    this.setData({ refreshing: true })
    this.loadCheckins(1).finally(() => {
      wx.stopPullDownRefresh()
      this.setData({ refreshing: false })
    })
  },

  onReachBottom() {
    if (this.data.loading || !this.data.hasMore) return
    this.loadCheckins(this.data.page + 1)
  },

  loadCurrentUser() {
    request({ url: '/user/info', method: 'GET' })
      .then((res) => {
        if (res.data) {
          this.setData({ currentUserId: res.data.id })
        }
      })
      .catch((err) => { console.warn(err) })
  },

  loadCheckins(page) {
    this.setData({ loading: true })
    return request({
      url: '/checkin/recent',
      method: 'GET',
      data: { page: page, size: this.data.size }
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
        console.error('Load checkins failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  onTapCard(e) {
    if (!requireLogin()) return
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
      .catch((err) => {
        console.error('Delete checkin failed:', err)
        wx.showToast({ title: '删除失败', icon: 'none' })
      })
  },

  onTapUser(e) {
    const userId = e.currentTarget.dataset.userid
    if (userId && userId !== this.data.currentUserId) {
      wx.navigateTo({ url: '/pages/user-profile/user-profile?userId=' + userId })
    }
  },

  onTapSearchBar() {
    wx.navigateTo({ url: '/pages/search/search' })
  },

  onTapHotNotes() {
    wx.switchTab({ url: '/pages/note-list/note-list' })
  },

  onTapHotCheckins() {
    wx.navigateTo({ url: '/pages/hot-checkins/hot-checkins' })
  },

  onTapMerchants() {
    wx.navigateTo({ url: '/pages/merchants/merchants' })
  },

  onTapRecommendShop() {
    if (!requireLogin()) return
    wx.navigateTo({ url: '/pages/shop-recommend/shop-recommend' })
  },

})
