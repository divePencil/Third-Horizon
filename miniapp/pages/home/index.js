const api = require('../../utils/api')

function formatDate(value) {
  if (!value) return '待定'
  return value.slice(0, 10)
}

Page({
  data: {
    loading: true,
    activities: []
  },

  onShow() {
    this.loadActivities()
  },

  async loadActivities() {
    this.setData({ loading: true })
    try {
      const activities = await api.activities()
      this.setData({
        activities: activities.map((item) => ({
          ...item,
          startTimeText: formatDate(item.startTime),
          endTimeText: formatDate(item.endTime)
        }))
      })
    } catch (error) {
      wx.showToast({ title: error.message, icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  openDetail(event) {
    wx.navigateTo({
      url: `/pages/activity/detail?id=${event.currentTarget.dataset.id}`
    })
  }
})
