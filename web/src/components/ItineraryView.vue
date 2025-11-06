<template>
  <div class="itinerary-view">
    <div class="header">
      <div class="title">行程概览</div>
      <div v-if="itinerary?.budgetEstimate" class="budget">
        <span>预算合计：{{ itinerary.budgetEstimate.total }}</span>
        <span class="sep">|</span>
        <span>交通：{{ itinerary.budgetEstimate.transport }}</span>
        <span class="sep">|</span>
        <span>住宿：{{ itinerary.budgetEstimate.lodging }}</span>
        <span class="sep">|</span>
        <span>餐饮：{{ itinerary.budgetEstimate.food }}</span>
        <span class="sep">|</span>
        <span>门票：{{ itinerary.budgetEstimate.ticket }}</span>
        <span class="sep">|</span>
        <span>其他：{{ itinerary.budgetEstimate.other }}</span>
      </div>
    </div>

    <div class="pager" v-if="days.length">
      <el-button size="small" :disabled="idx<=0" @click="prev">上一日</el-button>
      <div class="current">{{ currentLabel }}</div>
      <el-button size="small" :disabled="idx>=days.length-1" @click="next">下一日</el-button>
    </div>

    <el-timeline v-if="days.length">
      <el-timeline-item :timestamp="days[idx].date || ('D' + (idx+1))" placement="top">
        <el-card shadow="hover" class="day-card">
          <div class="acts">
            <div v-for="(a, i) in (days[idx].activities || [])" :key="i" class="act">
              <el-tag size="small" :type="tagType(a.type)">{{ a.type }}</el-tag>
              <span class="time" v-if="a.start || a.end">{{ a.start || '?' }} - {{ a.end || '?' }}</span>
              <span class="name">{{ a.name }}</span>
              <span class="cost" v-if="a.costEstimate">¥{{ a.costEstimate }}</span>
              <div class="notes" v-if="a.notes">{{ a.notes }}</div>
            </div>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
  
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
const props = defineProps<{ itinerary: any }>();

const idx = ref(0);
const days = computed(() => props.itinerary?.dayPlans || []);
const currentLabel = computed(() => {
  if (!days.value.length) return '';
  const d = days.value[idx.value];
  return (d?.date || `D${idx.value + 1}`) + ` / ${days.value.length}`;
});

watch(() => props.itinerary, () => { idx.value = 0; }, { deep: false });

function prev() { if (idx.value > 0) idx.value--; }
function next() { if (idx.value < days.value.length - 1) idx.value++; }

function tagType(t?: string) {
  switch ((t || '').toLowerCase()) {
    case 'transport': return 'info';
    case 'sight': return 'success';
    case 'food': return 'warning';
    case 'shopping': return 'danger';
    case 'rest': return '';
    default: return '';
  }
}
</script>

<style scoped>
.itinerary-view { display: grid; gap: 12px; }
.header { display: flex; align-items: center; justify-content: space-between; }
.title { font-weight: 700; }
.budget { color: var(--muted); font-size: 12px; display: flex; gap: 6px; align-items: center; }
.sep { color: #ccc; }
.pager { display: flex; align-items: center; gap: 12px; }
.current { color: var(--muted); font-size: 12px; }
.day-card { border-radius: 10px; }
.act { display: grid; grid-template-columns: auto auto 1fr auto; gap: 8px; align-items: center; padding: 6px 0; }
.act .notes { grid-column: 1 / -1; color: var(--muted); font-size: 12px; }
.time { color: var(--muted); font-size: 12px; }
.name { font-weight: 600; }
.cost { color: #f59e0b; font-weight: 600; font-size: 12px; }
</style>
