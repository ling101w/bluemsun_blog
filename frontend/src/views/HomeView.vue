<template>
  <div class="home">
    <HeroBanner @cta="handleStartWriting" />

    <section v-if="announcement" class="announcement surface-card" @click="openAnnouncement">
      <div class="announcement__header">
        <span class="announcement__badge">{{ t('home.announcementBadge') }}</span>
        <span class="announcement__time" v-if="announcement.publishTime">
          {{ t('general.publishedAt') }} {{ formatDate(announcement.publishTime) }}
        </span>
      </div>
      <h2 class="announcement__title">{{ announcement.title }}</h2>
      <p class="announcement__summary line-clamp">
        {{ announcement.summary || announcement.contentMd?.slice(0, 120) }}
      </p>
      <div class="announcement__meta">
        <span>{{ t('general.readLabel') }} {{ formatCount(announcement.viewCount) }}</span>
        <span>{{ t('general.likeLabel') }} {{ formatCount(announcement.likeCount) }}</span>
      </div>
      <el-button type="primary" text>{{ t('home.announcementCTA') }}</el-button>
    </section>

    <section class="toolbar surface-card">
      <el-input
        v-model="keyword"
        :placeholder="t('home.searchPlaceholder')"
        clearable
        @keyup.enter.native="handleSearch"
      >
        <template #append>
          <el-button type="primary" @click="handleSearch">{{ t('general.search') }}</el-button>
        </template>
      </el-input>
    </section>

    <section class="grid-two-column">
      <div class="article-list">
        <h2 class="section-title">{{ t('home.latestTitle') }}</h2>
        <el-skeleton v-if="loading" :rows="4" animated />
        <template v-else>
          <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
          <el-empty v-if="!articles.length"  :description="t('home.emptyArticles')" />
          <el-pagination
            v-else
            class="pagination"
            background
            layout="prev, pager, next"
            :total="total"
            :current-page="pageNo"
            :page-size="pageSize"
            @current-change="handlePageChange"
          />
        </template>
      </div>

      <div class="sidebar-area">
        <SidebarHighlight :title="t('home.hotListTitle')" :items="hotList" @select="gotoArticle" />
        <SidebarHighlight
          :title="t('home.creatorTitle')"
          :items="creatorList"
          @select="gotoProfile"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import HeroBanner from '@/components/HeroBanner.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import SidebarHighlight, { type SidebarItem } from '@/components/SidebarHighlight.vue'
import {
  fetchArticles,
  fetchHotArticles,
  fetchAnnouncement,
  fetchHotCreators,
  type ArticleListItem,
  type ArticleDetailResponse
} from '@/services/article'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { usePreferenceStore } from '@/stores/preference'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const preferenceStore = usePreferenceStore()
const t = (key: string) => preferenceStore.t(key)

const articles = ref<ArticleListItem[]>([])
const loading = ref(false)
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(10)
const keyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const filterType = ref<string | undefined>(typeof route.query.type === 'string' ? route.query.type : undefined)
const sortOption = ref<string | undefined>(typeof route.query.sort === 'string' ? route.query.sort : undefined)

const hotList = ref<SidebarItem[]>([])

const creatorList = ref<SidebarItem[]>([])



const announcement = ref<ArticleDetailResponse | null>(null)

