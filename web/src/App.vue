<template>
  <el-container>
    <el-header height="64px" class="nav-wrap">
      <div class="nav-left">
        <div class="brand">AI Travel Planner</div>
        <el-menu mode="horizontal" :default-active="active" @select="onSelect" class="nav-menu" router>
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/profile">个人中心</el-menu-item>
        </el-menu>
      </div>
      <div class="nav-right">
        <template v-if="!userEmail">
          <RouterLink class="btn ghost" to="/login">登录</RouterLink>
          <RouterLink class="btn" to="/register">注册</RouterLink>
        </template>
        <template v-else>
          <el-tag type="success">{{ userEmail }}</el-tag>
          <el-button @click="signOut">退出</el-button>
        </template>
        <el-button type="primary" @click="checkHealth">检查后端</el-button>
        <el-tag :type="healthStatus === 'ok' ? 'success' : 'info'" size="large">{{ healthStatus || '未检查' }}</el-tag>
        <span class="now small muted">{{ now }}</span>
      </div>
    </el-header>

    <el-main>
      <div class="container">
        <RouterView />
        <div class="footer">© AI Travel Planner · 仅用于课程与演示用途</div>
      </div>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router';
import { supabase } from './lib/supabase';

const healthStatus = ref<string>('');
const now = ref<string>('');
const router = useRouter();
const route = useRoute();
const active = ref(route.path);
const userEmail = ref('');

async function checkHealth() {
  try {
    const res = await fetch('/api/health');
    const data = await res.json();
    healthStatus.value = data.status || 'unknown';
  } catch (e) {
    healthStatus.value = 'error';
  }
}

onMounted(async () => {
  setInterval(() => { now.value = new Date().toLocaleString(); }, 1000);
  try {
    const { data } = await supabase.auth.getUser();
    userEmail.value = data.user?.email || '';
    supabase.auth.onAuthStateChange((_, session) => {
      userEmail.value = session?.user?.email || '';
    });
  } catch {}
});

watch(() => route.path, (p) => active.value = p);

function onSelect(path: string) {
  router.push(path);
}

async function signOut() {
  try { await supabase.auth.signOut(); } catch {}
}
</script>

<style scoped>
.nav-wrap { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid var(--border); }
.nav-left { display: flex; align-items: center; gap: 16px; }
.brand { font-weight: 800; letter-spacing: .2px; }
.nav-menu { border-bottom: none; }
.nav-right { display: flex; align-items: center; gap: 12px; }
.container { max-width: 1100px; margin: 20px auto 48px; padding: 0 20px; }
.now { margin-left: 8px; }
</style>
