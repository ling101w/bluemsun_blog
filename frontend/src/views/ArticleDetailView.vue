<template>
  <article class="detail">
    <el-skeleton v-if="loading" :rows="8" animated />
    <template v-else-if="article">
      <header class="detail__header">
        <div class="meta">
          <h1>{{ article.title }}</h1>
          <div class="meta__badges" v-if="article.pinned">
            <el-tag type="warning" size="small">平台公告</el-tag>
            <span v-if="article.pinnedTime" class="pin-time">置顶于 {{ formatDate(article.pinnedTime) }}</span>
          </div>
          <div class="meta__info">
            <span>类型：{{ article.type === 'PAID' ? '付费' : '免费' }}</span>
            <span v-if="article.publishTime">· 发布：{{ formatDate(article.publishTime) }}</span>
            <span>· 状态：{{ article.status }}</span>
            <span v-if="article.viewCount">· 浏览：{{ article.viewCount }}</span>
            <span v-if="article.likeCount">· 点赞：{{ article.likeCount }}</span>
            <span v-if="article.type === 'PAID' && article.purchased">· 已解锁</span>
          </div>
          <el-button type="primary" text :loading="articleLikeLoading" :disabled="articleLikeLoading" @click="handleLike">点赞文章</el-button>
        </div>
        <img v-if="article.coverUrl" :src="article.coverUrl" alt="cover" class="cover" />
      </header>

      <div class="detail__body">
        <div v-if="locked" class="locked-block">
          <h3>{{ t('articleDetail.lockedTitle') }}</h3>
          <p class="text-secondary">{{ t('articleDetail.lockedSubtitle') }}</p>
          <div class="locked-actions">
            <el-button type="primary" :loading="purchaseLoading" @click="handlePurchase">
              {{ t('articleDetail.lockedPrimary') }} ¥{{ article.price?.toFixed(2) ?? '0.00' }}
            </el-button>
            <el-button text :loading="articleLikeLoading" :disabled="articleLikeLoading" @click="handleLike">
              {{ t('articleDetail.lockedSecondary') }}
            </el-button>
          </div>
        </div>
        <div v-else v-html="article.contentHtml ?? renderMarkdown(article.contentMd ?? '')" />
      </div>

      <section v-if="attachments.length || isManageable" class="detail__aside surface-card">
        <div class="aside-header">
          <h3>{{ t('articleDetail.attachmentsTitle') }}</h3>
          <el-tag type="success" v-if="attachments.length">
            {{ attachments.length }} {{ t('articleDetail.attachmentsCountSuffix') }}
          </el-tag>
        </div>

        <div v-if="isManageable" class="upload-panel">
          <div class="upload-controls">
            <el-upload
              ref="attachmentUploadRef"
              class="attachment-uploader"
              drag
              action="#"
              :auto-upload="false"
              :multiple="true"
              :file-list="attachmentFileList"
              :on-change="handleAttachmentChange"
              :on-remove="handleAttachmentRemove"
              :before-upload="handleAttachmentBeforeUpload"
              :accept="acceptExtensions"
            >
              <el-icon class="attachment-uploader__icon"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择</em></div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 {{ allowedExtensions.join('/') }}，单个文件不超过 20MB
                </div>
              </template>
            </el-upload>
            <div class="upload-actions">
              <el-select v-model="uploadScope" size="small" class="scope-select">
                <el-option label="公开附件" value="PUBLIC" />
                <el-option label="仅付费可下" value="PAID_ONLY" />
              </el-select>
              <el-button type="primary" :disabled="!selectedFiles.length" :loading="uploading" @click="handleUpload">
                上传附件
              </el-button>
            </div>
          </div>
          <p class="text-secondary">{{ t('articleDetail.attachmentsTip') }}</p>
        </div>

        <el-empty v-if="!attachments.length" :description="t('articleDetail.attachmentsEmpty')" />
        <ul v-else>
          <li v-for="file in attachments" :key="file.id">
            <div class="attachment-info">
              <div>
                <span class="name">{{ file.name }}</span>
                <el-tag v-if="file.restrict" size="small" type="warning">付费可下</el-tag>
              </div>
              <span class="text-secondary" v-if="file.size">{{ formatSize(file.size) }}</span>
            </div>
            <el-button type="primary" link @click="download(file)">下载</el-button>
          </li>
        </ul>
      </section>

      <section class="detail__comment surface-card">
        <h3>评论 · {{ commentTotal }}</h3>
        <div class="comment-form">
          <el-input
            v-model="commentContent"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 8 }"
            placeholder="写下你的观点或疑问..."
          />
          <div class="comment-form__toolbar">
            <EmojiPicker @select="insertEmoji" />
            <el-button text size="small" @click="previewEmojiContent">表情预览</el-button>
          </div>
          <div class="comment-form__actions">
            <span class="text-secondary">支持 Markdown / 表情，敏感词自动拦截</span>
            <el-button type="primary" :loading="commentLoading" @click="submitComment">发布评论</el-button>
          </div>
        </div>
        <el-empty v-if="!comments.length" description="暂无评论，快来抢沙发" />
        <div class="comment-thread" v-else>
          <CommentNode
            v-for="item in comments"
            :key="item.id"
            :node="item"
            :article-id="currentArticleId()"
            :like-disabled="commentLikeLoading"
            @like="likeComment"
            @replied="loadComments"
          />
        </div>
      </section>
    </template>
    <el-empty v-else description="文章不存在或已被删除" />
  </article>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile, UploadFiles, UploadInstance, UploadProps } from 'element-plus'
