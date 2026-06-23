const { request, fixImageUrls, fixImageUrl } = require('../../utils/api')
const { requireLogin } = require('../../utils/auth')
const { formatTime } = require('../../utils/util')

Page({
  data: {
    userId: null,
    userInfo: {},
    checkinList: [],
    isFollowing: false,
    loading: true,
    page: 1,
    size: 10,
    hasMore: true
  },

  onLoad(options) {
    if (options.userId) {
      this.setData({ userId: options.userId })
      this.loadUserProfile(options.userId)
      this.loadUserCheckins(options.userId, 1)
    }
  },

  loadUserProfile(userId) {
    request({ url: '/user/' + userId, method: 'GET' })
      .then((res) => {
        const user = res.data
        if (!user) { wx.showToast({title:'用户不存在',icon:'none'}); setTimeout(()=>wx.navigateBack(),1500); return; }
        user.avatar = fixImageUrl(user.avatar)
        user.backgroundImage = fixImageUrl(user.backgroundImage)
        this.setData({
          userInfo: user,
          isFollowing: res.data.isFollowing || false
        })
        wx.setNavigationBarTitle({ title: res.data.username || '用户主页' })
        this.setData({ loading: false })
      })
      .catch((err) => {
        console.error('Load user profile failed:', err)
        this.setData({ loading: false })
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
  },

  loadUserCheckins(userId, page) {
    this.setData({ loading: true })
    request({
      url: '/checkin/user/' + userId,
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
        console.error('Load user checkins failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  onFollowToggle() {
    if (this._following) return
    if (!requireLogin()) return
    const userId = this.data.userId
    const isFollowing = this.data.isFollowing
    const method = isFollowing ? 'DELETE' : 'POST'

    this._following = true
    request({
      url: '/follow/' + userId,
      method: method
    })
      .then(() => {
        const newFollowing = !isFollowing
        const followerCount = this.data.userInfo.followerCount || 0
        this.setData({
          isFollowing: newFollowing,
          'userInfo.followerCount': newFollowing ? followerCount + 1 : Math.max(0, followerCount - 1)
        })
        wx.showToast({
          title: newFollowing ? '已关注' : '已取消关注',
          icon: 'none'
        })
      })
      .catch((err) => {
        console.error('Follow toggle failed:', err)
      })
      .finally(() => {
        this._following = false
      })
  },

  onTapCheckin(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/checkin-detail/checkin-detail?id=' + id })
  },

  onTapFollowers() {
    if (!this.data.userId) {
      wx.showToast({ title: '用户信息加载中，请稍后重试', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/follow-list/follow-list?userId=' + this.data.userId + '&tab=followers' })
  },

  onTapFollowing() {
    if (!this.data.userId) {
      wx.showToast({ title: '用户信息加载中，请稍后重试', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/follow-list/follow-list?userId=' + this.data.userId + '&tab=following' })
  },

  onReachBottom() {
    if (this.data.loading || !this.data.hasMore) return
    this.loadUserCheckins(this.data.userId, this.data.page + 1)
  }
})
