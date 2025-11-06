<template>
  <el-card shadow="hover" class="card">
    <template #header><div class="card-header">注册</div></template>
    <div class="login">
      <el-input v-model="email" placeholder="邮箱" style="max-width:320px" />
      <el-input v-model="password" type="password" placeholder="密码" style="max-width:320px" />
      <el-button type="primary" :loading="loading" @click="signUp">注册</el-button>
      <div class="small muted">已有账号？<RouterLink to="/login">去登录</RouterLink></div>
      <div class="small">{{ msg }}</div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { supabase, hasSupabaseConfig } from '../lib/supabase';

const route = useRoute();
const router = useRouter();
const email = ref('');
const password = ref('');
const loading = ref(false);
const msg = ref('');

async function signUp() {
  msg.value = '';
  if (!hasSupabaseConfig) { msg.value = '未配置 Supabase'; return; }
  loading.value = true;
  const { error } = await supabase.auth.signUp({ email: email.value, password: password.value });
  loading.value = false;
  if (error) { msg.value = error.message; return; }
  msg.value = '注册成功，请查收验证邮件';
  const redirect = (route.query.redirect as string) || '/';
  setTimeout(() => router.replace(redirect), 800);
}
</script>

<style scoped>
.login { display: grid; gap: 12px; align-items: start; }
</style>