import dayjs from 'dayjs'
import { fetchArticleDetail, likeArticle, trackArticleView, type ArticleDetailResponse } from '@/services/article'
import { fetchComments, submitComment as submitCommentApi, likeComment as likeCommentApi, type CommentNode as CommentNodeType } from '@/services/comment'
import { uploadAttachments, downloadAttachment } from '@/services/attachment'
import { createOrder, payOrderByBalance } from '@/services/order'
import { useUserStore } from '@/stores/user'
import { usePreferenceStore } from '@/stores/preference'
import CommentNode from '@/views/components/CommentNode.vue'
import EmojiPicker from '@/components/EmojiPicker.vue'
import { renderMarkdown } from '@/utils/markdown'
import { previewEmoji } from '@/services/comment'

interface AttachmentItem {
  id: number
  name: string
  url: string
  restrict: boolean
  size?: number
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const preferenceStore = usePreferenceStore()
const t = (key: string) => preferenceStore.t(key)

const article = ref<ArticleDetailResponse | null>(null)
const loading = ref(true)
// 评论树（后端返回树形结构，根评论 + 子评论）
const comments = ref<CommentNodeType[]>([])
const commentContent = ref('')
const commentLoading = ref(false)
const purchaseLoading = ref(false)
const articleLikeLoading = ref(false)
const commentLikeLoading = ref(false)
const uploading = ref(false)
const uploadScope = ref<'PUBLIC' | 'PAID_ONLY'>('PUBLIC')
const selectedFiles = ref<File[]>([])
const attachmentUploadRef = ref<UploadInstance>()
const attachmentFileList = ref<UploadFile[]>([])

const allowedExtensions = ['png', 'jpg', 'jpeg', 'gif', 'webp', 'pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt', 'md', 'zip']
const maxUploadSize = 20 * 1024 * 1024
const acceptExtensions = allowedExtensions.map((ext) => `.${ext}`).join(',')

const isManageable = computed(() => article.value?.canManageAttachments ?? false)
const commentTotal = computed(() => {
  const countComments = (nodes: CommentNodeType[]): number => {
    return nodes.reduce((total, node) => {
      return total + 1 + (node.children ? countComments(node.children) : 0)
    }, 0)
  }
  return countComments(comments.value)
})
const locked = computed(() => article.value?.locked ?? false)

// 统一获取当前文章 ID（优先使用文章详情中的 id，回退到路由参数）
function currentArticleId(): string {
  const byArticle = article.value?.id
  if (byArticle !== undefined && byArticle !== null) {
    return String(byArticle)
  }
  return String(route.params.id || '')
}

const attachments = computed<AttachmentItem[]>(() => {
  if (locked.value || !article.value?.attachments) return []
  try {
    const parsed = JSON.parse(article.value.attachments)
    if (Array.isArray(parsed)) {
      return parsed.map((item) => ({
        id: item.id,
        name: item.name ?? '附件',
        url: item.url,
        restrict: Boolean(item.restrict),
        size: item.size
      }))
    }
  } catch (err) {
    console.warn('附件解析失败', err)
  }
  return []
})

function formatDate(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : ''
}

function formatSize(size?: number) {
  if (!size && size !== 0) return ''
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  return `${(size / 1024 / 1024 / 1024).toFixed(1)} GB`
}

async function loadArticle() {
  const id = currentArticleId()
  if (!id) {
    ElMessage.error('文章 ID 无效')
    router.replace('/')
    return
  }
  loading.value = true
  try {
    const res = await fetchArticleDetail(id) // 文章详情（是否锁定、附件 JSON、统计等）
    if (res.success) {
      article.value = res.data
      trackArticleView(id)
    } else {
      article.value = null
      ElMessage.error(res.message)
    }
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  const id = currentArticleId()
  if (!id) return
  try {
    const res = await fetchComments(id) // 加载评论树（仅 APPROVED）
    if (res.success) {
      comments.value = Array.isArray(res.data) ? res.data : []
      console.log('评论加载成功:', comments.value.length, '条评论')
    } else {
      console.warn('评论加载失败:', res.message)
      comments.value = []
    }
  } catch (error) {
    console.error('评论加载异常:', error)
    comments.value = []
  }
}

async function handleLike() {
  if (!article.value || articleLikeLoading.value) return
  articleLikeLoading.value = true
  try {
    const res = await likeArticle(article.value.id)
    if (!res.success) {
      ElMessage.warning(res.message || '点赞失败')
      return
    }
    ElMessage.success('点赞成功')
    await loadArticle()
  } catch {
    // error message already shown globally
  } finally {
    articleLikeLoading.value = false
  }
}

async function submitComment() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再评论')
    router.push('/login')
    return
  }
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commentLoading.value = true
  try {
    const articleId = currentArticleId()
    if (!articleId) {
      ElMessage.error('文章 ID 无效，无法发表评论')
      return
    }
    await submitCommentApi({ articleId, content: commentContent.value }) // 服务内部会把字符串 ID 转为数字以适配后端 Long
    ElMessage.success('评论发布成功')
    commentContent.value = ''
    loadComments()
  } finally {
    commentLoading.value = false
  }
}

