<template>
  <section class="static-page surface-card">
    <header>
      <h1>{{ title }}</h1>
      <p class="text-secondary">{{ description }}</p>
    </header>
    <div class="content">
      <section v-for="section in sections" :key="section.heading">
        <h2>{{ section.heading }}</h2>
        <p>{{ section.body }}</p>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
interface Section {
  heading: string
  body: string
}

defineProps<{
  title: string
  description?: string
  sections: Section[]
}>()
</script>

<style scoped>
.static-page {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.4rem;
}

header h1 {
  margin: 0 0 0.4rem;
}

.content section + section {
  margin-top: 1rem;
}

.content h2 {
  margin-bottom: 0.3rem;
  font-size: 1.2rem;
}
</style>

