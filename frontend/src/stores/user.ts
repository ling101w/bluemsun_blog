import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import http from '@/services/http'
import router from '@/router'
import { sendEmailCode } from '@/services/auth'

export interface UserProfile {
  id: number
  email: string
  nickname: string
  avatar?: string
  role: string
  status: string
  profile?: string
  lastLoginTime?: string
  balance?: number
}

interface ApiResponse<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('access_token'))
  const profile = ref<UserProfile | null>(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))
  const isAdmin = computed(() => profile.value?.role === 'ADMIN')

  async function login(email: string, password: string) {
    loading.value = true
    try {
      const res = await http.post<ApiResponse<string>, ApiResponse<string>>('/auth/login', {
        email,
        password
      })
      if (!res.success) {
        throw new Error(res.message)
      }
      token.value = res.data
      localStorage.setItem('access_token', res.data)
      await fetchProfile()
      await router.push('/user/center')
    } finally {
      loading.value = false
    }
  }

  async function register(email: string, password: string, nickname: string, code: string) {
    await http.post<ApiResponse<boolean>, ApiResponse<boolean>>('/auth/register', {
      email,
      password,
      nickname,
      code
    })
  }

  async function requestCode(email: string) {
    return sendEmailCode(email, 'REGISTER')
  }

  async function fetchProfile() {
    if (!token.value) return
    const res = await http.get<ApiResponse<UserProfile>, ApiResponse<UserProfile>>('/users/me')
    if (res.success) {
      profile.value = res.data
    }
  }

  async function updateProfile(payload: { nickname?: string; avatar?: string; profile?: string }) {
    const res = await http.put<ApiResponse<boolean>, ApiResponse<boolean>>('/users/me', payload)
    if (!res.success) {
      throw new Error(res.message)
    }
    await fetchProfile()
  }

  function logout() {
    token.value = null
    profile.value = null
    localStorage.removeItem('access_token')
    router.push('/login')
  }

  return {
    token,
    profile,
    loading,
    isLoggedIn,
    isAdmin,
    login,
    register,
    requestCode,
    fetchProfile,
    updateProfile,
    logout
  }
})

