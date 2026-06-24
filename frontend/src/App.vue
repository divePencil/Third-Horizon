<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  Anchor,
  CalendarDays,
  Camera,
  CheckCircle2,
  ClipboardList,
  Compass,
  Copy,
  ImagePlus,
  LogIn,
  LogOut,
  Megaphone,
  MessageCircle,
  ShieldCheck,
  Trash2,
  UploadCloud,
  UserPlus,
  Waves
} from 'lucide-vue-next'
import { adminApi, publicApi } from './api'

const tabs = [
  { id: 'home', label: '首页' },
  { id: 'activities', label: '活动' },
  { id: 'albums', label: '相册' },
  { id: 'guides', label: '文章' },
  { id: 'admin', label: '管理' }
]

const brandStory = {
  name: '三人成行',
  englishName: 'The Third Horizon',
  tagline: '一人入海，照见自己；两人同行，照见彼此；三人成行，山海无界。',
  englishTagline: "Beyond the solo self, beyond the duo's mirror. The Third Horizon is where perspectives converge and the wild expands."
}

const activityTypes = ['自由潜', '渔猎', '背包船', '桨板', '露营', '划船']
const articleModules = ['新人指南', '组织介绍', '活动说明', '装备知识', '安全规范']
const articleMenuItems = articleModules.map((module) => ({ label: module, value: module }))
const defaultJoinInfo = () => ({
  setting: {
    title: '加入三人成行',
    subtitle: '一起看见无界山海',
    managerName: '三人成行管理员',
    managerWechatId: '请在后台维护管理员微信号',
    managerNote: '添加时备注：三人成行 + 昵称 + 想参与的活动'
  },
  groups: []
})

const defaultActivityPublishConfig = () => ({
  setting: {
    disclaimerContent: '户外及水上活动存在天气、水况、地形、装备、交通和个人身体状态等不确定风险。报名者应如实评估自身能力，确认健康状态，自行承担个人行为产生的风险，并服从组织者的安全安排。'
  },
  options: [
    { id: 'content-camping', category: 'CONTENT', label: '露营', sortOrder: 0, visible: true },
    { id: 'content-diving', category: 'CONTENT', label: '潜水', sortOrder: 1, visible: true },
    { id: 'content-boating', category: 'CONTENT', label: '划船', sortOrder: 2, visible: true },
    { id: 'content-spearfishing', category: 'CONTENT', label: '打鱼', sortOrder: 3, visible: true },
    { id: 'condition-swim', category: 'JOIN_CONDITION', label: '游泳能力', sortOrder: 0, visible: true },
    { id: 'condition-certificate', category: 'JOIN_CONDITION', label: '自由潜证书', sortOrder: 1, visible: true },
    { id: 'condition-spearfishing', category: 'JOIN_CONDITION', label: '渔猎经验', sortOrder: 2, visible: true },
    { id: 'equipment-fins', category: 'EQUIPMENT', activityType: '潜水', label: '脚蹼', sortOrder: 0, visible: true },
    { id: 'equipment-tent', category: 'EQUIPMENT', activityType: '露营', label: '帐篷', sortOrder: 1, visible: true }
  ]
})

const activeTab = ref('home')
const activeArticleModule = ref(articleModules[0])
const adminActivitiesLoaded = ref(false)
const activities = ref([])
const albums = ref([])
const articles = ref([])
const loading = ref(true)
const toast = ref('')
const viewingActivity = ref(null)
const selectedActivity = ref(null)
const selectedAlbum = ref(null)
const selectedAlbumMedia = ref([])
const albumDetailLoading = ref(false)
const showJoinPanel = ref(false)
const joinInfo = ref(defaultJoinInfo())
const activityPublishConfig = ref(defaultActivityPublishConfig())
const signedActivityQrMap = reactive({})
const storedAdminToken = localStorage.getItem('adminToken') || ''
const storedAdminExpiresAt = Number(localStorage.getItem('adminTokenExpiresAt') || 0)

const signupForm = reactive({
  activityId: null,
  nickname: '',
  wechatId: '',
  phone: '',
  emergencyContact: '',
  emergencyPhone: '',
  experienceLevel: '',
  hasInsurance: false,
  acceptDisclaimer: true,
  note: ''
})

const admin = reactive({
  token: storedAdminToken,
  auth: {
    username: localStorage.getItem('adminUsername') || 'admin',
    password: '',
    loggedIn: Boolean(storedAdminToken && storedAdminExpiresAt > Date.now()),
    expiresAt: storedAdminExpiresAt,
    loginLoading: false
  },
  view: 'activities',
  uploadUrl: '',
  activities: [],
  activityPage: 0,
  activitySize: 10,
  activityTotalPages: 0,
  activityTotalElements: 0,
  albums: [],
  albumPage: 0,
  albumSize: 10,
  albumTotalPages: 0,
  albumTotalElements: 0,
  currentAlbum: null,
  albumMedia: [],
  pendingAlbumFiles: [],
  pendingAlbumPreviews: [],
  articles: [],
  joinInfoLoaded: false,
  joinGroups: [],
  activityPublishConfigLoaded: false,
  activityOptions: [],
  activityPublishSetting: {
    disclaimerContent: ''
  },
  activityOption: {
    category: 'CONTENT',
    activityType: '',
    label: '',
    sortOrder: 0,
    visible: true
  },
  editingJoinGroupId: null,
  joinSetting: {
    title: '',
    subtitle: '',
    managerName: '',
    managerWechatId: '',
    managerNote: ''
  },
  joinGroup: {
    name: '',
    description: '',
    qrUrl: '',
    sortOrder: 0,
    visible: true
  },
  articlePage: 0,
  articleSize: 10,
  articleTotalPages: 0,
  articleTotalElements: 0,
  editingActivityId: null,
  editingAlbumId: null,
  editingArticleId: null,
  activity: {
    title: '',
    location: '',
    startTime: '',
    endTime: '',
    capacity: 8,
    feeCents: 0,
    aa: true,
    feeDescription: '',
    tempGroupQrUrl: '',
    status: 'OPEN',
    visibility: 'PUBLIC',
    coverUrl: '',
    summary: '',
    noSignupLimit: true,
    requirements: '',
    safetyNotes: '',
    activityContents: [],
    joinConditions: [],
    equipmentItems: [],
    meetingLocation: '',
    meetingTime: '',
    meetingMapUrl: '',
    itinerary: '',
    destinationName: '',
    destinationMapUrl: '',
    destinationFacilities: '',
    disclaimerRequired: true
  },
  album: {
    activityId: null,
    title: '',
    location: '',
    activityDate: '',
    coverUrl: '',
    story: '',
    visibility: 'PUBLIC'
  },
  article: {
    title: '',
    category: articleModules[0],
    coverUrl: '',
    excerpt: '',
    content: '',
    visibility: 'PUBLIC',
    published: true
  }
})

const uploading = reactive({
  activityCover: false,
  albumMedia: false,
  content: false,
  joinGroupQr: false,
  tempGroupQr: false
})

const emptyActivityForm = () => ({
  title: '',
  location: '',
  startTime: '',
  endTime: '',
  capacity: 8,
    feeCents: 0,
    aa: true,
    feeDescription: '',
    tempGroupQrUrl: '',
    status: 'OPEN',
  visibility: 'PUBLIC',
    coverUrl: '',
    summary: '',
    noSignupLimit: true,
    requirements: '',
    safetyNotes: '',
    activityContents: [],
    joinConditions: [],
    equipmentItems: [],
    meetingLocation: '',
    meetingTime: '',
    meetingMapUrl: '',
    itinerary: '',
    destinationName: '',
    destinationMapUrl: '',
    destinationFacilities: '',
    disclaimerRequired: true
  })

const emptyArticleForm = () => ({
  title: '',
  category: articleModules[0],
  coverUrl: '',
  excerpt: '',
  content: '',
  visibility: 'PUBLIC',
  published: true
})

const emptyAlbumForm = () => ({
  activityId: null,
  title: '',
  location: '',
  activityDate: '',
  coverUrl: '',
  story: '',
  visibility: 'PUBLIC'
})

const emptyJoinGroupForm = () => ({
  name: '',
  description: '',
  qrUrl: '',
  sortOrder: admin.joinGroups.length,
  visible: true
})

const emptyActivityOptionForm = () => ({
  category: 'CONTENT',
  activityType: '',
  label: '',
  sortOrder: admin.activityOptions.length,
  visible: true
})

const fallbackAlbums = [
  {
    id: 0,
    title: '夏季山海活动回顾',
    location: '公开海域',
    activityDate: '2026-06-01',
    coverUrl: 'https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=1200&q=80',
    story: '记录自由潜训练、桨板巡游、背包船探索、露营划船和活动后的安全复盘。'
  },
  {
    id: -1,
    title: '河湾露营与桨板巡游',
    location: '公开水域',
    activityDate: '2026-05-18',
    coverUrl: 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80',
    story: '从营地到水面，把轻量装备、划行节奏和伙伴照应串成一次完整周末。'
  },
  {
    id: -2,
    title: '背包船轻探路线',
    location: '山间水线',
    activityDate: '2026-04-26',
    coverUrl: 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=1200&q=80',
    story: '适合小队探索的轻量水线活动，重点记录路线、天气窗口和撤离方案。'
  }
]

