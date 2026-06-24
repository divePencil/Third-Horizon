const api = require('../../utils/api')

function toStartTime(date) {
  return date ? `${date}T00:00:00` : null
}

function toEndTime(date) {
  return date ? `${date}T23:59:59` : null
}

Page({
  data: {
    form: {
      title: '',
      startDate: '',
      endDate: '',
      summary: '',
      itinerary: '',
      requirements: '',
      safetyNotes: '',
      coverUrl: '',
      tempGroupQrUrl: '',
      aa: true,
      status: 'OPEN',
      visibility: 'PUBLIC'
    }
  },

  async ensureLogin() {
    if (wx.getStorageSync('token')) return
    const loginResult = await wx.login()
    const response = await api.login({ code: loginResult.code })
    wx.setStorageSync('token', response.token)
    wx.setStorageSync('user', response.user)
  },

  onInput(event) {
    this.setData({
      [`form.${event.currentTarget.dataset.field}`]: event.detail.value
    })
  },

  onDateChange(event) {
    this.setData({
      [`form.${event.currentTarget.dataset.field}`]: event.detail.value
    })
  },

  async chooseCover() {
    await this.ensureLogin()
    const result = await wx.chooseMedia({ count: 1, mediaType: ['image'] })
    const uploaded = await api.upload(result.tempFiles[0].tempFilePath, 'activities')
    this.setData({ 'form.coverUrl': uploaded.url })
  },

  async chooseQr() {
    await this.ensureLogin()
    const result = await wx.chooseMedia({ count: 1, mediaType: ['image'] })
    const uploaded = await api.upload(result.tempFiles[0].tempFilePath, 'activity-qrcodes')
    this.setData({ 'form.tempGroupQrUrl': uploaded.url })
  },

  async submit() {
    try {
      await this.ensureLogin()
      const payload = {
        ...this.data.form,
        startTime: toStartTime(this.data.form.startDate),
        endTime: toEndTime(this.data.form.endDate)
      }
      const activity = await api.createActivity(payload)
      wx.showToast({ title: '已发布', icon: 'success' })
      wx.navigateTo({ url: `/pages/activity/detail?id=${activity.id}` })
    } catch (error) {
      wx.showToast({ title: error.message, icon: 'none' })
    }
  }
})
