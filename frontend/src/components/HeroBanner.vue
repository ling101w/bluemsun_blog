<template>
  <section class="hero surface-card">
    <div class="hero__content">
      <div>
        <p class="hero__badge">{{ t('hero.badge') }}</p>
        <h1>{{ t('hero.heading') }}</h1>
        <p class="text-secondary">
          {{ t('hero.subheading') }}
        </p>
        <div class="hero__actions">
          <button class="primary" @click="$emit('cta')">{{ t('hero.cta') }}</button>
          <RouterLink to="/topics/premium" class="hero__link">{{ t('hero.link') }}</RouterLink>
        </div>
      </div>
      <div class="hero__cover">
        <div class="hero__img-wrap">
          <img
            class="hero__img"
            :src="coverUrl"
            alt="阅读插画"
            loading="lazy"
            decoding="async"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { usePreferenceStore } from '@/stores/preference'
import { API_BASE_URL } from '@/config'

const fallback = 'https://www.bing.com/th?id=OHR.Default_D.jpg'
const coverUrl = ref<string>(fallback)
const preferenceStore = usePreferenceStore()
const t = (key: string) => preferenceStore.t(key)

async function loadBingCover() {
  try {
    const base = API_BASE_URL.endsWith('/') ? API_BASE_URL.slice(0, -1) : API_BASE_URL
    const resp = await fetch(`${base}/common/bing-daily`, { cache: 'no-store' })
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
    const data = await resp.json() as { success: boolean; data?: string }
    const url = data?.data
    if (data?.success && url && typeof url === 'string') {
      coverUrl.value = url
    } else {
      coverUrl.value = fallback
    }
  } catch {
    coverUrl.value = fallback
  }
}

onMounted(loadBingCover)
</script>

<style scoped>
.hero {
  padding: 2.4rem 2.8rem;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.08), rgba(59, 130, 246, 0.02));
}

.hero__content {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 2.4rem;
  align-items: center;
}

.hero__badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.9rem;
  background: rgba(37, 99, 235, 0.12);
  color: var(--primary);
  border-radius: 999px;
  font-weight: 600;
  margin-bottom: 0.8rem;
}

h1 {
  font-size: clamp(2.2rem, 4vw, 2.9rem);
  margin: 0 0 1rem;
}

.hero__actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1.4rem;
}

.hero__link {
  color: var(--primary);
  font-weight: 600;
}

.hero__cover {
  display: flex;
  justify-content: flex-end;
}

.hero__img-wrap {
  width: 100%;
  max-width: 420px;
  aspect-ratio: 16 / 10;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 18px 36px -24px rgba(15, 23, 42, 0.45);
  border: 1px solid rgba(148, 163, 184, 0.25);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08), rgba(99, 102, 241, 0.06));
}

.hero__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transform: scale(1);
  transition: transform 0.5s ease, filter 0.4s ease;
  filter: saturate(1.05) contrast(1.02);
}

@media (max-width: 900px) {
  .hero {
    padding: 2rem 1.7rem;
  }

  .hero__content {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .hero__cover {
    justify-content: center;
  }

  .hero__actions {
    justify-content: center;
  }
}
</style>

