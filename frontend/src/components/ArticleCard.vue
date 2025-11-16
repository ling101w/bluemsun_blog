<template>
  <article class="card surface-card" @click="goDetail">
    <div class="card__info">
      <div class="card__badge" v-if="article.pinned">
        公告置顶
      </div>
      <h2>{{ article.title }}</h2>
      <p class="text-secondary line-clamp">{{ article.summary }}</p>
      <div class="card__meta">
        <div class="card__stat">
          <span v-if="article.publishTime">发布于 {{ formatDate(article.publishTime) }}</span>
          <span v-if="article.viewCount">· 浏览 {{ article.viewCount }}</span>
          <span v-if="article.likeCount">· 点赞 {{ article.likeCount }}</span>
          <span v-if="article.type === 'PAID'" class="paid-tag">付费 ¥{{ article.price }}</span>
        </div>
        <el-button type="primary" text size="small">查看详情</el-button>
      </div>
    </div>
    <div v-if="article.coverUrl" class="card__cover">
      <img :src="article.coverUrl" alt="cover" />
    </div>
  </article>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'

interface ArticleCardData {
  id: number
  title: string
  summary: string
  coverUrl?: string
  publishTime?: string
  viewCount?: number
  likeCount?: number
  price?: number
  type: string
  pinned?: boolean
}

const props = defineProps<{ article: ArticleCardData }>()

const router = useRouter()

function goDetail() {
  router.push(`/articles/${props.article.id}`)
}

function formatDate(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD') : ''
}
</script>

<style scoped>
.card {
  display: grid;
  grid-template-columns: 1fr 200px;
  gap: 1.2rem;
  padding: 1.2rem 1.4rem;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  align-items: center;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 36px -28px rgba(15, 23, 42, 0.55);
}

h2 {
  margin: 0 0 0.5rem;
  font-size: 1.2rem;
}

.line-clamp {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.8rem;
  gap: 0.8rem;
}

.card__stat {
  display: flex;
  gap: 0.9rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.card__badge {
  display: inline-flex;
  align-items: center;
  padding: 0.1rem 0.6rem;
  border-radius: 999px;
  background: rgba(251, 191, 36, 0.2);
  color: #b45309;
  font-size: 0.8rem;
  font-weight: 600;
  margin-bottom: 0.4rem;
}

.paid-tag {
  color: var(--primary);
  font-weight: 600;
}

.card__cover {
  height: 120px;
}

.card__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-md);
}

@media (max-width: 900px) {
  .card {
    grid-template-columns: 1fr;
  }

  .card__cover img {
    max-height: 180px;
  }

  .card__meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.6rem;
  }
}
</style>

