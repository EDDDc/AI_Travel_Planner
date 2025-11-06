<template>
  <div class="map-wrap">
    <div v-if="!amapKey" class="small muted">未配置 VITE_AMAP_KEY，无法加载地图</div>
    <div v-else-if="err" class="small hint">{{ err }}</div>
    <div v-else ref="mapEl" class="map"></div>
  </div>
  
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import AMapLoader from '@amap/amap-jsapi-loader';

type Pt = { name?: string; lat: number; lng: number; info?: string };
const props = defineProps<{ points: Pt[] }>();

const mapEl = ref<HTMLDivElement | null>(null);
let map: any = null;
let markers: any[] = [];
let polyline: any | null = null;
let infoWindow: any | null = null;

const amapKey = import.meta.env.VITE_AMAP_KEY as string | undefined;
const amapJsCode = import.meta.env.VITE_AMAP_SECURITY_JSCODE as string | undefined;
const err = ref('');

async function initMap() {
  if (!amapKey || !mapEl.value) return;
  try {
    // 如果启用了 JS 安全码，需要在加载前注入 window._AMapSecurityConfig
    if (amapJsCode) {
      (window as any)._AMapSecurityConfig = { securityJsCode: amapJsCode };
    }
    const AMap = await AMapLoader.load({
      key: amapKey,
      version: '2.0',
      plugins: ['AMap.ToolBar', 'AMap.Scale'],
      // 兼容 loader 新版参数（有些版本也从这里读取）
      securityJsCode: amapJsCode
    });
    map = new AMap.Map(mapEl.value, { zoom: 11 });
    map.addControl(new AMap.ToolBar());
    map.addControl(new AMap.Scale());
    infoWindow = new AMap.InfoWindow({ offset: new AMap.Pixel(0, -24) });
    renderPoints(AMap, props.points);
  } catch (e: any) {
    console.error('[AMap] load failed', e);
    const msg = (e && (e.message || e.toString())) || '未知错误';
    err.value = `加载高德地图失败：${msg}。请检查 VITE_AMAP_KEY、Referer 白名单与安全码配置。`;
  }
}

function renderPoints(AMap: any, pts: Pt[]) {
  // clear
  if (markers.length) { markers.forEach(m => m.setMap(null)); markers = []; }
  if (polyline) { polyline.setMap(null); polyline = null; }

  const path: any[] = [];
  pts.filter(p => p && typeof p.lat === 'number' && typeof p.lng === 'number')
     .forEach(p => {
       const marker = new AMap.Marker({ position: [p.lng, p.lat], title: p.name || '' });
       marker.on('click', () => {
         if (!infoWindow) return;
         const html = `<div style="min-width:180px"><div style="font-weight:600">${p.name ?? ''}</div><div style="color:#666;margin-top:4px">${p.info ?? ''}</div></div>`;
         infoWindow.setContent(html);
         infoWindow.open(map, marker.getPosition());
       });
       marker.setMap(map);
       markers.push(marker);
       path.push([p.lng, p.lat]);
     });
  if (path.length >= 2) {
    polyline = new AMap.Polyline({ path, strokeColor: '#409eff', strokeWeight: 4, showDir: true });
    polyline.setMap(map);
  }
  if (path.length > 0) {
    map.setFitView(markers);
  }
}

watch(() => props.points, async (v) => {
  if (!map || !amapKey) return;
  const AMap = (window as any).AMap;
  if (AMap) renderPoints(AMap, v || []);
}, { deep: true });

onMounted(initMap);
onBeforeUnmount(() => { map = null; });
</script>

<style scoped>
.map-wrap { width: 100%; height: 360px; }
.map { width: 100%; height: 100%; border-radius: 12px; overflow: hidden; border: 1px solid var(--border); }
</style>
