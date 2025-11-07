# 工作清单（最小可行版）

## 核心目标
- 满足 todo.txt 的功能要求，尽量简单实现。
- 语音/文字生成行程、预算估算与语音记账、地图展示、云端同步。
- 不在代码中硬编码任何 API Key。

## 开发步骤（当前状态）
- [x] 搭建后端 Spring Boot 脚手架
- [x] 搭建前端 Vue3 + Vite 脚手架
- [x] 行程生成最小 API（阿里百炼，固定 JSON 输出 → 已切换真实调用）
- [x] 语音 ASR 最小 API（讯飞，占位 → 已接入 WebSocket 最小版）
- [x] 高德地点检索代理 API（仅代理查询；前端用 JS SDK 展示标记）
- [x] 接入 Supabase Auth（前端登录）与最小数据表（行程/活动/记账）
- [x] 行程与预算 CRUD（前端直连 Supabase，RLS 仅本人可见）
- [x] 导航栏与个人中心页面
- [x] 登录/注册独立页面与跳转
- [x] 用户档案存储与编辑（profiles 表）
- [x] 前端接入高德地图展示（D1/Dn POI 与路径、信息窗、按日切换）
- [x] Docker 化与本地一键运行（docker-compose 前后端+Nginx）
- [x] 讯飞 ASR 真实对接（最小版 WebSocket）
- [x] CI/CD：GitHub Actions 构建并推送镜像、生成 README.pdf（已配置，需在仓库配置 Secrets 并运行）
- [x] README 修复乱码、补充使用与 CI/CD 说明
- [ ] 前端 MVP 完整度（语音创建行程入口 UI，当前仅文本/语音记账）
- [ ] 基础健壮性：错误提示、加载态、简单限流
- [ ] 交付 PDF 最终版（可使用 Actions 产物，或补充截图/流程后导出）

## 阶段与产出
- 阶段1：脚手架与通路
  - 成果：后端可启动/健康检查；前端首页可访问；环境变量样例
- 阶段2：AI 与语音
  - 成果：/api/itineraries/generate 与 /api/voice/asr 可用（阿里百炼真实/讯飞真实最小版）
- 阶段3：地图与检索
  - 成果：/api/places/search 可用；前端地图按日切换、标注信息窗
- 阶段4：账户与数据
  - 成果：Supabase Auth 登录；三张表与 RLS；前端可保存/加载行程与记账；个人资料可编辑
- 阶段5：交付
  - 成果：docker-compose 一键起；README 完整；Actions 可产出 README.pdf；镜像可推 ACR

## 数据表（最小化）
- itineraries: id, user_id, title, destination, start_date, end_date, people, budget_target, preferences_json, created_at, updated_at
- activities: id, itinerary_id, date, type, name, lat, lng, start, end, cost_estimate, extra_json
- budget_entries: id, itinerary_id, date, amount, currency, category, note, source, created_at
- profiles: user_id(PK), display_name, avatar_url, preferences(json), created_at, updated_at
- RLS：按 user_id = auth.uid() 可读写自身数据

## 简化约定
- 不做在线预订/支付；地图仅展示 POI 与简单路径（路线规划可后续增强）。
- 导出先提供 README.pdf（CI 产物）/JSON/文本；提交材料按要求整理。
- 所有密钥仅走服务端环境变量或前端本地 env 文件；前端不暴露服务端密钥。
- Supabase Auth 走前端 SDK；CRUD 前端直连 Supabase；LLM/ASR/高德走后端代理。

## 交付物
- 可运行的 docker-compose（前端+后端+Nginx）
- README（环境变量、运行、CI/CD、常见问题）
- PDF（Actions 产物或本地导出）
- GitHub Actions（构建并推送至 ACR，需配置 Secrets）
