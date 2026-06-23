const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    list: [],
    filteredList: [],
    page: 1,
    size: 10,
    loading: false,
    hasMore: true,
    activeFilter: 'all'
  },

  onShow() {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({ page: 1, list: [], hasMore: true })
    this.loadList()
  },

  async loadList() {
    if (this.data.loading || !this.data.hasMore) return
    this.setData({ loading: true })

    try {
      const res = await request({
        url: '/shop-recommend/mine',
        data: {
          page: this.data.page,
          size: this.data.size
        }
      })

      const newList = res.data || res.list || res || []
      const list = this.data.page === 1 ? newList : this.data.list.concat(newList)
      this.setData({
        list,
        hasMore: newList.length >= this.data.size,
        page: this.data.page + 1
      })
      this.applyFilter()
    } catch (err) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onReachBottom() {
    this.loadList()
  },

  onPullDownRefresh() {
    this.setData({ page: 1, list: [], hasMore: true })
    this.loadList().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  onFilterTap(e) {
    const filter = e.currentTarget.dataset.filter
    this.setData({ activeFilter: filter })
    this.applyFilter()
  },

  applyFilter() {
    const { list, activeFilter } = this.data
    if (activeFilter === 'all') {
      this.setData({ filteredList: list })
    } else {
      this.setData({
        filteredList: list.filter(item => (item.auditStatus || 'pending') === activeFilter)
      })
    }
  },

  onTapItem(e) {
    const id = e.currentTarget.dataset.id
    const item = this.data.list.find(i => i.id === id)
    if (item && item.merchantId) {
      wx.navigateTo({
        url: '/pages/merchant-detail/merchant-detail?id=' + item.merchantId
      })
    }
  },

  onResubmit(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: '/pages/shop-recommend/shop-recommend?id=' + id
    })
  }
})
