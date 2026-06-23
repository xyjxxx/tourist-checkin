const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    title: '',
    description: '',
    city: '',
    startDate: '',
    endDate: '',
    days: [
      { dayIndex: 0, pois: [] }
    ],
    submitting: false
  },

  onLoad() {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value })
  },

  onDescInput(e) {
    this.setData({ description: e.detail.value })
  },

  onCityInput(e) {
    this.setData({ city: e.detail.value })
  },

  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value })
    this.syncDays()
  },

  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value })
    this.syncDays()
  },

  syncDays() {
    const { startDate, endDate } = this.data
    if (!startDate || !endDate) return
    const start = new Date(startDate)
    const end = new Date(endDate)
    if (end < start) {
      wx.showToast({ title: '结束日期不能早于开始日期', icon: 'none' })
      this.setData({ endDate: '' })
      return
    }
    const diff = Math.floor((end - start) / 86400000) + 1
    const days = []
    for (let i = 0; i < diff; i++) {
      const existingPois = (this.data.days[i] && this.data.days[i].pois) || []
      const pois = existingPois.map((p) => p._id ? p : { ...p, _id: Date.now() + '_' + Math.random().toString(36).substr(2, 5) })
      days.push({ dayIndex: i, pois: pois })
    }
    this.setData({ days: days })
  },

  onAddDay() {
    const days = this.data.days.slice()
    days.push({ dayIndex: days.length, pois: [] })
    this.setData({ days: days })
  },

  onAddPoi(e) {
    const dayIdx = e.currentTarget.dataset.day
    const pois = JSON.parse(JSON.stringify(this.data.days[dayIdx].pois))
    pois.push({ _id: Date.now() + '_' + Math.random().toString(36).substr(2, 5), name: '' })
    this.setData({ ['days[' + dayIdx + '].pois']: pois })
  },

  onDeletePoi(e) {
    const dayIdx = e.currentTarget.dataset.day
    const poiIdx = e.currentTarget.dataset.poi
    const pois = JSON.parse(JSON.stringify(this.data.days[dayIdx].pois))
    pois.splice(poiIdx, 1)
    this.setData({ ['days[' + dayIdx + '].pois']: pois })
  },

  onPoiNameInput(e) {
    const dayIdx = e.currentTarget.dataset.day
    const poiIdx = e.currentTarget.dataset.poi
    this.setData({ ['days[' + dayIdx + '].pois[' + poiIdx + '].name']: e.detail.value })
  },

  onSubmit() {
    if (this.data.submitting) return
    if (!this.data.title.trim()) {
      wx.showToast({ title: '请输入标题', icon: 'none' })
      return
    }
    if (!this.data.startDate || !this.data.endDate) {
      wx.showToast({ title: '请选择日期', icon: 'none' })
      return
    }
    if (this.data.startDate > this.data.endDate) {
      wx.showToast({ title: '结束日期不能早于开始日期', icon: 'none' })
      return
    }

    const planData = {
      title: this.data.title,
      description: this.data.description,
      city: this.data.city,
      startDate: this.data.startDate,
      endDate: this.data.endDate,
      days: this.data.days.map((day) => ({
        dayIndex: day.dayIndex,
        pois: day.pois.filter((poi) => poi.name.trim()).map(({ _id, ...rest }) => rest)
      }))
    }

    this.setData({ submitting: true })
    request({
      url: '/trip-plan',
      method: 'POST',
      data: planData
    })
      .then(() => {
        wx.showToast({ title: '创建成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      })
      .catch((err) => {
        console.error('Create plan failed:', err)
        wx.showToast({ title: '创建失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ submitting: false })
      })
  }
})
