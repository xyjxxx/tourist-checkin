const { request, uploadFile } = require('../../utils/api')
const { checkLogin, requireLogin } = require('../../utils/auth')
const amap = require('../../utils/amap-wx')

Page({
  data: {
    mode: 'select',
    lng: 0,
    lat: 0,
    markers: [],
    locationName: '',
    locationFailed: false,
    content: '',
    imageList: [],
    submitting: false,
    topicInput: '',
    topicTags: [],
    showTopicInput: false,
    createdCheckInId: null,
    showSuccess: false,
    recommendShop: false,
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
    if (options.locationId) {
      this.setData({ mode: 'checkin' })
      this.loadLocationById(options.locationId)
    }
  },

  onShow() {
    if (this.getTabBar()) this.getTabBar().setSelected(2)
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    // 每次进入页面重置推荐状态，避免上次勾选残留
    this.setData({ recommendShop: false })
  },

  onSelectCheckin() {
    if (!requireLogin()) return
    this.setData({ mode: 'checkin' })
    this.getCurrentLocation()
  },

  onSelectNote() {
    if (!requireLogin()) return
    wx.navigateTo({ url: '/pages/note-create/note-create' })
  },

  onSelectShop() {
    if (!requireLogin()) return
    wx.navigateTo({ url: '/pages/shop-recommend/shop-recommend' })
  },

  onBackToSelect() {
    this.setData({ mode: 'select', content: '', imageList: [], topicTags: [], showTopicInput: false, recommendShop: false })
  },


  loadLocationById(id) {
    request({ url: '/location/' + id, method: 'GET' })
      .then((res) => {
        if (res.data) {
          this.setData({
            lng: res.data.longitude,
            lat: res.data.latitude,
            locationName: res.data.name || '',
            markers: [{
              id: 1,
              longitude: res.data.longitude,
              latitude: res.data.latitude,
              width: 30,
              height: 30
            }]
          })
        } else {
          this.getCurrentLocation()
        }
      })
      .catch(() => {
        this.getCurrentLocation()
      })
  },

  getCurrentLocation() {
    amap.getRegeo().then((res) => {
      this.setData({
        lng: res.longitude,
        lat: res.latitude,
        locationName: res.name || res.address || '',
        markers: [{
          id: 1,
          longitude: res.longitude,
          latitude: res.latitude,
          width: 30,
          height: 30
        }]
      })
    }).catch(() => {
      this.setData({ locationFailed: true })
      wx.showToast({ title: '获取位置失败', icon: 'none' })
    })
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
    const location = this.data.lng && this.data.lat
      ? this.data.lng + ',' + this.data.lat : ''
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
      lng: poi.longitude,
      lat: poi.latitude,
      locationName: poi.name,
      showPoiSearch: false,
      poiKeyword: '',
      poiResults: [],
      markers: [{
        id: 1,
        longitude: poi.longitude,
        latitude: poi.latitude,
        width: 30,
        height: 30
      }]
    })
  },

  onClosePoiSearch() {
    this.setData({ showPoiSearch: false, poiKeyword: '', poiResults: [] })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  onChooseImage() {
    const remaining = 9 - this.data.imageList.length
    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = res.tempFiles.map(f => f.tempFilePath)
        this.setData({
          imageList: this.data.imageList.concat(newImages)
        })
      }
    })
  },

  onDeleteImage(e) {
    const index = e.currentTarget.dataset.index
    const list = this.data.imageList.slice()
    list.splice(index, 1)
    this.setData({ imageList: list })
  },

  onTopicInput(e) {
    this.setData({ topicInput: e.detail.value })
  },

  onAddTopic() {
    const tag = this.data.topicInput.trim()
    if (!tag) return
    if (this.data.topicTags.length >= 5) {
      wx.showToast({ title: '最多添加5个话题', icon: 'none' })
      return
    }
    if (this.data.topicTags.indexOf(tag) !== -1) {
      wx.showToast({ title: '话题已存在', icon: 'none' })
      return
    }
    this.setData({
      topicTags: this.data.topicTags.concat([tag]),
      topicInput: ''
    })
  },

  onDeleteTopic(e) {
    const index = e.currentTarget.dataset.index
    const tags = this.data.topicTags.slice()
    tags.splice(index, 1)
    this.setData({ topicTags: tags })
  },

  onShowTopicInput() {
    this.setData({ showTopicInput: true })
  },

  onSubmit() {
    if (this.data.submitting) return
    if (!this.data.content.trim() && this.data.imageList.length === 0) {
      wx.showToast({ title: '请输入内容或添加图片', icon: 'none' })
      return
    }

    this.setData({ submitting: true })

    const uploadPromises = this.data.imageList.map((filePath) => {
      return uploadFile(filePath)
    })

    Promise.allSettled(uploadPromises)
      .then((settledResults) => {
        const failedCount = settledResults.filter((r) => r.status === 'rejected').length
        if (failedCount > 0) {
          wx.showToast({ title: failedCount + '张图片上传失败', icon: 'none', duration: 2000 })
        }
        const imageUrls = settledResults
          .filter((r) => r.status === 'fulfilled')
          .map((r) => r.value.data.url)
        return request({
          url: '/checkin',
          method: 'POST',
          data: {
            content: this.data.content,
            images: imageUrls,
            longitude: this.data.lng,
            latitude: this.data.lat,
            locationName: this.data.locationName
          }
        })
      })
      .then((res) => {
        const checkInId = res.data
        // Attach topics if any
        if (checkInId && this.data.topicTags.length > 0) {
          return request({
            url: '/topic/attach/' + checkInId,
            method: 'POST',
            data: { topics: this.data.topicTags }
          }).then(() => checkInId)
        }
        return checkInId
      })
      .then((checkInId) => {
        this.setData({ showSuccess: true, createdCheckInId: checkInId || null })
        getApp().globalData.needRefreshFeed = true
        const recommend = this.data.recommendShop
        setTimeout(() => {
          if (recommend) {
            wx.navigateTo({
              url: '/pages/shop-recommend/shop-recommend?lng=' + this.data.lng +
                   '&lat=' + this.data.lat +
                   '&address=' + encodeURIComponent(this.data.locationName || '')
            })
          } else if (checkInId) {
            wx.navigateTo({ url: '/pages/checkin-detail/checkin-detail?id=' + checkInId })
          } else {
            wx.switchTab({ url: '/pages/index/index' })
          }
          // Safety net: if navigation didn't dismiss the overlay, force hide it
          setTimeout(() => {
            if (this.data.showSuccess) {
              this.setData({ showSuccess: false, submitting: false, mode: 'select' })
            }
          }, 500)
        }, 1200)
      })
      .catch((err) => {
        console.error('Submit checkin failed:', err)
        wx.showToast({ title: '发布失败', icon: 'none' })
        this.setData({ submitting: false, showSuccess: false })
      })
  },

  onToggleRecommend() {
    this.setData({ recommendShop: !this.data.recommendShop })
  }
})
