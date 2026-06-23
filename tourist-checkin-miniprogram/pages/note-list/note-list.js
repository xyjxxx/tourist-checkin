const { request, fixImageUrl } = require('../../utils/api')

const app = getApp()

Page({
  data: {
    activeTab: 'hot',
    noteList: [],
    loading: false,
    tabSwitching: false,
    hasMore: true,
    page: 1
  },

  onLoad() {
    this.loadNotes()
  },

  onShow() {
    if (this.getTabBar()) this.getTabBar().setSelected(3)
  },

  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true })
    this.loadNotes().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.loading || !this.data.hasMore) return
    this.setData({ page: this.data.page + 1 })
    this.loadNotes()
  },

  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.activeTab) return
    this.setData({ tabSwitching: true, activeTab: tab, noteList: [], page: 1, hasMore: true })
    setTimeout(() => {
      this.setData({ tabSwitching: false })
      this.loadNotes()
    }, 150)
  },

  loadNotes() {
    const tab = this.data.activeTab
    let url = '/travel-note/hot'
    let data = { page: this.data.page, size: 10 }

    if (tab === 'recent') {
      url = '/travel-note/recent'
      data = { page: this.data.page, size: 10 }
    } else if (tab === 'mine') {
      const userId = app.globalData.userInfo && app.globalData.userInfo.id
      if (!userId) {
        this.setData({ noteList: [] })
        return Promise.resolve()
      }
      url = '/travel-note/user/' + userId
      data = { page: this.data.page, size: 10 }
    }

    this.setData({ loading: true })
    return request({ url: url, method: 'GET', data: data })
      .then((res) => {
        const list = (res.data || []).map(item => ({
          ...item,
          coverImage: fixImageUrl(item.coverImage),
          authorAvatar: fixImageUrl(item.authorAvatar || (item.author && item.author.avatar) || ''),
          authorName: item.authorName || (item.author && item.author.username) || ''
        }))
        this.setData({
          noteList: this.data.page === 1 ? list : this.data.noteList.concat(list),
          hasMore: list.length >= 10
        })
      })
      .catch((err) => {
        console.error('Load notes failed:', err)
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  onTapNote(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/note-detail/note-detail?id=' + id })
  }
})
