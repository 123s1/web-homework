#!/usr/bin/env bash
# 步骤 0：安装运行依赖（JDK 21、Node.js、MySQL、Nginx、Maven）
# 支持 Ubuntu/Debian（apt）与 CentOS/RHEL（yum/dnf）。
# 可重复执行：已安装的组件会跳过。

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_common.sh"
load_env
detect_pkg_mgr

install_apt() {
  as_root apt-get update -y
  log "安装 OpenJDK 21 / Maven / MySQL / Nginx / curl ..."
  as_root env DEBIAN_FRONTEND=noninteractive apt-get install -y \
    openjdk-21-jdk maven mysql-server nginx curl ca-certificates

  if ! command -v node >/dev/null 2>&1; then
    log "安装 Node.js 20.x ..."
    curl -fsSL https://deb.nodesource.com/setup_20.x | as_root bash -
    as_root env DEBIAN_FRONTEND=noninteractive apt-get install -y nodejs
  fi
}

install_yum() {
  local PM="${PKG_MGR}"
  as_root "${PM}" install -y java-21-openjdk-devel maven nginx curl
  # MySQL：CentOS 默认仓库通常是 mysql-server 或 mysql-community-server
  if ! command -v mysqld >/dev/null 2>&1 && ! command -v mysql >/dev/null 2>&1; then
    as_root "${PM}" install -y mysql-server || as_root "${PM}" install -y mysql-community-server || \
      warn "未能通过默认仓库安装 MySQL，请手动安装 MySQL 8。"
  fi
  if ! command -v node >/dev/null 2>&1; then
    log "安装 Node.js 20.x ..."
    curl -fsSL https://rpm.nodesource.com/setup_20.x | as_root bash -
    as_root "${PM}" install -y nodejs
  fi
}

case "${PKG_MGR}" in
  apt) install_apt ;;
  yum|dnf) install_yum ;;
esac

log "启动并设置 MySQL / Nginx 开机自启 ..."
as_root systemctl enable --now mysql 2>/dev/null || as_root systemctl enable --now mysqld 2>/dev/null || \
  warn "无法通过 systemctl 启动 MySQL，请确认 MySQL 服务名。"
as_root systemctl enable --now nginx 2>/dev/null || warn "无法启动 Nginx。"

log "依赖安装完成。版本信息："
java -version 2>&1 | head -1 || true
mvn -version 2>&1 | head -1 || true
node -v 2>&1 || true
mysql --version 2>&1 || true
nginx -v 2>&1 || true
log "下一步：执行 ./01-init-database.sh 初始化数据库。"
