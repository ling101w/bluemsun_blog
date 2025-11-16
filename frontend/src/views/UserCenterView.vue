<template>
  <div class="center">
    <section class="surface-card profile" v-if="profile">
      <div class="profile__header">
        <img :src="toAbsoluteUrl(profile.avatar) ?? defaultAvatar" alt="avatar" />
        <div class="profile__info">
          <h2>{{ profile.nickname }}</h2>
          <p class="text-secondary">{{ profile.email }} · 角色：{{ profile.role }}</p>
          <p class="text-secondary">账号状态：{{ profile.status }}</p>
          <p class="text-secondary">
            账户余额：
            <strong class="balance">¥{{ (profile.balance ?? 0).toFixed(2) }}</strong>
          </p>
        </div>
        <el-button type="primary" @click="openEdit">编辑资料</el-button>
      </div>
    </section>

    <section class="surface-card list">
      <div class="list__header">
        <h3>我的订单</h3>
      </div>
      <el-table :data="orders" border stripe size="large" v-loading="orderLoading">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="articleId" label="文章 ID" width="100" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="140">
          <template #default="scope">
            <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" />
        <el-table-column prop="expireTime" label="过期时间">
          <template #default="scope">
            {{ scope.row.expireTime ? scope.row.expireTime : '--' }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!orderLoading && !orders.length" description="暂无订单记录" />
      <el-pagination
        v-if="orders.length"
        class="pagination"
        background
        layout="prev, pager, next"
        :current-page="pageNo"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
      />
    </section>
  </div>

  <el-dialog v-model="editVisible" title="编辑个人资料" width="480px" @closed="handleEditClosed">
    <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="editForm.nickname" maxlength="32" show-word-limit placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="头像" prop="avatar">
        <div class="avatar-upload">
          <img :src="toAbsoluteUrl(editForm.avatar || profile?.avatar) || defaultAvatar" alt="avatar preview" />
          <div class="avatar-upload__controls">
            <el-upload
              ref="avatarUploadRef"
              class="avatar-upload__button"
              :show-file-list="false"
              :before-upload="handleAvatarBeforeUpload"
            >
              <el-button type="primary" :loading="avatarUploading">上传图片</el-button>
            </el-upload>
            <el-input v-model="editForm.avatar" placeholder="或粘贴网络图片地址" />
          </div>
        </div>
      </el-form-item>
      <el-form-item label="个人简介" prop="profile">
        <el-input
          v-model="editForm.profile"
          type="textarea"
          rows="4"
          maxlength="512"
          show-word-limit
          placeholder="介绍一下自己吧~"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取 消</el-button>
      <el-button type="primary" :loading="editLoading" @click="handleEditSave">保 存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadInstance, UploadProps } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { fetchMyOrders, type OrderItem } from '@/services/order'
import { uploadAvatar } from '@/services/user'
import { API_ORIGIN } from '@/config'

const userStore = useUserStore()
const defaultAvatar = 'https://www.gravatar.com/avatar/?d=identicon&s=120'

function toAbsoluteUrl(url?: string) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return `${API_ORIGIN}${url.startsWith('/') ? url : `/${url}`}`
}

