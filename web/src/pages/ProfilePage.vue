<template>
  <el-card shadow="hover">
    <template #header><div class="card-header">个人中心</div></template>
    <div v-if="!supabaseReady" class="hint">未配置 Supabase（请在 web/.env.local 设置 VITE_SUPABASE_URL 与 VITE_SUPABASE_ANON_KEY）</div>
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
      <div v-else class="small">请先 <RouterLink to="/login">登录</RouterLink> 或 <RouterLink to="/register">注册</RouterLink></div>
    </div>
  </el-card>

  <el-card shadow="hover" style="margin-top:12px">
    <template #header><div class="card-header">资料与偏好</div></template>
    <div v-if="!userEmail" class="small muted">登录后编辑个人资料</div>
    <div v-else>
      <el-form label-width="90">
        <el-form-item label="昵称"><el-input v-model="profile.display_name" placeholder="显示名称" style="max-width:320px" /></el-form-item>
        <el-form-item label="头像URL"><el-input v-model="profile.avatar_url" placeholder="头像链接（可选）" style="max-width:420px" /></el-form-item>
        <el-form-item label="默认币种"><el-input v-model="pref.currency" placeholder="例如 CNY" style="max-width:220px" /></el-form-item>
        <el-form-item label="语言地区"><el-input v-model="pref.locale" placeholder="例如 zh-CN" style="max-width:220px" /></el-form-item>
        <el-form-item label="AI 模型"><el-input v-model="pref.aiModel" placeholder="阿里百炼模型标识（可选）" style="max-width:320px" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
          <span class="small" style="margin-left:8px">{{ saveMsg }}</span>
        </el-form-item>
      </el-form>
      <div class="small muted" style="margin-top:8px">不在前端存储服务端密钥；如需密钥，将在后端安全存储并通过设置页对接。</div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { supabase, hasSupabaseConfig } from '../lib/supabase';

const supabaseReady = hasSupabaseConfig;
const userEmail = ref<string>('');
const profile = ref<{ display_name?: string; avatar_url?: string }>({});
const pref = ref({ currency: 'CNY', locale: 'zh-CN', aiModel: '' });
const saving = ref(false);
const saveMsg = ref('');

onMounted(async () => {
  if (!supabaseReady) return;
  const { data } = await supabase.auth.getUser();
  userEmail.value = data.user?.email || '';
  supabase.auth.onAuthStateChange((_event: any, session: any) => {
    userEmail.value = session?.user?.email || '';
    if (userEmail.value) loadProfile();
  });
  if (userEmail.value) loadProfile();
});

async function loadProfile() {
  const { data: userData } = await supabase.auth.getUser();
  const uid = userData.user?.id as string | undefined;
  if (!uid) return;
  const { data } = await supabase.from('profiles').select('*').eq('user_id', uid).single();
  if (data) {
    profile.value.display_name = data.display_name || '';
    profile.value.avatar_url = data.avatar_url || '';
    const p = data.preferences || data.preferences_json;
    if (p) {
      pref.value.currency = p.currency || pref.value.currency;
      pref.value.locale = p.locale || pref.value.locale;
      pref.value.aiModel = p.aiModel || pref.value.aiModel;
    }
  }
}

async function saveProfile() {
  saveMsg.value = '';
  const { data: userData } = await supabase.auth.getUser();
  const uid = userData.user?.id as string | undefined;
  if (!uid) { saveMsg.value = '未登录'; return; }
  saving.value = true;
  const upsertData = {
    user_id: uid,
    display_name: profile.value.display_name || null,
    avatar_url: profile.value.avatar_url || null,
    preferences: { currency: pref.value.currency, locale: pref.value.locale, aiModel: pref.value.aiModel },
  };
  const { error } = await supabase.from('profiles').upsert(upsertData, { onConflict: 'user_id' }).select().single();
  saving.value = false;
  saveMsg.value = error ? ('保存失败：' + error.message) : '保存成功';
}

async function signOut() {
  try { await supabase.auth.signOut(); } catch {}
}
</script>

<style scoped>
.row { display: flex; align-items: center; justify-content: space-between; }
.login { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
</style>

