<template>
  <main class="container">
    <h1>AI 旅行规划师（Web）</h1>
    <section class="actions">
      <button @click="checkHealth">检查后端健康</button>
      <span class="status" :data-ok="healthStatus === 'ok'">状态：{{ healthStatus || '未检查' }}</span>
    </section>

    <section class="auth">
      <h2>账户（Supabase Auth）</h2>
      <div v-if="!supabaseReady" class="hint">未配置 Supabase（请在 web/.env.local 中设置 VITE_SUPABASE_URL 与 VITE_SUPABASE_ANON_KEY）</div>
      <div v-else>
        <div v-if="userEmail">
          <div>已登录：{{ userEmail }}</div>
          <button @click="signOut">退出登录</button>
        </div>
        <div v-else class="login">
          <input v-model="email" type="email" placeholder="邮箱" />
          <input v-model="password" type="password" placeholder="密码" />
          <button @click="signIn">登录</button>
          <button @click="signUp" class="ghost">注册</button>
          <span class="auth-msg">{{ authMsg }}</span>
        </div>
      </div>
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
import { onMounted, ref } from 'vue';
import { supabase, hasSupabaseConfig } from './lib/supabase';

const healthStatus = ref<string>('');
const supabaseReady = hasSupabaseConfig;
const userEmail = ref<string>('');
const email = ref('');
const password = ref('');
const authMsg = ref('');

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
  if (!supabaseReady) return;
  const { data } = await supabase.auth.getUser();
  userEmail.value = data.user?.email || '';
  supabase.auth.onAuthStateChange((event, session) => {
    userEmail.value = session?.user?.email || '';
  });
});

async function signIn() {
  if (!supabaseReady) return;
  authMsg.value = '';
  const { error } = await supabase.auth.signInWithPassword({ email: email.value, password: password.value });
  authMsg.value = error ? error.message : '登录成功';
}

async function signUp() {
  if (!supabaseReady) return;
  authMsg.value = '';
  const { error } = await supabase.auth.signUp({ email: email.value, password: password.value });
  authMsg.value = error ? error.message : '注册成功，请查收验证邮件';
}

async function signOut() {
  if (!supabaseReady) return;
  await supabase.auth.signOut();
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
.auth { margin-top: 24px; }
.login { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
.ghost { background: #fff; border: 1px solid #999; }
.auth-msg { margin-left: 8px; color: #555; }
.hint { color: #b26a00; }
</style>
