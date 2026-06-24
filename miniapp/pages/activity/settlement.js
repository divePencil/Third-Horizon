const api = require('../../utils/api')

function yuan(cents) {
  return ((cents || 0) / 100).toFixed(2)
}

Page({
  data: {
    id: null,
    members: [],
    expenses: [],
    settlement: null,
    selectedMap: {},
    form: {
      title: '',
      amount: '',
      note: ''
    }
  },

  onLoad(options) {
    this.setData({ id: options.id })
    this.ensureLogin().then(() => this.load())
  },

  async ensureLogin() {
    if (wx.getStorageSync('token')) return
    const loginResult = await wx.login()
    const response = await api.login({ code: loginResult.code })
    wx.setStorageSync('token', response.token)
    wx.setStorageSync('user', response.user)
  },

  async load() {
    try {
      const [members, expenses, settlement] = await Promise.all([
        api.members(this.data.id),
        api.expenses(this.data.id),
        api.settlement(this.data.id)
      ])
      const selectedMap = {}
      members.forEach((member) => {
        selectedMap[member.userId] = true
      })
      this.setData({
        members,
        selectedMap,
        expenses: expenses.map((item) => ({
          ...item,
          amountText: yuan(item.expense.amountCents)
        })),
        settlement: settlement ? {
          ...settlement,
          items: settlement.items.map((item) => ({ ...item, amountText: yuan(item.amountCents) }))
        } : null
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

  onShareChange(event) {
    const values = event.detail.value.map((value) => Number(value))
    const selectedMap = {}
    this.data.members.forEach((member) => {
      selectedMap[member.userId] = values.includes(member.userId)
    })
    this.setData({ selectedMap })
  },

  async submitExpense() {
    try {
      const shareUserIds = Object.keys(this.data.selectedMap)
        .filter((userId) => this.data.selectedMap[userId])
        .map((userId) => Number(userId))
      await api.createExpense(this.data.id, {
        title: this.data.form.title,
        amountCents: Math.round(Number(this.data.form.amount || 0) * 100),
        note: this.data.form.note,
        shareUserIds
      })
      this.setData({ form: { title: '', amount: '', note: '' } })
      await this.load()
      wx.showToast({ title: '已保存', icon: 'success' })
    } catch (error) {
      wx.showToast({ title: error.message, icon: 'none' })
    }
  },

  async generate() {
    try {
      const settlement = await api.generateSettlement(this.data.id)
      this.setData({
        settlement: {
          ...settlement,
          items: settlement.items.map((item) => ({ ...item, amountText: yuan(item.amountCents) }))
        }
      })
      wx.showToast({ title: '已生成', icon: 'success' })
    } catch (error) {
      wx.showToast({ title: error.message, icon: 'none' })
    }
  }
})
