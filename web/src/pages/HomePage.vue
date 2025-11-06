<template>
  <section>
    <el-card class="hero" shadow="hover" body-style="padding: 18px 18px">
      <div style="display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap">
        <div>
          <h2 style="margin:0 0 6px">Generate Your Trip</h2>
          <div class="muted">Enter destination, dates, people and budget.</div>
        </div>
        <div class="section-actions">
          <el-button type="primary" @click="generateItinerary" :loading="genLoading">Generate</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="card" shadow="hover">
      <template #header>
        <div class="card-header">Parameters</div>
      </template>
      <el-form label-width="90">
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="Destination"><el-input v-model="gen.destination" placeholder="e.g. Nanjing" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="8"><el-form-item label="Dates">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="to" start-placeholder="Start" end-placeholder="End" style="width: 100%" @change="onDateChange" />
          </el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="4"><el-form-item label="Days"><el-input-number v-model="gen.days" :min="1" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="People"><el-input-number v-model="gen.people" :min="1" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12" :md="6"><el-form-item label="Budget"><el-input-number v-model="gen.budget" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-button type="primary" @click="generateItinerary" :loading="genLoading">Generate</el-button>
      </el-form>
    </el-card>

    <el-row :gutter="16" style="margin-top:12px">
      <el-col :xs="24" :md="14">
        <el-card shadow="hover">
          <template #header><div class="card-header">Result</div></template>
          <div v-if="!generated" class="small muted">No data yet</div>
          <div v-else>
            <div class="summary">
              {{ generated.destination }} · {{ generated.days }} days · {{ generated.itineraryId }}
              <el-tag size="small" type="info" style="margin-left:8px">source: {{ aiSource }}</el-tag>
            </div>
            <el-button type="success" :disabled="!canSave" @click="saveItinerary">Save</el-button>
            <el-switch v-model="showRaw" active-text="Show raw JSON" style="margin-left:12px" />
            <span class="small" style="margin-left:8px">{{ saveMsg }}</span>
            <div v-if="showRaw"><pre style="margin-top:8px">{{ JSON.stringify(generated, null, 2) }}</pre></div>
            <ItineraryView :itinerary="generated" />
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-top:12px">
          <template #header>
            <div class="card-header" style="display:flex;align-items:center;justify-content:space-between;gap:12px">
              <span>Map Preview</span>
              <div v-if="generated?.dayPlans?.length">
                <el-select v-model="selectedDayIndex" size="small" style="width:160px" @change="(i:number)=>buildMapPointsFromDayIndex(i)">
                  <el-option v-for="(d,i) in generated.dayPlans" :key="i" :label="(d.date ? d.date : ('D' + (i+1)))" :value="i" />
                </el-select>
              </div>
            </div>
          </template>
          <div v-if="mapPoints.length === 0" class="small muted">No points to show</div>
          <MapView v-else :points="mapPoints" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="hover">
          <template #header><div class="card-header">My Itineraries</div></template>
          <div v-if="!supabaseReady">Configure Supabase to list</div>
          <div v-else-if="!userEmail">Login to view</div>
          <el-table v-else :data="myItineraries" size="small" style="width:100%">
            <el-table-column label="Title" :min-width="200" v-slot="scope">
              <div><strong>{{ scope.row.title || (scope.row.destination + ' · ' + (scope.row.days || '')) }}</strong></div>
              <div class="small muted">{{ scope.row.destination }}</div>
            </el-table-column>
            <el-table-column label="Op" width="120" v-slot="scope">
              <el-button link type="danger" @click="removeItinerary(scope.row.id)">Delete</el-button>
            </el-table-column>
          </el-table>
          <div style="margin-top:8px"><el-button v-if="supabaseReady && userEmail" size="small" @click="loadItineraries">Refresh</el-button></div>
        </el-card>

        <el-card shadow="hover" style="margin-top:12px">
          <template #header><div class="card-header">Budget Entry</div></template>
          <div v-if="!(supabaseReady && userEmail)" class="small muted">Login to use</div>
          <div v-else class="grid">
            <el-select v-model="selectedItineraryId" placeholder="Select itinerary" @change="loadBudgetSummary(selectedItineraryId)">
              <el-option v-for="it in myItineraries" :key="it.id" :label="it.title || it.destination" :value="it.id" />
            </el-select>
            <el-input-number v-model="beAmount" :min="0" />
            <el-input v-model="beCategory" placeholder="category (e.g. food)" />
            <el-input v-model="beNote" placeholder="note (optional)" />
            <el-button type="primary" @click="addBudgetEntry">Add</el-button>
            <div class="small">{{ budgetMsg }}</div>
            <div v-if="budgetSummary" class="small">{{ budgetSummary.count }} items, total {{ budgetSummary.total }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { supabase, hasSupabaseConfig } from '../lib/supabase';
import MapView from '../components/MapView.vue';
import ItineraryView from '../components/ItineraryView.vue';

const supabaseReady = hasSupabaseConfig;
const userEmail = ref<string>('');

onMounted(async () => {
  if (!supabaseReady) return;
  const { data } = await supabase.auth.getUser();
  userEmail.value = data.user?.email || '';
  supabase.auth.onAuthStateChange((_event: any, session: any) => {
    userEmail.value = session?.user?.email || '';
    if (userEmail.value) loadItineraries();
  });
  if (userEmail.value) loadItineraries();
});

// form state
const gen = ref<{ destination: string; days: number; people: number; budget?: number }>({
  destination: 'Nanjing',
  days: 2,
  people: 2,
  budget: 3000,
});
const dateRange = ref<[Date, Date] | null>(null);
const startDate = ref<string | undefined>();
const endDate = ref<string | undefined>();

// result state
const generated = ref<any>(null);
const genLoading = ref(false);
const saveMsg = ref('');
const aiSource = ref<string>('unknown');
const showRaw = ref(false);

// list & budget
const myItineraries = ref<any[]>([]);
const selectedItineraryId = ref<string>('');
const beAmount = ref<number | null>(null);
const beCategory = ref<string>('food');
const beNote = ref<string>('');
const budgetMsg = ref('');
const budgetSummary = ref<{ count: number; total: number } | null>(null);

const selectedDayIndex = ref<number>(0);
const mapPoints = ref<{ name?: string; lat: number; lng: number; info?: string }[]>([]);

async function generateItinerary() {
  genLoading.value = true;
  saveMsg.value = '';
  try {
    const res = await fetch('/api/itineraries/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ destination: gen.value.destination, days: gen.value.days, people: gen.value.people, budget: gen.value.budget, startDate: startDate.value, endDate: endDate.value, preferences: ['food', 'kids'] }),
    });
    aiSource.value = res.headers.get('X-AI-Source') || 'unknown';
    generated.value = await res.json();
    selectedDayIndex.value = 0;
    await buildMapPointsFromDayIndex(0);
  } catch (e) {
    saveMsg.value = 'Generate failed';
  } finally {
    genLoading.value = false;
  }
}