async function likeComment(id: string | number) {
  if (commentLikeLoading.value) return
  commentLikeLoading.value = true
  try {
    const res = await likeCommentApi(id)
    if (!res.success) {
      ElMessage.warning(res.message || '点赞失败')
      return
    }
    ElMessage.success('点赞成功')
    loadComments()
  } catch {
    // interceptor already displays message
  } finally {
    commentLikeLoading.value = false
  }
}

function resetFileInput() {
  selectedFiles.value = []
  attachmentFileList.value = []
  attachmentUploadRef.value?.clearFiles()
}

async function handleUpload() {
  if (!article.value || !selectedFiles.value.length) {
    return
  }
  for (const file of selectedFiles.value) {
    if (file.size > maxUploadSize) {
      ElMessage.warning(`文件 ${file.name} 过大，限制 20MB`)
      return
    }
    const ext = file.name.split('.').pop()?.toLowerCase()
    if (!ext || !allowedExtensions.includes(ext)) {
      ElMessage.warning(`文件 ${file.name} 类型不支持`)
      return
    }
  }
  uploading.value = true
  try {
    const res = await uploadAttachments(article.value.id, selectedFiles.value, uploadScope.value)
    if (!res.success) {
      throw new Error(res.message)
    }
    ElMessage.success('附件上传成功')
    resetFileInput()
    await loadArticle()
  } catch (error) {
    const message = error instanceof Error && error.message ? error.message : '附件上传失败'
    ElMessage.error(message)
  } finally {
    uploading.value = false
  }
}

function download(item: AttachmentItem) {
  if (locked.value) {
    ElMessage.warning(t('articleDetail.attachmentsDownloadWarning'))
    return
  }
  downloadAttachment(item.id).catch((error) => {
    const message = error instanceof Error && error.message ? error.message : '下载失败'
    ElMessage.error(message)
  })
}

