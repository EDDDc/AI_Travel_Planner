<template>
  <el-card shadow="hover" class="card">
    <template #header><div class="card-header">登录</div></template>
    <div class="login">
      <el-input v-model="email" placeholder="邮箱" style="max-width:320px" />
      <el-input v-model="password" type="password" placeholder="密码" style="max-width:320px" />
      <el-button type="primary" :loading="loading" @click="signIn">登录</el-button>
      <div class="small muted">还没有账号？<RouterLink to="/register">去注册</RouterLink></div>
      <div class="small">{{ msg }}</div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { supabase, hasSupabaseConfig } from '../lib/supabase';

const route = useRoute();
const router = useRouter();
const email = ref('');
const password = ref('');
const loading = ref(false);
const msg = ref('');

async function signIn() {
  msg.value = '';
  if (!hasSupabaseConfig) { msg.value = '未配置 Supabase'; return; }
  loading.value = true;
  const { error } = await supabase.auth.signInWithPassword({ email: email.value, password: password.value });
  loading.value = false;
  if (error) { msg.value = error.message; return; }
  const redirect = (route.query.redirect as string) || '/';
  router.replace(redirect);
}
</script>

<style scoped>
.login { display: grid; gap: 12px; align-items: start; }
</style>

