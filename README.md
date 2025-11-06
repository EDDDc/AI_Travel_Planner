# AI Travel Planner（Web 版 AI 旅行规划师）

一个尽可能简单但可交付的 AI 旅行规划工具。

支持能力
- 语音/文字生成行程（对接阿里云百炼，未配置时回退示例）
- 预算估算与记账（最小版）
- 地图展示与地点检索（高德 JS + REST）
- 账户与云端同步（Supabase）

## 仓库结构
- `server/` — Spring Boot 3 后端（Java 17）
- `web/` — Vue 3 + Vite 前端
- `docs/PRD.md` — 产品需求文档
- `docs/WORKPLAN.md` — 工作清单（勾选进度）

## 本地快速开始
前置：Java 17、Maven、Node.js 18+、npm

1) 启动后端
```
cd server
mvn spring-boot:run
```
健康检查：`GET http://localhost:8080/api/health`

2) 启动前端
```
cd web
npm install
npm run dev
```
开发端口 `5173`，`/api` 已代理至 `http://localhost:8080`

## Docker 一键运行
1) 准备 `.env`
   - 复制根目录 `.env.example` 为 `.env`，按需填写：
     - 服务端密钥：`BAILIAN_API_KEY`、`XF_APP_ID`、`XF_API_KEY`、`XF_API_SECRET`、`AMAP_KEY`
     - 前端构建：`VITE_SUPABASE_URL`、`VITE_SUPABASE_ANON_KEY`
     - 地图（前端构建）：`VITE_AMAP_KEY`（可选 `VITE_AMAP_SECURITY_JSCODE`）

2) 启动
```
docker compose up --build -d
```
前端：`http://localhost:5173`（Nginx）
后端健康：`http://localhost:8080/api/health`

说明
- Nginx 把前端 `/api` 代理到 `server:8080`
- 未配置密钥时，AI/ASR/高德检索使用占位/本地示例，页面仍可体验流程

## 接入 Supabase（可选）
1) 在 Supabase 控制台创建项目，获取 Project URL 与 anon key
2) 前端环境文件 `web/.env.local`：
```
VITE_SUPABASE_URL=你的ProjectURL
VITE_SUPABASE_ANON_KEY=你的AnonKey
```
3) 重启前端 `npm run dev` 后，首页“账户”区域可登录/注册
4) 在 Supabase SQL 编辑器执行 `docs/supabase_schema.sql`（建表与 RLS）

## 环境变量清单
- `BAILIAN_API_KEY`（百炼行程/预算生成）
- `BAILIAN_MODEL`（可选，默认 `qwen-turbo`）
- `BAILIAN_API_BASE`（可选，默认 `https://dashscope.aliyuncs.com/compatible-mode/v1`）
- `XF_APP_ID`、`XF_API_KEY`、`XF_API_SECRET`（讯飞 ASR）
- `AMAP_KEY`（高德 REST，用于 `/api/places/search`）
- `VITE_SUPABASE_URL`、`VITE_SUPABASE_ANON_KEY`（前端登录）
- `VITE_AMAP_KEY`（前端地图 JS Key，可选 `VITE_AMAP_SECURITY_JSCODE`）

后端示例配置：`server/src/main/resources/application.yml`（不包含密钥）

## 地图配置（前端 JS SDK）
- 在 `web/.env.local` 设置：
```
VITE_AMAP_KEY=你的JS_API_Key
# 如开启 JS 安全码：
VITE_AMAP_SECURITY_JSCODE=你的安全码
```
- 高德控制台 Referer 白名单（每行一个 Origin）：
  - 开发：`http://localhost:5173`、`http://127.0.0.1:5173`
  - 生产：`https://你的域名`
  - 保存后等待 5–10 分钟生效

## 常见问题（FAQ）
- 中文乱码：本仓库文档均为 UTF‑8 编码；若仍乱码，请在编辑器选择 UTF‑8 查看
- 接口 404：确保后端已在 8080 端口运行，且前端使用 `npm run dev` 以启用代理

## 贡献
采用约定式提交信息（如 `feat: ...`、`fix: ...`、`docs: ...`）
