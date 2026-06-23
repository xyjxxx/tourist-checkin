const { request, uploadFile } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')
const amap = require('../../utils/amap-wx')

Page({
  data: {
    name: '',
    category: '',
    categoryIndex: -1,
    categories: ['火锅', '小吃', '中餐', '西餐', '甜品', '烧烤', '海鲜', '饮品', '其他'],
    address: '',
    longitude: '',
    latitude: '',
    city: '',
    avgPrice: '',
    images: [],
    recommendReason: '',
    signatureDish: '',
    businessHours: '',
    phone: '',
    warning: '',
    submitting: false,
    showPoiSearch: false,
    poiKeyword: '',
    poiResults: [],
    poiSearching: false
  },

  onLoad(options) {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    if (options.lng && options.lat) {
      this.setData({
        longitude: options.lng,
        latitude: options.lat,
        address: options.address || ''
      })
    }
  },

  onNameInput(e) {
    this.setData({ name: e.detail.value })
  },

  onCategoryTap(e) {
    const idx = Number(e.currentTarget.dataset.index)
    if (idx === this.data.categoryIndex) {
      this.setData({ categoryIndex: -1, category: '' })
    } else {
      this.setData({
        categoryIndex: idx,
        category: this.data.categories[idx]
      })
    }
  },

  onAvgPriceInput(e) {
    this.setData({ avgPrice: e.detail.value })
  },

  onRecommendReasonInput(e) {
    this.setData({ recommendReason: e.detail.value })
  },

  onSignatureDishInput(e) {
    this.setData({ signatureDish: e.detail.value })
  },

  onBusinessHoursInput(e) {
    this.setData({ businessHours: e.detail.value })
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  onWarningInput(e) {
    this.setData({ warning: e.detail.value })
  },

  onChooseLocation() {
    this.setData({ showPoiSearch: true, poiKeyword: '', poiResults: [] })
  },

  onPoiSearchInput(e) {
    const keyword = e.detail.value
    this.setData({ poiKeyword: keyword })
    if (this._poiTimer) clearTimeout(this._poiTimer)
    if (!keyword.trim()) {
      this.setData({ poiResults: [] })
      return
    }
    this._poiTimer = setTimeout(() => {
      this._doPoiSearch(keyword)
    }, 400)
  },

  _doPoiSearch(keyword) {
    this.setData({ poiSearching: true })
    const location = this.data.longitude && this.data.latitude
      ? this.data.longitude + ',' + this.data.latitude : ''
    amap.getInputtips({ keywords: keyword, location })
      .then((results) => {
        this.setData({ poiResults: results, poiSearching: false })
      })
      .catch(() => {
        this.setData({ poiResults: [], poiSearching: false })
      })
  },

  onSelectPoi(e) {
    const idx = e.currentTarget.dataset.index
    const poi = this.data.poiResults[idx]
    if (!poi) return
    this.setData({
      longitude: poi.longitude,
      latitude: poi.latitude,
      address: poi.name,
      city: poi.city || '',
      showPoiSearch: false,
      poiKeyword: '',
      poiResults: []
    })
  },

  onClosePoiSearch() {
    this.setData({ showPoiSearch: false, poiKeyword: '', poiResults: [] })
  },

  onChooseImage() {
    const remaining = 9 - this.data.images.length
    if (remaining <= 0) return
    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = res.tempFiles.map(f => f.tempFilePath)
        this.setData({
          images: this.data.images.concat(newImages)
        })
      }
    })
  },

  onDeleteImage(e) {
    const idx = e.currentTarget.dataset.index
    const images = this.data.images.slice()
    images.splice(idx, 1)
    this.setData({ images })
  },

  async onSubmit() {
    const { name, category, address, longitude, latitude, images, recommendReason, submitting } = this.data

    if (submitting) return

    if (!name.trim()) {
      this.setData({ warning: '' })
      wx.showToast({ title: '请输入店铺名称', icon: 'none' })
      return
    }
    if (!category) {
      wx.showToast({ title: '请选择菜系分类', icon: 'none' })
      return
    }
    if (!address) {
      wx.showToast({ title: '请选择店铺位置', icon: 'none' })
      return
    }
    if (!longitude || !latitude) {
      wx.showToast({ title: '位置信息不完整', icon: 'none' })
      return
    }
    if (images.length < 3) {
      wx.showToast({ title: '请至少上传3张实拍图片', icon: 'none' })
      return
    }
    if (recommendReason.length < 10) {
      wx.showToast({ title: '推荐理由至少10个字', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    wx.showLoading({ title: '提交中...' })

    try {
      const uploadResults = await Promise.allSettled(
        images.map(img => uploadFile(img))
      )
      const failedCount = uploadResults.filter(r => r.status === 'rejected').length
      if (failedCount > 0) {
        wx.showToast({ title: failedCount + '张图片上传失败', icon: 'none', duration: 2000 })
      }
      const imageUrls = uploadResults
        .filter(r => r.status === 'fulfilled')
        .map(r => r.value.url || r.value.data?.url || r.value)

      await request({
        url: '/shop-recommend',
        method: 'POST',
        data: {
          name: name.trim(),
          category,
          address,
          longitude: Number(longitude),
          latitude: Number(latitude),
          city: this.data.city,
          avgPrice: this.data.avgPrice ? Number(this.data.avgPrice) : 0,
          images: imageUrls,
          recommendReason,
          signatureDish: this.data.signatureDish,
          businessHours: this.data.businessHours,
          phone: this.data.phone,
          warning: this.data.warning
        }
      })

      wx.hideLoading()
      wx.showToast({ title: '推荐成功', icon: 'success' })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (err) {
      wx.hideLoading()
      wx.showToast({ title: '提交失败，请重试', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
