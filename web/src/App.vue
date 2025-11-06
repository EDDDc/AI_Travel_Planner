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

    <section class="itinerary">
      <h2>行程生成与保存（演示）</h2>
      <div class="gen-form">
        <input v-model="gen.destination" placeholder="目的地" />
        <input v-model.number="gen.days" type="number" min="1" placeholder="天数" />
        <input v-model.number="gen.people" type="number" min="1" placeholder="人数" />
        <input v-model.number="gen.budget" type="number" min="0" placeholder="预算（可选）" />
        <button @click="generateItinerary">生成行程</button>
      </div>
      <div v-if="genLoading">生成中...</div>
      <div v-if="generated" class="gen-result">
        <div class="summary">目的地：{{ generated.destination }}｜天数：{{ generated.days }}｜方案ID：{{ generated.itineraryId }}</div>
        <button :disabled="!canSave" @click="saveItinerary">保存到云端</button>
        <div class="small">{{ saveMsg }}</div>
        <pre>{{ JSON.stringify(generated, null, 2) }}</pre>
      </div>

      <div class="list">
        <h3>我的行程</h3>
        <div v-if="!supabaseReady">需配置 Supabase 才能列出</div>
        <div v-else-if="!userEmail">需登录后查看</div>
        <ul v-else>
          <li v-for="it in myItineraries" :key="it.id">
            <strong>{{ it.title || (it.destination + '·' + (it.days || '')) }}</strong>
            <span class="muted">（{{ it.destination }}）</span>
            <button class="ghost" @click="removeItinerary(it.id)">删除</button>
          </li>
        </ul>
        <button v-if="supabaseReady && userEmail" class="ghost" @click="loadItineraries">刷新</button>

        <div v-if="supabaseReady && userEmail" class="budget">
          <h4>记一笔预算</h4>
          <select v-model="selectedItineraryId" @change="loadBudgetSummary(selectedItineraryId)">
            <option value="" disabled>选择行程</option>
            <option v-for="it in myItineraries" :key="it.id" :value="it.id">{{ it.title || it.destination }}</option>
          </select>
          <input v-model.number="beAmount" type="number" min="0" placeholder="金额" />
          <input v-model="beCategory" placeholder="类别(如 food)" />
          <input v-model="beNote" placeholder="备注(可选)" />
          <button @click="addBudgetEntry">新增</button>
          <span class="small">{{ budgetMsg }}</span>
          <div v-if="budgetSummary" class="small">当前行程：{{ budgetSummary.count }} 笔，共计 {{ budgetSummary.total }}</div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
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
    if (userEmail.value) loadItineraries();
  });
  if (userEmail.value) loadItineraries();
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

// 行程生成与保存（演示）
const gen = ref<{ destination: string; days: number; people: number; budget?: number }>({
  destination: '东京',
  days: 2,
  people: 2,
  budget: 6000,
});
const generated = ref<any>(null);
const genLoading = ref(false);
const saveMsg = ref('');
const myItineraries = ref<any[]>([]);

async function generateItinerary() {
  genLoading.value = true;
  saveMsg.value = '';
  try {
    const res = await fetch('/api/itineraries/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...gen.value, preferences: ['美食', '亲子'] }),
    });
    generated.value = await res.json();
  } catch (e) {
    saveMsg.value = '生成失败';
  } finally {
    genLoading.value = false;
  }
}

const canSave = computed(() => supabaseReady && !!userEmail.value && !!generated.value);

async function saveItinerary() {
  if (!canSave.value) return;
  saveMsg.value = '保存中...';
  const user = (await supabase.auth.getUser()).data.user;
  if (!user) { saveMsg.value = '未登录'; return; }
  const title = `${generated.value.destination}·${generated.value.days}天行程`;
  const insertData: any = {
    user_id: user.id,
    title,
    destination: generated.value.destination,
    people: gen.value.people,
    budget_target: gen.value.budget || null,
    preferences_json: { preferences: ['美食', '亲子'] },
  };
  const { data, error } = await supabase.from('itineraries').insert(insertData).select().single();
  if (error) { saveMsg.value = '保存失败：' + error.message; return; }
  saveMsg.value = '保存成功';
  await loadItineraries();
}

async function loadItineraries() {
  if (!supabaseReady) return;
  const { data, error } = await supabase.from('itineraries').select('*').order('created_at', { ascending: false });
  if (!error) myItineraries.value = data || [];
}

async function removeItinerary(id: string) {
  if (!supabaseReady) return;
  await supabase.from('itineraries').delete().eq('id', id);
  await loadItineraries();
}

// 预算记账（最小）
const selectedItineraryId = ref<string>('');
const beAmount = ref<number | null>(null);
const beCategory = ref<string>('food');
const beNote = ref<string>('');
const budgetMsg = ref('');
const budgetSummary = ref<{ count: number; total: number } | null>(null);

async function addBudgetEntry() {
  budgetMsg.value = '';
  if (!supabaseReady || !selectedItineraryId.value || !beAmount.value) {
    budgetMsg.value = '请选择行程并填写金额';
    return;
  }
  const { error } = await supabase.from('budget_entries').insert({
    itinerary_id: selectedItineraryId.value,
    amount: beAmount.value,
    currency: 'CNY',
    category: beCategory.value,
    note: beNote.value,
    source: 'manual',
  });
  budgetMsg.value = error ? ('新增失败：' + error.message) : '新增成功';
  if (!error) {
    beAmount.value = null;
    beNote.value = '';
    await loadBudgetSummary(selectedItineraryId.value);
  }
}

async function loadBudgetSummary(itineraryId: string) {
  if (!supabaseReady || !itineraryId) { budgetSummary.value = null; return; }
  const { data, error } = await supabase.from('budget_entries').select('amount').eq('itinerary_id', itineraryId);
  if (error) { budgetSummary.value = null; return; }
  const total = (data || []).reduce((sum: number, row: any) => sum + Number(row.amount || 0), 0);
  budgetSummary.value = { count: data?.length || 0, total };
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
.itinerary { margin-top: 24px; }
.gen-form { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin-bottom: 8px; }
.gen-result { margin-top: 8px; }
.summary { margin: 8px 0; }
.muted { color: #666; margin: 0 8px; }
.small { color: #555; margin-top: 4px; }
</style>
