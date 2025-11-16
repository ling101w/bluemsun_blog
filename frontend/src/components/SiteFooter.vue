<template>
  <footer class="footer">
    <div class="footer__inner">
      <div>
        <h4>蓝旭博客</h4>
        <p class="text-secondary">记录技术与生活，汲取灵感，向优秀创作者致敬。</p>
      </div>
      <div class="footer__links">
        <div>
          <span>站点导航</span>
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/topics/ai">AI 助手</RouterLink>
          <RouterLink to="/topics/premium">付费专栏</RouterLink>
        </div>
        <div>
          <span>帮助中心</span>
          <RouterLink to="/about">关于我们</RouterLink>
          <RouterLink to="/support">支持与反馈</RouterLink>
          <RouterLink to="/privacy">隐私政策</RouterLink>
        </div>
      </div>
    </div>
    <div class="footer__bottom">© {{ currentYear }} Bluemsun. All rights reserved.</div>
  </footer>
</template>

<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<style scoped>
.footer {
  margin-top: auto;
  padding: 2.5rem 1.5rem 1.2rem;
  background: #0f172a;
  color: rgba(255, 255, 255, 0.86);
}

.footer__inner {
  max-width: 1100px;
  margin: 0 auto 1.8rem;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 2rem;
}

.footer__links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 1.4rem;
}

.footer__links span {
  display: block;
  margin-bottom: 0.6rem;
  font-weight: 600;
}

.footer__links a {
  display: block;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 0.4rem;
  font-size: 0.95rem;
  transition: color 0.2s ease;
}

.footer__links a:hover {
  color: rgba(255, 255, 255, 0.95);
}

.footer__bottom {
  text-align: center;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.55);
}

@media (max-width: 768px) {
  .footer__inner {
    flex-direction: column;
  }
}
</style>