const canSave = computed(() => supabaseReady && !!userEmail.value && !!generated.value);

async function buildMapPointsFromDayIndex(i: number) {
  mapPoints.value = [];
  const day = generated.value?.dayPlans?.[i];
  if (!day?.activities?.length) return;
  const pts: { name?: string; lat: number; lng: number; info?: string }[] = [];
  for (const a of day.activities as any[]) {
    const info = [a.start && a.end ? `${a.start} - ${a.end}` : '', a.notes || ''].filter(Boolean).join(' · ');
    if (typeof a.lat === 'number' && typeof a.lng === 'number') {
      pts.push({ name: a.name, lat: a.lat, lng: a.lng, info });
    } else if (a.name) {
      try {
        const resp = await fetch('/api/places/search?q=' + encodeURIComponent(a.name));
        const data = await resp.json();
        const first = data.items?.[0];
        if (first && typeof first.lat === 'number' && typeof first.lng === 'number') {
          pts.push({ name: a.name, lat: first.lat, lng: first.lng, info });
        }
      } catch {}
    }
    if (pts.length >= 12) break;
  }
  mapPoints.value = pts;
}

async function saveItinerary() {
  if (!canSave.value) return;
  saveMsg.value = 'Saving...';
  const user = (await supabase.auth.getUser()).data.user;
  if (!user) { saveMsg.value = 'Not logged in'; return; }
  const title = `${generated.value.destination} · ${generated.value.days} days`;
  const insertData: any = {
    user_id: user.id,
    title,
    destination: generated.value.destination,
    people: gen.value.people,
    budget_target: gen.value.budget || null,
    preferences_json: { preferences: ['food', 'kids'] },
  };
  const { error } = await supabase.from('itineraries').insert(insertData).select().single();
  if (error) { saveMsg.value = 'Save failed: ' + error.message; return; }
  saveMsg.value = 'Saved';
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

async function addBudgetEntry() {
  budgetMsg.value = '';
  if (!supabaseReady || !selectedItineraryId.value || !beAmount.value) {
    budgetMsg.value = 'Select itinerary and amount';
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
  budgetMsg.value = error ? ('Add failed: ' + error.message) : 'Added';
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

function onDateChange() {
  if (!dateRange.value || dateRange.value.length !== 2) {
    startDate.value = undefined;
    endDate.value = undefined;
    return;
  }
  const [s, e] = dateRange.value;
  startDate.value = fmtDate(s);
  endDate.value = fmtDate(e);
  const ms = e.getTime() - s.getTime();
  const days = Math.max(1, Math.ceil(ms / (24*60*60*1000)) + 1);
  gen.value.days = days;
}

function fmtDate(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}
</script>

<style scoped>
.itinerary { margin-top: 8px; }
</style>