const visibleActivities = computed(() => activities.value)
const visibleAlbums = computed(() => albums.value.length ? albums.value : fallbackAlbums)
const selectedArticleMenu = computed(() => articleMenuItems.find((item) => item.value === activeArticleModule.value) || articleMenuItems[0])
const moduleArticles = computed(() => articles.value.filter((article) => (article.category || '') === activeArticleModule.value))
const isAdminLoggedIn = computed(() => admin.auth.loggedIn && admin.token && (!admin.auth.expiresAt || admin.auth.expiresAt > Date.now()))
const adminSessionLabel = computed(() => {
  if (!admin.auth.expiresAt) return ''
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(admin.auth.expiresAt))
})
const upcomingActivities = computed(() => {
  const now = Date.now()
  return visibleActivities.value
    .filter((activity) => activity.startTime && new Date(activity.startTime).getTime() >= now)
    .sort((first, second) => new Date(first.startTime).getTime() - new Date(second.startTime).getTime())
})
const nextActivity = computed(() => upcomingActivities.value[0] || null)
const heroAlbumTrack = computed(() => {
  const source = visibleAlbums.value
  return source.length > 4 ? [...source, ...source] : source
})
const shouldScrollHeroAlbums = computed(() => visibleAlbums.value.length > 4)
const calendarActivities = computed(() => upcomingActivities.value.slice(1, 4))
const hasMoreCalendarActivities = computed(() => upcomingActivities.value.length > 4)
const activityContentOptions = computed(() => activityPublishConfig.value.options.filter((option) => option.category === 'CONTENT' && option.visible !== false))
const activityJoinConditionOptions = computed(() => activityPublishConfig.value.options.filter((option) => option.category === 'JOIN_CONDITION' && option.visible !== false))
const activityEquipmentOptions = computed(() => activityPublishConfig.value.options.filter((option) => option.category === 'EQUIPMENT' && option.visible !== false))
const adminActivityContentOptions = computed(() => admin.activityOptions.filter((option) => option.category === 'CONTENT'))
const adminActivityJoinConditionOptions = computed(() => admin.activityOptions.filter((option) => option.category === 'JOIN_CONDITION'))
const adminActivityEquipmentOptions = computed(() => admin.activityOptions.filter((option) => option.category === 'EQUIPMENT'))

onMounted(() => {
  if (admin.token && !isAdminLoggedIn.value) {
    logoutAdmin(false)
  }
  loadData()
})

function handleHeroAlbumWheel(event) {
  const scroller = event.currentTarget
  if (!scroller || scroller.scrollWidth <= scroller.clientWidth) return
  if (Math.abs(event.deltaX) >= Math.abs(event.deltaY)) return

  scroller.scrollLeft += event.deltaY
  event.preventDefault()
}

async function setActiveTab(tabId) {
  activeTab.value = tabId
  if (tabId === 'admin' && isAdminLoggedIn.value && admin.view === 'activities' && !adminActivitiesLoaded.value) {
    await loadAdminActivities()
    adminActivitiesLoaded.value = true
  }
}

async function loadData() {
  loading.value = true
  try {
    const [activityData, albumData, articleData] = await Promise.all([
      publicApi.activities(),
      publicApi.albums(),
      publicApi.articles()
    ])
    activities.value = activityData
    albums.value = albumData
    articles.value = articleData
    try {
      joinInfo.value = await publicApi.joinInfo()
    } catch (error) {
      joinInfo.value = defaultJoinInfo()
    }
    try {
      activityPublishConfig.value = await publicApi.activityPublishConfig()
    } catch (error) {
      activityPublishConfig.value = defaultActivityPublishConfig()
    }
  } catch (error) {
    showToast('后端暂未连接，当前显示示例内容')
  } finally {
    loading.value = false
  }
}

function showToast(message) {
  toast.value = message
  window.setTimeout(() => {
    toast.value = ''
  }, 2800)
}

async function loginAdmin() {
  if (!admin.auth.username || !admin.auth.password) {
    showToast('请输入管理员账号和密码')
    return
  }
  admin.auth.loginLoading = true
  try {
    const result = await adminApi.login({
      username: admin.auth.username,
      password: admin.auth.password
    })
    admin.token = result.token
    admin.auth.password = ''
    admin.auth.loggedIn = true
    admin.auth.expiresAt = result.expiresAt
    localStorage.setItem('adminToken', result.token)
    localStorage.setItem('adminTokenExpiresAt', String(result.expiresAt))
    localStorage.setItem('adminUsername', result.username || admin.auth.username)
    adminActivitiesLoaded.value = false
    await switchAdminView(admin.view || 'activities')
    showToast('管理员登录成功')
  } catch (error) {
    logoutAdmin(false)
    showToast(error.message)
  } finally {
    admin.auth.loginLoading = false
  }
}

function logoutAdmin(showMessage = true) {
  admin.token = ''
  admin.auth.password = ''
  admin.auth.loggedIn = false
  admin.auth.expiresAt = 0
  adminActivitiesLoaded.value = false
  admin.activities = []
  admin.albums = []
  admin.articles = []
  admin.joinInfoLoaded = false
  admin.joinGroups = []
  admin.activityPublishConfigLoaded = false
  admin.activityOptions = []
  admin.currentAlbum = null
  admin.albumMedia = []
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminTokenExpiresAt')
  if (showMessage) {
    showToast('已退出管理员登录')
  }
}

function handleAdminError(error) {
  if (error.status === 401) {
    logoutAdmin(false)
  }
  showToast(error.message)
}

function parseList(value) {
  if (Array.isArray(value)) return value
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return String(value).split(/[，,、\n]/).map((item) => item.trim()).filter(Boolean)
  }
}

function stringifyList(value) {
  return JSON.stringify(Array.isArray(value) ? value : parseList(value))
}

function dateToStartTime(value) {
  return value ? `${value}T00:00:00` : null
}

function dateToEndTime(value) {
  return value ? `${value}T23:59:59` : null
}

async function copyText(text, successMessage = '已复制') {
  try {
    await navigator.clipboard.writeText(text)
    showToast(successMessage)
  } catch (error) {
    showToast('复制失败，请手动复制')
  }
}

function openSignup(activity) {
  selectedActivity.value = activity
  signupForm.activityId = activity.id
}

function openActivityDetail(activity) {
  viewingActivity.value = activity
  activeTab.value = 'activities'
}

function closeActivityDetail() {
  viewingActivity.value = null
}

async function submitSignup() {
  if (!selectedActivity.value) {
    showToast('请先选择活动')
    return
  }
  if (selectedActivity.value.disclaimerRequired !== false && !signupForm.acceptDisclaimer) {
    showToast('请先确认免责申明')
    return
  }
  try {
    const signedActivityId = signupForm.activityId
    const result = await publicApi.signup({ ...signupForm })
    if (result?.tempGroupQrUrl) {
      signedActivityQrMap[signedActivityId] = result.tempGroupQrUrl
    }
    Object.assign(signupForm, {
      activityId: null,
      nickname: '',
      wechatId: '',
      phone: '',
      emergencyContact: '',
      emergencyPhone: '',
      experienceLevel: '',
      hasInsurance: false,
      acceptDisclaimer: true,
      note: ''
    })
    selectedActivity.value = null
    showToast('报名已提交，等待管理员确认')
  } catch (error) {
    showToast(error.message)
  }
}

function activityTempGroupQrUrl(activity) {
  if (!activity?.id) return ''
  return signedActivityQrMap[activity.id] || ''
}

async function openAlbumDetail(album) {
  activeTab.value = 'albums'
  selectedAlbum.value = album
  selectedAlbumMedia.value = []
  albumDetailLoading.value = true
  try {
    selectedAlbumMedia.value = await publicApi.albumMedia(album.id)
  } catch (error) {
    showToast(error.message)
  } finally {
    albumDetailLoading.value = false
  }
}

function closeAlbumDetail() {
  selectedAlbum.value = null
  selectedAlbumMedia.value = []
}

async function saveActivity() {
  try {
    const payload = {
      ...admin.activity,
      requirements: admin.activity.noSignupLimit ? '' : admin.activity.requirements,
      activityContents: stringifyList(admin.activity.activityContents),
      joinConditions: stringifyList(admin.activity.noSignupLimit ? [] : admin.activity.joinConditions),
      equipmentItems: stringifyList(admin.activity.equipmentItems),
      startTime: dateToStartTime(admin.activity.startTime),
      endTime: dateToEndTime(admin.activity.endTime || admin.activity.startTime),
      meetingTime: admin.activity.meetingTime || null,
      feeCents: admin.activity.aa ? 0 : admin.activity.feeCents
    }
    if (admin.editingActivityId) {
      await adminApi.updateActivity(admin.token, admin.editingActivityId, payload)
      showToast('活动已更新')
    } else {
      await adminApi.createActivity(admin.token, payload)
      showToast('活动已发布')
    }
    resetActivityForm()
    await loadAdminActivities(admin.activityPage)
    admin.view = 'activities'
    await loadData()
  } catch (error) {
    handleAdminError(error)
  }
}

async function loadAdminActivities(page = admin.activityPage) {
  if (!isAdminLoggedIn.value) return
  try {
    const result = await adminApi.activities(admin.token, page, admin.activitySize)
    admin.activities = result.content || []
    admin.activityPage = result.number || 0
    admin.activityTotalPages = result.totalPages || 0
    admin.activityTotalElements = result.totalElements || 0
  } catch (error) {
    handleAdminError(error)
  }
}

async function loadAdminArticles(page = admin.articlePage) {
  if (!isAdminLoggedIn.value) return
  try {
    const result = await adminApi.articles(admin.token, page, admin.articleSize)
    admin.articles = result.content || []
    admin.articlePage = result.number || 0
    admin.articleTotalPages = result.totalPages || 0
    admin.articleTotalElements = result.totalElements || 0
  } catch (error) {
    handleAdminError(error)
  }
}

async function loadAdminAlbums(page = admin.albumPage) {
  if (!isAdminLoggedIn.value) return
  try {
    const result = await adminApi.albums(admin.token, page, admin.albumSize)
    admin.albums = result.content || []
    admin.albumPage = result.number || 0
    admin.albumTotalPages = result.totalPages || 0
    admin.albumTotalElements = result.totalElements || 0
  } catch (error) {
    handleAdminError(error)
  }
}

async function loadAdminJoinInfo() {
  if (!isAdminLoggedIn.value) return
  try {
    const result = await adminApi.joinInfo(admin.token)
    applyAdminJoinInfo(result)
    admin.joinInfoLoaded = true
  } catch (error) {
    handleAdminError(error)
  }
}

