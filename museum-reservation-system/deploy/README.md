# 博物馆预约系统 · Linux / VMware 部署说明

本目录提供在 **Linux 虚拟机（Ubuntu 或 CentOS）** 上部署博物馆预约系统的全部脚本与配置，
满足大作业「虚拟机部署（VMware + Linux + JDK + MySQL）」的要求。

部署后架构：

```text
宿主机浏览器
    ↓  http://<虚拟机IP>:80
Nginx（监听 80，托管前端 dist + 反向代理 /api）
    ↓  /api -> http://127.0.0.1:8080
Spring Boot 后端（systemd 服务 museum-backend，JDK 21）
    ↓  JDBC
MySQL 8（museum_reservation 库）
```

---

## 0. 虚拟机环境要求

- VMware Workstation / Player 新建虚拟机。
- 操作系统：Ubuntu 22.04（推荐）或 CentOS 7/8；亦兼容 **Ubuntu 20.04**（脚本会在 20.04 上自动改用 Adoptium Temurin 21 安装 JDK 21）。
- **内存固定分配 8GB（8192MB）**（作业硬性要求）。
- 网络模式建议 **NAT** 或 **桥接**，保证宿主机能访问虚拟机 IP。
- 虚拟机内可联网（脚本需要在线安装 JDK/Node/MySQL/Nginx）。

查看虚拟机 IP：

```bash
ip addr | grep inet
```

---

## 1. 文件清单

| 文件 | 作用 |
|---|---|
| `deploy.env.example` | 部署配置示例（数据库、端口、目录） |
| `_common.sh` | 公共函数库（被其它脚本引用，不单独执行） |
| `00-install-prereqs.sh` | 安装 JDK 21 / Maven / Node / MySQL / Nginx |
| `01-init-database.sh` | 创建数据库、导入表结构、创建业务账号 |
| `02-build.sh` | 构建后端 jar 与前端 dist |
| `03-deploy.sh` | 发布产物、安装 systemd 服务与 Nginx 站点并启动 |
| `deploy-all.sh` | 一键执行上述 0→3 全流程 |
| `museum-backend.service.template` | 后端 systemd 服务模板 |
| `nginx-museum.conf.template` | Nginx 站点配置模板 |

---

## 2. 一键部署（推荐）

```bash
# 1) 把整个项目拷贝到虚拟机，例如 /home/user/museum-reservation-system
cd museum-reservation-system/deploy

# 2) 准备配置（按需修改数据库密码、端口）
cp deploy.env.example deploy.env
vi deploy.env

# 3) 一键部署（需要 sudo）
sudo ./deploy-all.sh
```

执行完成后，在宿主机浏览器访问：

```text
http://<虚拟机IP>/
```

默认管理员账号：`admin / admin123`

> 若依赖已手动装好，可跳过安装步骤：`SKIP_INSTALL=1 sudo ./deploy-all.sh`

---

## 3. 分步部署（排查问题时使用）

```bash
cd museum-reservation-system/deploy
cp deploy.env.example deploy.env   # 首次必须

sudo ./00-install-prereqs.sh   # 安装依赖
./01-init-database.sh          # 初始化数据库（root 连接 MySQL）
./02-build.sh                  # 构建前后端
sudo ./03-deploy.sh            # 发布并启动服务
```

---

## 4. 配置项说明（deploy.env）

| 变量 | 含义 | 默认值 |
|---|---|---|
| `DB_HOST` / `DB_PORT` | MySQL 地址端口 | `127.0.0.1` / `3306` |
| `DB_NAME` | 数据库名 | `museum_reservation` |
| `DB_USERNAME` / `DB_PASSWORD` | 后端使用的业务账号 | `museum` / `Museum@123456` |
| `MYSQL_ROOT_PASSWORD` | 仅初始化时用的 root 密码（全新 apt 安装常为空） | 空 |
| `SERVER_PORT` | 后端端口 | `8080` |
| `HTTP_PORT` | Nginx 对外端口 | `80` |
| `APP_HOME` | 部署目录（jar / 前端 / 日志） | `/opt/museum-reservation` |
| `RUN_USER` | 运行后端的系统用户 | `museum` |

> `deploy.env` 含数据库密码，已被 `.gitignore` 忽略，不会提交到仓库。

---

## 5. 服务管理与日志

```bash
# 后端服务
sudo systemctl status museum-backend
sudo systemctl restart museum-backend
sudo journalctl -u museum-backend -f
tail -f /opt/museum-reservation/logs/backend.log

# Nginx
sudo systemctl reload nginx
sudo nginx -t

# 健康检查
curl http://127.0.0.1:8080/api/health
```

---

## 6. 宿主机访问与防火墙

确保虚拟机放行 80 端口（如开启防火墙）：

```bash
# Ubuntu (ufw)
sudo ufw allow 80/tcp

# CentOS (firewalld)
sudo firewall-cmd --permanent --add-port=80/tcp && sudo firewall-cmd --reload
```

然后在宿主机浏览器访问 `http://<虚拟机IP>/` 即可使用全部功能。

---

## 7. 常见问题

- **后端起不来 / database=DOWN**：检查 `deploy.env` 的数据库账号密码，确认已执行 `01-init-database.sh`；
  查看 `/opt/museum-reservation/logs/backend.log`。
- **页面能开但接口 502**：后端未启动，`sudo systemctl status museum-backend` 查看；确认 8080 端口。
- **root 连接 MySQL 报错**：全新 apt 安装的 MySQL root 走 socket，需用 `sudo` 运行 `01-init-database.sh`（脚本已处理）；
  若 root 设了密码，请在 `deploy.env` 填 `MYSQL_ROOT_PASSWORD`。
- **CentOS 上 MySQL 服务名是 `mysqld`**：脚本已兼容 `mysql` 与 `mysqld` 两种服务名。

---

## 8. 验证清单（部署完成后逐项确认）

- [ ] `curl http://127.0.0.1:8080/api/health` 返回 `status=UP, database=UP`
- [ ] 宿主机浏览器打开 `http://<虚拟机IP>/` 显示前台首页
- [ ] 管理员可用 `admin / admin123` 登录后台
- [ ] 游客可完成「登录 → 选时段 → 预约 → 查看凭证/二维码」
- [ ] `sudo systemctl status museum-backend` 与 `nginx` 均为 active
