const { request } = require('../../utils/api')

Page({
  data: { merchant: null, loading: false, markers: [] },
  onLoad(options) {
    if (options.id) this.loadMerchant(options.id)
  },
  loadMerchant(id) {
    this.setData({ loading: true })
    request({ url: '/merchant/' + id, method: 'GET' })
      .then((res) => {
        const m = res.data
        if (!m) { wx.showToast({title:'商家不存在',icon:'none'}); setTimeout(()=>wx.navigateBack(),1500); return; }
        const markers = []
        if (m.longitude && m.latitude) {
          markers.push({ id: 1, longitude: m.longitude, latitude: m.latitude, width: 30, height: 30, callout: { content: m.name, padding: 8, borderRadius: 4, display: 'ALWAYS' } })
        }
        this.setData({ merchant: m, markers })
      })
      .catch(() => { wx.showToast({ title: '加载失败', icon: 'none' }) })
      .finally(() => { this.setData({ loading: false }) })
  },
  onCallPhone() {
    const m = this.data.merchant
    if (!m || !m.phone) return
    wx.makePhoneCall({ phoneNumber: m.phone })
  },
  onNavigate() {
    const m = this.data.merchant
    if (!m) return
    if (m.latitude && m.longitude) {
      wx.openLocation({ latitude: m.latitude, longitude: m.longitude, name: m.name, address: m.address })
    }
  }
})