async function loadArticles() {
  loading.value = true
  try {
    const params: {
      pageNo: number
      pageSize: number
      keyword?: string
      type?: string
      status?: string
    } = {
      pageNo: pageNo.value,
      pageSize: pageSize.value
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    if (filterType.value) {
      params.type = filterType.value
    }
    if (sortOption.value === 'featured') {
      params.status = 'PUBLISHED'
    }
    const res = await fetchArticles(params)
    if (res.success) {
      articles.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    loading.value = false
  }
}

async function loadHotList() {
  try {
    const res = await fetchHotArticles(10)
    if (res.success && Array.isArray(res.data)) {
      hotList.value = res.data.map((a, idx) => ({
        id: String(a.id),
        index: String(idx + 1).padStart(2, '0'),
        title: a.title,
        meta: `阅读 ${formatCount(a.viewCount)} · 点赞 ${formatCount(a.likeCount)}`
      }))
    } else {
      hotList.value = []
    }
  } catch {
    hotList.value = []
  }
}

async function loadCreators() {
  try {
    const res = await fetchHotCreators(6)
    if (res.success && Array.isArray(res.data)) {
      creatorList.value = res.data.map((creator, index) => ({
        id: String(creator.id),
        index: String.fromCharCode(65 + index),
        title: creator.nickname || `创作者 ${index + 1}`,
        meta: `文章 ${creator.articleCount} · 阅读 ${formatCount(creator.viewCount)} · 点赞 ${formatCount(creator.likeCount)}`
      }))
    } else {
      creatorList.value = []
    }
  } catch {
    creatorList.value = []
  }
}

async function loadAnnouncement() {
  try {
    const res = await fetchAnnouncement()
    if (res.success) {
      announcement.value = res.data ?? null
    } else {
      announcement.value = null
    }
  } catch {
    announcement.value = null
  }
}

function formatCount(v?: number) {
  if (v === undefined || v === null) return '0'
  const n = Number(v)
  if (n >= 10000) return (n / 1000 / 10).toFixed(1) + 'k'
  return String(n)
}

function handleSearch() {
  pageNo.value = 1
  const nextQuery: Record<string, any> = { ...route.query }
  const trimmed = keyword.value.trim()
  if (trimmed) {
    nextQuery.keyword = trimmed
  } else {
    delete nextQuery.keyword
  }
  router.push({ path: '/', query: nextQuery })
}

function handlePageChange(page: number) {
  pageNo.value = page
  loadArticles()
}

function handleStartWriting() {
  if (!userStore.isLoggedIn) {
    ElMessage.info(t('home.loginHint'))
    router.push('/login')
    return
  }
  router.push('/articles/new')
}

function gotoArticle(item: SidebarItem) {
  router.push(`/articles/${item.id}`)
}

function gotoProfile(item: SidebarItem) {
  router.push(`/user/${item.id}`)
}

function openAnnouncement() {
  if (!announcement.value) return
  router.push(`/articles/${announcement.value.id}`)
}

watch(
  () => route.query,
  (query) => {
    keyword.value = typeof query.keyword === 'string' ? query.keyword : ''
    filterType.value = typeof query.type === 'string' ? query.type : undefined
    sortOption.value = typeof query.sort === 'string' ? query.sort : undefined
    pageNo.value = 1
    loadArticles()
    loadHotList()
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  loadAnnouncement()
  loadCreators()
})

function formatDate(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : ''
}
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.announcement {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  padding: 1.8rem;
  cursor: pointer;
  border-left: 4px solid var(--primary);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.announcement:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 32px -26px rgba(37, 99, 235, 0.55);
}

.announcement__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.8rem;
}

.announcement__badge {
  padding: 0.2rem 0.9rem;
  border-radius: 999px;
  background: rgba(251, 191, 36, 0.2);
  color: #b45309;
  font-weight: 600;
  font-size: 0.85rem;
}

.announcement__time {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.announcement__title {
  margin: 0;
}

.announcement__summary {
  margin: 0;
  color: var(--text-secondary);
}

.announcement__meta {
  display: flex;
  gap: 1.2rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.line-clamp {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.toolbar {
  padding: 1rem 1.4rem;
}

.grid-two-column {
  display: grid;
  grid-template-columns: minmax(0, 2.4fr) minmax(0, 1fr);
  gap: 1.6rem;
  align-items: flex-start;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 1.4rem;
  max-height: 78vh;
  overflow-y: auto;
  padding-right: 0.6rem;
}

.article-list::-webkit-scrollbar {
  width: 6px;
}

.article-list::-webkit-scrollbar-thumb {
  background: rgba(15, 23, 42, 0.2);
  border-radius: 999px;
}

.pagination {
  align-self: flex-end;
}

.sidebar-area {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  position: sticky;
  top: 1rem;
}

@media (max-width: 1024px) {
  .grid-two-column {
    grid-template-columns: 1fr;
  }

  .article-list {
    max-height: none;
    overflow: visible;
    padding-right: 0;
  }

  .sidebar-area {
    flex-direction: row;
    overflow-x: auto;
    position: static;
  }

  .sidebar-area > * {
    min-width: 320px;
  }
}

@media (max-width: 768px) {
  .sidebar-area {
    flex-direction: column;
  }
}
</style>
