<template>
  <div class="editor-page">
    <section class="surface-card editor-panel">
      <header class="panel-header">
        <div>
          <h2>创作文章</h2>
          <p class="text-secondary">填写标题、正文及摘要，发布后即可在平台展示。</p>
        </div>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">发布文章</el-button>
      </header>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="请填写文章标题" />
        </el-form-item>

        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4 }"
            maxlength="300"
            show-word-limit
            placeholder="可选，用几句话概述文章要点"
          />
        </el-form-item>

        <div class="grid">
          <el-form-item label="文章类型" prop="type">
            <el-radio-group v-model="form.type">
              <el-radio-button label="FREE">免费</el-radio-button>
              <el-radio-button label="PAID">付费</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="售价（元）" prop="price">
            <el-input-number
              v-model="form.price"
              :precision="2"
              :step="1"
              :min="0"
              :disabled="!isPaid"
              placeholder="0.00"
            />
          </el-form-item>
          <el-form-item label="允许评论" prop="allowComment">
            <el-switch v-model="form.allowComment" active-text="允许" inactive-text="关闭" />
          </el-form-item>
        </div>

        <div class="grid">
          <el-form-item label="封面图地址" prop="coverUrl">
            <el-input v-model="form.coverUrl" placeholder="可选，例如 https://cdn.example.com/cover.png" />
          </el-form-item>
          <el-form-item label="分类 ID" prop="categoryId">
            <el-input-number v-model="form.categoryId" :min="1" :placeholder="'可选，未配置分类可留空'" />
          </el-form-item>
        </div>

        <el-form-item label="正文内容 (Markdown)" prop="contentMd">
          <el-input
            v-model="form.contentMd"
            type="textarea"
            :autosize="{ minRows: 12, maxRows: 24 }"
            placeholder="支持 Markdown 语法，建议先写 Markdown 再预览生成 HTML"
          />
        </el-form-item>

        <el-form-item label="自定义 HTML（可选）" prop="contentHtml">
          <el-input
            v-model="form.contentHtml"
            type="textarea"
            :autosize="{ minRows: 6, maxRows: 16 }"
            placeholder="若不填写，将自动由 Markdown 渲染 HTML"
          />
        </el-form-item>
      </el-form>
    </section>

    <section class="surface-card preview-panel">
      <div class="preview-header">
        <h3>实时预览</h3>
        <span class="text-secondary">根据 Markdown 渲染，实际发布以渲染结果为准</span>
      </div>
      <div class="preview-body" v-html="previewHtml"></div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { renderMarkdown } from '@/utils/markdown'
import { createArticle, type ArticleMutatePayload } from '@/services/article'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  title: '',
  summary: '',
  coverUrl: '',
  categoryId: undefined as number | undefined,
  type: 'FREE',
  price: 0,
  allowComment: true,
  contentMd: '',
  contentHtml: ''
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, message: '标题至少 2 个字符', trigger: 'blur' }
  ],
  summary: [{ max: 300, message: '摘要不超过 300 字', trigger: 'blur' }],
  coverUrl: [{ max: 255, message: '封面地址过长', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  price: [
    {
      validator: (_rule, value, callback) => {
        if (form.type === 'PAID' && (!value || value <= 0)) {
          callback(new Error('付费文章请设置大于 0 的售价'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  contentMd: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}

const isPaid = computed(() => form.type === 'PAID')
const previewHtml = computed(() => {
  const htmlSource = form.contentHtml?.trim() ? form.contentHtml : renderMarkdown(form.contentMd)
  return htmlSource || '<p class="text-secondary">输入 Markdown 内容后即可预览</p>'
})

watch(
  () => form.type,
  (value) => {
    if (value === 'FREE') {
      form.price = 0
    } else if (!form.price || form.price <= 0) {
      form.price = 1
    }
  }
)

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    const payload: ArticleMutatePayload = {
      title: form.title.trim(),
      summary: form.summary?.trim() || undefined,
      coverUrl: form.coverUrl?.trim() || undefined,
      categoryId: form.categoryId,
      type: form.type,
      price: isPaid.value ? Number(form.price || 0) : 0,
      allowComment: form.allowComment,
      contentMd: form.contentMd,
      contentHtml: form.contentHtml?.trim() || renderMarkdown(form.contentMd),
      attachments: null
    }

    const res = await createArticle(payload)
    if (!res.success) {
      throw new Error(res.message)
    }
    ElMessage.success('文章已发布，可在详情页上传附件')
    router.push(`/articles/${res.data}`)
  } catch (error) {
    const message = error instanceof Error ? error.message : '发布失败，请稍后再试'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.editor-page {
  display: flex;
  flex-direction: column;
  gap: 1.6rem;
  margin: 1.5rem auto 2.5rem;
  max-width: 1080px;
}

.editor-panel,
.preview-panel {
  padding: 1.8rem 2rem;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.panel-header h2 {
  margin: 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.2rem;
}

.preview-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.preview-body {
  min-height: 240px;
  padding: 1rem 1.2rem;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: var(--radius-md);
  line-height: 1.75;
}

.preview-body :deep(h1),
.preview-body :deep(h2),
.preview-body :deep(h3) {
  margin-top: 1.5rem;
}

.preview-body :deep(pre) {
  background-color: #0f172a;
  color: #f8fafc;
  padding: 0.85rem;
  border-radius: var(--radius-sm);
  overflow: auto;
}

@media (max-width: 768px) {
  .editor-panel,
  .preview-panel {
    padding: 1.4rem 1.2rem;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>

