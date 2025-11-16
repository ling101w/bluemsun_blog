<template>
  <div class="dashboard">
    <section class="surface-card overview">
      <h2>控制台概览</h2>
      <el-skeleton :loading="overviewLoading" animated :rows="2">
        <div class="overview__grid">
          <div class="metric">
            <span class="metric__label">用户总数</span>
            <strong>{{ overview?.userCount ?? '--' }}</strong>
          </div>
          <div class="metric">
            <span class="metric__label">文章总数</span>
            <strong>{{ overview?.articleCount ?? '--' }}</strong>
          </div>
          <div class="metric">
            <span class="metric__label">评论总数</span>
            <strong>{{ overview?.commentCount ?? '--' }}</strong>
          </div>
          <div class="metric">
            <span class="metric__label">订单总数</span>
            <strong>{{ overview?.orderCount ?? '--' }}</strong>
          </div>
        </div>
      </el-skeleton>
    </section>

    <section class="surface-card table">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="订单管理" name="orders">
          <div class="table__header">
            <h3>订单列表</h3>
            <el-input-number v-model="pageSize" :min="5" :max="30" size="small" />
          </div>
          <el-table :data="orders" border stripe v-loading="orderLoading">
            <el-table-column prop="orderNo" label="订单号" width="200" />
            <el-table-column prop="articleId" label="文章 ID" width="120" />
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="scope">¥{{ scope.row.amount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="140">
              <template #default="scope">
                <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="payChannel" label="渠道" width="120" />
            <el-table-column prop="payTime" label="支付时间" />
            <el-table-column prop="expireTime" label="过期时间" />
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
          <el-empty v-if="!orderLoading && !orders.length" description="暂无订单数据" />
          <el-pagination
            v-if="orders.length"
            class="pagination"
            background
            layout="prev, pager, next"
            :current-page="pageNo"
            :page-size="pageSize"
            :total="total"
            @current-change="handlePageChange"
          />
        </el-tab-pane>

        <el-tab-pane label="文章管理" name="articles">
          <div class="table__header">
            <h3>文章管理</h3>
            <div class="action-group">
              <el-select v-model="articleQuery.status" placeholder="状态" clearable size="small" @change="loadArticles">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已发布" value="PUBLISHED" />
                <el-option label="已下线" value="OFFLINE" />
              </el-select>
              <el-input
                v-model="articleQuery.keyword"
                placeholder="搜索标题"
                size="small"
                @keyup.enter="loadArticles"
                clearable
              />
              <el-button type="primary" size="small" @click="loadArticles">查询</el-button>
            </div>
          </div>
          <el-table :data="articles" border stripe v-loading="articleLoading">
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="status" label="状态" width="100" />
            <el-table-column prop="publishTime" label="发布时间" />
            <el-table-column prop="pinned" label="公告" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.pinned" size="small" type="warning">公告</el-tag>
                <span v-else class="text-secondary">—</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="320">
              <template #default="scope">
                <el-button
                  type="success"
                  link
                  :disabled="scope.row.status === 'PUBLISHED'"
                  @click="publishArticle(scope.row.id)"
                >
                  发布
                </el-button>
                <el-button type="warning" link @click="offline(scope.row.id)" :disabled="scope.row.status === 'OFFLINE'">下线</el-button>
                <el-button
                  v-if="scope.row.pinned"
                  type="info"
                  link
                  @click="togglePin(scope.row.id, false)"
                >
                  取消公告
                </el-button>
                <el-button
                  v-else
                  type="primary"
                  link
                  :disabled="scope.row.status !== 'PUBLISHED' || scope.row.type === 'PAID'"
                  @click="togglePin(scope.row.id, true)"
                >
                  设为公告
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!articleLoading && !articles.length" description="暂无文章" />
        </el-tab-pane>

        <el-tab-pane label="用户管理" name="users">
          <div class="table__header">
            <h3>用户管理</h3>
            <div class="action-group">
              <el-select v-model="userQuery.status" placeholder="状态" clearable size="small" @change="loadUsers">
                <el-option label="正常" value="active" />
                <el-option label="禁用" value="disabled" />
              </el-select>
              <el-input
                v-model="userQuery.keyword"
                placeholder="搜索昵称/邮箱"
                size="small"
                @keyup.enter="loadUsers"
                clearable
              />
              <el-button type="primary" size="small" @click="loadUsers">查询</el-button>
            </div>
          </div>
          <el-table :data="users" border stripe v-loading="userLoading">
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="role" label="角色" width="100" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" width="150">
              <template #default="scope">¥{{ (scope.row.balance ?? 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" />
            <el-table-column label="操作" width="280">
              <template #default="scope">
                <el-button
                  type="warning"
                  link
                  :disabled="scope.row.status !== 'active'"
                  @click="changeUserStatus(scope.row.id, 'disabled')"
                >禁用</el-button>
                <el-button
                  type="success"
                  link
                  :disabled="scope.row.status === 'active'"
                  @click="changeUserStatus(scope.row.id, 'active')"
                >启用</el-button>
                <el-button type="primary" link @click="openRecharge(scope.row)">充值</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!userLoading && !users.length" description="暂无用户" />
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="recharge.visible" title="余额充值" width="360px">
      <el-form :model="recharge" label-width="80px">
        <el-form-item label="用户">
          <span>{{ recharge.nickname }}</span>
        </el-form-item>
        <el-form-item label="金额（元）">
          <el-input-number v-model="recharge.amount" :min="1" :max="100000" :precision="2" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recharge.visible = false">取消</el-button>
        <el-button type="primary" :loading="recharge.loading" @click="handleRecharge">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  fetchAllOrders as fetchAdminOrders,
  fetchAdminOverview,
  fetchAdminArticles,
  fetchAdminUsers,
  updateUserStatus,
  offlineArticle,
  pinArticle as pinArticleAdmin,
  unpinArticle as unpinArticleAdmin,
  rechargeUserBalance,
  publishArticle as publishArticleAdmin
} from '@/services/admin'
import type { OrderItem } from '@/services/order'
import type { ArticleListItem } from '@/services/article'
import type { UserProfile } from '@/stores/user'

const userStore = useUserStore()

const overview = ref<{ userCount: number; articleCount: number; commentCount: number; orderCount: number } | null>(null)
const overviewLoading = ref(false)

const orders = ref<OrderItem[]>([])
const orderLoading = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)