const profile = computed(() => userStore.profile)
const orders = ref<OrderItem[]>([])
const orderLoading = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const avatarUploadRef = ref<UploadInstance>()
const editForm = reactive({
  nickname: '',
  avatar: '',
  profile: ''
})
const avatarUploading = ref(false)
const editRules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 32, message: '昵称长度需在 1~32 个字符之间', trigger: 'blur' }
  ],
  avatar: [
    {
      validator: (_rule, value, callback) => {
        if (value && value.length > 255) {
          callback(new Error('头像地址过长'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  profile: [
    {
      validator: (_rule, value, callback) => {
        if (value && value.length > 512) {
          callback(new Error('个人简介过长'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const res = await fetchMyOrders(pageNo.value, pageSize.value)
    if (res.success) {
      orders.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    orderLoading.value = false
  }
}

function handlePageChange(page: number) {
  pageNo.value = page
  loadOrders()
}

function statusLabel(status: string) {
  switch (status) {
    case 'PAID':
      return '已支付'
    case 'PAYING':
      return '待支付'
    case 'CREATED':
      return '已创建'
    case 'CLOSED':
      return '已关闭'
    default:
      return status ?? '--'
  }
}

function statusType(status: string) {
  switch (status) {
    case 'PAID':
      return 'success'
    case 'PAYING':
      return 'warning'
    case 'CREATED':
      return 'info'
    case 'CLOSED':
      return 'danger'
    default:
      return 'info'
  }
}

function openEdit() {
  if (!profile.value) {
    ElMessage.warning('请先登录')
    return
  }
  editForm.nickname = profile.value.nickname ?? ''
  editForm.avatar = profile.value.avatar ?? ''
  editForm.profile = profile.value.profile ?? ''
  editVisible.value = true
  nextTick(() => {
    editFormRef.value?.clearValidate()
  })
}

async function handleEditSave() {
  if (!editFormRef.value) return
  try {
    await editFormRef.value.validate()
  } catch (err) {
    return
  }
  editLoading.value = true
  try {
    await userStore.updateProfile({
      nickname: editForm.nickname,
      avatar: editForm.avatar,
      profile: editForm.profile
    })
    ElMessage.success('资料更新成功')
    editVisible.value = false
  } catch (err) {
    const message = err instanceof Error ? err.message : '更新失败'
    ElMessage.error(message)
  } finally {
    editLoading.value = false
  }
}

function handleEditClosed() {
  editFormRef.value?.resetFields()
  editLoading.value = false
  avatarUploading.value = false
  avatarUploadRef.value?.clearFiles()
}

onMounted(async () => {
  if (!userStore.profile && userStore.isLoggedIn) {
    await userStore.fetchProfile()
  }
  if (userStore.isLoggedIn) {
    loadOrders()
  }
})

const handleAvatarBeforeUpload: UploadProps['beforeUpload'] = async (rawFile) => {
  if (!rawFile) return false
  const allowedTypes = ['image/png', 'image/jpeg', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('仅支持 PNG/JPG/GIF/WebP 等图片格式')
    return false
  }
  const limitMb = 5
  if (rawFile.size / 1024 / 1024 > limitMb) {
    ElMessage.error(`头像文件不能超过 ${limitMb}MB`)
    return false
  }
  avatarUploading.value = true
  try {
    const res = await uploadAvatar(rawFile as File)
    if (!res.success) {
      throw new Error(res.message)
    }
    editForm.avatar = res.data.url
    ElMessage.success('头像上传成功')
  } catch (error) {
    const message = error instanceof Error ? error.message : '头像上传失败'
    ElMessage.error(message)
  } finally {
    avatarUploading.value = false
    avatarUploadRef.value?.clearFiles()
  }
  return false
}
</script>

<style scoped>
.center {
  display: flex;
  flex-direction: column;
  gap: 1.8rem;
}

.profile {
  padding: 1.8rem;
}

.profile__header {
  display: flex;
  align-items: center;
  gap: 1.4rem;
  margin-bottom: 1.2rem;
}

.profile__header img {
  width: 88px;
  height: 88px;
  border-radius: 50%;
}

.profile__info .balance {
  color: var(--color-primary);
  font-weight: 600;
}

.list {
  padding: 1.6rem;
}

.list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.pagination {
  margin-top: 1rem;
  align-self: flex-end;
}

@media (max-width: 768px) {
  .profile__header {
    flex-direction: column;
    align-items: flex-start;
  }
}

.avatar-upload {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.avatar-upload img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(37, 99, 235, 0.2);
}

.avatar-upload__controls {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  min-width: 220px;
}

.avatar-upload__button :deep(.el-upload) {
  display: inline-flex;
}
</style>
