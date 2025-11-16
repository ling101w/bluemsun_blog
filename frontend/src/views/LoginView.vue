<template>
  <div class="auth surface-card">
    <div class="auth__toggle">
      <el-button :type="mode === 'login' ? 'primary' : 'default'" @click="switchMode('login')">登录</el-button>
      <el-button :type="mode === 'register' ? 'primary' : 'default'" @click="switchMode('register')">注册</el-button>
    </div>
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" autocomplete="email" />
      </el-form-item>
      <el-form-item v-if="mode === 'register'" label="昵称" prop="nickname">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
      </el-form-item>
      <el-form-item v-if="mode === 'register'" label="邮箱验证码" prop="code">
        <el-input v-model="form.code" maxlength="6">
          <template #append>
            <el-button type="primary" link :disabled="codeCountdown > 0" @click="handleSendCode">
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-button class="submit" type="primary" :loading="userStore.loading" @click="handleSubmit">
        {{ mode === 'login' ? '登录' : '完成注册' }}
      </el-button>
      <p class="text-secondary tips">登录视为同意《用户协议》《隐私政策》。</p>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onBeforeUnmount, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const mode = ref<'login' | 'register'>('login')

const formRef = ref<FormInstance>()
const form = reactive({
  email: '',
  password: '',
  nickname: '',
  code: ''
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  nickname: [
    {
      validator: (_rule, value, callback) => {
        if (mode.value === 'register' && !value) {
          callback(new Error('请输入昵称'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  code: [
    {
      validator: (_rule, value, callback) => {
        if (mode.value === 'register') {
          if (!value) {
            callback(new Error('请输入验证码'))
            return
          }
          if (value.length !== 6) {
            callback(new Error('验证码为 6 位数字'))
            return
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const formRefValue = formRef
const codeCountdown = ref(0)
let countdownTimer: number | null = null

function switchMode(target: 'login' | 'register') {
  mode.value = target
}

watch(mode, () => {
  form.code = ''
  if (mode.value === 'login') {
    form.nickname = ''
  }
})

function clearTimer() {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
    countdownTimer = null
  }
}

async function handleSendCode() {
  if (codeCountdown.value > 0) {
    return
  }
  if (mode.value !== 'register') {
    ElMessage.warning('当前登录无需验证码')
    return
  }
  if (!form.email) {
    ElMessage.warning('请先填写邮箱')
    return
  }
  try {
    const res = await userStore.requestCode(form.email)
    if (!res.success) {
      throw new Error(res.message)
    }
    ElMessage.success('验证码已发送，请查收邮箱')
    codeCountdown.value = 60
    clearTimer()
    countdownTimer = window.setInterval(() => {
      codeCountdown.value -= 1
      if (codeCountdown.value <= 0) {
        codeCountdown.value = 0
        clearTimer()
      }
    }, 1000)
  } catch (err) {
    const message = err instanceof Error ? err.message : '验证码发送失败'
    ElMessage.error(message)
  }
}

function handleSubmit() {
  formRefValue.value?.validate(async (valid) => {
    if (!valid) {
      return
    }
    try {
      if (mode.value === 'register') {
        await userStore.register(form.email, form.password, form.nickname, form.code)
        ElMessage.success('注册成功，请使用验证码登录')
        switchMode('login')
        return
      }
      await userStore.login(form.email, form.password)
      ElMessage.success('登录成功')
    } catch (err) {
      const message = err instanceof Error ? err.message : '操作失败'
      ElMessage.error(message)
    }
  })
}

onBeforeUnmount(() => {
  clearTimer()
})
</script>

<style scoped>
.auth {
  max-width: 420px;
  margin: 4rem auto;
  padding: 2.2rem 2.4rem;
  display: flex;
  flex-direction: column;
  gap: 1.6rem;
}

.auth__toggle {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.6rem;
}

.submit {
  width: 100%;
}

.tips {
  text-align: center;
  font-size: 0.85rem;
}

@media (max-width: 480px) {
  .auth {
    margin: 3rem 1rem;
    padding: 1.8rem;
  }
}
</style>

