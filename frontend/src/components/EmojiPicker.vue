<template>
  <div class="emoji-picker">
    <div class="emoji" v-for="emoji in emojis" :key="emoji.code" @click="$emit('select', emoji.code)">
      <span>{{ emoji.icon }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const emojis = [
  { code: ':smile:', icon: '😄' },
  { code: ':laughing:', icon: '😆' },
  { code: ':wink:', icon: '😉' },
  { code: ':thumbs_up:', icon: '👍' },
  { code: ':fire:', icon: '🔥' },
  { code: ':rocket:', icon: '🚀' },
  { code: ':sparkles:', icon: '✨' },
  { code: ':idea:', icon: '💡' },
  { code: ':heart:', icon: '❤️' },
  { code: ':pray:', icon: '🙏' }
]
</script>

<style scoped>
.emoji-picker {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(36px, 1fr));
  gap: 8px;
}

.emoji {
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--surface-subtle);
  border-radius: 8px;
  cursor: pointer;
  padding: 6px;
  transition: transform 0.1s ease;
}

.emoji:hover {
  transform: scale(1.08);
}
</style>


