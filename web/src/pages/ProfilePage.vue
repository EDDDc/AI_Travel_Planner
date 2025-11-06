<template>
  <section class="card">
    <h2>个人中心</h2>
    <div v-if="!supabaseReady" class="hint">未配置 Supabase（请在 web/.env.local 中设置 VITE_SUPABASE_URL 与 VITE_SUPABASE_ANON_KEY）</div>
    <div v-else>
      <div v-if="userEmail">
        <div class="row">
          <div>
            <div class="small muted">当前账户</div>
            <div><strong>{{ userEmail }}</strong></div>
          </div>
          <div>
            <button class="btn ghost" @click="signOut">退出登录</button>
          </div>
        </div>
      </div>
      <div v-else class="login">
        <input class="input" v-model="email" type="email" placeholder="邮箱" />
        <input class="input" v-model="password" type="password" placeholder="密码" />
        <button class="btn" @click="signIn">登录</button>
        <button class="btn ghost" @click="signUp">注册</button>
        <span class="auth-msg">{{ authMsg }}</span>
      </div>
    </div>
  </section>

  <section class="card">
    <h2>偏好与密钥（占位）</h2>
    <div class="grid">
      <input class="input" v-model="pref.currency" placeholder="默认币种，例如 CNY" />
      <input class="input" v-model="pref.locale" placeholder="语言/地区，例如 zh-CN" />
      <input class="input" v-model="pref.aiModel" placeholder="阿里百炼模型标识（可选）" />
    </div>
    <div class="small muted" style="margin-top:8px">说明：为安全起见，不在前端保存或展示服务端密钥；后续在设置页通过后端安全存储。</div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { supabase, hasSupabaseConfig } from '../lib/supabase';

const supabaseReady = hasSupabaseConfig;
const userEmail = ref<string>('');
const email = ref('');
const password = ref('');
const authMsg = ref('');

onMounted(async () => {
  if (!supabaseReady) return;
  const { data } = await supabase.auth.getUser();
  userEmail.value = data.user?.email || '';
  supabase.auth.onAuthStateChange((_, session) => {
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

const pref = ref({ currency: 'CNY', locale: 'zh-CN', aiModel: '' });
</script>

<style scoped>
.row { display: flex; align-items: center; justify-content: space-between; }
.login { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
</style>

