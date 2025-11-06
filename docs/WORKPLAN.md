# 工作清单（最小可行版�?
## 核心目标
- 满足 todo.txt 的功能要求，尽量简单实现�?- 语音/文字生成行程、预算估算与语音记账、地图展示、云端同步�?- 不在代码中硬编码任何 API Key�?
## 开发步�?1) 搭建后端 Spring Boot 脚手�?2) 搭建前端 Vue3 + Vite 脚手�?3) 行程生成最�?API（阿里百炼，固定 JSON 输出�?4) 语音 ASR 最�?API（讯飞，浏览器录音上传）
5) 高德地点检索代�?API（仅代理查询；前端用 JS SDK 展示标记�?6) 接入 Supabase Auth（前端登录）与最小数据表（行�?活动/记账�?7) 行程与预�?CRUD（前端直�?Supabase，RLS 仅本人可见）
8) 前端 MVP：创建向导（文本/语音）→ 生成行程 �?地图与列表展�?�?语音记账
9) Docker 化与本地一键运行（docker-compose 前后�?Nginx�?10) README 与提�?PDF（含仓库地址与运行说明）
11) 基础健壮性：错误提示、加载态、简单限�?
## 阶段与产�?- 阶段1：脚手架与通路
  - 成果：后端可启动/健康检查；前端首页可访问；环境变量样例
- 阶段2：AI 与语�?  - 成果�?api/itineraries/generate �?/api/voice/asr 可用（Postman 可测�?- 阶段3：地图与检�?  - 成果�?api/places/search 可用；前端地图能显示 POI 标记
- 阶段4：账户与数据
  - 成果：Supabase Auth 登录；三张表�?RLS；前端可保存/加载行程与记�?- 阶段5：交�?  - 成果：docker-compose 一键起；README 完整；导�?PDF

## 数据表（最小化�?- itineraries: id, user_id, title, destination, start_date, end_date, people, budget_target, preferences_json, created_at, updated_at
- activities: id, itinerary_id, date, type, name, lat, lng, start, end, cost_estimate, extra_json
- budget_entries: id, itinerary_id, date, amount, currency, category, note, source, created_at
- RLS：按 user_id = auth.uid() 可读写自身数�?
## 简化约�?- 不做在线预订/支付；地图仅展示 POI 与简单路线�?- 导出先提�?JSON/文本；PDF 仅作为提交材料�?- 所有密钥仅走服务端环境变量；前端不暴露�?- Supabase Auth 走前�?SDK；CRUD 前端直连 Supabase；LLM/ASR/高德走后端代理�?
## 交付�?- 可运行的 docker-compose（前�?后端+Nginx�?- README（环境变量、运行、测试、常见问题）
- 提交 PDF（含仓库地址�?README 要点�?- GitHub Actions（可选，最后阶段添加并推�?ACR�?
