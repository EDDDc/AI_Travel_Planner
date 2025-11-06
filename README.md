# AI Travel Planner（Web 版 AI 旅行规划师）

一个“尽可能简单”的 AI 旅行规划工具原型。支持：
- 语音/文字输入旅行需求，生成个性化行程（后续接入阿里百炼）
- 预算估算与语音记账（后续实现）
- 地图展示与地点检索（后续接入高德）
- 账户与云端同步（后续接入 Supabase）

当前状态：已完成最小可运行的后端与前端脚手架，可调用后端健康检查。

## 仓库结构
- `server/` — Spring Boot 3 后端（Java 17）
- `web/` — Vue 3 + Vite 前端
- `docs/PRD.md` — 产品需求文档
- `docs/WORKPLAN.md` — 工作清单（勾选进度）

## 快速开始

前置条件：Java 17、Maven、Node.js 18+、npm。

1) 启动后端
```
cd server
mvn spring-boot:run
```
默认端口 `8080`，健康检查 `GET http://localhost:8080/api/health`。

2) 启动前端
```
cd web
npm install
npm run dev
```
开发端口 `5173`，已将 `/api` 代理到 `http://localhost:8080`。

打开浏览器访问 `http://localhost:5173`，点击“检查后端健康”，应显示 `ok`。

## 环境变量（占位）
为避免在代码中硬编码密钥，以下值通过环境注入：
- `BAILIAN_API_KEY` — 阿里云百炼（行程与预算生成）
- `XF_APP_ID`、`XF_API_KEY`、`XF_API_SECRET` — 科大讯飞（语音识别）
- `AMAP_KEY` — 高德地图 Web 服务/JS SDK（根据接入方式区分）

后端示例配置参见 `server/src/main/resources/application.yml`（不包含密钥）。

## 计划与范围
- 详细 PRD：见 `docs/PRD.md`
- 开发清单与进度：见 `docs/WORKPLAN.md`
- 提交要求：不将任何 API Key 写入代码；最终提供 README、可运行的 Docker 镜像与 PDF 说明（后续补充）。

## 常见问题（FAQ）
- 中文乱码：本仓库文档使用 UTF-8（必要时带 BOM），若仍乱码，请检查 IDE 编码设置。
- 接口 404：确保后端已在 8080 端口运行，且前端使用 `npm run dev` 以启用代理。

## 贡献
采用约定式提交信息（如 `feat: ...`、`fix: ...`、`docs: ...`）。

