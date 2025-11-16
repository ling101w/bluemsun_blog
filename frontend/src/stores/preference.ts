import { ref, watch } from 'vue'
import { defineStore } from 'pinia'
import { translate, type Language } from '@/locales/messages'

type ThemeMode = 'light' | 'dark'

const THEME_KEY = 'pref_theme'
const LANGUAGE_KEY = 'pref_language'

export const usePreferenceStore = defineStore('preference', () => {
  const prefersDark =
    typeof window !== 'undefined' && window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches

  const theme = ref<ThemeMode>((localStorage.getItem(THEME_KEY) as ThemeMode) || (prefersDark ? 'dark' : 'light'))
  const language = ref<Language>((localStorage.getItem(LANGUAGE_KEY) as Language) || 'zh')

  const applyTheme = (value: ThemeMode) => {
    if (typeof document !== 'undefined') {
      document.documentElement.setAttribute('data-theme', value)
    }
  }

  applyTheme(theme.value)

  watch(
    theme,
    (value) => {
      localStorage.setItem(THEME_KEY, value)
      applyTheme(value)
    },
    { immediate: false }
  )

  watch(
    language,
    (value) => {
      localStorage.setItem(LANGUAGE_KEY, value)
    },
    { immediate: false }
  )

  const setTheme = (value: ThemeMode) => {
    theme.value = value
  }

  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  const setLanguage = (value: Language) => {
    language.value = value
  }

  const toggleLanguage = () => {
    language.value = language.value === 'zh' ? 'en' : 'zh'
  }

  const t = (path: string) => translate(language.value, path)

  return {
    theme,
    language,
    setTheme,
    toggleTheme,
    setLanguage,
    toggleLanguage,
    t
  }
})

