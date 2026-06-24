const api = require('../../utils/api')

function formatDate(value) {
  if (!value) return '待定'
  return value.slice(0, 10)
}

Page({
  data: {
    activities: []
  },

  onShow() {
    if (wx.getStorageSync('token')) {
      this.load()
    }
  },

  async login() {
    try {
      const loginResult = await wx.login()
      const response = await api.login({ code: loginResult.code })
      wx.setStorageSync('token', response.token)
      wx.setStorageSync('user', response.user)
      await this.load()
      wx.showToast({ title: '已登录', icon: 'success' })
    } catch (error) {
      wx.showToast({ title: error.message, icon: 'none' })
    }
  },

  async load() {
    const activities = await api.myActivities()
    this.setData({
      activities: activities.map((item) => ({
        ...item,
        activity: {
          ...item.activity,
          startTimeText: formatDate(item.activity.startTime),
          endTimeText: formatDate(item.activity.endTime)
        }
      }))
    })
  },

  openDetail(event) {
    wx.navigateTo({ url: `/pages/activity/detail?id=${event.currentTarget.dataset.id}` })
  }
})
