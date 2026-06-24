const api = require('../../utils/api')

function formatDate(value) {
  if (!value) return '待定'
  return value.slice(0, 10)
}

Page({
  data: {
    id: null,
    activity: null,
    tempGroupQrUrl: '',
    form: {
      nickname: '',
      phone: '',
      experienceLevel: '',
      hasInsurance: false
    }
  },

  onLoad(options) {
    this.setData({ id: options.id })
    this.loadActivity()
  },

  async loadActivity() {
    try {
      const activity = await api.activity(this.data.id)
      this.setData({
        activity: {
          ...activity,
          startTimeText: formatDate(activity.startTime),
          endTimeText: formatDate(activity.endTime)
        }
      })
    } catch (error) {
      wx.showToast({ title: error.message, icon: 'none' })
    }
  },

  onInput(event) {
    this.setData({
      [`form.${event.currentTarget.dataset.field}`]: event.detail.value
    })
  },

  async ensureLogin() {
    if (wx.getStorageSync('token')) return true
    const loginResult = await wx.login()
    const response = await api.login({ code: loginResult.code })
    wx.setStorageSync('token', response.token)
    wx.setStorageSync('user', response.user)
    return true
  },

  async signup() {
    try {
      await this.ensureLogin()
      const result = await api.signup(this.data.id, this.data.form)
      this.setData({ tempGroupQrUrl: result.tempGroupQrUrl || '' })
      wx.showToast({ title: '报名成功', icon: 'success' })
    } catch (error) {
      wx.showToast({ title: error.message, icon: 'none' })
    }
  },

  shareActivity() {
    wx.showShareMenu({ withShareTicket: true })
    wx.showToast({ title: '请使用右上角分享', icon: 'none' })
  },

  goSettlement() {
    wx.navigateTo({ url: `/pages/activity/settlement?id=${this.data.id}` })
  },

  onShareAppMessage() {
    return {
      title: this.data.activity?.title || '三人成行活动',
      path: `/pages/activity/detail?id=${this.data.id}`,
      imageUrl: this.data.activity?.coverUrl || ''
    }
  }
})
