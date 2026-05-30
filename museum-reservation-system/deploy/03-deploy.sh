#!/usr/bin/env bash
# 步骤 3：发布并启动服务（需要 root / sudo）
#   1. 创建运行用户与部署目录 APP_HOME。
#   2. 拷贝后端 jar、前端 dist、写入 backend.env。
#   3. 安装 systemd 服务（后端）并启动。
#   4. 安装 Nginx 站点配置（前端 + /api 反代）并重载。

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_common.sh"
load_env

JAR_SRC="$(ls -1 "${PROJECT_ROOT}"/backend/target/*.jar 2>/dev/null | grep -v '\.original$' | head -1 || true)"
DIST_SRC="${PROJECT_ROOT}/frontend/dist"
if [[ -z "${JAR_SRC}" ]]; then
  err "未找到后端 jar，请先执行 ./02-build.sh"
  exit 1
fi
if [[ ! -d "${DIST_SRC}" ]]; then
  err "未找到前端 dist，请先执行 ./02-build.sh"
  exit 1
fi

# 1. 运行用户
if ! id "${RUN_USER}" >/dev/null 2>&1; then
  log "创建运行用户：${RUN_USER}"
  as_root useradd --system --no-create-home --shell /usr/sbin/nologin "${RUN_USER}" || \
    as_root useradd --system --shell /sbin/nologin "${RUN_USER}"
fi

# 2. 部署目录与产物
log "准备部署目录：${APP_HOME}"
as_root mkdir -p "${APP_HOME}/frontend" "${APP_HOME}/logs"
as_root cp "${JAR_SRC}" "${APP_HOME}/museum-reservation-backend.jar"
as_root rm -rf "${APP_HOME}/frontend"/*
as_root cp -r "${DIST_SRC}"/* "${APP_HOME}/frontend/"

log "写入后端环境变量文件：${APP_HOME}/backend.env"
as_root tee "${APP_HOME}/backend.env" >/dev/null <<EOF
DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
DB_NAME=${DB_NAME}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
SERVER_PORT=${SERVER_PORT}
EOF
# Spring Boot 读取 SERVER_PORT 需要映射为 server.port
as_root sed -i '/^SERVER_PORT=/d' "${APP_HOME}/backend.env"
echo "SERVER_PORT=${SERVER_PORT}" | as_root tee -a "${APP_HOME}/backend.env" >/dev/null
echo "SPRING_APPLICATION_JSON={\"server\":{\"port\":${SERVER_PORT}}}" | as_root tee -a "${APP_HOME}/backend.env" >/dev/null

as_root chown -R "${RUN_USER}:${RUN_USER}" "${APP_HOME}"
as_root chmod 640 "${APP_HOME}/backend.env"

# 3. systemd 服务
log "安装 systemd 服务：museum-backend"
TMP_SVC="$(mktemp)"
sed -e "s|__RUN_USER__|${RUN_USER}|g" \
    -e "s|__APP_HOME__|${APP_HOME}|g" \
    "${DEPLOY_DIR}/museum-backend.service.template" > "${TMP_SVC}"
as_root cp "${TMP_SVC}" /etc/systemd/system/museum-backend.service
rm -f "${TMP_SVC}"
as_root systemctl daemon-reload
as_root systemctl enable museum-backend
as_root systemctl restart museum-backend

# 4. Nginx 站点
log "安装 Nginx 站点配置"
TMP_NGINX="$(mktemp)"
sed -e "s|__HTTP_PORT__|${HTTP_PORT}|g" \
    -e "s|__SERVER_PORT__|${SERVER_PORT}|g" \
    -e "s|__APP_HOME__|${APP_HOME}|g" \
    "${DEPLOY_DIR}/nginx-museum.conf.template" > "${TMP_NGINX}"

if [[ -d /etc/nginx/sites-available ]]; then
  # Debian/Ubuntu 风格
  as_root cp "${TMP_NGINX}" /etc/nginx/sites-available/museum.conf
  as_root ln -sf /etc/nginx/sites-available/museum.conf /etc/nginx/sites-enabled/museum.conf
  as_root rm -f /etc/nginx/sites-enabled/default
else
  # CentOS/RHEL 风格
  as_root cp "${TMP_NGINX}" /etc/nginx/conf.d/museum.conf
fi
rm -f "${TMP_NGINX}"

# Nginx 需要有权限读取 APP_HOME/frontend
as_root chmod o+x "${APP_HOME}" "${APP_HOME}/frontend" 2>/dev/null || true

as_root nginx -t
as_root systemctl reload nginx || as_root systemctl restart nginx

log "部署完成！"
log "  后端服务状态： systemctl status museum-backend"
log "  后端日志：     ${APP_HOME}/logs/backend.log"
log "  访问地址：     http://<本机IP>:${HTTP_PORT}/"
log "  健康检查：     curl http://127.0.0.1:${SERVER_PORT}/api/health"
