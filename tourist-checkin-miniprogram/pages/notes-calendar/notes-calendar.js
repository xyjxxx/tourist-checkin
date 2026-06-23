const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    year: 0,
    month: 0,
    weekdays: ['日', '一', '二', '三', '四', '五', '六'],
    days: [],
    monthCheckins: [],
    totalDays: 0,
    currentStreak: 0,
    monthDays: 0,
    loading: false,
    checkinDates: {}
  },

  onLoad() {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const now = new Date()
    this.setData({ year: now.getFullYear(), month: now.getMonth() + 1 })
    this.loadData()
  },

  loadData() {
    this.setData({ loading: true })
    Promise.all([
      request({ url: '/checkin/my', method: 'GET', data: { page: 1, size: 200 } }),
      request({ url: '/statistics/report', method: 'GET' })
    ]).then(([checkinRes, reportRes]) => {
      const checkins = checkinRes.data || []
      const report = reportRes.data || {}
      const checkinDates = {}
      const weekdayNames = ['日', '一', '二', '三', '四', '五', '六']

      checkins.forEach((c) => {
        const d = new Date(c.checkInTime)
        const key = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
        checkinDates[key] = true
        c.dayNum = d.getDate()
        c.weekday = '周' + weekdayNames[d.getDay()]
      })

      this.setData({
        checkinDates,
        totalDays: report.totalCheckIns || checkins.length,
        currentStreak: report.longestStreak || 0
      })
      this.buildCalendar()
      this.filterMonthCheckins(checkins)
    }).catch((err) => { console.warn(err) }).finally(() => {
      this.setData({ loading: false })
    })
  },

  buildCalendar() {
    const { year, month, checkinDates } = this.data
    const firstDay = new Date(year, month - 1, 1).getDay()
    const daysInMonth = new Date(year, month, 0).getDate()
    const daysInPrev = new Date(year, month - 1, 0).getDate()
    const today = new Date()
    const todayStr = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0') + '-' + String(today.getDate()).padStart(2, '0')
    const days = []

    // Previous month padding
    for (let i = firstDay - 1; i >= 0; i--) {
      days.push({ day: daysInPrev - i, isCurrentMonth: false, isChecked: false, isToday: false })
    }
    // Current month
    let monthDays = 0
    for (let d = 1; d <= daysInMonth; d++) {
      const key = year + '-' + String(month).padStart(2, '0') + '-' + String(d).padStart(2, '0')
      const isChecked = !!checkinDates[key]
      if (isChecked) monthDays++
      days.push({ day: d, isCurrentMonth: true, isChecked, isToday: key === todayStr })
    }
    // Next month padding
    const remaining = 42 - days.length
    for (let d = 1; d <= remaining; d++) {
      days.push({ day: d, isCurrentMonth: false, isChecked: false, isToday: false })
    }
    this.setData({ days, monthDays })
  },

  filterMonthCheckins(checkins) {
    const { year, month } = this.data
    const filtered = checkins.filter((c) => {
      const d = new Date(c.checkInTime)
      return d.getFullYear() === year && d.getMonth() + 1 === month
    }).sort((a, b) => new Date(b.checkInTime) - new Date(a.checkInTime))
    this.setData({ monthCheckins: filtered })
  },

  onPrevMonth() {
    let { year, month } = this.data
    month--
    if (month < 1) { month = 12; year-- }
    this.setData({ year, month, loading: true })
    this.loadData()
  },

  onNextMonth() {
    let { year, month } = this.data
    month++
    if (month > 12) { month = 1; year++ }
    this.setData({ year, month, loading: true })
    this.loadData()
  },

  onTapCheckin(e) {
    wx.navigateTo({ url: '/pages/checkin-detail/checkin-detail?id=' + e.currentTarget.dataset.id })
  }
})
