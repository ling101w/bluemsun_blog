<template>
  <header class="navbar" :class="{ 'navbar--scrolled': isScrolled }">
    <div class="navbar__inner">
      <RouterLink to="/" class="navbar__logo">蓝旭博客</RouterLink>

      <nav class="navbar__links" v-if="!isMobile">
        <RouterLink to="/" class="nav-link">{{ t('nav.home') }}</RouterLink>
        <RouterLink to="/articles/featured" class="nav-link">{{ t('nav.featured') }}</RouterLink>
        <RouterLink to="/ai" class="nav-link">{{ t('nav.ai') }}</RouterLink>
        <RouterLink to="/topics/premium" class="nav-link">{{ t('nav.premium') }}</RouterLink>
      </nav>

      <div class="navbar__right">
        <div class="search-box" v-if="!isMobile">
          <input v-model="keyword" type="search" :placeholder="searchPlaceholder" @keyup.enter="handleSearch" />
        </div>

        <div class="preference-switch" v-if="!isMobile">
          <button class="pref-btn" type="button" :title="themeTooltip" @click="preferenceStore.toggleTheme()">
            {{ themeIcon }}
          </button>
          <button class="pref-btn" type="button" :title="languageTooltip" @click="preferenceStore.toggleLanguage()">
            {{ languageLabel }}
          </button>
        </div>

        <RouterLink v-if="userStore.isLoggedIn" to="/articles/new" class="write-btn">{{ t('nav.write') }}</RouterLink>
        <button class="primary" v-else @click="router.push('/login')">{{ t('nav.login') }}</button>

        <div v-if="userStore.isLoggedIn" ref="userEntryRef" class="user-entry" :class="{ 'is-open': showUserMenu }">
          <button
            class="user-trigger"
            type="button"
            @click="toggleUserMenu"
            aria-haspopup="true"
            :aria-expanded="showUserMenu"
          >
            <img :src="avatarSrc" class="user-avatar" alt="avatar" />
            <span class="user-name" v-if="!isMobile">{{ userStore.profile?.nickname ?? '--' }}</span>
          </button>
          <div class="user-menu">
            <RouterLink to="/user/center" @click="closeUserMenu">{{ t('nav.profile') }}</RouterLink>
            <RouterLink v-if="userStore.isAdmin" to="/admin" @click="closeUserMenu">{{ t('nav.admin') }}</RouterLink>
            <button class="link" @click="handleLogout">{{ t('nav.logout') }}</button>
          </div>
        </div>

        <button class="menu-btn" v-if="isMobile" @click="toggleDrawer">
          <span></span><span></span><span></span>
        </button>
      </div>
    </div>

    <transition name="fade-scale">
      <div v-if="isMobile && showDrawer" class="drawer">
        <RouterLink to="/" @click="toggleDrawer">{{ t('nav.home') }}</RouterLink>
        <RouterLink to="/articles/featured" @click="toggleDrawer">{{ t('nav.featured') }}</RouterLink>
        <RouterLink to="/ai" @click="toggleDrawer">{{ t('nav.ai') }}</RouterLink>
        <RouterLink to="/topics/premium" @click="toggleDrawer">{{ t('nav.premium') }}</RouterLink>
        <RouterLink v-if="userStore.isLoggedIn" to="/articles/new" @click="toggleDrawer">{{ t('nav.write') }}</RouterLink>
        <RouterLink v-if="userStore.isLoggedIn" to="/user/center" @click="toggleDrawer">{{ t('nav.profile') }}</RouterLink>
        <RouterLink v-if="userStore.isAdmin" to="/admin" @click="toggleDrawer">{{ t('nav.admin') }}</RouterLink>
        <button v-if="userStore.isLoggedIn" class="link" @click="handleLogout">{{ t('nav.logout') }}</button>
        <button v-else class="primary" @click="router.push('/login')">{{ t('nav.login') }}</button>

        <div class="drawer__preferences">
          <button class="pref-btn" type="button" :title="themeTooltip" @click="preferenceStore.toggleTheme()">
            {{ themeIcon }}
          </button>
          <button class="pref-btn" type="button" :title="languageTooltip" @click="preferenceStore.toggleLanguage()">
            {{ languageLabel }}
          </button>
        </div>
      </div>
    </transition>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePreferenceStore } from '@/stores/preference'
import { API_ORIGIN } from '@/config'

const router = useRouter()
const userStore = useUserStore()
const preferenceStore = usePreferenceStore()

const keyword = ref('')
const isScrolled = ref(false)
const showDrawer = ref(false)
const showUserMenu = ref(false)
const userEntryRef = ref<HTMLElement | null>(null)

const defaultAvatar = 'https://www.gravatar.com/avatar/?d=identicon&s=128'

