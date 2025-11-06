<template>
  <main class="container">
    <h1>AI 旅行规划师（Web）</h1>
    <section class="actions">
      <button @click="checkHealth">检查后端健康</button>
      <span class="status" :data-ok="healthStatus === 'ok'">状态：{{ healthStatus || '未检查' }}</span>
    </section>

    <section class="next">
      <h2>接下来要做</h2>
      <ul>
        <li>行程生成最小 API 对接</li>
        <li>语音 ASR 最小 API 对接</li>
        <li>高德地图展示与检索</li>
      </ul>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const healthStatus = ref<string>('');

async function checkHealth() {
  try {
    const res = await fetch('/api/health');
    const data = await res.json();
    healthStatus.value = data.status || 'unknown';
  } catch (e) {
    healthStatus.value = 'error';
  }
}
</script>

<style scoped>
.container { max-width: 760px; margin: 40px auto; padding: 0 16px; font-family: -apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif; }
.actions { display: flex; gap: 12px; align-items: center; }
button { padding: 8px 12px; cursor: pointer; }
.status[data-ok="true"] { color: #2e7d32; }
.status[data-ok="false"] { color: #9c27b0; }
h1 { margin-bottom: 12px; }
.next { margin-top: 24px; }
</style>

