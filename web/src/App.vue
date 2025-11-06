<template>
  <main class="container">
    <header class="header">
      <div>
        <h1 class="title">AI 旅行规划师（Web）</h1>
        <p class="subtitle">快速生成行程、轻松记账、地图查看。</p>
      </div>
      <div class="section-actions">
        <button class="btn" @click="checkHealth">检查后端健康</button>
        <span class="status" :data-ok="healthStatus === 'ok'">{{ healthStatus ? ('状态：' + healthStatus) : '状态：未检查' }}</span>
      </div>
    </header>

    <nav class="card" style="display:flex;align-items:center;justify-content:space-between;gap:12px;">
      <div class="section-actions" style="gap:16px">
        <RouterLink class="btn ghost" to="/">首页</RouterLink>
        <RouterLink class="btn ghost" to="/profile">个人中心</RouterLink>
      </div>
      <div class="small muted">{{ now }}</div>
    </nav>

    <RouterView />

    <div class="footer">© AI Travel Planner · 仅用于课程与演示用途</div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, RouterView } from 'vue-router';

const healthStatus = ref<string>('');
const now = ref<string>('');

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
});
</script>

<style scoped>
.auth { margin-top: 8px; }
.next { margin-top: 8px; }
.itinerary { margin-top: 8px; }
.login { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
</style>