const articles = ref<ArticleListItem[]>([])
const articleLoading = ref(false)
const articleQuery = ref({ pageNo: 1, pageSize: 10, status: '', keyword: '' })

const users = ref<Array<UserProfile & { createTime: string }>>([])
const userLoading = ref(false)
const userQuery = ref({ pageNo: 1, pageSize: 10, status: '', keyword: '' })

const activeTab = ref('orders')
const recharge = reactive({
  visible: false,
  userId: 0,
  nickname: '',
  amount: 100,
  loading: false
})

async function loadOverview() {
  overviewLoading.value = true
  try {
    const res = await fetchAdminOverview()
    if (res.success) {
      overview.value = res.data
    }
  } finally {
    overviewLoading.value = false
  }
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const res = await fetchAdminOrders(pageNo.value, pageSize.value)
    if (res.success) {
      orders.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    orderLoading.value = false
  }
}

async function loadArticles() {
  articleLoading.value = true
  try {
    const { pageNo, pageSize, keyword, status } = articleQuery.value
    const params = {
      pageNo,
      pageSize,
      keyword: keyword?.trim() ? keyword.trim() : undefined,
      status: status || undefined
    }
    const res = await fetchAdminArticles(params)
    if (res.success) {
      articles.value = res.data.records
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    articleLoading.value = false
  }
}

async function loadUsers() {
  userLoading.value = true
  try {
    const { pageNo, pageSize, keyword, status } = userQuery.value
    const params = {
      pageNo,
      pageSize,
      keyword: keyword?.trim() ? keyword.trim() : undefined,
      status: status || undefined
    }
    const res = await fetchAdminUsers(params)
    if (res.success) {
      users.value = res.data.records
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    userLoading.value = false
  }
}

function handlePageChange(page: number) {
  pageNo.value = page
  loadOrders()
}

async function offline(id: number) {
  await offlineArticle(id)
  ElMessage.success('文章已下线')
  loadArticles()
}

async function togglePin(id: number, pin: boolean) {
  const api = pin ? pinArticleAdmin : unpinArticleAdmin
  const res = await api(id)
  if (res.success) {
    ElMessage.success(pin ? '已设为公告' : '已取消公告')
    loadArticles()
  } else {
    ElMessage.error(res.message)
  }
}

async function publishArticle(id: number) {
  const res = await publishArticleAdmin(id)
  if (res.success) {
    ElMessage.success('文章已发布')
    loadArticles()
  } else {
    ElMessage.error(res.message || '发布失败')
  }
}

async function changeUserStatus(id: number, status: string) {
  const res = await updateUserStatus(id, status)
  if (res.success) {
    ElMessage.success('状态更新成功')
    loadUsers()
  } else {
    ElMessage.error(res.message)
  }
}

function openRecharge(user: UserProfile & { createTime: string }) {
  recharge.visible = true
  recharge.userId = Number(user.id)
  recharge.nickname = user.nickname
  recharge.amount = 100
}

async function handleRecharge() {
  if (!recharge.userId || recharge.amount <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  recharge.loading = true
  try {
    const res = await rechargeUserBalance(recharge.userId, recharge.amount)
    if (!res.success) {
      throw new Error(res.message)
    }
    ElMessage.success('充值成功')
    recharge.visible = false
    loadUsers()
  } catch (error) {
    const message = error instanceof Error ? error.message : '充值失败'
    ElMessage.error(message)
  } finally {
    recharge.loading = false
  }
}

function statusLabel(status: string) {
  switch (status) {
    case 'PAID':
      return '已支付'
    case 'PAYING':
      return '待支付'
    case 'CREATED':
      return '已创建'
    case 'CLOSED':
      return '已关闭'
    default:
      return status ?? '--'
  }
}

function statusType(status: string) {
  switch (status) {
    case 'PAID':
      return 'success'
    case 'PAYING':
      return 'warning'
    case 'CREATED':
      return 'info'
    case 'CLOSED':
      return 'danger'
    default:
      return 'info'
  }
}

watch(pageSize, () => {
  pageNo.value = 1
  loadOrders()
})

onMounted(async () => {
  if (!userStore.isAdmin) {
    ElMessage.warning('当前账户无权访问后台面板')
    return
  }
  await loadOverview()
  loadOrders()
  loadArticles()
  loadUsers()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 1.8rem;
}

.overview {
  padding: 1.6rem;
}

.overview__grid {
  margin-top: 1.4rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1.2rem;
}

.metric {
  background: rgba(37, 99, 235, 0.04);
  border-radius: var(--radius-md);
  padding: 1.1rem 1.2rem;
}

.metric__label {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.metric strong {
  display: block;
  font-size: 1.6rem;
  margin-top: 0.4rem;
}

.table {
  padding: 1.6rem;
}

.table__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  gap: 1rem;
}

.action-group {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.pagination {
  margin-top: 1rem;
  align-self: flex-end;
}
</style>

