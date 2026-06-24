const app = getApp()

function baseUrl() {
  return app.globalData.apiBase.replace(/\/$/, '')
}

function request(path, options = {}) {
  const token = wx.getStorageSync('token') || ''
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${baseUrl()}${path}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.header || {})
      },
      success(response) {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          resolve(response.data)
        } else {
          reject(new Error(response.data?.message || 'request failed'))
        }
      },
      fail: reject
    })
  })
}

function upload(filePath, folder = 'miniapp') {
  const token = wx.getStorageSync('token') || ''
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: `${baseUrl()}/api/user/uploads`,
      filePath,
      name: 'file',
      formData: { folder },
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success(response) {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          resolve(JSON.parse(response.data))
        } else {
          reject(new Error(response.data || 'upload failed'))
        }
      },
      fail: reject
    })
  })
}

module.exports = {
  request,
  upload,
  login: (payload) => request('/api/auth/wechat/login', { method: 'POST', data: payload }),
  activities: () => request('/api/public/activities'),
  activity: (id) => request(`/api/public/activities/${id}`),
  publishConfig: () => request('/api/public/activity-publish-config'),
  me: () => request('/api/user/me'),
  myActivities: () => request('/api/user/activities'),
  createActivity: (payload) => request('/api/user/activities', { method: 'POST', data: payload }),
  signup: (id, payload) => request(`/api/user/activities/${id}/signup`, { method: 'POST', data: payload }),
  members: (id) => request(`/api/user/activities/${id}/members`),
  expenses: (id) => request(`/api/user/activities/${id}/expenses`),
  createExpense: (id, payload) => request(`/api/user/activities/${id}/expenses`, { method: 'POST', data: payload }),
  settlement: (id) => request(`/api/user/activities/${id}/settlement`),
  generateSettlement: (id) => request(`/api/user/activities/${id}/settlement`, { method: 'POST' }),
  createAlbum: (id, payload) => request(`/api/user/activities/${id}/albums`, { method: 'POST', data: payload }),
  createMedia: (albumId, payload) => request(`/api/user/albums/${albumId}/media`, { method: 'POST', data: payload })
}
