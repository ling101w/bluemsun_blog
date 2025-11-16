<template>
  <div class="comment-node">
    <div class="comment-header">
      <img :src="node.avatar ?? defaultAvatar" alt="avatar" />
      <div>
        <strong>{{ node.nickname ?? `用户 ${node.userId}` }}</strong>
        <span class="text-secondary">{{ formatDate(node.createTime) }}</span>
      </div>
    </div>
    <div class="comment-content">
      <p>{{ node.content }}</p>
      <div class="comment-actions">
        <el-button
          type="primary"
          text
          :disabled="props.likeDisabled"
          :loading="props.likeDisabled"
          @click="$emit('like', node.id)"
        >
          👍 {{ node.likeCount }}
        </el-button>
        <el-button type="default" text @click="toggleReply">回复</el-button>
      </div>
      <div v-if="showReply" class="reply-box">
        <el-input
          v-model="replyContent"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 6 }"
          placeholder="回复该评论..."
        />
        <div class="reply-actions">
          <el-button text @click="cancelReply">取消</el-button>
          <el-button type="primary" :loading="replyLoading" @click="submitReply">发送回复</el-button>
        </div>
      </div>
    </div>
    <div v-if="flatChildren.length" class="comment-children">
      <CommentNode
        v-for="child in flatChildren"
        :key="child.id"
        :node="child"
        :article-id="articleId"
        :like-disabled="props.likeDisabled"
        @like="$emit('like', $event)"
        @replied="$emit('replied')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import dayjs from 'dayjs'
import type { CommentNode } from '@/services/comment'
import { replyComment } from '@/services/comment'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'CommentNode' })

const props = withDefaults(
  defineProps<{ node: CommentNode; articleId: string | number; likeDisabled?: boolean }>(),
  {
    likeDisabled: false
  }
)
const emit = defineEmits<{ (e: 'like', id: string | number): void; (e: 'replied'): void }>()

const defaultAvatar = 'https://www.gravatar.com/avatar/?d=identicon&s=48'

function formatDate(value: string) {
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}

const showReply = ref(false)
const replyContent = ref('')
const replyLoading = ref(false)
const flatChildren = computed(() => flattenChildren(props.node.children ?? []))

function toggleReply() {
  showReply.value = !showReply.value
}

function cancelReply() {
  replyContent.value = ''
  showReply.value = false
}

async function submitReply() {
  const content = replyContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replyLoading.value = true
  try {
    await replyComment(props.node.id, { articleId: props.articleId, content })
    ElMessage.success('回复成功')
    replyContent.value = ''
    showReply.value = false
    emit('replied')
  } catch (e) {
    ElMessage.error('回复失败，请稍后重试')
  } finally {
    replyLoading.value = false
  }
}

function flattenChildren(children: CommentNode[]): CommentNode[] {
  const result: CommentNode[] = []
  for (const child of children) {
    result.push({ ...child, children: [] })
    if (child.children?.length) {
      result.push(...flattenChildren(child.children))
    }
  }
  return result
}
</script>

<style scoped>
.comment-node {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  padding: 1rem 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.comment-header {
  display: flex;
  gap: 0.8rem;
  align-items: center;
}

.comment-header img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.comment-content {
  margin-left: 2.8rem;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.comment-actions {
  display: flex;
  gap: 0.8rem;
}

.reply-box {
  margin-top: 0.6rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.reply-actions {
  display: flex;
  gap: 0.6rem;
  justify-content: flex-end;
}

.comment-children {
  margin-left: 2.8rem;
  border-left: 2px solid rgba(148, 163, 184, 0.2);
  padding-left: 1rem;
  margin-top: 0.6rem;
}
</style>
