<template>
  <aside class="sidebar surface-card">
    <h3>{{ title }}</h3>
    <ul>
      <li v-for="item in items" :key="item.id" @click="$emit('select', item)">
        <div class="index">{{ item.index }}</div>
        <div class="content">
          <span class="name">{{ item.title }}</span>
          <span class="meta">{{ item.meta }}</span>
        </div>
      </li>
    </ul>
  </aside>
</template>

<script setup lang="ts">
export interface SidebarItem {
  id: string | number
  title: string
  meta: string
  index: string
}

defineProps<{
  title: string
  items: SidebarItem[]
}>()

defineEmits<{ select: [SidebarItem] }>()
</script>

<style scoped>
.sidebar {
  padding: 1.4rem;
}

h3 {
  margin: 0 0 1rem;
  font-size: 1.1rem;
  font-weight: 700;
}

ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

li {
  display: flex;
  gap: 1rem;
  cursor: pointer;
  transition: transform 0.16s ease;
}

li:hover {
  transform: translateX(6px);
}

.index {
  font-weight: 700;
  color: var(--primary);
  font-size: 1.1rem;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.meta {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

@media (max-width: 1024px) {
  .sidebar {
    padding: 1.2rem;
  }
}
</style>

