<template>
  <div class="ai-panel surface-card">
    <section class="ai-input">
      <div class="header">
        <h2>AI 助手</h2>
        <span class="text-secondary">支持润色、扩写、总结、翻译、续写</span>
      </div>

      <el-form :model="form" label-position="top">
        <el-form-item label="任务类型">
          <el-select v-model="form.scene" placeholder="请选择">
            <el-option label="润色" value="POLISH" />
            <el-option label="扩写" value="EXTEND" />
            <el-option label="总结" value="SUMMARY" />
            <el-option label="翻译" value="TRANSLATE" />
            <el-option label="续写" value="CONTINUE" />
          </el-select>
        </el-form-item>

        <el-form-item label="输入内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :autosize="{ minRows: 6, maxRows: 10 }"
            maxlength="2000"
            show-word-limit
            placeholder="请输入要处理的文本"
          />
        </el-form-item>

        <el-form-item v-if="form.scene === 'TRANSLATE'" label="额外信息（可填写目标语言，例如 EN/CN）">
          <el-input v-model="form.extra" placeholder="例如：EN" />
        </el-form-item>

        <div class="actions">
          <el-button type="primary" :loading="loading" @click="handleSubmit">生成结果</el-button>
          <el-button text @click="reset">清空</el-button>
        </div>
      </el-form>
    </section>

    <section class="ai-output" v-loading="loading">
      <div class="output-header">
        <h3>生成结果</h3>
        <el-button text size="small" @click="loadHistory">刷新历史</el-button>
      </div>
      <el-empty v-if="!result && !history.length" description="还没有生成记录" />
      <div v-else>
        <div v-if="result" class="result-card">
          <header>
            <el-tag size="small" type="primary">最新</el-tag>
            <span>{{ sceneLabel(result.scene) }}</span>
            <span class="time">{{ formatDate(result.createTime) }}</span>
          </header>
          <div class="prompt">
            <strong>输入</strong>
            <p>{{ result.prompt }}</p>
          </div>
          <div class="answer">
            <strong>输出</strong>
            <p>{{ result.response }}</p>
          </div>
        </div>

        <el-divider v-if="history.length" content-position="left">历史记录</el-divider>

        <el-timeline v-if="history.length">
          <el-timeline-item v-for="item in history" :key="item.id" :timestamp="formatDate(item.createTime)" placement="top">
            <el-card shadow="hover" class="history-card">
              <div class="history-header">
                <el-tag size="small">{{ sceneLabel(item.scene) }}</el-tag>
                <span class="tokens" v-if="item.tokenUsage">Token: {{ item.tokenUsage }}</span>
              </div>
              <p class="prompt">{{ item.prompt }}</p>
              <p class="response">{{ item.response }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { runAiTask, fetchAiHistory, type AiHistoryItem } from '@/services/ai'

const form = reactive({
  scene: 'POLISH',
  content: '',
  extra: ''
})

const loading = ref(false)
const result = ref<AiHistoryItem | null>(null)
const history = ref<AiHistoryItem[]>([])

const sceneLabel = (scene: string) => {
  switch (scene) {
    case 'POLISH':
      return '润色'
    case 'EXTEND':
      return '扩写'
    case 'SUMMARY':
      return '总结'
    case 'TRANSLATE':
      return '翻译'
    case 'CONTINUE':
      return '续写'
    default:
      return scene
  }
}

const formatDate = (value?: string) => {
  if (!value) return ''
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

async function handleSubmit() {
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  loading.value = true
  try {
    const res = await runAiTask({ scene: form.scene, content: form.content, extra: form.extra })
    if (!res.success) {
      throw new Error(res.message)
    }
    result.value = res.data
    ElMessage.success('AI 生成成功')
    loadHistory()
  } catch (error) {
    const message = error instanceof Error ? error.message : '生成失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  try {
    const res = await fetchAiHistory(10)
    if (res.success) {
      history.value = res.data
    }
  } catch (error) {
    console.warn('history load failed', error)
  }
}

function reset() {
  form.content = ''
  form.extra = ''
  result.value = null
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.ai-panel {
  max-width: 960px;
  margin: 2rem auto;
  padding: 1.8rem;
  display: grid;
  grid-template-columns: minmax(320px, 380px) 1fr;
  gap: 2rem;
}

.ai-input {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.header {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.actions {
  display: flex;
  gap: 1rem;
}

.ai-output {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-card {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  padding: 1rem;
  background: rgba(37, 99, 235, 0.05);
  border-radius: var(--radius-md);
}

.result-card header {
  display: flex;
  gap: 0.6rem;
  align-items: center;
}

.result-card .time {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.prompt,
.answer {
  background: var(--surface);
  padding: 0.6rem 0.8rem;
  border-radius: var(--radius-sm);
}

.history-card {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-card .prompt {
  font-weight: 500;
}

.history-card .response {
  color: var(--text-secondary);
}

.tokens {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

@media (max-width: 960px) {
  .ai-panel {
    grid-template-columns: 1fr;
  }
}
</style>