async function handlePurchase() {
  if (!article.value) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再购买')
    router.push('/login')
    return
  }
  purchaseLoading.value = true
  try {
    const articleId = String(route.params.id || '')
    const orderRes = await createOrder({ articleId, amount: article.value.price ?? 0 })
    if (!orderRes.success) {
      throw new Error(orderRes.message)
    }
    const payRes = await payOrderByBalance(orderRes.data.orderNo)
    if (!payRes.success) {
      throw new Error(payRes.message)
    }
    ElMessage.success('支付成功，已扣除余额并解锁全文')
    await userStore.fetchProfile()
    await loadArticle()
    await loadComments()
  } catch (error) {
    const message = error instanceof Error && error.message ? error.message : '购买失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    purchaseLoading.value = false
  }
}

const previewEmojiContent = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入内容后再预览')
    return
  }
  try {
    const res = await previewEmoji({ content: commentContent.value })
    if (res.success) {
      ElMessage.info(res.data)
    }
  } catch (error) {
    console.warn('preview failed', error)
  }
}

const insertEmoji = (code: string) => {
  commentContent.value = `${commentContent.value}${code}`
}

const handleAttachmentBeforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.size > maxUploadSize) {
    ElMessage.warning(`文件 ${rawFile.name} 过大，限制 20MB`)
    return false
  }
  const ext = rawFile.name.split('.').pop()?.toLowerCase()
  if (!ext || !allowedExtensions.includes(ext)) {
    ElMessage.warning(`文件 ${rawFile.name} 类型不支持`)
    return false
  }
  return true
}

const handleAttachmentChange: UploadProps['onChange'] = (_file, files) => {
  attachmentFileList.value = files
  selectedFiles.value = files.map((item) => item.raw).filter((f): f is File => Boolean(f))
}

const handleAttachmentRemove: UploadProps['onRemove'] = (_file, files) => {
  attachmentFileList.value = files
  selectedFiles.value = files.map((item) => item.raw).filter((f): f is File => Boolean(f))
}

watch(
  () => route.params.id,
  () => {
    loadArticle()
    loadComments()
  },
  { immediate: true }
)

onMounted(() => {
  if (!userStore.profile && userStore.isLoggedIn) {
    userStore.fetchProfile()
  }
})
</script>

<style scoped>
.detail {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.detail__header {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 2rem;
  align-items: center;
}

.meta h1 {
  margin: 0 0 0.8rem;
  font-size: clamp(2rem, 4vw, 2.8rem);
}

.meta__info {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  margin-bottom: 0.8rem;
  color: var(--text-secondary);
}

.meta__badges {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.4rem;
}

.pin-time {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.cover {
  width: 100%;
  border-radius: var(--radius-lg);
  object-fit: cover;
}

.detail__body {
  background-color: var(--surface);
  border-radius: var(--radius-lg);
  padding: 2.2rem;
  line-height: 1.75;
  font-size: 1.05rem;
  box-shadow: 0 18px 36px -32px rgba(15, 23, 42, 0.48);
}

.detail__body h2 {
  margin-top: 2rem;
  margin-bottom: 1rem;
}

.locked-block {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: flex-start;
  text-align: left;
}

.locked-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.detail__aside {
  padding: 1.6rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.aside-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.upload-panel {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  padding: 1rem;
  border: 1px dashed rgba(148, 163, 184, 0.4);
  border-radius: var(--radius-md);
}

.upload-controls {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.scope-select {
  width: 140px;
}

.upload-actions {
  display: flex;
  gap: 0.8rem;
  align-items: center;
  flex-wrap: wrap;
}

.attachment-uploader {
  width: 100%;
}

.attachment-uploader__icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.detail__aside ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail__aside li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  font-size: 0.95rem;
}

.attachment-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.attachment-info .name {
  font-weight: 500;
}

.detail__comment {
  padding: 1.8rem;
  display: flex;
  flex-direction: column;
  gap: 1.4rem;
}

.comment-form {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.comment-thread {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

@media (max-width: 900px) {
  .detail__header {
    grid-template-columns: 1fr;
  }

  .detail__body {
    padding: 1.6rem;
  }

  .detail__comment {
    padding: 1.4rem;
  }
}

@media (max-width: 600px) {
  .meta__info {
    flex-direction: column;
    align-items: flex-start;
  }

  .upload-controls {
    flex-direction: column;
  }

  .scope-select,
  .upload-actions {
    width: 100%;
  }
}
</style>

