import { createApp } from 'vue';
import App from './App.vue';
import './style.css';
import { router } from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';

createApp(App)
  .use(router)
  .use(ElementPlus, { locale: zhCn })
  .mount('#app');
