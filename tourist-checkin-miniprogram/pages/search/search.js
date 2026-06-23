const { request } = require('../../utils/api')

const HISTORY_KEY = 'search_history'
const MAX_HISTORY = 10

Page({
  data: {
    keyword: '',
    searching: false,
    hasSearched: false,
    notes: [],
    topics: [],
    history: []
  },
  onLoad() {
    this.setData({ history: wx.getStorageSync(HISTORY_KEY) || [] })
  },
  onInput(e) { this.setData({ keyword: e.detail.value }) },
  onSearch() {
    const keyword = this.data.keyword.trim()
    if (!keyword) return
    this.saveHistory(keyword)
    this.setData({ searching: true, hasSearched: false, notes: [], topics: [] })
    Promise.all([
      request({ url: '/topic/search', method: 'GET', data: { keyword } }).then((res) => {
        this.setData({ topics: res.data || [] })
      }).catch((err) => { console.warn(err) }),
      request({ url: '/travel-note/recent', method: 'GET', data: { page: 1, size: 10, keyword } }).then((res) => {
        this.setData({ notes: res.data || [] })
      }).catch((err) => { console.warn(err) })
    ]).finally(() => {
      this.setData({ searching: false, hasSearched: true })
    })
  },
  onClear() { this.setData({ keyword: '', searching: false, hasSearched: false, notes: [], topics: [] }) },
  onCancel() { wx.navigateBack() },
  saveHistory(keyword) {
    let history = this.data.history.filter((h) => h !== keyword)
    history.unshift(keyword)
    if (history.length > MAX_HISTORY) history = history.slice(0, MAX_HISTORY)
    this.setData({ history })
    wx.setStorageSync(HISTORY_KEY, history)
  },
  onClearHistory() {
    this.setData({ history: [] })
    wx.removeStorageSync(HISTORY_KEY)
  },
  onTapHistory(e) {
    this.setData({ keyword: e.currentTarget.dataset.keyword })
    this.onSearch()
  },
  onTapNote(e) { wx.navigateTo({ url: '/pages/note-detail/note-detail?id=' + e.currentTarget.dataset.id }) },
  onTapTopic(e) { wx.navigateTo({ url: '/pages/topic-feed/topic-feed?name=' + encodeURIComponent(e.currentTarget.dataset.name) }) }
})
