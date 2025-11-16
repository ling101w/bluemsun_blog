export type Language = 'zh' | 'en'

interface MessageTree {
  [key: string]: string | MessageTree
}

export const messages: Record<Language, MessageTree> = {
  zh: {
    nav: {
      home: '首页',
      featured: '精选文章',
      ai: 'AI 助手',
      premium: '付费专区',
      write: '写文章',
      login: '登录 / 注册',
      logout: '退出登录',
      profile: '个人中心',
      admin: '后台管理',
      searchPlaceholder: '搜索文章 / 标签 / 作者',
      toggleTheme: '切换日夜模式',
      toggleLanguage: '切换语言'
    },
    hero: {
      badge: '蓝旭精选 · 为热爱创作的你准备',
      heading: '写给创造者的阅读空间',
      subheading: '汇聚编程、产品、设计、AI、随笔等优质内容，也欢迎你将思考与故事写下，与世界分享。',
      cta: '快速写作',
      link: '浏览付费专栏'
    },
    home: {
      announcementBadge: '平台公告',
      announcementCTA: '查看公告',
      latestTitle: '最新发布',
      hotListTitle: '热门榜单',
      creatorTitle: '创作者推荐',
      emptyArticles: '暂无文章，稍后再来~',
      searchPlaceholder: '搜索文章 / 标签 / 作者',
      loginHint: '请先登录后再创作文章'
    },
    general: {
      search: '搜索',
      publishedAt: '发布时间',
      readLabel: '浏览',
      likeLabel: '点赞',
      viewAnnouncement: '查看公告'
    },
    preference: {
      themeToggle: '切换日夜模式',
      languageToggle: '切换语言',
      languageShortZh: '中',
      languageShortEn: 'EN'
    },
    articleDetail: {
      lockedTitle: '该文章为付费内容',
      lockedSubtitle: '购买后可解锁全文与全部附件。',
      lockedPrimary: '立即购买',
      lockedSecondary: '先点赞收藏',
      attachmentsTitle: '附件 / 资源',
      attachmentsCountSuffix: '个附件',
      attachmentsEmpty: '暂无附件',
      attachmentsTip: '支持 PNG/JPG/PDF/ZIP 等文件类型，单个文件不超过 20MB',
      attachmentsDownloadWarning: '请购买后再下载附件'
    }
  },
  en: {
    nav: {
      home: 'Home',
      featured: 'Featured',
      ai: 'AI Assistant',
      premium: 'Premium Zone',
      write: 'Write',
      login: 'Sign in / Register',
      logout: 'Sign out',
      profile: 'Profile',
      admin: 'Admin',
      searchPlaceholder: 'Search articles / tags / authors',
      toggleTheme: 'Toggle light/dark',
      toggleLanguage: 'Switch language'
    },
    hero: {
      badge: 'Bluemsun Picks · For creators',
      heading: 'A reading space for makers',
      subheading:
        'Discover excellent content about coding, product, design, AI and essays, and share your own stories with the world.',
      cta: 'Start writing',
      link: 'Browse premium columns'
    },
    home: {
      announcementBadge: 'Announcement',
      announcementCTA: 'View announcement',
      latestTitle: 'Latest posts',
      hotListTitle: 'Trending',
      creatorTitle: 'Creators to follow',
      emptyArticles: 'No articles yet, please check back later.',
      searchPlaceholder: 'Search articles / tags / authors',
      loginHint: 'Please log in before publishing new content'
    },
    general: {
      search: 'Search',
      publishedAt: 'Published',
      readLabel: 'Views',
      likeLabel: 'Likes',
      viewAnnouncement: 'View announcement'
    },
    preference: {
      themeToggle: 'Toggle light/dark',
      languageToggle: 'Switch language',
      languageShortZh: '中',
      languageShortEn: 'EN'
    },
    articleDetail: {
      lockedTitle: 'This article is paid content',
      lockedSubtitle: 'Purchase to unlock the full article and all attachments.',
      lockedPrimary: 'Buy now',
      lockedSecondary: 'Like first',
      attachmentsTitle: 'Attachments / Resources',
      attachmentsCountSuffix: 'attachments',
      attachmentsEmpty: 'No attachments yet',
      attachmentsTip: 'Supports PNG/JPG/PDF/ZIP and more, up to 20MB per file.',
      attachmentsDownloadWarning: 'Please purchase before downloading attachments.'
    }
  }
}

export function translate(language: Language, path: string): string {
  const segments = path.split('.')
  let current: string | MessageTree | undefined = messages[language]

  for (const segment of segments) {
    if (current && typeof current === 'object') {
      current = current[segment]
    } else {
      current = undefined
      break
    }
  }

  if (typeof current === 'string') {
    return current
  }

  // fallback to zh
  current = messages.zh
  for (const segment of segments) {
    if (current && typeof current === 'object') {
      current = current[segment]
    } else {
      current = undefined
      break
    }
  }
  return typeof current === 'string' ? current : path
}
