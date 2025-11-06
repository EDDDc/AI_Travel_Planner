import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import HomePage from './pages/HomePage.vue';
import ProfilePage from './pages/ProfilePage.vue';

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: HomePage },
  { path: '/profile', name: 'profile', component: ProfilePage }
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

