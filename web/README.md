# AI Travel Planner Web (Vue 3 + Vite)

最小可运行的前端脚手架，用于对接后端 /api 与后续功能开发。

## 本地开发

1. 安装依赖：
   npm install

2. 启动开发服务器（含 /api 代理到 http://localhost:8080）：
   npm run dev

3. 构建与预览：
   npm run build
   npm run preview

> 注意：需要后端在 8080 端口运行，Vite 已配置 `server.proxy` 到 `/api`。

## 功能说明（当前）
- 首页展示，并提供“检查后端健康”按钮，调用 `/api/health`。
- 后续将集成行程生成、ASR、地图展示。

