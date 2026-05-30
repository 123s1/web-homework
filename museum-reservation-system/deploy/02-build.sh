#!/usr/bin/env bash
# 步骤 2：构建前后端产物
#   后端：mvn clean package -DskipTests  ->  backend/target/*.jar
#   前端：npm install + npm run build     ->  frontend/dist
# 前端使用 .env.production（VITE_API_BASE_URL=/api），由 Nginx 反向代理到后端。

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_common.sh"
load_env

# ---- 后端 ----
log "构建后端 jar 包 ..."
pushd "${PROJECT_ROOT}/backend" >/dev/null
if [[ -x ./mvnw ]]; then
  ./mvnw -q clean package -DskipTests
else
  mvn -q clean package -DskipTests
fi
JAR_FILE="$(ls -1 target/*.jar 2>/dev/null | grep -v '\.original$' | head -1 || true)"
if [[ -z "${JAR_FILE}" ]]; then
  err "后端构建失败：target 下未找到 jar。"
  exit 1
fi
log "后端构建完成：backend/${JAR_FILE}"
popd >/dev/null

# ---- 前端 ----
log "构建前端静态文件 ..."
pushd "${PROJECT_ROOT}/frontend" >/dev/null
# 生产环境 API 走相对路径 /api，由 Nginx 代理
if [[ ! -f .env.production ]]; then
  echo "VITE_API_BASE_URL=/api" > .env.production
  log "已生成 frontend/.env.production"
fi
if [[ -f package-lock.json ]]; then
  npm ci || npm install
else
  npm install
fi
npm run build
if [[ ! -d dist ]]; then
  err "前端构建失败：未生成 dist 目录。"
  exit 1
fi
log "前端构建完成：frontend/dist"
popd >/dev/null

log "构建全部完成。下一步：执行 sudo ./03-deploy.sh 发布并启动服务。"
