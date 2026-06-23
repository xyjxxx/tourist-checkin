const { request } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    plan: null,
    loading: false,
    mapLng: 116.397428,
    mapLat: 39.90923,
    markers: []
  },

  onLoad(options) {
    if (!checkLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    if (options.id) {
      this.loadPlan(options.id)
    }
  },

  loadPlan(id) {
    this.setData({ loading: true })
    request({ url: '/trip-plan/' + id, method: 'GET' })
      .then((res) => {
        const plan = res.data
        if (!plan) { wx.showToast({title:'行程不存在',icon:'none'}); setTimeout(()=>wx.navigateBack(),1500); return; }
        const markers = []
        let firstPoi = null

        if (plan.days) {
          plan.days.forEach((day, dayIdx) => {
            if (day.pois) {
              day.pois.forEach((poi, poiIdx) => {
                if (poi.longitude && poi.latitude) {
                  const marker = {
                    id: dayIdx * 100 + poiIdx,
                    longitude: poi.longitude,
                    latitude: poi.latitude,
                    width: 24,
                    height: 24,
                    callout: {
                      content: poi.name || ('Day ' + (dayIdx + 1)),
                      padding: 8,
                      borderRadius: 4,
                      display: 'BYCLICK'
                    }
                  }
                  markers.push(marker)
                  if (!firstPoi) firstPoi = poi
                }
              })
            }
          })
        }

        this.setData({
          plan: plan,
          markers: markers,
          mapLng: firstPoi ? firstPoi.longitude : 116.397428,
          mapLat: firstPoi ? firstPoi.latitude : 39.90923
        })
      })
      .catch((err) => {
        console.error('Load plan failed:', err)
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})
