const { request } = require('../../utils/api')
const amap = require('../../utils/amap-wx')

Page({
  data: {
    lat: 0,
    lng: 0,
    merchants: [],
    categories: ['全部', '火锅', '小吃', '中餐', '西餐', '甜品', '烧烤', '海鲜', '饮品'],
    activeCategory: '全部',
    loading: false,
    radius: 5000,
    locationFailed: false
  },

  onLoad() {
    this.getLocation()
  },

  onPullDownRefresh() {
    this.setData({ locationFailed: false })
    this.getLocation()
    wx.stopPullDownRefresh()
  },

  getLocation() {
    amap.getLocation().then((res) => {
      this.setData({ lat: res.latitude, lng: res.longitude })
      this.loadMerchants()
    }).catch(() => {
      this.setData({ locationFailed: true })
      wx.showToast({ title: '获取位置失败，请下拉刷新重试', icon: 'none' })
    })
  },

  loadMerchants() {
    if (!this.data.lat || !this.data.lng) return
    this.setData({ loading: true })

    const params = {
      lat: this.data.lat,
      lng: this.data.lng,
      radius: this.data.radius
    }
    if (this.data.activeCategory !== '全部') {
      params.category = this.data.activeCategory
    }

    request({ url: '/shop-recommend/nearby', method: 'GET', data: params })
      .then((res) => {
        this.setData({ merchants: res.data || [] })
      })
      .catch((err) => {
        console.error('Load merchants failed:', err)
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  onTapCategory(e) {
    const category = e.currentTarget.dataset.category
    if (category === this.data.activeCategory) return
    this.setData({ activeCategory: category })
    this.loadMerchants()
  },

  onTapMerchant(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/merchant-detail/merchant-detail?id=' + id })
  }
})
