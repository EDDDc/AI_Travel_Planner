# AI Travel Planner锛圵eb 鐗?AI 鏃呰瑙勫垝甯堬級

涓€涓€滃敖鍙兘绠€鍗曗€濈殑 AI 鏃呰瑙勫垝宸ュ叿鍘熷瀷銆傛敮鎸侊細
- 璇煶/鏂囧瓧杈撳叆鏃呰闇€姹傦紝鐢熸垚涓€у寲琛岀▼锛堝悗缁帴鍏ラ樋閲岀櫨鐐硷級
- 棰勭畻浼扮畻涓庤闊宠璐︼紙鍚庣画瀹炵幇锛?- 鍦板浘灞曠ず涓庡湴鐐规绱紙鍚庣画鎺ュ叆楂樺痉锛?- 璐︽埛涓庝簯绔悓姝ワ紙鍚庣画鎺ュ叆 Supabase锛?
褰撳墠鐘舵€侊細宸插畬鎴愭渶灏忓彲杩愯鐨勫悗绔笌鍓嶇鑴氭墜鏋讹紝鍙皟鐢ㄥ悗绔仴搴锋鏌ャ€?
## 浠撳簱缁撴瀯
- `server/` 鈥?Spring Boot 3 鍚庣锛圝ava 17锛?- `web/` 鈥?Vue 3 + Vite 鍓嶇
- `docs/PRD.md` 鈥?浜у搧闇€姹傛枃妗?- `docs/WORKPLAN.md` 鈥?宸ヤ綔娓呭崟锛堝嬀閫夎繘搴︼級

## 蹇€熷紑濮?
鍓嶇疆鏉′欢锛欽ava 17銆丮aven銆丯ode.js 18+銆乶pm銆?
1) 鍚姩鍚庣
```
cd server
mvn spring-boot:run
```
榛樿绔彛 `8080`锛屽仴搴锋鏌?`GET http://localhost:8080/api/health`銆?
2) 鍚姩鍓嶇
```
cd web
npm install
npm run dev
```
寮€鍙戠鍙?`5173`锛屽凡灏?`/api` 浠ｇ悊鍒?`http://localhost:8080`銆?
鎵撳紑娴忚鍣ㄨ闂?`http://localhost:5173`锛岀偣鍑烩€滄鏌ュ悗绔仴搴封€濓紝搴旀樉绀?`ok`銆?
## Docker 涓€閿繍琛?
1) 鍑嗗鐜鍙橀噺
   - 澶嶅埗鏍圭洰褰?`.env.example` 涓?`.env`锛屾寜闇€濉啓锛?     - 鏈嶅姟鍣ㄧ瀵嗛挜锛歚BAILIAN_API_KEY`銆乣XF_APP_ID`銆乣XF_API_KEY`銆乣XF_API_SECRET`銆乣AMAP_KEY`
     - 鍓嶇鏋勫缓鍙橀噺锛歚VITE_SUPABASE_URL`銆乣VITE_SUPABASE_ANON_KEY`锛堢敤浜庢瀯寤?web 闈欐€佺珯鐐癸級

2) 鏋勫缓骞跺惎鍔?```
docker compose up --build -d
```
   - 鍓嶇璁块棶锛歨ttp://localhost:5173 锛圢ginx锛?   - 鍚庣鍋ュ悍锛歨ttp://localhost:8080/api/health

璇存槑
- Nginx 宸插皢鍓嶇 `/api` 浠ｇ悊鍒板悗绔?`server:8080`銆?- 鑻ユ湭璁剧疆瀵嗛挜锛孉I/ASR/楂樺痉妫€绱㈠皢浣跨敤鍗犱綅/鏈湴绀轰緥锛岄〉闈粛鍙綋楠屽熀鏈祦绋嬨€?
### 鎺ュ叆 Supabase锛堝彲閫夛級
1) 鍦?Supabase 鎺у埗鍙板垱寤洪」鐩紝鑾峰彇 `Project URL` 涓?`anon key`銆?2) 鍓嶇閰嶇疆鐜鍙橀噺锛氬鍒?`web/.env.example` 涓?`web/.env.local` 骞跺～鍐欙細
```
VITE_SUPABASE_URL=浣犵殑ProjectURL
VITE_SUPABASE_ANON_KEY=浣犵殑AnonKey
```
3) 鍓嶇閲嶅惎 `npm run dev`锛岄〉闈⑩€滆处鎴封€濆尯鍩熷彲娉ㄥ唽/鐧诲綍锛堥偖绠?瀵嗙爜锛夈€?4) 鏁版嵁琛ㄤ笌 RLS锛氬皢 `docs/supabase_schema.sql` 鍦?Supabase SQL 缂栬緫鍣ㄦ墽琛屻€?
## 鐜鍙橀噺锛堝崰浣嶏級
涓洪伩鍏嶅湪浠ｇ爜涓‖缂栫爜瀵嗛挜锛屼互涓嬪€奸€氳繃鐜娉ㄥ叆锛?- `BAILIAN_API_KEY` 鈥?闃块噷浜戠櫨鐐硷紙琛岀▼涓庨绠楃敓鎴愶級
- `XF_APP_ID`銆乣XF_API_KEY`銆乣XF_API_SECRET` 鈥?绉戝ぇ璁锛堣闊宠瘑鍒級
- `AMAP_KEY` 鈥?楂樺痉鍦板浘 Web 鏈嶅姟/JS SDK锛堟牴鎹帴鍏ユ柟寮忓尯鍒嗭級

鍚庣绀轰緥閰嶇疆鍙傝 `server/src/main/resources/application.yml`锛堜笉鍖呭惈瀵嗛挜锛夈€?
## 璁″垝涓庤寖鍥?- 璇︾粏 PRD锛氳 `docs/PRD.md`
- 寮€鍙戞竻鍗曚笌杩涘害锛氳 `docs/WORKPLAN.md`
- 鎻愪氦瑕佹眰锛氫笉灏嗕换浣?API Key 鍐欏叆浠ｇ爜锛涙渶缁堟彁渚?README銆佸彲杩愯鐨?Docker 闀滃儚涓?PDF 璇存槑锛堝悗缁ˉ鍏咃級銆?
## 甯歌闂锛團AQ锛?- 涓枃涔辩爜锛氭湰浠撳簱鏂囨。浣跨敤 UTF-8锛堝繀瑕佹椂甯?BOM锛夛紝鑻ヤ粛涔辩爜锛岃妫€鏌?IDE 缂栫爜璁剧疆銆?- 鎺ュ彛 404锛氱‘淇濆悗绔凡鍦?8080 绔彛杩愯锛屼笖鍓嶇浣跨敤 `npm run dev` 浠ュ惎鐢ㄤ唬鐞嗐€?
## 璐＄尞
閲囩敤绾﹀畾寮忔彁浜や俊鎭紙濡?`feat: ...`銆乣fix: ...`銆乣docs: ...`锛夈€?

### 地图配置（前端 JS SDK）
- 前端变量：在 web/.env.local 设置
  - VITE_AMAP_KEY=你的JS_API_Key`n  - 可选：VITE_AMAP_SECURITY_JSCODE=你的JS安全码（若在高德控制台开启了安全码）
- 高德控制台 Referer 白名单建议（每行一个 Origin）：
  - 开发：http://localhost:5173、http://127.0.0.1:5173`n  - 生产：https://yourdomain.com（按需添加子域）
  - 保存后需等待 5–10 分钟生效
- 若白名单留空，等同不限来源（不推荐用于生产）。生产环境请开启白名单并启用 JS 安全码。

