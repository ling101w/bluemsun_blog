<template>
  <div class="app-shell">
    <SiteNavbar />
    <main class="app-main">
      <RouterView :key="$route.fullPath" />
    </main>
    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import SiteNavbar from './components/SiteNavbar.vue'
import SiteFooter from './components/SiteFooter.vue'
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--page-bg);
  color: var(--text-color);
}

.app-main {
  flex: 1;
  width: min(1100px, 92vw);
  margin: 0 auto;
  padding: 1.5rem 0 3rem;
}

@media (max-width: 768px) {
  .app-main {
    width: min(100vw, 95vw);
    padding: 1rem 0 2rem;
  }
}
</style>

