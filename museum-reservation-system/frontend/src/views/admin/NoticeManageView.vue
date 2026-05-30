<template>
  <AdminLayout>
    <div class="content-card p-4 p-lg-5 mb-4">
      <div class="row align-items-end g-4">
        <div class="col-xl-6">
          <span class="status-pill mb-3">公告管理</span>
          <h1 class="section-title mb-3">公告通知管理</h1>
          <p class="text-muted-strong mb-0">维护游客首页展示的公告内容，支持按类型和启用状态筛选。</p>
        </div>
        <div class="col-xl-6">
          <form class="row g-3" @submit.prevent="loadNotices">
            <div class="col-md-5">
              <label class="form-label fw-semibold" for="filterType">公告类型</label>
              <select id="filterType" v-model="filters.type" class="form-select rounded-4">
                <option value="">全部类型</option>
                <option v-for="option in noticeTypes" :key="option.value" :value="option.value">{{ option.label }}</option>
              </select>
            </div>
            <div class="col-md-4">
              <label class="form-label fw-semibold" for="filterEnabled">启用状态</label>
              <select id="filterEnabled" v-model="filters.enabled" class="form-select rounded-4">
                <option value="">全部状态</option>
                <option value="1">已启用</option>
                <option value="0">已停用</option>
              </select>
            </div>
            <div class="col-md-3 d-grid align-self-end">
              <button class="btn btn-museum" type="submit" :disabled="loading">查询</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <ErrorAlert :message="errorMessage" />

    <div v-if="successMessage" class="alert alert-success rounded-4">{{ successMessage }}</div>

    <div class="row g-4">
      <div class="col-xl-5">
        <div class="content-card p-4 p-lg-5">
          <div class="d-flex justify-content-between align-items-center gap-3 mb-4">
            <div>
              <h2 class="h4 fw-bold mb-1">{{ editingId ? '编辑公告' : '新增公告' }}</h2>
              <div class="text-muted">{{ editingId ? '修改已有公告内容和展示状态。' : '创建一条新的游客端公告。' }}</div>
            </div>
            <button v-if="editingId" class="btn btn-sm btn-outline-secondary rounded-pill" type="button" @click="resetForm">取消编辑</button>
          </div>

          <form class="row g-4" @submit.prevent="submitForm">
            <div class="col-12">
              <label class="form-label fw-semibold" for="title">公告标题</label>
              <input
                id="title"
                v-model.trim="form.title"
                class="form-control rounded-4"
                :class="{ 'is-invalid': validationErrors.title }"
                type="text"
                placeholder="请输入公告标题"
              />
              <div class="invalid-feedback">{{ validationErrors.title }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="type">公告类型</label>
              <select id="type" v-model="form.type" class="form-select rounded-4" :class="{ 'is-invalid': validationErrors.type }">
                <option value="">请选择类型</option>
                <option v-for="option in noticeTypes" :key="option.value" :value="option.value">{{ option.label }}</option>
              </select>
              <div class="invalid-feedback">{{ validationErrors.type }}</div>
            </div>

            <div class="col-md-6">
              <label class="form-label fw-semibold" for="enabled">启用状态</label>
              <select id="enabled" v-model.number="form.enabled" class="form-select rounded-4">
                <option :value="1">已启用</option>
                <option :value="0">已停用</option>
              </select>
            </div>

            <div class="col-12">
              <label class="form-label fw-semibold" for="content">公告内容</label>
              <textarea
                id="content"
                v-model.trim="form.content"
                class="form-control rounded-4"
                :class="{ 'is-invalid': validationErrors.content }"
                rows="7"
                placeholder="请输入公告详细内容"
              ></textarea>
              <div class="invalid-feedback">{{ validationErrors.content }}</div>
            </div>

            <div class="col-12 d-flex flex-wrap gap-3">
              <button class="btn btn-museum" type="submit" :disabled="saving">
                <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
                {{ saving ? '保存中...' : editingId ? '保存修改' : '新增公告' }}
              </button>
              <button class="btn btn-outline-museum" type="button" :disabled="saving" @click="resetForm">清空表单</button>
            </div>
          </form>
        </div>
      </div>

      <div class="col-xl-7">
        <LoadingState v-if="loading" />

        <div v-else-if="notices.length" class="d-flex flex-column gap-3">
          <article v-for="notice in notices" :key="notice.id" class="content-card p-4">
            <div class="d-flex flex-column flex-lg-row justify-content-between gap-3">
              <div>
                <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                  <span class="badge rounded-pill text-bg-light">{{ getNoticeTypeLabel(notice.type) }}</span>
                  <span class="badge rounded-pill" :class="notice.enabled === 1 ? 'text-bg-success' : 'text-bg-secondary'">
                    {{ notice.enabled === 1 ? '已启用' : '已停用' }}
                  </span>
                </div>
                <h3 class="h5 fw-bold mb-2">{{ notice.title }}</h3>
                <p class="text-muted-strong text-pre-line mb-3">{{ notice.content }}</p>
                <div class="small text-muted">
                  创建：{{ formatDateTime(notice.createdAt) }} / 更新：{{ formatDateTime(notice.updatedAt) }}
                </div>
              </div>
              <div class="d-flex flex-lg-column gap-2 align-self-start">
                <button class="btn btn-sm btn-outline-museum" type="button" @click="startEdit(notice)">编辑</button>
                <button class="btn btn-sm btn-outline-secondary" type="button" @click="toggleEnabled(notice)">
                  {{ notice.enabled === 1 ? '停用' : '启用' }}
                </button>
                <button class="btn btn-sm btn-outline-danger" type="button" @click="removeNotice(notice)">删除</button>
              </div>
            </div>
          </article>
        </div>

        <EmptyState v-else text="暂无公告数据，请新增公告或调整筛选条件" />
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import EmptyState from '../../components/EmptyState.vue'
import ErrorAlert from '../../components/ErrorAlert.vue'
import LoadingState from '../../components/LoadingState.vue'
import AdminLayout from '../../layouts/AdminLayout.vue'
import { createAdminNotice, deleteAdminNotice, listAdminNotices, updateAdminNotice } from '../../api/admin'
import { formatDateTime } from '../../utils/format'

const noticeTypes = [
  { value: 'NORMAL', label: '普通通知' },
  { value: 'EXHIBITION', label: '展览活动' },
  { value: 'CLOSE', label: '闭馆通知' },
  { value: 'RULE', label: '参观规则' }
]

const filters = reactive({
  type: '',
  enabled: ''
})

const form = reactive({
  title: '',
  content: '',
  type: 'NORMAL',
  enabled: 1
})

const validationErrors = reactive({
  title: '',
  content: '',
  type: ''
})

const notices = ref([])
const editingId = ref(null)
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

onMounted(() => {
  loadNotices()
})

async function loadNotices() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params = {}
    if (filters.type) {
      params.type = filters.type
    }
    if (filters.enabled !== '') {
      params.enabled = Number(filters.enabled)
    }
    notices.value = await listAdminNotices(params)
  } catch (error) {
    errorMessage.value = error.message
    notices.value = []
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  errorMessage.value = ''
  successMessage.value = ''
  if (!validateForm()) {
    return
  }

  saving.value = true
  try {
    const data = buildRequestData()
    if (editingId.value) {
      await updateAdminNotice(editingId.value, data)
      successMessage.value = '公告修改成功'
    } else {
      await createAdminNotice(data)
      successMessage.value = '公告新增成功'
    }
    resetForm()
    await loadNotices()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

function startEdit(notice) {
  editingId.value = notice.id
  form.title = notice.title || ''
  form.content = notice.content || ''
  form.type = notice.type || 'NORMAL'
  form.enabled = notice.enabled ?? 1
  clearValidation()
  errorMessage.value = ''
  successMessage.value = ''
}

async function toggleEnabled(notice) {
  errorMessage.value = ''
  successMessage.value = ''
  saving.value = true
  try {
    await updateAdminNotice(notice.id, {
      title: notice.title,
      content: notice.content,
      type: notice.type,
      enabled: notice.enabled === 1 ? 0 : 1
    })
    successMessage.value = notice.enabled === 1 ? '公告已停用' : '公告已启用'
    await loadNotices()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

async function removeNotice(notice) {
  const confirmed = window.confirm(`确定要删除公告“${notice.title}”吗？`)
  if (!confirmed) {
    return
  }

  errorMessage.value = ''
  successMessage.value = ''
  saving.value = true
  try {
    await deleteAdminNotice(notice.id)
    if (editingId.value === notice.id) {
      resetForm()
    }
    successMessage.value = '公告删除成功'
    await loadNotices()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

function resetForm() {
  editingId.value = null
  form.title = ''
  form.content = ''
  form.type = 'NORMAL'
  form.enabled = 1
  clearValidation()
}

function validateForm() {
  clearValidation()

  if (!form.title) {
    validationErrors.title = '公告标题不能为空'
  }
  if (!form.content) {
    validationErrors.content = '公告内容不能为空'
  }
  if (!form.type) {
    validationErrors.type = '公告类型不能为空'
  }

  return !validationErrors.title && !validationErrors.content && !validationErrors.type
}

function clearValidation() {
  validationErrors.title = ''
  validationErrors.content = ''
  validationErrors.type = ''
}

function buildRequestData() {
  return {
    title: form.title,
    content: form.content,
    type: form.type,
    enabled: form.enabled
  }
}

function getNoticeTypeLabel(type) {
  return noticeTypes.find((item) => item.value === type)?.label || type || '-'
}
</script>
