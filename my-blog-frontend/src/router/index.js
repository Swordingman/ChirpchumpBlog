// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from "@/store/auth.js";

const routes = [
    {
        path: '/',
        name: 'Home',
        component: HomeView
    },
    {
        path: '/post/:slug', // 文章详情页，使用 slug 作为参数
        name: 'PostDetail',
        // 路由懒加载，优化首屏加载速度
        component: () => import('../views/PostDetailView.vue')
    },
    {
        path: '/category/:categorySlug',
        name: 'CategoryPosts',
        component: () => import('../views/CategoryPostsView.vue')
    },
    {
        path: '/tag/:tagSlug',
        name: 'TagPosts',
        component: () => import('../views/TagPostsView.vue')
    },
    {
        path: '/archives',
        name: 'Archives',
        component: () => import('../views/ArchivesView.vue')
    },
    {
        path: '/about',
        name: 'About',
        component: () => import('../views/AboutView.vue')
    },
    // 后台管理相关路由 (示例，后续完善)
    {
        path: '/admin',
        name: 'AdminLayout', // 给布局组件一个name
        component: () => import('../layouts/AdminLayout.vue'),
        meta: { requiresAuth: true, title: '后台系统' }, // 父级路由也可能有title，面包屑可以用到
        redirect: '/admin/dashboard', // 重定向到默认子页面
        children: [
            {
                path: 'dashboard', // 注意这里path没有前导 /
                name: 'AdminDashboard',
                component: () => import('../views/admin/DashboardView.vue'),
                meta: { title: '仪表盘', icon: 'DataLine' } // icon用于侧边栏 (可选)
            },
            {
                path: 'posts',
                name: 'AdminPosts',
                component: () => import('../views/admin/PostListView.vue'),
                meta: { title: '文章管理', icon: 'Tickets' }
            },
            {
                path: 'categories',
                name: 'AdminCategories',
                component: () => import('../views/admin/CategoryListView.vue'),
                meta: { title: '分类管理' }
            },
            {
                path: 'tags',
                name: 'AdminTags',
                component: () => import('../views/admin/TagListView.vue'),
                meta: { title: '标签管理' }
            },
            {
                path: 'posts/create',
                name: 'AdminPostCreate',
                component: () => import('../views/admin/PostEditView.vue'), // 复用同一个编辑组件
                meta: { title: '新建文章' }
            },
            {
                path: 'posts/edit/:id',
                name: 'AdminPostEdit',
                component: () => import('../views/admin/PostEditView.vue'), // 复用同一个编辑组件
                props: true, // 将路由参数 id 作为 prop 传入 PostEditView
                meta: { title: '编辑文章' }
            },
            {
                path: 'settings',
                name: 'AdminSettings',
                component: () => import('../views/admin/SettingsView.vue'),
                meta: { title: '系统设置' }
            },
        ]
    },
    {
        path: '/admin/login',
        name: 'AdminLogin',
        component: () => import('../views/admin/LoginView.vue')
    },
    // 404 页面
    {
        path: '/:pathMatch(.*)*', // 匹配所有未匹配到的路径
        name: 'NotFound',
        component: () => import('../views/NotFoundView.vue')
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL), // 使用 history 模式
    routes,
    scrollBehavior(to, from, savedPosition) {
        // 路由切换时，滚动到页面顶部或上次离开的位置
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()
    const isAuthenticated = authStore.isAuthenticated
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    console.log('Navigating to:', to.fullPath, '| Requires Auth:', requiresAuth, '| Is Authenticated:', isAuthenticated); // 添加日志

    if (requiresAuth && !isAuthenticated) {
        console.log('Redirecting to AdminLogin because requiresAuth and not authenticated.');
        next({
            name: 'AdminLogin',
            query: { redirect: to.fullPath }
        })
    } else if (to.name === 'AdminLogin' && isAuthenticated) {
        console.log('Redirecting to AdminDashboard because authenticated and trying to access AdminLogin.');
        next({ name: 'AdminDashboard' })
    } else {
        console.log('Proceeding with navigation.');
        next() // 确保所有分支都有 next() 调用
    }
})

export default router