async function loadAdminActivityPublishConfig() {
  if (!isAdminLoggedIn.value) return
  try {
    const result = await adminApi.activityPublishConfig(admin.token)
    applyAdminActivityPublishConfig(result)
    admin.activityPublishConfigLoaded = true
  } catch (error) {
    handleAdminError(error)
  }
}

function applyAdminActivityPublishConfig(result) {
  const setting = result?.setting || defaultActivityPublishConfig().setting
  Object.assign(admin.activityPublishSetting, {
    disclaimerContent: setting.disclaimerContent || ''
  })
  admin.activityOptions = result?.options || []
}

function applyAdminJoinInfo(result) {
  const setting = result?.setting || defaultJoinInfo().setting
  Object.assign(admin.joinSetting, {
    title: setting.title || '',
    subtitle: setting.subtitle || '',
    managerName: setting.managerName || '',
    managerWechatId: setting.managerWechatId || '',
    managerNote: setting.managerNote || ''
  })
  admin.joinGroups = result?.groups || []
}

async function switchAdminView(view) {
  admin.view = view
  if (!isAdminLoggedIn.value) return
  if (view === 'activities' && !adminActivitiesLoaded.value) {
    await loadAdminActivities()
    adminActivitiesLoaded.value = true
  }
  if (view === 'articles' && !admin.articleTotalElements && !admin.articles.length) {
    await loadAdminArticles()
  }
  if (view === 'albums' && !admin.albumTotalElements && !admin.albums.length) {
    await loadAdminAlbums()
  }
  if (view === 'join' && !admin.joinInfoLoaded) {
    await loadAdminJoinInfo()
  }
  if (view === 'activityConfig' && !admin.activityPublishConfigLoaded) {
    await loadAdminActivityPublishConfig()
  }
}

async function saveActivityPublishSetting() {
  try {
    const saved = await adminApi.updateActivityPublishSetting(admin.token, admin.activityPublishSetting)
    activityPublishConfig.value = {
      ...activityPublishConfig.value,
      setting: saved
    }
    await loadAdminActivityPublishConfig()
    showToast('免责申明已保存')
  } catch (error) {
    handleAdminError(error)
  }
}

async function saveActivityOption() {
  if (admin.activityOption.category === 'EQUIPMENT' && !admin.activityOption.activityType) {
    showToast('请先选择所属活动项目')
    return
  }
  try {
    const payload = {
      ...admin.activityOption,
      activityType: admin.activityOption.category === 'EQUIPMENT' ? admin.activityOption.activityType : '',
      sortOrder: admin.activityOption.sortOrder || 0,
      visible: admin.activityOption.visible !== false
    }
    await adminApi.createActivityOption(admin.token, payload)
    showToast('活动选项已创建')
    resetActivityOptionForm()
    await loadAdminActivityPublishConfig()
    await loadData()
  } catch (error) {
    handleAdminError(error)
  }
}

function resetActivityOptionForm() {
  Object.assign(admin.activityOption, emptyActivityOptionForm())
}

async function deleteActivityOption(option) {
  if (!window.confirm(`确定删除选项「${option.label}」吗？`)) {
    return
  }
  try {
    await adminApi.deleteActivityOption(admin.token, option.id)
    await loadAdminActivityPublishConfig()
    await loadData()
    showToast('活动选项已删除')
  } catch (error) {
    handleAdminError(error)
  }
}

async function saveJoinSetting() {
  try {
    const saved = await adminApi.updateJoinSetting(admin.token, admin.joinSetting)
    joinInfo.value = {
      ...joinInfo.value,
      setting: saved
    }
    await loadAdminJoinInfo()
    showToast('加入入口信息已保存')
  } catch (error) {
    handleAdminError(error)
  }
}

async function saveJoinGroup() {
  try {
    const payload = {
      ...admin.joinGroup,
      sortOrder: admin.joinGroup.sortOrder || 0,
      visible: admin.joinGroup.visible !== false
    }
    if (admin.editingJoinGroupId) {
      await adminApi.updateJoinGroup(admin.token, admin.editingJoinGroupId, payload)
      showToast('微信群已更新')
    } else {
      await adminApi.createJoinGroup(admin.token, payload)
      showToast('微信群已创建')
    }
    resetJoinGroupForm()
    await loadAdminJoinInfo()
    await loadData()
  } catch (error) {
    handleAdminError(error)
  }
}

function editJoinGroup(group) {
  admin.editingJoinGroupId = group.id
  Object.assign(admin.joinGroup, {
    name: group.name || '',
    description: group.description || '',
    qrUrl: group.qrUrl || '',
    sortOrder: group.sortOrder || 0,
    visible: group.visible !== false
  })
}

function resetJoinGroupForm() {
  admin.editingJoinGroupId = null
  Object.assign(admin.joinGroup, emptyJoinGroupForm())
}

async function deleteJoinGroup(group) {
  if (!window.confirm(`确定删除微信群「${group.name}」吗？`)) {
    return
  }
  try {
    await adminApi.deleteJoinGroup(admin.token, group.id)
    if (admin.editingJoinGroupId === group.id) {
      resetJoinGroupForm()
    }
    await loadAdminJoinInfo()
    await loadData()
    showToast('微信群已删除')
  } catch (error) {
    handleAdminError(error)
  }
}

async function uploadJoinGroupQr(event) {
  const file = event.target.files?.[0]
  if (!file) return
  uploading.joinGroupQr = true
  try {
    const result = await adminApi.upload(admin.token, file, 'join-groups')
    admin.joinGroup.qrUrl = result.url
    showToast('群二维码已上传')
  } catch (error) {
    handleAdminError(error)
  } finally {
    uploading.joinGroupQr = false
    event.target.value = ''
  }
}

async function uploadTempGroupQr(event) {
  const file = event.target.files?.[0]
  if (!file) return
  uploading.tempGroupQr = true
  try {
    const result = await adminApi.upload(admin.token, file, 'activity-groups')
    admin.activity.tempGroupQrUrl = result.url
    showToast('活动临时群二维码已上传')
  } catch (error) {
    handleAdminError(error)
  } finally {
    uploading.tempGroupQr = false
    event.target.value = ''
  }
}

function editActivity(activity) {
  admin.editingActivityId = activity.id
  admin.view = 'activityForm'
  Object.assign(admin.activity, {
    title: activity.title || '',
    location: activity.location || '',
    startTime: toDateInput(activity.startTime),
    endTime: toDateInput(activity.endTime || activity.startTime),
    capacity: activity.capacity || 8,
    feeCents: activity.feeCents || 0,
    aa: activity.aa !== false,
    feeDescription: activity.feeDescription || '',
    tempGroupQrUrl: activity.tempGroupQrUrl || '',
    status: activity.status || 'OPEN',
    visibility: activity.visibility || 'PUBLIC',
    coverUrl: activity.coverUrl || '',
    summary: activity.summary || '',
    noSignupLimit: !activity.requirements && !parseList(activity.joinConditions).length,
    requirements: activity.requirements || '',
    safetyNotes: activity.safetyNotes || '',
    activityContents: parseList(activity.activityContents),
    joinConditions: parseList(activity.joinConditions),
    equipmentItems: parseList(activity.equipmentItems),
    meetingLocation: activity.meetingLocation || '',
    meetingTime: toDatetimeLocal(activity.meetingTime),
    meetingMapUrl: activity.meetingMapUrl || '',
    itinerary: activity.itinerary || '',
    destinationName: activity.destinationName || '',
    destinationMapUrl: activity.destinationMapUrl || '',
    destinationFacilities: activity.destinationFacilities || '',
    disclaimerRequired: activity.disclaimerRequired !== false
  })
  showToast('已载入活动，可编辑后保存')
}

async function deleteActivity(activity) {
  if (!window.confirm(`确定删除活动「${activity.title}」吗？关联报名和相册记录也会删除。`)) {
    return
  }
  try {
    await adminApi.deleteActivity(admin.token, activity.id)
    if (admin.editingActivityId === activity.id) {
      resetActivityForm()
    }
    await loadAdminActivities(admin.activityPage)
    admin.view = 'activities'
    await loadData()
    showToast('活动已删除')
  } catch (error) {
    handleAdminError(error)
  }
}

function resetActivityForm() {
  admin.editingActivityId = null
  Object.assign(admin.activity, emptyActivityForm())
}

function createActivityFromAdmin() {
  resetActivityForm()
  admin.view = 'activityForm'
}

async function saveAlbum() {
  try {
    const payload = {
      ...admin.album,
      activityId: admin.album.activityId || null,
      activityDate: admin.album.activityDate || null
    }
    let savedAlbum
    if (admin.editingAlbumId) {
      savedAlbum = await adminApi.updateAlbum(admin.token, admin.editingAlbumId, payload)
      showToast('相册已更新')
    } else {
      savedAlbum = await adminApi.createAlbum(admin.token, payload)
      showToast('相册已创建')
    }
    if (admin.pendingAlbumFiles.length) {
      const uploadedMedia = await uploadFilesToAlbum(savedAlbum.id, admin.pendingAlbumFiles)
      admin.pendingAlbumFiles = []
      clearPendingAlbumPreviews()
      const firstImage = uploadedMedia.find((media) => media.type === 'IMAGE')
      if (!savedAlbum.coverUrl && firstImage) {
        savedAlbum = await adminApi.updateAlbum(admin.token, savedAlbum.id, {
          ...savedAlbum,
          coverUrl: firstImage.url
        })
      }
    }
    resetAlbumForm()
    await loadAdminAlbums(admin.albumPage)
    admin.view = 'albums'
    await loadData()
  } catch (error) {
    handleAdminError(error)
  }
}

