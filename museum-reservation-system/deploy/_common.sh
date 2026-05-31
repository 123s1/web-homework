#!/usr/bin/env bash
# 公共函数库：被其它部署脚本 source 引用，不单独执行。
# 提供：日志输出、配置加载、包管理器检测、root 权限封装。

set -euo pipefail

# 颜色日志
log()  { printf '\033[1;32m[INFO]\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[WARN]\033[0m %s\n' "$*"; }
err()  { printf '\033[1;31m[ERROR]\033[0m %s\n' "$*" >&2; }

# 关键目录
DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${DEPLOY_DIR}/.." && pwd)"

# 加载 deploy.env（不存在时回退到 deploy.env.example 并提示）
load_env() {
  if [[ -f "${DEPLOY_DIR}/deploy.env" ]]; then
    # shellcheck disable=SC1091
    set -a; source "${DEPLOY_DIR}/deploy.env"; set +a
    log "已加载配置：${DEPLOY_DIR}/deploy.env"
  else
    warn "未找到 deploy.env，使用 deploy.env.example 的默认值。"
    warn "建议执行：cp ${DEPLOY_DIR}/deploy.env.example ${DEPLOY_DIR}/deploy.env 后修改。"
    # shellcheck disable=SC1091
    set -a; source "${DEPLOY_DIR}/deploy.env.example"; set +a
  fi

  # 默认值兜底
  DB_HOST="${DB_HOST:-127.0.0.1}"
  DB_PORT="${DB_PORT:-3306}"
  DB_NAME="${DB_NAME:-museum_reservation}"
  DB_USERNAME="${DB_USERNAME:-museum}"
  DB_PASSWORD="${DB_PASSWORD:-Museum@123456}"
  MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-}"
  SERVER_PORT="${SERVER_PORT:-8080}"
  HTTP_PORT="${HTTP_PORT:-80}"
  APP_HOME="${APP_HOME:-/opt/museum-reservation}"
  RUN_USER="${RUN_USER:-museum}"
}

# 检测包管理器：apt（Ubuntu/Debian）或 yum/dnf（CentOS/RHEL）
detect_pkg_mgr() {
  if command -v apt-get >/dev/null 2>&1; then
    PKG_MGR="apt"
  elif command -v dnf >/dev/null 2>&1; then
    PKG_MGR="dnf"
  elif command -v yum >/dev/null 2>&1; then
    PKG_MGR="yum"
  else
    err "未检测到 apt/yum/dnf，无法自动安装依赖。请手动安装 JDK21、Node、MySQL、Nginx。"
    exit 1
  fi
  log "检测到包管理器：${PKG_MGR}"
}

# 以 root 执行命令（已是 root 直接跑，否则用 sudo）
as_root() {
  if [[ "$(id -u)" -eq 0 ]]; then
    "$@"
  else
    sudo "$@"
  fi
}

# 构造 mysql root 连接参数
mysql_root_args() {
  if [[ -n "${MYSQL_ROOT_PASSWORD}" ]]; then
    printf -- '-uroot -p%s' "${MYSQL_ROOT_PASSWORD}"
  else
    printf -- '-uroot'
  fi
}
