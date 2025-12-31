import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { authService } from '@/features/auth/api/authService';

// Lazy load components
const NotesPage = () => import('@/pages/NotesPage.vue');
const LoginPage = () => import('@/features/auth/pages/LoginPage.vue');
const RegisterPage = () => import('@/features/auth/pages/RegisterPage.vue');
const EmailVerificationPage = () => import('@/features/auth/components/EmailVerificationPage.vue');

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    redirect: '/notes'
  },
  {
    path: '/notes',
    name: 'notes',
    component: NotesPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPage,
    meta: { requiresGuest: true }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterPage,
    meta: { requiresGuest: true }
  },
  {
    path: '/verify-email',
    name: 'verify-email',
    component: EmailVerificationPage,
    props: (route: any) => ({ token: route.query.token })
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// Navigation guard for authentication
router.beforeEach((to: any, _from: any, next: any) => {
  const isAuthenticated = authService.isAuthenticated();

  // Check if route requires authentication
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'login', query: { redirect: to.fullPath } });
    return;
  }

  // Check if route requires guest (not authenticated)
  if (to.meta.requiresGuest && isAuthenticated) {
    next({ name: 'notes' });
    return;
  }

  next();
});

export default router;

