<template>
  <section>
    <el-card class="hero" shadow="hover" body-style="padding: 18px 18px">
      <div style="display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap">
        <div>
          <h2 style="margin:0 0 6px">快速生成你的专属行程</h2>
          <div class="muted">输入目的地与天数，马上得到可执行方案。</div>
        </div>
        <div class="section-actions">
          <el-button type="primary" @click="generateItinerary" :loading="genLoading">一键生成</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="card" shadow="hover">
      <template #header>
        <div class="card-header">行程参数</div>
      </template>
      <el-form label-width="80">
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="目的地"><el-input v-model="gen.destination" placeholder="如 东京" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="天数"><el-input-number v-model="gen.days" :min="1" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="人数"><el-input-number v-model="gen.people" :min="1" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="预算"><el-input-number v-model="gen.budget" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-button type="primary" @click="generateItinerary" :loading="genLoading">生成行程</el-button>
      </el-form>
    </el-card>

    <el-row :gutter="16" style="margin-top:12px">
      <el-col :xs="24" :md="14">
        <el-card shadow="hover">
          <template #header><div class="card-header">生成结果</div></template>
          <div v-if="!generated" class="small muted">尚未生成</div>
          <div v-else>
            <div class="summary">目的地：{{ generated.destination }}｜天数：{{ generated.days }}｜方案ID：{{ generated.itineraryId }}</div>
            <el-button type="success" :disabled="!canSave" @click="saveItinerary">保存到云端</el-button>
            <span class="small" style="margin-left:8px">{{ saveMsg }}</span>
            <pre style="margin-top:8px">{{ JSON.stringify(generated, null, 2) }}</pre>
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-top:12px">
          <template #header><div class="card-header">地图预览（D1）</div></template>
          <div v-if="mapPoints.length === 0" class="small muted">暂无可渲染的地点（生成或补全后显示）</div>
          <MapView v-else :points="mapPoints" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="hover">
          <template #header><div class="card-header">我的行程</div></template>
          <div v-if="!supabaseReady">需配置 Supabase 才能列出</div>
          <div v-else-if="!userEmail">需登录后查看（前往“个人中心”登录）</div>
          <el-table v-else :data="myItineraries" size="small" style="width:100%">
            <el-table-column label="标题" :min-width="200" v-slot="scope">
              <div><strong>{{ scope.row.title || (scope.row.destination + '·' + (scope.row.days || '')) }}</strong></div>
              <div class="small muted">{{ scope.row.destination }}</div>
            </el-table-column>
            <el-table-column label="操作" width="120" v-slot="scope">
              <el-button link type="danger" @click="removeItinerary(scope.row.id)">删除</el-button>
            </el-table-column>
          </el-table>
          <div style="margin-top:8px"><el-button v-if="supabaseReady && userEmail" size="small" @click="loadItineraries">刷新</el-button></div>
        </el-card>

        <el-card shadow="hover" style="margin-top:12px">
          <template #header><div class="card-header">记一笔预算</div></template>
          <div v-if="!(supabaseReady && userEmail)" class="small muted">登录后使用</div>
          <div v-else class="grid">
            <el-select v-model="selectedItineraryId" placeholder="选择行程" @change="loadBudgetSummary(selectedItineraryId)">
              <el-option v-for="it in myItineraries" :key="it.id" :label="it.title || it.destination" :value="it.id" />
            </el-select>
            <el-input-number v-model="beAmount" :min="0" />
            <el-input v-model="beCategory" placeholder="类别(如 food)" />
            <el-input v-model="beNote" placeholder="备注(可选)" />
            <el-button type="primary" @click="addBudgetEntry">新增</el-button>
            <div class="small">{{ budgetMsg }}</div>
            <div v-if="budgetSummary" class="small">当前行程：{{ budgetSummary.count }} 笔，共计 {{ budgetSummary.total }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <section class="card">
      <h2>快速提示</h2>
      <ul class="bullet-list">
        <li>未配置 Supabase 也可体验行程生成功能；保存/列表需登录。</li>
        <li>预算记账支持简单统计，后续将提供图表与分类汇总。</li>
        <li>地图与地点检索将集成高德 JS SDK，敬请期待。</li>
      </ul>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { supabase, hasSupabaseConfig } from '../lib/supabase';
import MapView from '../components/MapView.vue';

const supabaseReady = hasSupabaseConfig;
const userEmail = ref<string>('');

onMounted(async () => {
  if (!supabaseReady) return;
  const { data } = await supabase.auth.getUser();
  userEmail.value = data.user?.email || '';
  supabase.auth.onAuthStateChange((_, session) => {
    userEmail.value = session?.user?.email || '';
    if (userEmail.value) loadItineraries();
  });
  if (userEmail.value) loadItineraries();
});

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
    await buildMapPoints();
  } catch (e) {
    saveMsg.value = '生成失败';
  } finally {
    genLoading.value = false;
  }
}

const canSave = computed(() => supabaseReady && !!userEmail.value && !!generated.value);

// 地图点位（取第一天活动，若缺经纬度则尝试查询）
const mapPoints = ref<{ name?: string; lat: number; lng: number }[]>([]);

async function buildMapPoints() {
  mapPoints.value = [];
  if (!generated.value) return;
  const day0 = generated.value.dayPlans?.[0];
  if (!day0 || !day0.activities) return;
  const acts = day0.activities as any[];
  const pts: { name?: string; lat: number; lng: number }[] = [];
  for (const a of acts) {
    if (typeof a.lat === 'number' && typeof a.lng === 'number') {
      pts.push({ name: a.name, lat: a.lat, lng: a.lng });
    } else if (a.name) {
      try {
        const res = await fetch('/api/places/search?q=' + encodeURIComponent(a.name));
        const data = await res.json();
        const first = data.items?.[0];
        if (first && typeof first.lat === 'number' && typeof first.lng === 'number') {
          pts.push({ name: a.name, lat: first.lat, lng: first.lng });
        }
      } catch {}
    }
    if (pts.length >= 8) break; // 控制点位数量，避免过多请求
  }
  mapPoints.value = pts;
}

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
  const { error } = await supabase.from('itineraries').insert(insertData).select().single();
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
.itinerary { margin-top: 8px; }
</style>
