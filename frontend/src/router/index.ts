import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import ArticleDetailView from '@/views/ArticleDetailView.vue'
import UserCenterView from '@/views/UserCenterView.vue'
import AdminDashboardView from '@/views/AdminDashboardView.vue'
import LoginView from '@/views/LoginView.vue'
import AiAssistantView from '@/views/AiAssistantView.vue'
import ArticleEditorView from '@/views/ArticleEditorView.vue'
import StaticPageView from '@/views/StaticPageView.vue'
import { useUserStore } from '@/stores/user'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    requiresAdmin?: boolean
  }
}

/**
 * 路由配置：仿照 Medium/CSDN 的页面布局。
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: '蓝旭博客 - 精选文章' }
    },
    {
      path: '/articles/featured',
      redirect: { path: '/', query: { sort: 'featured' } }
    },
    {
      path: '/topics/premium',
      redirect: { path: '/', query: { type: 'PAID' } }
    },
    {
      path: '/articles/:id',
      name: 'article-detail',
      component: ArticleDetailView,
      meta: { title: '文章详情' }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { title: '登录 / 注册' }
    },
    {
      path: '/user/center',
      name: 'user-center',
      component: UserCenterView,
      meta: { title: '个人中心', requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: AdminDashboardView,
      meta: { title: '后台管理面板', requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/topics/ai',
      redirect: '/ai'
    },
    {
      path: '/about',
      name: 'about',
      component: StaticPageView,
      props: {
        title: '关于我们',
        description: '蓝旭博客是面向开发者与创作者的内容社区，记录经验、灵感与真实故事。',
        sections: [
          {
            heading: '我们的使命',
            body: '帮助创作者建立影响力、帮助读者获取灵感，让优质内容更容易被看到。'
          },
          {
            heading: '团队介绍',
            body: '团队成员来自技术与设计背景，对知识分享和社区运营有着长久的热情。'
          }
        ]
      },
      meta: { title: '关于我们' }
    },
    {
      path: '/support',
      name: 'support',
      component: StaticPageView,
      props: {
        title: '支持与反馈',
        description: '遇到问题或想要提出建议？欢迎通过以下方式联系我们。',
        sections: [
          {
            heading: '联系方式',
            body: '邮箱：support@bluemsun.com，工作日 10:00-19:00 回复。'
          },
          {
            heading: '社区守则',
            body: '请尊重原创与多元观点，任何违法内容将被立即处理。'
          }
        ]
      },
      meta: { title: '支持与反馈' }
    },
    {
      path: '/privacy',
      name: 'privacy',
      component: StaticPageView,
      props: {
        title: '隐私政策',
        description: '我们承诺保护您的数据安全，下列条款解释了信息的收集与使用方式。',
        sections: [
          {
            heading: '数据收集',
            body: '仅收集提供服务所需的最小化信息，如注册邮箱、昵称等。'
          },
          {
            heading: '数据使用',
            body: '数据用于登录鉴权、内容推荐与安全审计，不会出售给第三方。'
          }
        ]
      },
      meta: { title: '隐私政策' }
    },
    {
      path: '/ai',
      name: 'ai-assistant',
      component: AiAssistantView,
      meta: { title: 'AI 助手', requiresAuth: true }
    },
    {
      path: '/articles/new',
      name: 'article-create',
      component: ArticleEditorView,
      meta: { title: '创作文章', requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'login' })
    return
  }
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ name: 'home' })
    return
  }
  next()
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} · 蓝旭博客`
  }
})

export default router