async function editAlbum(album) {
  admin.editingAlbumId = album.id
  admin.currentAlbum = album
  admin.albumMedia = []
  admin.view = 'albumForm'
  Object.assign(admin.album, {
    activityId: album.activityId || null,
    title: album.title || '',
    location: album.location || '',
    activityDate: album.activityDate || '',
    coverUrl: album.coverUrl || '',
    story: album.story || '',
    visibility: album.visibility || 'PUBLIC'
  })
  await loadAlbumMedia(album.id)
}

async function deleteAlbum(album) {
  if (!window.confirm(`确定删除相册「${album.title}」吗？相册媒体记录也会删除。`)) {
    return
  }
  try {
    await adminApi.deleteAlbum(admin.token, album.id)
    if (admin.editingAlbumId === album.id) {
      resetAlbumForm()
    }
    await loadAdminAlbums(admin.albumPage)
    admin.view = 'albums'
    await loadData()
    showToast('相册已删除')
  } catch (error) {
    handleAdminError(error)
  }
}

function resetAlbumForm() {
  admin.editingAlbumId = null
  admin.currentAlbum = null
  admin.albumMedia = []
  admin.pendingAlbumFiles = []
  clearPendingAlbumPreviews()
  Object.assign(admin.album, emptyAlbumForm())
}

function createAlbumFromAdmin() {
  resetAlbumForm()
  admin.view = 'albumForm'
}

async function loadAlbumMedia(albumId = admin.currentAlbum?.id) {
  if (!albumId) return
  try {
    admin.albumMedia = await adminApi.albumMedia(admin.token, albumId)
  } catch (error) {
    handleAdminError(error)
  }
}

async function uploadAlbumMedia(event) {
  const files = Array.from(event.target.files || [])
  if (!files.length || !admin.currentAlbum) return
  uploading.albumMedia = true
  try {
    let sortOrder = admin.albumMedia.length
    for (const file of files) {
      const result = await adminApi.upload(admin.token, file, `albums/${admin.currentAlbum.id}`)
      await adminApi.createMedia(admin.token, {
        albumId: admin.currentAlbum.id,
        url: result.url,
        objectKey: result.objectKey,
        title: file.name,
        caption: '',
        type: file.type.startsWith('video/') ? 'VIDEO' : 'IMAGE',
        sortOrder
      })
      sortOrder += 1
    }
    await loadAlbumMedia()
    showToast('媒体已上传')
  } catch (error) {
    handleAdminError(error)
  } finally {
    uploading.albumMedia = false
    event.target.value = ''
  }
}

function selectAlbumFiles(event) {
  clearPendingAlbumPreviews()
  admin.pendingAlbumFiles = Array.from(event.target.files || [])
  admin.pendingAlbumPreviews = admin.pendingAlbumFiles.map((file) => ({
    name: file.name,
    type: file.type.startsWith('video/') ? 'VIDEO' : 'IMAGE',
    url: URL.createObjectURL(file)
  }))
}

function clearPendingAlbumPreviews() {
  admin.pendingAlbumPreviews.forEach((preview) => URL.revokeObjectURL(preview.url))
  admin.pendingAlbumPreviews = []
}

async function uploadFilesToAlbum(albumId, files) {
  const uploadedMedia = []
  let sortOrder = 0
  for (const file of files) {
    const result = await adminApi.upload(admin.token, file, `albums/${albumId}`)
    const media = await adminApi.createMedia(admin.token, {
      albumId,
      url: result.url,
      objectKey: result.objectKey,
      title: file.name,
      caption: '',
      type: file.type.startsWith('video/') ? 'VIDEO' : 'IMAGE',
      sortOrder
    })
    uploadedMedia.push(media)
    sortOrder += 1
  }
  return uploadedMedia
}

async function deleteMedia(media) {
  if (!window.confirm(`确定删除媒体「${media.title || media.id}」吗？`)) {
    return
  }
  try {
    await adminApi.deleteMedia(admin.token, media.id)
    await loadAlbumMedia()
    showToast('媒体已删除')
  } catch (error) {
    handleAdminError(error)
  }
}

async function setAlbumCover(media) {
  if (!admin.editingAlbumId || media.type !== 'IMAGE') return
  try {
    const payload = {
      ...admin.album,
      coverUrl: media.url,
      activityId: admin.album.activityId || null,
      activityDate: admin.album.activityDate || null
    }
    const savedAlbum = await adminApi.updateAlbum(admin.token, admin.editingAlbumId, payload)
    admin.album.coverUrl = savedAlbum.coverUrl || media.url
    if (admin.currentAlbum) {
      admin.currentAlbum.coverUrl = admin.album.coverUrl
    }
    await loadAdminAlbums(admin.albumPage)
    await loadData()
    showToast('相册封面已设置')
  } catch (error) {
    handleAdminError(error)
  }
}

async function saveArticle() {
  try {
    const payload = {
      ...admin.article,
      category: admin.article.category || articleModules[0]
    }
    if (admin.editingArticleId) {
      await adminApi.updateArticle(admin.token, admin.editingArticleId, payload)
      showToast('文章已更新')
    } else {
      await adminApi.createArticle(admin.token, payload)
      showToast('文章已发布')
    }
    resetArticleForm()
    await loadAdminArticles(admin.articlePage)
    admin.view = 'articles'
    await loadData()
  } catch (error) {
    handleAdminError(error)
  }
}

function editArticle(article) {
  admin.editingArticleId = article.id
  admin.view = 'articleForm'
  Object.assign(admin.article, {
    title: article.title || '',
    category: article.category || articleModules[0],
    coverUrl: article.coverUrl || '',
    excerpt: article.excerpt || '',
    content: article.content || '',
    visibility: article.visibility || 'PUBLIC',
    published: article.published !== false
  })
}

async function deleteArticle(article) {
  if (!window.confirm(`确定删除文章「${article.title}」吗？`)) {
    return
  }
  try {
    await adminApi.deleteArticle(admin.token, article.id)
    if (admin.editingArticleId === article.id) {
      resetArticleForm()
    }
    await loadAdminArticles(admin.articlePage)
    admin.view = 'articles'
    await loadData()
    showToast('文章已删除')
  } catch (error) {
    handleAdminError(error)
  }
}

function resetArticleForm() {
  admin.editingArticleId = null
  Object.assign(admin.article, emptyArticleForm())
}

function createArticleFromAdmin() {
  resetArticleForm()
  admin.view = 'articleForm'
}

async function uploadFile(event, target = 'content') {
  const file = event.target.files?.[0]
  if (!file) return
  uploading[target] = true
  try {
    const result = await adminApi.upload(admin.token, file)
    admin.uploadUrl = result.url
    if (target === 'activityCover') {
      admin.activity.coverUrl = result.url
      showToast('活动封面已上传')
    } else {
      admin.article.coverUrl = result.url
      showToast('上传成功，链接已填入文章封面字段')
    }
  } catch (error) {
    handleAdminError(error)
  } finally {
    uploading[target] = false
    event.target.value = ''
  }
}

function formatDate(value) {
  if (!value) return '时间待定'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

function formatDateOnly(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(new Date(value))
}

function formatDateRange(start, end) {
  if (!start && !end) return '日期待定'
  const startText = formatDateOnly(start)
  const endText = formatDateOnly(end || start)
  return startText === endText ? startText : `${startText} - ${endText}`
}

function formatCalendarDay(value) {
  if (!value) return '--'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  }).format(new Date(value))
}

