/**
 * 高德地图微信小程序工具类
 * 封装高德地图 Web Service API，提供逆地理编码、POI搜索等功能
 *
 * 使用前需要：
 * 1. 在高德开放平台申请小程序 key (https://lbs.amap.com)
 * 2. 在微信公众平台后台将 restapi.amap.com 添加为合法域名
 */

const AMAP_KEY = 'ebd5eb4cb421c3849a8c88b93e6d9a07'
const AMAP_BASE = 'https://restapi.amap.com'

function request(params) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: AMAP_BASE + params.url,
      data: { key: AMAP_KEY, ...params.data },
      success(res) {
        if (res.data && res.data.status === '1') {
          resolve(res.data)
        } else {
          reject(new Error(res.data && res.data.info || '高德接口请求失败'))
        }
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

/**
 * 获取当前定位 + 逆地理编码（坐标 → 地址）
 * @returns {Promise<{longitude, latitude, name, address, city}>}
 */
function getRegeo() {
  return new Promise((resolve, reject) => {
    wx.getLocation({
      type: 'gcj02',
      success(loc) {
        request({
          url: '/v3/geocode/regeo',
          data: {
            location: loc.longitude + ',' + loc.latitude,
            extensions: 'base',
            radius: 1000
          }
        }).then((data) => {
          const regeo = data.regeocode || {}
          const addr = regeo.addressComponent || {}
          resolve({
            longitude: loc.longitude,
            latitude: loc.latitude,
            name: (regeo.formatted_address || '').replace(/^.*?[省市区县]/, '') || '',
            address: regeo.formatted_address || '',
            city: addr.city || addr.province || ''
          })
        }).catch(() => {
          // 逆地理编码失败，仍返回坐标
          resolve({
            longitude: loc.longitude,
            latitude: loc.latitude,
            name: '',
            address: '',
            city: ''
          })
        })
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

/**
 * 仅获取当前坐标（不做逆地理编码）
 * @returns {Promise<{longitude, latitude}>}
 */
function getLocation() {
  return new Promise((resolve, reject) => {
    wx.getLocation({
      type: 'gcj02',
      success(loc) {
        resolve({ longitude: loc.longitude, latitude: loc.latitude })
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

/**
 * POI 关键字搜索
 * @param {Object} opts
 * @param {string} opts.keywords - 搜索关键字
 * @param {string} [opts.location] - 中心点坐标 "lng,lat"
 * @param {string} [opts.city] - 城市
 * @param {number} [opts.page] - 页码
 * @returns {Promise<Array<{name, address, longitude, latitude, city, type}>>}
 */
function getPoiAround(opts) {
  const data = {
    keywords: opts.keywords || '',
    offset: 20,
    page: opts.page || 1,
    extensions: 'base'
  }
  if (opts.location) data.location = opts.location
  if (opts.city) data.city = opts.city

  return request({ url: '/v3/place/text', data }).then((res) => {
    return (res.pois || []).map((poi) => {
      const loc = (poi.location || '').split(',')
      return {
        name: poi.name || '',
        address: poi.address || '',
        longitude: parseFloat(loc[0]) || 0,
        latitude: parseFloat(loc[1]) || 0,
        city: poi.cityname || '',
        type: poi.type || ''
      }
    })
  })
}

/**
 * 输入提示（自动补全）
 * @param {Object} opts
 * @param {string} opts.keywords - 输入关键字
 * @param {string} [opts.city] - 城市
 * @param {string} [opts.location] - 中心点坐标 "lng,lat"
 * @returns {Promise<Array<{name, address, longitude, latitude, city}>>}
 */
function getInputtips(opts) {
  const data = {
    keywords: opts.keywords || '',
    city: opts.city || '',
    citylimit: opts.city ? 'true' : 'false',
    datatype: 'poi'
  }
  if (opts.location) data.location = opts.location

  return request({ url: '/v3/assistant/inputtips', data }).then((res) => {
    return (res.tips || [])
      .filter((t) => t.location)
      .map((tip) => {
        const loc = (tip.location || '').split(',')
        return {
          name: tip.name || '',
          address: tip.address || '',
          longitude: parseFloat(loc[0]) || 0,
          latitude: parseFloat(loc[1]) || 0,
          city: tip.city || (Array.isArray(tip.city) ? tip.city[0] : '')
        }
      })
  })
}

/**
 * 逆地理编码（坐标 → 地址）
 * @param {number} longitude
 * @param {number} latitude
 * @returns {Promise<{name, address, city}>}
 */
function regeo(longitude, latitude) {
  return request({
    url: '/v3/geocode/regeo',
    data: {
      location: longitude + ',' + latitude,
      extensions: 'base',
      radius: 1000
    }
  }).then((data) => {
    const regeoData = data.regeocode || {}
    const addr = regeoData.addressComponent || {}
    return {
      name: (regeoData.formatted_address || '').replace(/^.*?[省市区县]/, '') || '',
      address: regeoData.formatted_address || '',
      city: addr.city || addr.province || ''
    }
  })
}

module.exports = {
  getRegeo,
  getLocation,
  getPoiAround,
  getInputtips,
  regeo
}
