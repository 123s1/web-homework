#!/usr/bin/env bash
# 步骤 0：安装运行依赖（JDK 21、Node.js、MySQL、Nginx、Maven）
# 支持 Ubuntu/Debian（apt）与 CentOS/RHEL（yum/dnf）。
# 可重复执行：已安装的组件会跳过。

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_common.sh"
load_env
detect_pkg_mgr

# 安装 JDK 21（apt）。Ubuntu 22.04+ 直接用 openjdk-21-jdk；
# Ubuntu 20.04 默认仓库没有 openjdk-21，自动回退到 Adoptium Temurin 21。
ensure_jdk21_apt() {
  if command -v javac >/dev/null 2>&1 && javac -version 2>&1 | grep -q ' 21'; then
    log "已检测到 JDK 21，跳过安装。"; return 0
  fi
  log "安装 OpenJDK 21 ..."
  if apt-cache show openjdk-21-jdk >/dev/null 2>&1; then
    if as_root env DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-21-jdk; then
      return 0
    fi
    warn "openjdk-21-jdk 安装失败，改用 Adoptium Temurin 21。"
  else
    warn "默认仓库无 openjdk-21-jdk（如 Ubuntu 20.04），改用 Adoptium Temurin 21。"
  fi
  # Adoptium Temurin 21（支持 focal/jammy 等）
  as_root env DEBIAN_FRONTEND=noninteractive apt-get install -y wget apt-transport-https gnupg ca-certificates
  as_root mkdir -p /etc/apt/keyrings
  wget -qO- https://packages.adoptium.net/artifactory/api/gpg/key/public \
    | as_root gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
  # shellcheck disable=SC1091
  . /etc/os-release
  echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb ${VERSION_CODENAME} main" \
    | as_root tee /etc/apt/sources.list.d/adoptium.list >/dev/null
  as_root apt-get update -y
  as_root env DEBIAN_FRONTEND=noninteractive apt-get install -y temurin-21-jdk
}

# 将默认 java/javac 指向 JDK 21，避免 maven 拉入的旧 default-jdk 影响编译。
set_default_java21() {
  local j jc
  j="$(update-alternatives --list java 2>/dev/null | grep -E '/(temurin-21|java-21)' | head -1 || true)"
  [[ -n "${j}" ]] && as_root update-alternatives --set java "${j}" >/dev/null 2>&1 || true
  jc="$(update-alternatives --list javac 2>/dev/null | grep -E '/(temurin-21|java-21)' | head -1 || true)"
  [[ -n "${jc}" ]] && as_root update-alternatives --set javac "${jc}" >/dev/null 2>&1 || true
}

install_apt() {
  as_root apt-get update -y
  ensure_jdk21_apt
  log "安装 Maven / MySQL / Nginx / curl ..."
  as_root env DEBIAN_FRONTEND=noninteractive apt-get install -y \
    maven mysql-server nginx curl ca-certificates
  set_default_java21

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
