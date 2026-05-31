#!/usr/bin/env bash
# 一键部署：依次执行 安装依赖 -> 初始化数据库 -> 构建 -> 发布启动。
# 用法：
#   cp deploy.env.example deploy.env   # 按需修改数据库密码等
#   sudo ./deploy-all.sh
#
# 如需跳过依赖安装（已装好 JDK/Node/MySQL/Nginx），可：
#   SKIP_INSTALL=1 sudo ./deploy-all.sh

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/_common.sh"
load_env

cd "${DEPLOY_DIR}"

if [[ "${SKIP_INSTALL:-0}" != "1" ]]; then
  log "===== 步骤 0/3：安装依赖 ====="
  bash ./00-install-prereqs.sh
else
  warn "已设置 SKIP_INSTALL=1，跳过依赖安装。"
fi

log "===== 步骤 1/3：初始化数据库 ====="
bash ./01-init-database.sh

log "===== 步骤 2/3：构建前后端 ====="
bash ./02-build.sh

log "===== 步骤 3/3：发布并启动 ====="
bash ./03-deploy.sh

log "===== 全部完成 ====="
log "浏览器访问： http://<虚拟机IP>:${HTTP_PORT}/"
log "管理员后台： http://<虚拟机IP>:${HTTP_PORT}/  (admin / admin123)"
