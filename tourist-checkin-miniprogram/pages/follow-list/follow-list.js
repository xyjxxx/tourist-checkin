const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    activeTab: 'following',
    userList: [],
    loading: true,
    userId: null
  },

  onLoad(options) {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    if (options.userId) {
      this.setData({ userId: options.userId })
    }
    if (options.tab) {
      this.setData({ activeTab: options.tab })
    }
    this.loadUsers()
  },

  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.activeTab) return
    this.setData({ activeTab: tab, userList: [] })
    this.loadUsers()
  },

  loadUsers() {
    const tab = this.data.activeTab
    const userId = this.data.userId
    const url = tab === 'following'
      ? '/follow/following' + (userId ? '?userId=' + userId : '')
      : '/follow/followers' + (userId ? '?userId=' + userId : '')

    this.setData({ loading: true })
    request({ url: url, method: 'GET' })
      .then((res) => {
        this.setData({ userList: res.data || [] })
      })
      .catch((err) => {
        console.error('Load users failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  onToggleFollow(e) {
    const userId = e.currentTarget.dataset.id
    const isFollowing = String(e.currentTarget.dataset.following) === 'true'

    request({
      url: '/follow/' + userId,
      method: isFollowing ? 'DELETE' : 'POST'
    }).then(() => {
      const list = this.data.userList.map((item) => {
        if (item.id === userId) {
          return { ...item, isFollowing: !isFollowing }
        }
        return item
      })
      this.setData({ userList: list })
      wx.showToast({
        title: isFollowing ? '已取消关注' : '已关注',
        icon: 'none'
      })
    }).catch((err) => {
      console.error('Toggle follow failed:', err)
    })
  },

  onTapUser(e) {
    const userId = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/user-profile/user-profile?userId=' + userId })
  }
})