function formatCalendarTime(value) {
  if (!value) return '时间待定'
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

function toDatetimeLocal(value) {
  if (!value) return ''
  const date = new Date(value)
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return offsetDate.toISOString().slice(0, 16)
}

function toDateInput(value) {
  if (!value) return ''
  const date = new Date(value)
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return offsetDate.toISOString().slice(0, 10)
}
</script>

<template>
  <main>
    <header class="site-header">
      <button class="brand" type="button" @click="activeTab = 'home'">
        <Waves :size="24" />
        <span>
          <strong>{{ brandStory.name }}</strong>
          <small>{{ brandStory.englishName }}</small>
        </span>
      </button>
      <nav aria-label="主导航">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          type="button"
          :class="{ active: activeTab === tab.id }"
          @click="setActiveTab(tab.id)"
        >
          {{ tab.label }}
        </button>
      </nav>
    </header>

    <section v-if="activeTab === 'home'" class="hero">
      <div class="hero-media">
        <img src="/images/hero.png" alt="三人成行山海户外活动背景" />
      </div>
      <div class="hero-copy">
        <div class="hero-main">
          <h1>{{ brandStory.name }}</h1>
          <span>{{ brandStory.tagline }}</span>
          <em>{{ brandStory.englishTagline }}</em>
          <div class="hero-actions">
            <button type="button" class="ghost" @click="activeTab = 'guides'; activeArticleModule = '新人指南'">
              <ShieldCheck :size="18" />
              新人指南
            </button>
            <button type="button" @click="showJoinPanel = true">
              <UserPlus :size="18" />
              加入组织
            </button>
          </div>
          <div class="activity-type-list" aria-label="活动类型">
            <span v-for="type in activityTypes" :key="type">{{ type }}</span>
          </div>
        </div>

        <aside class="hero-calendar" aria-label="活动日历">
          <div class="hero-calendar-heading">
            <CalendarDays :size="18" />
            <span>活动日历</span>
          </div>
          <button v-if="nextActivity" type="button" class="hero-calendar-next" @click="openActivityDetail(nextActivity)">
            <strong>{{ formatCalendarDay(nextActivity.startTime) }}</strong>
            <span>{{ nextActivity.title }}</span>
            <small>{{ formatCalendarTime(nextActivity.startTime) }} · {{ nextActivity.location || '报名后通知' }}</small>
          </button>
          <div v-if="calendarActivities.length" class="hero-calendar-list">
            <button
              v-for="activity in calendarActivities"
              :key="activity.id"
              type="button"
              @click="openActivityDetail(activity)"
            >
              <time>{{ formatCalendarDay(activity.startTime) }}</time>
              <span>{{ activity.title }}</span>
            </button>
          </div>
          <button v-if="hasMoreCalendarActivities" type="button" class="hero-calendar-more" @click="activeTab = 'activities'">
            查看更多活动
          </button>
          <div v-if="!nextActivity" class="hero-calendar-empty">暂无未过期活动</div>
        </aside>
      </div>
    </section>

    <section v-if="activeTab === 'home'" class="hero-albums-strip" aria-label="活动相册">
      <div class="hero-albums-heading">
        <Camera :size="18" />
        <span>活动相册</span>
      </div>
      <div class="hero-album-scroll" @wheel="handleHeroAlbumWheel">
        <div
          class="hero-album-marquee"
          :class="{ paused: !shouldScrollHeroAlbums }"
          :style="{ '--hero-album-count': Math.min(visibleAlbums.length, 4) }"
        >
          <article
            v-for="(album, index) in heroAlbumTrack"
            :key="`${album.id}-${index}`"
            class="hero-album-card"
            role="button"
            tabindex="0"
            @click="openAlbumDetail(album)"
            @keyup.enter="openAlbumDetail(album)"
          >
            <img :src="album.coverUrl || fallbackAlbums[0].coverUrl" :alt="album.title" />
            <div class="hero-album-info">
              <h3>{{ album.title }}</h3>
              <small>{{ album.activityDate || '日期待补充' }} · {{ album.location || '公开水域' }}</small>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section v-if="activeTab === 'home'" class="dashboard">
      <div class="principles">
        <article>
          <ShieldCheck :size="22" />
          <h3>安全优先</h3>
          <p>活动前确认健康状态、装备、天气和搭档制度，禁止单独下水。</p>
        </article>
        <article>
          <Compass :size="22" />
          <h3>山海同行</h3>
          <p>活动覆盖自由潜、渔猎、背包船、桨板、露营和划船，按水域与天气灵活组织。</p>
        </article>
        <article>
          <Anchor :size="22" />
          <h3>合规环保</h3>
          <p>遵守当地水域、营地、禁渔期、保护物种、工具限制和活动区域规定。</p>
        </article>
      </div>
    </section>

    <section v-if="activeTab === 'activities'" class="content-shell">
      <div v-if="!viewingActivity" class="section-title">
        <CalendarDays :size="22" />
        <h2>活动计划</h2>
      </div>
      <div v-if="!viewingActivity" class="activity-list">
        <article
          v-for="activity in visibleActivities"
          :key="activity.id"
          class="activity-row activity-row-action"
          role="button"
          tabindex="0"
          @click="openActivityDetail(activity)"
          @keyup.enter="openActivityDetail(activity)"
        >
          <img v-if="activity.coverUrl" class="activity-cover" :src="activity.coverUrl" :alt="activity.title" />
          <div>
            <div class="activity-title-line">
              <span class="id-pill">ID #{{ activity.id }}</span>
              <h3>{{ activity.title }}</h3>
            </div>
            <p>{{ activity.summary }}</p>
            <small>{{ formatDateRange(activity.startTime, activity.endTime) }} · {{ activity.location || '报名后通知' }}</small>
          </div>
          <button type="button" @click.stop="openActivityDetail(activity)">查看详情</button>
        </article>
        <div v-if="!visibleActivities.length" class="empty-state">
          <h3>暂无活动计划</h3>
          <p>当前数据库中还没有公开活动。管理员发布后会自动出现在这里。</p>
        </div>
      </div>
      <div v-if="viewingActivity" class="activity-detail">
        <button type="button" class="ghost" @click="closeActivityDetail">返回活动列表</button>
        <article class="activity-detail-card">
          <div class="activity-detail-hero">
            <div v-if="viewingActivity.coverUrl" class="activity-detail-cover">
              <img :src="viewingActivity.coverUrl" :alt="viewingActivity.title" />
            </div>
            <div class="activity-detail-summary">
              <div class="activity-title-line">
                <span class="id-pill">ID #{{ viewingActivity.id }}</span>
                <h2>{{ viewingActivity.title }}</h2>
              </div>
              <p>{{ viewingActivity.summary }}</p>
              <dl class="activity-detail-facts">
                <div>
                  <dt>时间</dt>
                  <dd>{{ formatDateRange(viewingActivity.startTime, viewingActivity.endTime) }}</dd>
                </div>
                <div>
                  <dt>地点</dt>
                  <dd>{{ viewingActivity.location || '报名后通知' }}</dd>
                </div>
                <div>
                  <dt>人数</dt>
                  <dd>{{ viewingActivity.capacity || '不限' }}</dd>
                </div>
                <div>
                  <dt>费用</dt>
                  <dd>{{ viewingActivity.aa !== false ? 'AA' : `¥${((viewingActivity.feeCents || 0) / 100).toFixed(2)}` }}</dd>
                </div>
              </dl>
              <button type="button" class="activity-detail-cta" @click="openSignup(viewingActivity)">报名参加</button>
            </div>
          </div>
          <div class="activity-detail-main">
            <div class="activity-detail-sections">
              <section v-if="parseList(viewingActivity.activityContents).length" class="detail-chip-section">
                <h3>活动项目</h3>
                <div class="chip-list">
                  <span v-for="item in parseList(viewingActivity.activityContents)" :key="item" class="pill">{{ item }}</span>
                </div>
              </section>
              <section v-if="parseList(viewingActivity.joinConditions).length" class="detail-chip-section">
                <h3>加入条件</h3>
                <div class="chip-list">
                  <span v-for="item in parseList(viewingActivity.joinConditions)" :key="item" class="pill">{{ item }}</span>
                </div>
              </section>
              <section v-if="parseList(viewingActivity.equipmentItems).length" class="detail-chip-section">
                <h3>装备清单</h3>
                <div class="chip-list">
                  <span v-for="item in parseList(viewingActivity.equipmentItems)" :key="item" class="pill">{{ item }}</span>
                </div>
              </section>
              <section v-if="viewingActivity.feeDescription">
                <h3>费用说明</h3>
                <p>{{ viewingActivity.feeDescription }}</p>
              </section>
              <section>
                <h3>活动临时群</h3>
                <img
                  v-if="activityTempGroupQrUrl(viewingActivity)"
                  class="detail-qr"
                  :src="activityTempGroupQrUrl(viewingActivity)"
                  alt="活动临时群二维码"
                />
                <div v-else class="locked-qr">
                  <MessageCircle :size="24" />
                  <p>报名提交后显示活动临时群二维码。</p>
                  <button type="button" class="ghost" @click="openSignup(viewingActivity)">报名参加</button>
                </div>
              </section>
              <section v-if="viewingActivity.itinerary" class="detail-wide">
                <h3>行程安排</h3>
                <p>{{ viewingActivity.itinerary }}</p>
              </section>
              <section v-if="viewingActivity.requirements">
                <h3>报名要求</h3>
                <p>{{ viewingActivity.requirements }}</p>
              </section>
              <section v-if="viewingActivity.safetyNotes">
                <h3>注意事项</h3>
                <p>{{ viewingActivity.safetyNotes }}</p>
              </section>
              <section v-if="viewingActivity.disclaimerRequired !== false" class="detail-disclaimer detail-wide">
                <h3>免责申明</h3>
                <p>{{ activityPublishConfig.setting.disclaimerContent }}</p>
              </section>
            </div>
          </div>
        </article>
      </div>
    </section>

    <section v-if="activeTab === 'albums'" class="content-shell">
      <div v-if="!selectedAlbum" class="section-title">
        <Camera :size="22" />
        <h2>活动相册</h2>
      </div>
      <div v-if="!selectedAlbum" class="album-grid">
        <article
          v-for="album in visibleAlbums"
          :key="album.id"
          class="album-card album-card-action"
          role="button"
          tabindex="0"
          @click="openAlbumDetail(album)"
          @keyup.enter="openAlbumDetail(album)"
        >
          <img :src="album.coverUrl || fallbackAlbums[0].coverUrl" :alt="album.title" />
          <div>
            <small>{{ album.activityDate || '日期待补充' }} · {{ album.location || '公开海域' }}</small>
            <h3>{{ album.title }}</h3>
            <p>{{ album.story }}</p>
            <button type="button">查看相册</button>
          </div>
        </article>
      </div>
      <div v-if="selectedAlbum" class="album-detail">
        <button type="button" class="ghost" @click="closeAlbumDetail">返回相册列表</button>
        <div class="album-detail-head">
          <div>
            <small>{{ selectedAlbum.activityDate || '日期待补充' }} · {{ selectedAlbum.location || '公开海域' }}</small>
            <h2>{{ selectedAlbum.title }}</h2>
            <p>{{ selectedAlbum.story }}</p>
          </div>
        </div>
        <div v-if="albumDetailLoading" class="empty-state">
          <h3>正在加载相册内容...</h3>
        </div>
        <div v-else-if="selectedAlbumMedia.length" class="album-media-grid">
          <article v-for="media in selectedAlbumMedia" :key="media.id" class="album-media-item">
            <img v-if="media.type === 'IMAGE'" :src="media.url" :alt="media.title || selectedAlbum.title" />
            <video v-else :src="media.url" controls />
            <p v-if="media.caption || media.title">{{ media.caption || media.title }}</p>
          </article>
        </div>
        <div v-else class="empty-state">
          <h3>暂无相册内容</h3>
          <p>管理员上传图片或视频后，会在这里展示。</p>
        </div>
      </div>
    </section>

    <section v-if="activeTab === 'guides'" class="content-shell">
      <div class="section-title">
        <ShieldCheck :size="22" />
        <h2>文章</h2>
      </div>
      <div class="guide-layout">
        <aside class="article-module-menu" aria-label="文章模块">
          <button
            v-for="item in articleMenuItems"
            :key="item.value"
            type="button"
            :class="{ active: activeArticleModule === item.value }"
            @click="activeArticleModule = item.value"
          >
            {{ item.label }}
          </button>
        </aside>
        <div class="guide-content">
          <div class="guide-content-head">
            <h3>{{ selectedArticleMenu.label }}</h3>
          </div>
          <div v-if="moduleArticles.length" class="guide-card-list">
            <article v-for="article in moduleArticles" :key="article.id" class="guide-card">
              <span class="pill">{{ article.category || '文章' }}</span>
              <h3>{{ article.title }}</h3>
              <p>{{ article.excerpt || article.content }}</p>
            </article>
          </div>
          <div v-else class="empty-state">
            <h3>暂无{{ selectedArticleMenu.label }}</h3>
            <p>在后台文章管理中新建文章，并选择对应模块后会展示在这里。</p>
          </div>
        </div>
      </div>
    </section>

    <section v-if="activeTab === 'admin'" class="content-shell admin-shell">
      <div class="section-title">
        <LogIn :size="22" />
        <h2>管理台</h2>
      </div>

      <form v-if="!isAdminLoggedIn" class="admin-login-panel" @submit.prevent="loginAdmin">
        <div>
          <h3>管理员登录</h3>
          <p>只有管理员账号可以进入活动、相册、文章和加入入口管理。</p>
        </div>
        <label class="field">
          <span>账号</span>
          <input v-model.trim="admin.auth.username" autocomplete="username" placeholder="请输入管理员账号" />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="admin.auth.password" type="password" autocomplete="current-password" placeholder="请输入管理员密码" />
        </label>
        <button type="submit" :disabled="admin.auth.loginLoading">
          <LogIn :size="18" />
          {{ admin.auth.loginLoading ? '登录中' : '登录管理台' }}
        </button>
      </form>

      <div v-else class="admin-session-bar">
        <span>已登录：{{ admin.auth.username }}<template v-if="adminSessionLabel">，有效期至 {{ adminSessionLabel }}</template></span>
        <button type="button" class="ghost" @click="logoutAdmin()">
          <LogOut :size="18" />
          退出登录
        </button>
      </div>

      <div v-if="isAdminLoggedIn" class="admin-layout">
        <aside class="admin-side-menu" aria-label="后台管理菜单">
          <button type="button" :class="{ active: admin.view === 'activities' }" @click="switchAdminView('activities')">
            <ClipboardList :size="18" />
            活动管理
          </button>
          <button type="button" :class="{ active: admin.view === 'activityConfig' }" @click="switchAdminView('activityConfig')">
            <CheckCircle2 :size="18" />
            发布配置
          </button>
          <button type="button" :class="{ active: admin.view === 'albums' }" @click="switchAdminView('albums')">
            <Camera :size="18" />
            相册管理
          </button>
          <button type="button" :class="{ active: admin.view === 'articles' }" @click="switchAdminView('articles')">
            <ImagePlus :size="18" />
            文章管理
          </button>
          <button type="button" :class="{ active: admin.view === 'join' }" @click="switchAdminView('join')">
            <UserPlus :size="18" />
            加入管理
          </button>
        </aside>

        <div class="admin-content">
          <section v-if="admin.view === 'activities'" class="admin-page">
            <div class="admin-page-head">
              <div class="panel-heading">
                <ClipboardList :size="20" />
                <h3>活动管理</h3>
              </div>
              <button type="button" @click="createActivityFromAdmin">
                <Megaphone :size="18" />
                新建活动
              </button>
            </div>
            <section class="admin-list" v-if="admin.activities.length">
              <article v-for="activity in admin.activities" :key="activity.id" class="admin-list-row with-cover">
                <div class="admin-list-cover">
                  <img v-if="activity.coverUrl" :src="activity.coverUrl" :alt="activity.title" />
                  <Megaphone v-else :size="28" />
                </div>
                <div>
                  <div class="row-meta">
                    <span class="id-pill">ID #{{ activity.id }}</span>
                    <span class="pill">{{ activity.status }}</span>
                  </div>
                  <h3>{{ activity.title }}</h3>
                  <p>{{ formatDateRange(activity.startTime, activity.endTime) }} · {{ activity.location || '地点待定' }}</p>
                </div>
                <div class="row-actions">
                  <button type="button" class="ghost" @click="editActivity(activity)">编辑</button>
                  <button type="button" class="danger" @click="deleteActivity(activity)">删除</button>
                </div>
              </article>
            </section>
            <div v-else class="empty-state">
              <h3>暂无活动</h3>
              <p>点击右上角“新建活动”发布第一条活动。</p>
            </div>
            <div class="pagination" v-if="admin.activityTotalPages > 1">
              <button type="button" class="ghost" :disabled="admin.activityPage === 0" @click="loadAdminActivities(admin.activityPage - 1)">上一页</button>
              <span>第 {{ admin.activityPage + 1 }} / {{ admin.activityTotalPages }} 页，共 {{ admin.activityTotalElements }} 条</span>
              <button type="button" class="ghost" :disabled="admin.activityPage + 1 >= admin.activityTotalPages" @click="loadAdminActivities(admin.activityPage + 1)">下一页</button>
            </div>
          </section>

          <section v-if="admin.view === 'activityForm'" class="admin-page">
            <form class="admin-panel single" @submit.prevent="saveActivity">
          <div class="panel-heading">
            <Megaphone :size="20" />
            <h3>{{ admin.editingActivityId ? '编辑活动' : '发布活动' }}</h3>
          </div>
          <label class="field">
            <span>标题</span>
            <input v-model="admin.activity.title" required />
          </label>
          <label class="field full">
            <span>简介</span>
            <textarea v-model="admin.activity.summary" rows="4" />
          </label>
          <label class="field full">
            <span>行程安排</span>
            <textarea v-model="admin.activity.itinerary" rows="5" placeholder="按时间线填写：09:00 集合；10:30 抵达；..." />
          </label>
          <label class="field">
            <span>地点</span>
            <input v-model="admin.activity.location" />
          </label>
          <label class="field">
            <span>活动开始日期</span>
            <input v-model="admin.activity.startTime" type="date" />
          </label>
          <label class="field">
            <span>活动结束日期</span>
            <input v-model="admin.activity.endTime" type="date" />
          </label>
          <label class="field">
            <span>人数上限</span>
            <input v-model.number="admin.activity.capacity" type="number" min="1" />
          </label>
          <label class="field full">
            <span>活动封面</span>
            <label class="inline-upload">
              <UploadCloud :size="20" />
              <strong>{{ uploading.activityCover ? '上传中...' : '选择图片上传' }}</strong>
              <input type="file" accept="image/*" @change="uploadFile($event, 'activityCover')" />
            </label>
            <img v-if="admin.activity.coverUrl" class="cover-preview" :src="admin.activity.coverUrl" alt="活动封面预览" />
          </label>
          <label class="check-line full">
            <input v-model="admin.activity.aa" type="checkbox" />
            <span>费用AA</span>
          </label>
          <label v-if="!admin.activity.aa" class="field">
            <span>报名费用（元）</span>
            <input :value="(admin.activity.feeCents || 0) / 100" type="number" min="0" step="0.01" @input="admin.activity.feeCents = Math.round(Number($event.target.value || 0) * 100)" />
          </label>
          <label v-if="!admin.activity.aa" class="field full">
            <span>费用说明</span>
            <textarea v-model="admin.activity.feeDescription" rows="3" />
          </label>
          <label class="field full">
            <span>活动临时群二维码</span>
            <label class="inline-upload">
              <UploadCloud :size="20" />
              <strong>{{ uploading.tempGroupQr ? '上传中...' : '选择二维码图片上传' }}</strong>
              <input type="file" accept="image/*" @change="uploadTempGroupQr" />
            </label>
            <img v-if="admin.activity.tempGroupQrUrl" class="cover-preview square-preview" :src="admin.activity.tempGroupQrUrl" alt="活动临时群二维码预览" />
          </label>
          <div class="field full">
            <span>活动项目</span>
            <div class="option-grid">
              <label v-for="option in activityContentOptions" :key="option.id || option.label" class="check-line">
                <input v-model="admin.activity.activityContents" type="checkbox" :value="option.label" />
                <span>{{ option.label }}</span>
              </label>
            </div>
          </div>
          <div class="field full">
            <span>装备清单</span>
            <div class="option-grid">
              <label v-for="option in activityEquipmentOptions" :key="option.id || option.label" class="check-line">
                <input v-model="admin.activity.equipmentItems" type="checkbox" :value="option.label" />
                <span>{{ option.activityType ? `${option.activityType} · ` : '' }}{{ option.label }}</span>
              </label>
            </div>
          </div>
          <label class="check-line full">
            <input v-model="admin.activity.noSignupLimit" type="checkbox" />
            <span>不限制报名</span>
          </label>
          <div v-if="!admin.activity.noSignupLimit" class="field full">
            <span>加入条件</span>
            <div class="option-grid">
              <label v-for="option in activityJoinConditionOptions" :key="option.id || option.label" class="check-line">
                <input v-model="admin.activity.joinConditions" type="checkbox" :value="option.label" />
                <span>{{ option.label }}</span>
              </label>
            </div>
          </div>
          <label v-if="!admin.activity.noSignupLimit" class="field full">
            <span>报名要求</span>
            <textarea v-model="admin.activity.requirements" rows="3" />
          </label>
          <label class="field full">
            <span>注意事项</span>
            <textarea v-model="admin.activity.safetyNotes" rows="3" />
          </label>
          <label class="check-line full">
            <input v-model="admin.activity.disclaimerRequired" type="checkbox" />
            <span>报名需确认免责申明</span>
          </label>
          <button type="submit">
            <CheckCircle2 :size="18" />
            {{ admin.editingActivityId ? '保存修改' : '发布活动' }}
          </button>
          <button v-if="admin.editingActivityId" type="button" class="ghost" @click="resetActivityForm">
            取消编辑
          </button>
            </form>
          </section>

          <section v-if="admin.view === 'albums'" class="admin-page">
            <div class="admin-page-head">
              <div class="panel-heading">
                <Camera :size="20" />
                <h3>相册管理</h3>
              </div>
              <button type="button" @click="createAlbumFromAdmin">
                <Camera :size="18" />
                新建相册
              </button>
            </div>
            <section class="admin-list" v-if="admin.albums.length">
              <article v-for="album in admin.albums" :key="album.id" class="admin-list-row with-cover">
                <div class="admin-list-cover">
                  <img v-if="album.coverUrl" :src="album.coverUrl" :alt="album.title" />
                  <Camera v-else :size="28" />
                </div>
                <div>
                  <div class="row-meta">
                    <span class="id-pill">ID #{{ album.id }}</span>
                    <span class="pill">{{ album.visibility }}</span>
                  </div>
                  <h3>{{ album.title }}</h3>
                  <p>{{ album.activityDate || '日期待补充' }} · {{ album.location || '地点待定' }}</p>
                </div>
                <div class="row-actions">
                  <button type="button" class="ghost" @click="editAlbum(album)">编辑</button>
                  <button type="button" class="danger" @click="deleteAlbum(album)">删除</button>
                </div>
              </article>
            </section>
            <div v-else class="empty-state">
              <h3>暂无相册</h3>
              <p>点击右上角“新建相册”发布活动相册。</p>
            </div>
            <div class="pagination" v-if="admin.albumTotalPages > 1">
              <button type="button" class="ghost" :disabled="admin.albumPage === 0" @click="loadAdminAlbums(admin.albumPage - 1)">上一页</button>
              <span>第 {{ admin.albumPage + 1 }} / {{ admin.albumTotalPages }} 页，共 {{ admin.albumTotalElements }} 条</span>
              <button type="button" class="ghost" :disabled="admin.albumPage + 1 >= admin.albumTotalPages" @click="loadAdminAlbums(admin.albumPage + 1)">下一页</button>
            </div>
          </section>

          <section v-if="admin.view === 'activityConfig'" class="admin-page activity-config-page">
            <div class="admin-page-head">
              <div class="panel-heading">
                <CheckCircle2 :size="20" />
                <h3>活动发布配置</h3>
              </div>
            </div>

            <form class="admin-panel single" @submit.prevent="saveActivityPublishSetting">
              <div class="panel-heading">
                <ShieldCheck :size="20" />
                <h3>默认免责申明</h3>
              </div>
              <label class="field full">
                <span>申明内容</span>
                <textarea v-model="admin.activityPublishSetting.disclaimerContent" rows="6" required />
              </label>
              <button type="submit">
                <CheckCircle2 :size="18" />
                保存免责申明
              </button>
            </form>

            <section v-if="admin.activityOptions.length" class="activity-option-groups">
              <div class="activity-option-group">
                <h4>活动项目</h4>
                <div class="activity-option-list">
                  <span v-for="option in adminActivityContentOptions" :key="option.id" class="activity-option-item">
                    {{ option.label }}
                    <button type="button" :title="`删除 ${option.label}`" @click="deleteActivityOption(option)">
                      <Trash2 :size="14" />
                    </button>
                  </span>
                </div>
              </div>
              <div class="activity-option-group">
                <h4>装备清单</h4>
                <div class="activity-option-list">
                  <span v-for="option in adminActivityEquipmentOptions" :key="option.id" class="activity-option-item">
                    {{ option.activityType ? `${option.activityType} · ` : '' }}{{ option.label }}
                    <button type="button" :title="`删除 ${option.label}`" @click="deleteActivityOption(option)">
                      <Trash2 :size="14" />
                    </button>
                  </span>
                </div>
              </div>
              <div class="activity-option-group">
                <h4>加入条件</h4>
                <div class="activity-option-list">
                  <span v-for="option in adminActivityJoinConditionOptions" :key="option.id" class="activity-option-item">
                    {{ option.label }}
                    <button type="button" :title="`删除 ${option.label}`" @click="deleteActivityOption(option)">
                      <Trash2 :size="14" />
                    </button>
                  </span>
                </div>
              </div>
            </section>

            <form class="admin-panel single" @submit.prevent="saveActivityOption">
              <div class="panel-heading">
                <ClipboardList :size="20" />
                <h3>新增选项</h3>
              </div>
              <label class="field">
                <span>选项类型</span>
                <select v-model="admin.activityOption.category">
                  <option value="CONTENT">活动项目</option>
                  <option value="JOIN_CONDITION">加入条件</option>
                  <option value="EQUIPMENT">装备清单</option>
                </select>
              </label>
              <label v-if="admin.activityOption.category === 'EQUIPMENT'" class="field">
                <span>所属活动项目</span>
                <select v-model="admin.activityOption.activityType" required>
                  <option value="">请选择活动项目</option>
                  <option v-for="option in adminActivityContentOptions" :key="option.id || option.label" :value="option.label">
                    {{ option.label }}
                  </option>
                </select>
              </label>
              <label class="field">
                <span>选项名称</span>
                <input v-model="admin.activityOption.label" required />
              </label>
              <label class="field">
                <span>排序</span>
                <input v-model.number="admin.activityOption.sortOrder" type="number" min="0" />
              </label>
              <label class="check-line full">
                <input v-model="admin.activityOption.visible" type="checkbox" />
                <span>在发布活动时显示</span>
              </label>
              <button type="submit">
                <CheckCircle2 :size="18" />
                新增选项
              </button>
            </form>
          </section>

          <section v-if="admin.view === 'albumForm'" class="admin-page">
            <form class="admin-panel single" @submit.prevent="saveAlbum">
              <div class="panel-heading">
                <Camera :size="20" />
                <h3>{{ admin.editingAlbumId ? '编辑相册' : '新建相册' }}</h3>
              </div>
              <label class="field">
                <span>标题</span>
                <input v-model="admin.album.title" required />
              </label>
              <label class="field">
                <span>关联活动 ID</span>
                <input v-model.number="admin.album.activityId" type="number" min="1" />
              </label>
              <label class="field">
                <span>地点</span>
                <input v-model="admin.album.location" />
              </label>
              <label class="field">
                <span>活动日期</span>
                <input v-model="admin.album.activityDate" type="date" />
              </label>
              <label class="field full">
                <span>相册故事</span>
                <textarea v-model="admin.album.story" rows="5" />
              </label>
              <label class="field full">
                <span>相册内容</span>
                <label class="inline-upload">
                  <UploadCloud :size="20" />
                  <strong>{{ admin.editingAlbumId && uploading.albumMedia ? '上传中...' : '选择多张图片/视频' }}</strong>
                  <input type="file" multiple accept="image/*,video/mp4,video/quicktime" @change="admin.editingAlbumId ? uploadAlbumMedia($event) : selectAlbumFiles($event)" />
                </label>
                <p v-if="admin.pendingAlbumFiles.length" class="file-hint">
                  已选择 {{ admin.pendingAlbumFiles.length }} 个文件，保存相册后自动上传。
                </p>
                <div v-if="!admin.editingAlbumId && admin.pendingAlbumPreviews.length" class="media-grid form-media-grid">
                  <article v-for="preview in admin.pendingAlbumPreviews" :key="preview.url" class="media-card">
                    <img v-if="preview.type === 'IMAGE'" :src="preview.url" :alt="preview.name" />
                    <video v-else :src="preview.url" controls />
                    <div>
                      <strong>{{ preview.name }}</strong>
                    </div>
                  </article>
                </div>
              </label>
              <div v-if="admin.editingAlbumId" class="field full">
                <span>已有内容</span>
                <div v-if="admin.albumMedia.length" class="media-grid form-media-grid">
                  <article v-for="media in admin.albumMedia" :key="media.id" class="media-card">
                    <img v-if="media.type === 'IMAGE'" :src="media.url" :alt="media.title || '相册图片'" />
                    <video v-else :src="media.url" controls />
                    <button type="button" class="media-delete" :title="`删除 ${media.title || media.id}`" @click="deleteMedia(media)">
                      <Trash2 :size="16" />
                    </button>
                    <div>
                      <strong>{{ media.title || media.type }}</strong>
                      <button
                        v-if="media.type === 'IMAGE'"
                        type="button"
                        class="ghost media-cover-action"
                        :class="{ active: admin.album.coverUrl === media.url }"
                        @click="setAlbumCover(media)"
                      >
                        {{ admin.album.coverUrl === media.url ? '当前封面' : '设为封面' }}
                      </button>
                    </div>
                  </article>
                </div>
                <p v-else class="file-hint">暂无图片或视频，可以点击上方上传。</p>
              </div>
              <button type="submit">
                <CheckCircle2 :size="18" />
                {{ admin.editingAlbumId ? '保存修改' : '创建相册' }}
              </button>
              <button v-if="admin.editingAlbumId" type="button" class="ghost" @click="resetAlbumForm">
                取消编辑
              </button>
            </form>
          </section>

          <section v-if="admin.view === 'articles'" class="admin-page">
            <div class="admin-page-head">
              <div class="panel-heading">
                <ImagePlus :size="20" />
                <h3>文章管理</h3>
              </div>
              <button type="button" @click="createArticleFromAdmin">
                <ImagePlus :size="18" />
                新建文章
              </button>
            </div>
            <section class="admin-list" v-if="admin.articles.length">
              <article v-for="article in admin.articles" :key="article.id" class="admin-list-row">
                <div>
                  <span class="pill">{{ article.category || '文章' }}</span>
                  <h3>{{ article.title }}</h3>
                  <p>{{ article.published ? '已发布' : '草稿' }} · {{ article.category || '未分配模块' }}</p>
                </div>
                <div class="row-actions">
                  <button type="button" class="ghost" @click="editArticle(article)">编辑</button>
                  <button type="button" class="danger" @click="deleteArticle(article)">删除</button>
                </div>
              </article>
            </section>
            <div v-else class="empty-state">
              <h3>暂无文章</h3>
              <p>点击右上角“新建文章”发布新人指南、组织介绍、活动说明、装备知识或安全规范。</p>
            </div>
            <div class="pagination" v-if="admin.articleTotalPages > 1">
              <button type="button" class="ghost" :disabled="admin.articlePage === 0" @click="loadAdminArticles(admin.articlePage - 1)">上一页</button>
              <span>第 {{ admin.articlePage + 1 }} / {{ admin.articleTotalPages }} 页，共 {{ admin.articleTotalElements }} 条</span>
              <button type="button" class="ghost" :disabled="admin.articlePage + 1 >= admin.articleTotalPages" @click="loadAdminArticles(admin.articlePage + 1)">下一页</button>
            </div>
          </section>

          <section v-if="admin.view === 'join'" class="admin-page">
            <div class="admin-page-head">
              <div class="panel-heading">
                <UserPlus :size="20" />
                <h3>加入管理</h3>
              </div>
            </div>

            <form class="admin-panel single" @submit.prevent="saveJoinSetting">
              <div class="panel-heading">
                <MessageCircle :size="20" />
                <h3>入口信息</h3>
              </div>
              <label class="field">
                <span>弹窗标题</span>
                <input v-model="admin.joinSetting.title" required />
              </label>
              <label class="field">
                <span>弹窗副标题</span>
                <input v-model="admin.joinSetting.subtitle" required />
              </label>
              <label class="field">
                <span>管理员名称</span>
                <input v-model="admin.joinSetting.managerName" required />
              </label>
              <label class="field">
                <span>管理员微信号</span>
                <input v-model="admin.joinSetting.managerWechatId" required />
              </label>
              <label class="field full">
                <span>添加备注提示</span>
                <textarea v-model="admin.joinSetting.managerNote" rows="3" />
              </label>
              <button type="submit">
                <CheckCircle2 :size="18" />
                保存入口信息
              </button>
            </form>

            <section class="admin-list" v-if="admin.joinGroups.length">
              <article v-for="group in admin.joinGroups" :key="group.id" class="admin-list-row with-cover">
                <div class="admin-list-cover">
                  <img v-if="group.qrUrl" :src="group.qrUrl" :alt="group.name" />
                  <MessageCircle v-else :size="28" />
                </div>
                <div>
                  <span class="pill">{{ group.visible ? '显示' : '隐藏' }} · 排序 {{ group.sortOrder }}</span>
                  <h3>{{ group.name }}</h3>
                  <p>{{ group.description || '暂无介绍' }}</p>
                </div>
                <div class="row-actions">
                  <button type="button" class="ghost" @click="editJoinGroup(group)">编辑</button>
                  <button type="button" class="danger" @click="deleteJoinGroup(group)">删除</button>
                </div>
              </article>
            </section>

            <form class="admin-panel single" @submit.prevent="saveJoinGroup">
              <div class="panel-heading">
                <MessageCircle :size="20" />
                <h3>{{ admin.editingJoinGroupId ? '编辑微信群' : '新增微信群' }}</h3>
              </div>
              <label class="field">
                <span>群名称</span>
                <input v-model="admin.joinGroup.name" required />
              </label>
              <label class="field">
                <span>排序</span>
                <input v-model.number="admin.joinGroup.sortOrder" type="number" min="0" />
              </label>
              <label class="field full">
                <span>群介绍</span>
                <textarea v-model="admin.joinGroup.description" rows="3" />
              </label>
              <label class="field full">
                <span>群二维码</span>
                <label class="inline-upload">
                  <UploadCloud :size="20" />
                  <strong>{{ uploading.joinGroupQr ? '上传中...' : '选择二维码图片上传' }}</strong>
                  <input type="file" accept="image/*" @change="uploadJoinGroupQr" />
                </label>
                <img v-if="admin.joinGroup.qrUrl" class="cover-preview square-preview" :src="admin.joinGroup.qrUrl" alt="群二维码预览" />
              </label>
              <label class="check-line full">
                <input v-model="admin.joinGroup.visible" type="checkbox" />
                <span>在前台加入入口中显示</span>
              </label>
              <button type="submit">
                <CheckCircle2 :size="18" />
                {{ admin.editingJoinGroupId ? '保存微信群' : '新增微信群' }}
              </button>
              <button v-if="admin.editingJoinGroupId" type="button" class="ghost" @click="resetJoinGroupForm">
                取消编辑
              </button>
            </form>
          </section>

          <section v-if="admin.view === 'articleForm'" class="admin-page">
            <form class="admin-panel single" @submit.prevent="saveArticle">
          <div class="panel-heading">
            <ImagePlus :size="20" />
            <h3>{{ admin.editingArticleId ? '编辑文章' : '发布文章' }}</h3>
          </div>
          <label class="upload-box">
            <UploadCloud :size="28" />
            <span>上传图片/视频到腾讯云 COS</span>
            <input type="file" accept="image/*,video/mp4,video/quicktime" @change="uploadFile($event, 'content')" />
          </label>
          <p v-if="admin.uploadUrl" class="upload-url">{{ admin.uploadUrl }}</p>
          <label class="field">
            <span>文章标题</span>
            <input v-model="admin.article.title" required />
          </label>
          <label class="field">
            <span>所属模块</span>
            <select v-model="admin.article.category">
              <option v-for="module in articleModules" :key="module" :value="module">{{ module }}</option>
            </select>
          </label>
          <label class="field full">
            <span>摘要</span>
            <textarea v-model="admin.article.excerpt" rows="3" />
          </label>
          <label class="field full">
            <span>正文</span>
            <textarea v-model="admin.article.content" rows="8" required />
          </label>
          <button type="submit">
            <CheckCircle2 :size="18" />
            {{ admin.editingArticleId ? '保存修改' : '发布文章' }}
          </button>
          <button v-if="admin.editingArticleId" type="button" class="ghost" @click="resetArticleForm">
            取消编辑
          </button>
            </form>
          </section>
        </div>
      </div>
    </section>

    <div v-if="selectedActivity" class="modal-backdrop" @click.self="selectedActivity = null">
      <form class="signup-modal" @submit.prevent="submitSignup">
        <h2>{{ selectedActivity.title }}</h2>
        <p>填写报名信息后，管理员会在微信群内确认。</p>
        <label class="field">
          <span>昵称</span>
          <input v-model="signupForm.nickname" required />
        </label>
        <label class="field">
          <span>微信号</span>
          <input v-model="signupForm.wechatId" />
        </label>
        <label class="field">
          <span>手机号</span>
          <input v-model="signupForm.phone" />
        </label>
        <label class="field">
          <span>紧急联系人</span>
          <input v-model="signupForm.emergencyContact" />
        </label>
        <label class="field">
          <span>紧急联系人电话</span>
          <input v-model="signupForm.emergencyPhone" />
        </label>
        <label class="field full">
          <span>户外/水上活动经验</span>
          <textarea v-model="signupForm.experienceLevel" rows="3" />
        </label>
        <label class="check-line full">
          <input v-model="signupForm.hasInsurance" type="checkbox" />
          <span>我已配置或将自行确认活动保险</span>
        </label>
        <div v-if="selectedActivity.disclaimerRequired !== false" class="field full">
          <span>免责申明</span>
          <p class="disclaimer-box">{{ activityPublishConfig.setting.disclaimerContent }}</p>
        </div>
        <label v-if="selectedActivity.disclaimerRequired !== false" class="check-line full">
          <input v-model="signupForm.acceptDisclaimer" type="checkbox" />
          <span>我已阅读并确认免责申明</span>
        </label>
        <label class="field full">
          <span>备注</span>
          <textarea v-model="signupForm.note" rows="3" />
        </label>
        <div class="modal-actions">
          <button type="button" class="ghost" @click="selectedActivity = null">取消</button>
          <button type="submit">提交报名</button>
        </div>
      </form>
    </div>

    <div v-if="showJoinPanel" class="modal-backdrop" @click.self="showJoinPanel = false">
      <section class="join-modal" aria-label="加入三人成行">
        <div class="join-modal-head">
          <div>
            <p>{{ joinInfo.setting.title }}</p>
            <h2>{{ joinInfo.setting.subtitle }}</h2>
          </div>
          <button type="button" class="ghost" @click="showJoinPanel = false">关闭</button>
        </div>

        <article class="join-manager-card">
          <div class="join-icon">
            <MessageCircle :size="24" />
          </div>
          <div>
            <h3>添加{{ joinInfo.setting.managerName }}</h3>
            <p>{{ joinInfo.setting.managerNote }}</p>
            <div class="join-copy-line">
              <strong>{{ joinInfo.setting.managerWechatId }}</strong>
              <button type="button" class="ghost" @click="copyText(joinInfo.setting.managerWechatId, '管理员微信已复制')">
                <Copy :size="16" />
                复制
              </button>
            </div>
          </div>
        </article>

        <div class="join-group-grid">
          <article v-for="group in joinInfo.groups" :key="group.id || group.name" class="join-group-card">
            <div v-if="group.qrUrl" class="join-qr">
              <img :src="group.qrUrl" :alt="group.name" />
            </div>
            <div v-else class="join-qr placeholder">
              <MessageCircle :size="30" />
              <span>群二维码待更新</span>
            </div>
            <h3>{{ group.name }}</h3>
            <p>{{ group.description }}</p>
          </article>
          <article v-if="!joinInfo.groups.length" class="join-group-card">
            <div class="join-qr placeholder">
              <MessageCircle :size="30" />
              <span>微信群待更新</span>
            </div>
            <h3>微信群入口待更新</h3>
            <p>管理员维护群二维码后会在这里展示。</p>
          </article>
        </div>
      </section>
    </div>

    <p v-if="toast" class="toast">{{ toast }}</p>
    <p v-if="loading" class="loading">正在加载活动数据...</p>
  </main>
</template>
