<template>
  <el-card shadow="hover">
    <template #header><div class="card-header">个人中心</div></template>
    <div v-if="!supabaseReady" class="hint">未配置 Supabase（请在 web/.env.local 中设置 VITE_SUPABASE_URL 与 VITE_SUPABASE_ANON_KEY）</div>
    <div v-else>
      <div v-if="userEmail" class="row">
        <div>
          <div class="small muted">当前账户</div>
          <div><strong>{{ userEmail }}</strong></div>
        </div>
        <div>
          <el-button @click="signOut">退出登录</el-button>
        </div>
      </div>
      <div v-else>
        <el-tabs type="card">
          <el-tab-pane label="登录">
            <div class="login">
              <el-input v-model="email" placeholder="邮箱" style="max-width:260px" />
              <el-input v-model="password" type="password" placeholder="密码" style="max-width:260px" />
              <el-button type="primary" @click="signIn">登录</el-button>
              <span class="small">{{ authMsg }}</span>
            </div>
          </el-tab-pane>
          <el-tab-pane label="注册">
            <div class="login">
              <el-input v-model="email" placeholder="邮箱" style="max-width:260px" />
              <el-input v-model="password" type="password" placeholder="密码" style="max-width:260px" />
              <el-button @click="signUp">注册</el-button>
              <span class="small">{{ authMsg }}</span>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </el-card>

  <el-card shadow="hover" style="margin-top:12px">
    <template #header><div class="card-header">偏好与密钥（占位）</div></template>
    <div class="grid">
      <el-input v-model="pref.currency" placeholder="默认币种，例如 CNY" />
      <el-input v-model="pref.locale" placeholder="语言/地区，例如 zh-CN" />
      <el-input v-model="pref.aiModel" placeholder="阿里百炼模型标识（可选）" />
    </div>
    <div class="small muted" style="margin-top:8px">说明：为安全起见，不在前端保存或展示服务端密钥；后续在设置页通过后端安全存储。</div>
  </el-card>
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