const isMobile = computed(() => window.innerWidth < 768)
const searchPlaceholder = computed(() => preferenceStore.t('nav.searchPlaceholder'))
const themeIcon = computed(() => (preferenceStore.theme === 'light' ? '🌙' : '☀️'))
const languageLabel = computed(() =>
  preferenceStore.language === 'zh'
    ? preferenceStore.t('preference.languageShortZh')
    : preferenceStore.t('preference.languageShortEn')
)
const themeTooltip = computed(() => preferenceStore.t('nav.toggleTheme'))
const languageTooltip = computed(() => preferenceStore.t('nav.toggleLanguage'))
const t = (key: string) => preferenceStore.t(key)

const avatarSrc = computed(() => {
  const url = userStore.profile?.avatar
  if (!url) return defaultAvatar
  if (/^https?:\/\//i.test(url)) return url
  return `${API_ORIGIN}${url.startsWith('/') ? url : `/${url}`}`
})

function handleScroll() {
  isScrolled.value = window.scrollY > 12
}

function handleSearch() {
  if (!keyword.value.trim()) return
  router.push({ path: '/', query: { keyword: keyword.value.trim() } })
  keyword.value = ''
}

function toggleDrawer() {
  showDrawer.value = !showDrawer.value
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function closeUserMenu() {
  showUserMenu.value = false
}

function handleDocumentClick(event: MouseEvent) {
  const target = event.target as Node | null
  if (!target) return
  if (userEntryRef.value && !userEntryRef.value.contains(target)) {
    closeUserMenu()
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/')
  showDrawer.value = false
  closeUserMenu()
}

onMounted(() => {
  handleScroll()
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleDocumentClick)
})
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 20;
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
  background-color: transparent;
}

.navbar--scrolled {
  background-color: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(14px);
  box-shadow: 0 10px 28px -24px rgba(15, 23, 42, 0.9);
}

.navbar__inner {
  width: min(1100px, 92vw);
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 0;
  gap: 1.5rem;
}

.navbar__logo {
  font-weight: 800;
  font-size: 1.3rem;
  color: var(--text-color);
}

.navbar__links {
  display: flex;
  align-items: center;
  gap: 1.2rem;
}

.nav-link {
  color: var(--text-secondary);
  font-weight: 600;
  transition: color 0.2s ease;
}

.nav-link:hover {
  color: var(--primary);
}

.navbar__right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.search-box input {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 0.4rem 0.8rem;
  background-color: transparent;
  color: var(--text-color);
}

.preference-switch,
.drawer__preferences {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.pref-btn {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background-color: var(--surface);
  color: var(--text-color);
  cursor: pointer;
  font-size: 0.9rem;
}

.write-btn {
  padding: 0.4rem 1rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--primary);
  color: var(--primary);
  font-weight: 600;
}

.user-entry {
  position: relative;
}

.user-entry.is-open .user-menu {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  background: transparent;
  border: none;
  cursor: pointer;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid rgba(37, 99, 235, 0.3);
}

.user-name {
  font-size: 0.95rem;
  color: var(--text-secondary);
}

.user-menu {
  position: absolute;
  right: 0;
  top: 48px;
  min-width: 200px;
  background-color: var(--surface);
  border-radius: var(--radius-md);
  box-shadow: var(--surface-shadow);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  padding: 0.5rem;
  gap: 0.2rem;
  opacity: 0;
  transform: translateY(-8px);
  pointer-events: none;
  transition: all 0.15s ease;
  z-index: 10;
}

.user-menu a,
.user-menu .link {
  padding: 0.55rem 0.65rem;
  border-radius: var(--radius-sm);
  text-align: left;
  font-size: 0.95rem;
  background: transparent;
  border: none;
  cursor: pointer;
  color: inherit;
}

.user-menu a:hover,
.user-menu .link:hover {
  background-color: var(--primary-soft);
}

.menu-btn {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 5px;
  cursor: pointer;
}

.menu-btn span {
  width: 18px;
  height: 2px;
  background-color: var(--text-color);
  border-radius: 999px;
}

.drawer {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  padding: 1rem 1.5rem 1.6rem;
  background-color: var(--surface);
  border-bottom-left-radius: var(--radius-lg);
  border-bottom-right-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}

.drawer .link,
.drawer a {
  text-align: left;
  border: none;
  font-size: 1rem;
  background: transparent;
  cursor: pointer;
  color: inherit;
}

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.18s ease;
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scaleY(0.96);
  transform-origin: top;
}

@media (max-width: 768px) {
  .navbar__inner {
    padding: 0.75rem 1.1rem;
  }

  .navbar__links {
    display: none;
  }

  .search-box,
  .preference-switch {
    display: none;
  }
}
</style>
