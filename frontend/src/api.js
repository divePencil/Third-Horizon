const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    }
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'request failed' }))
    const requestError = new Error(error.message || 'request failed')
    requestError.status = response.status
    throw requestError
  }

  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return null
  }

  return response.json()
}

function authHeaders(token) {
  return { Authorization: `Bearer ${token}` }
}

export const publicApi = {
  activities: () => request('/api/public/activities'),
  albums: () => request('/api/public/albums'),
  albumMedia: (id) => request(`/api/public/albums/${id}/media`),
  articles: () => request('/api/public/articles'),
  joinInfo: () => request('/api/public/join-info'),
  activityPublishConfig: () => request('/api/public/activity-publish-config'),
  signup: (payload) => request('/api/public/signups', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export const adminApi = {
  login: (payload) => request('/api/auth/admin/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  }),
  activities: (token, page = 0, size = 10) => request(`/api/admin/activities?page=${page}&size=${size}`, {
    headers: authHeaders(token)
  }),
  createActivity: (token, payload) => request('/api/admin/activities', {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  updateActivity: (token, id, payload) => request(`/api/admin/activities/${id}`, {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  deleteActivity: (token, id) => request(`/api/admin/activities/${id}`, {
    method: 'DELETE',
    headers: authHeaders(token)
  }),
  albums: (token, page = 0, size = 10) => request(`/api/admin/albums?page=${page}&size=${size}`, {
    headers: authHeaders(token)
  }),
  createAlbum: (token, payload) => request('/api/admin/albums', {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  updateAlbum: (token, id, payload) => request(`/api/admin/albums/${id}`, {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  albumMedia: (token, id) => request(`/api/admin/albums/${id}/media`, {
    headers: authHeaders(token)
  }),
  deleteAlbum: (token, id) => request(`/api/admin/albums/${id}`, {
    method: 'DELETE',
    headers: authHeaders(token)
  }),
  createMedia: (token, payload) => request('/api/admin/media', {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  deleteMedia: (token, id) => request(`/api/admin/media/${id}`, {
    method: 'DELETE',
    headers: authHeaders(token)
  }),
  articles: (token, page = 0, size = 10) => request(`/api/admin/articles?page=${page}&size=${size}`, {
    headers: authHeaders(token)
  }),
  joinInfo: (token) => request('/api/admin/join-info', {
    headers: authHeaders(token)
  }),
  activityPublishConfig: (token) => request('/api/admin/activity-publish-config', {
    headers: authHeaders(token)
  }),
  updateActivityPublishSetting: (token, payload) => request('/api/admin/activity-publish-config/setting', {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  createActivityOption: (token, payload) => request('/api/admin/activity-publish-config/options', {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  updateActivityOption: (token, id, payload) => request(`/api/admin/activity-publish-config/options/${id}`, {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  deleteActivityOption: (token, id) => request(`/api/admin/activity-publish-config/options/${id}`, {
    method: 'DELETE',
    headers: authHeaders(token)
  }),
  updateJoinSetting: (token, payload) => request('/api/admin/join-info/setting', {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  createJoinGroup: (token, payload) => request('/api/admin/join-info/groups', {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  updateJoinGroup: (token, id, payload) => request(`/api/admin/join-info/groups/${id}`, {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  deleteJoinGroup: (token, id) => request(`/api/admin/join-info/groups/${id}`, {
    method: 'DELETE',
    headers: authHeaders(token)
  }),
  createArticle: (token, payload) => request('/api/admin/articles', {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  updateArticle: (token, id, payload) => request(`/api/admin/articles/${id}`, {
    method: 'PUT',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  }),
  deleteArticle: (token, id) => request(`/api/admin/articles/${id}`, {
    method: 'DELETE',
    headers: authHeaders(token)
  }),
  async upload(token, file, folder = 'albums') {
    const body = new FormData()
    body.append('file', file)
    body.append('folder', folder)

    const response = await fetch(`${API_BASE}/api/admin/uploads`, {
      method: 'POST',
      headers: authHeaders(token),
      body
    })

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'upload failed' }))
      const requestError = new Error(error.message || 'upload failed')
      requestError.status = response.status
      throw requestError
    }

    return response.json()
  }
}
