#!/usr/bin/env bash
# 步骤 1：初始化数据库
#   1. 用 root 创建数据库并导入 database/schema.sql（含表结构 + 初始数据 + admin 账号）。
#   2. 创建独立业务账号 DB_USERNAME 并授予 museum_reservation 库权限。
# 可重复执行：schema.sql 内部使用 IF NOT EXISTS / ON DUPLICATE KEY，幂等。

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_common.sh"
load_env

SCHEMA_FILE="${PROJECT_ROOT}/database/schema.sql"
if [[ ! -f "${SCHEMA_FILE}" ]]; then
  err "未找到表结构脚本：${SCHEMA_FILE}"
  exit 1
fi

if ! command -v mysql >/dev/null 2>&1; then
  err "未找到 mysql 客户端，请先执行 ./00-install-prereqs.sh"
  exit 1
fi

# shellcheck disable=SC2046
ROOT_ARGS=$(mysql_root_args)

log "导入表结构与初始数据：${SCHEMA_FILE}"
# 本机 root 通常走 socket（无密码或 auth_socket）；远程则用 -h
if [[ "${DB_HOST}" == "127.0.0.1" || "${DB_HOST}" == "localhost" ]]; then
  as_root mysql ${ROOT_ARGS} < "${SCHEMA_FILE}"
else
  mysql -h "${DB_HOST}" -P "${DB_PORT}" ${ROOT_ARGS} < "${SCHEMA_FILE}"
fi

log "创建业务数据库账号：${DB_USERNAME}"
SQL_USER="
CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
ALTER USER '${DB_USERNAME}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USERNAME}'@'%';
CREATE USER IF NOT EXISTS '${DB_USERNAME}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
ALTER USER '${DB_USERNAME}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USERNAME}'@'localhost';
FLUSH PRIVILEGES;
"
if [[ "${DB_HOST}" == "127.0.0.1" || "${DB_HOST}" == "localhost" ]]; then
  echo "${SQL_USER}" | as_root mysql ${ROOT_ARGS}
else
  echo "${SQL_USER}" | mysql -h "${DB_HOST}" -P "${DB_PORT}" ${ROOT_ARGS}
fi

log "验证：统计已创建的表数量 ..."
COUNT_SQL="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}';"
if [[ "${DB_HOST}" == "127.0.0.1" || "${DB_HOST}" == "localhost" ]]; then
  as_root mysql ${ROOT_ARGS} -N -e "${COUNT_SQL}"
else
  mysql -h "${DB_HOST}" -P "${DB_PORT}" ${ROOT_ARGS} -N -e "${COUNT_SQL}"
fi

log "数据库初始化完成（默认管理员 admin / admin123）。"
log "下一步：执行 ./02-build.sh 构建前后端。"
