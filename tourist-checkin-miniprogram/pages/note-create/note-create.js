const { request, uploadFile } = require('../../utils/api')
const { checkLogin } = require('../../utils/auth')

Page({
  data: {
    title: '',
    content: '',
    coverImage: '',
    city: '',
    tags: '',
    imageList: [],
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

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  onCityInput(e) {
    this.setData({ city: e.detail.value })
  },

  onTagsInput(e) {
    this.setData({ tags: e.detail.value })
  },

  onChooseCover() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({ coverImage: res.tempFiles[0].tempFilePath })
      }
    })
  },

  onChooseImages() {
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

  onSubmit() {
    if (this.data.submitting) return
    if (!this.data.title.trim()) {
      wx.showToast({ title: '请输入标题', icon: 'none' })
      return
    }
    if (!this.data.content.trim()) {
      wx.showToast({ title: '请输入内容', icon: 'none' })
      return
    }

    this.setData({ submitting: true })

    const imageUploads = this.data.imageList.map((filePath) => uploadFile(filePath))

    Promise.allSettled(imageUploads)
      .then((settledResults) => {
        const failedCount = settledResults.filter(r => r.status === 'rejected').length
        if (failedCount > 0) {
          wx.showToast({ title: failedCount + '张图片上传失败', icon: 'none', duration: 2000 })
        }
        const imageUrls = settledResults
          .filter((r) => r.status === 'fulfilled')
          .map((r) => r.value.data.url || r.value.data)

        let coverPromise
        if (this.data.coverImage) {
          coverPromise = uploadFile(this.data.coverImage)
            .then((r) => r.data.url || r.data)
            .catch(() => '')
        } else {
          coverPromise = Promise.resolve('')
        }

        return coverPromise.then((coverUrl) => {
          if (!coverUrl && imageUrls.length > 0) {
            coverUrl = imageUrls[0]
          }

          const tagList = this.data.tags
            ? this.data.tags.split(/[,，]/).map((t) => t.trim()).filter((t) => t)
            : []

          return request({
            url: '/travel-note',
            method: 'POST',
            data: {
              title: this.data.title,
              content: this.data.content,
              coverImage: coverUrl,
              city: this.data.city,
              tags: tagList,
              images: imageUrls
            }
          })
        })
      })
      .then(() => {
        wx.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      })
      .catch((err) => {
        console.error('Publish note failed:', err)
        wx.showToast({ title: '发布失败', icon: 'none' })
      })
      .finally(() => {
        this.setData({ submitting: false })
      })
  }
})
