// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from "@/store/auth.js";
import {ElMessage} from "element-plus";

const routes = [
    {
        path: '/',
        name: 'Home',
        component: HomeView
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/LoginView.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/RegisterView.vue')
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
        path: '/dashboard', // 不再是 /admin，而是 /dashboard
        name: 'DashboardLayout', // 布局的路由名称
        component: () => import('../layouts/DashboardLayout.vue'), // 指向新的通用布局
        meta: { requiresAuth: true }, // 访问这个布局下的所有页面都需要登录
        redirect: '/dashboard/my-posts', // 登录后默认跳转到“我的文章”
        children: [
            // --- 面向所有登录用户的路由 ---
            {
                path: 'my-posts',
                name: 'MyPosts', // 用户的文章列表
                component: () => import('../views/dashboard/MyPostListView.vue'), // 需要创建这个新组件
                meta: { title: '我的文章' }
            },
            {
                path: 'posts/create',
                name: 'PostCreate', // 路由名称去掉 "Admin" 前缀，更通用
                component: () => import('../views/dashboard/PostEditView.vue'), // 组件可以复用，但可能需要放到 dashboard 目录下
                meta: { title: '写文章' }
            },
            {
                path: 'posts/edit/:id',
                name: 'PostEdit', // 路由名称去掉 "Admin" 前缀
                component: () => import('../views/dashboard/PostEditView.vue'),
                props: true,
                meta: { title: '编辑文章' }
            },
            {
                path: 'profile',
                name: 'Profile', // 个人资料页面
                component: () => import('../views/dashboard/ProfileView.vue'),
                meta: { title: '个人资料' }
            },

            // --- 仅限管理员可见的路由 ---
            {
                path: 'admin/all-posts',
                name: 'AdminAllPosts',
                component: () => import('../views/dashboard/admin/AllPostListView.vue'), // 管理员查看所有文章的列表
                meta: { title: '文章管理', requiresAdmin: true } // 使用自定义 meta 字段来标记需要管理员权限
            },
            {
                path: 'admin/categories',
                name: 'AdminCategories',
                component: () => import('../views/dashboard/admin/CategoryListView.vue'),
                meta: { title: '分类管理', requiresAdmin: true }
            },
            {
                path: 'admin/tags',
                name: 'AdminTags',
                component: () => import('../views/dashboard/admin/TagListView.vue'),
                meta: { title: '标签管理', requiresAdmin: true }
            },
            {
                path: 'admin/settings', // 设置页面通常也是管理员专属
                name: 'AdminSettings',
                component: () => import('../views/dashboard/admin/SettingsView.vue'),
                meta: { title: '系统设置', requiresAdmin: true }
            }
        ]

    },
    {
        path: '/logout',
        name: 'Logout',
        beforeEnter: (to, from, next) => {
            const authStore = useAuthStore();

            authStore.logout();
            ElMessage.success('您已成功退出登录。');

            next({ name: 'Login' });
        }
    },

    // 404 页面
    {
        path: '/:pathMatch(.*)*', // 匹配所有未匹配到的路径
        name: 'NotFound',
        component: () => import('../views/NotFoundView.vue')
    },
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
    const authStore = useAuthStore();

    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);

    // 如果用户已登录
    if (authStore.isAuthenticated) {
        if (to.name === 'Login' || to.name === 'Register') {
            // 已登录用户访问登录/注册页，直接跳转到他们的仪表盘首页
            next({ name: 'MyPosts' });
        } else if (requiresAdmin && !authStore.isAdmin) {
            // 需要管理员权限，但当前用户不是管理员
            ElMessage.error('您没有权限访问此页面！');
            // 跳转到用户的默认页面，或者来源页，或者一个专门的403页面
            next({ name: 'MyPosts' });
        } else {
            // 其他情况（已登录访问需要登录的页面，或已登录的管理员访问需要管理员的页面）都正常放行
            next();
        }
    } else { // 如果用户未登录
        if (requiresAuth) {
            // 需要登录但未登录，跳转到登录页
            ElMessage.warning('请先登录以访问此页面。');
            next({ name: 'Login', query: { redirect: to.fullPath } });
        } else {
            // 访问公共页面，正常放行
            next();
        }
    }
});

export default router