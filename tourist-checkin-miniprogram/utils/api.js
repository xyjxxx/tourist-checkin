function getBaseUrl() {
  const app = getApp()
  return (app && app.globalData ? app.globalData.baseUrl : '') + '/api'
}

function getOrigin() {
  const app = getApp()
  return (app && app.globalData ? app.globalData.baseUrl : '')
}

// 将相对路径图片URL转为绝对路径
function fixImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return getOrigin() + url
}

// 批量修复图片URL数组
function fixImageUrls(urls) {
  if (!Array.isArray(urls)) return []
  return urls.map(fixImageUrl)
}

function request(options) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    wx.request({
      url: getBaseUrl() + options.url,
      method: options.method || 'GET',
      data: options.data,
      timeout: 15000,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : '',
        ...(options.header || {})
      },
      success(res) {
        if (res.statusCode === 401) {
          if (!options.silent401) {
            wx.removeStorageSync('token')
            const _app = getApp()
            if (_app) _app.globalData.userInfo = null
            wx.reLaunch({ url: '/pages/login/login' })
          }
          reject(new Error('未登录'))
          return
        }
        if (res.statusCode === 403) {
          wx.showToast({ title: '无权限', icon: 'none' })
          reject(new Error('无权限'))
          return
        }
        if (!res.data || res.data.code !== 200) {
          wx.showToast({ title: (res.data && res.data.message) || '请求失败', icon: 'none' })
          reject(new Error((res.data && res.data.message) || '请求失败'))
          return
        }
        resolve(res.data)
      },
      fail(err) {
        wx.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    wx.uploadFile({
      url: getBaseUrl() + '/file/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success(res) {
        try {
          const data = JSON.parse(res.data)
          if (data.code !== 200) {
            wx.showToast({ title: data.message || '上传失败', icon: 'none' })
            reject(new Error(data.message))
            return
          }
          resolve(data)
        } catch (e) {
          wx.showToast({ title: '上传响应解析失败', icon: 'none' })
          reject(e)
        }
      },
      fail(err) {
        wx.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = { request, uploadFile, fixImageUrl, fixImageUrls }
