# 博物馆预约系统同学运行说明

这份文档给拿到项目压缩包的同学使用，用于在本地 Windows 环境运行博物馆预约系统。

## 1. 项目说明

项目名称：博物馆预约系统

技术栈：

- 后端：Java 21 + Spring Boot 3.3.5 + Maven
- 前端：Vue 3 + Vite + Axios + Vue Router + Bootstrap
- 数据库：MySQL 8.0

项目结构：

```text
museum-reservation-system
├─ backend      后端 Spring Boot 项目
├─ frontend     前端 Vue 3 项目
├─ database     数据库建表和初始化脚本
├─ scripts      辅助脚本
├─ WEBSITE_CHECK_GUIDE.md
└─ RUN_FOR_CLASSMATE.md
```

## 2. 需要安装的软件

运行前请确认电脑已经安装：

1. Java 21

```powershell
java -version
```

2. Node.js 和 npm

```powershell
node -v
npm -v
```

建议 Node.js 版本为 18 或更高。

3. MySQL 8.0

请确认 MySQL 服务已经启动，并记住本机 MySQL 的 root 密码。

## 3. 解压项目

把压缩包解压到任意英文路径或简单中文路径，例如：

```text
D:\museum-reservation-system
```

后续命令中的路径请根据你的实际解压目录调整。

## 4. 初始化数据库

进入项目根目录：

```powershell
cd D:\museum-reservation-system
```

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS museum_reservation
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

导入数据库脚本：

```powershell
mysql -u root -p museum_reservation < database\schema.sql
```

执行后输入你的 MySQL root 密码。

初始化完成后，默认管理员账号为：

```text
用户名：admin
密码：admin123
```

## 5. 启动后端

进入后端目录：

```powershell
cd D:\museum-reservation-system\backend
```

推荐使用下面命令启动后端：

```powershell
powershell.exe -ExecutionPolicy Bypass -File .\start-backend-interactive.ps1
```

看到提示后输入 MySQL 密码：

```text
Input MySQL password:
```

如果启动成功，会看到类似：

```text
Started MuseumReservationApplication
```

后端地址：

```text
http://127.0.0.1:8080
```

健康检查地址：

```text
http://127.0.0.1:8080/api/health
```

正常结果应包含：

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "database": "UP"
  }
}
```

## 6. 启动前端

另开一个 PowerShell 窗口，进入前端目录：

```powershell
cd D:\museum-reservation-system\frontend
```

第一次运行需要安装前端依赖：

```powershell
npm.cmd install
```

启动前端：

```powershell
npm.cmd run dev
```

正常会显示：

```text
Local: http://127.0.0.1:5173/
```

浏览器打开：

```text
http://127.0.0.1:5173
```

如果提示 5173 被占用，Vite 会自动换成 5174 或其他端口，以控制台实际显示的地址为准。

## 7. 游客端使用流程

1. 打开网站首页。
2. 查看场馆信息和公告。
3. 进入游客登录页面。
4. 输入姓名、身份证号和手机号。
5. 选择可预约时段。
6. 提交预约。
7. 查看预约结果。
8. 到“我的预约”页面输入身份证号查询预约记录。

可用测试数据：

```text
姓名：测试游客
身份证号：110101199001010047
手机号：13900000047
```

## 8. 管理员端使用流程

管理员登录信息：

```text
用户名：admin
密码：admin123
```

登录后可以使用：

- 后台首页统计
- 场馆信息管理
- 公告通知管理
- 预约活动管理
- 预约时段管理
- 预约记录查询
- CSV 导出
- 系统状态查看

## 9. 前端构建检查

进入前端目录：

```powershell
cd D:\museum-reservation-system\frontend
```

执行构建：

```powershell
npm.cmd run build
```

正常结果：

```text
✓ built successfully
```

## 10. 常见问题

### 10.1 无法运行 ps1 脚本

如果直接运行：

```powershell
.\start-backend-interactive.ps1
```

提示禁止运行脚本，请改用：

```powershell
powershell.exe -ExecutionPolicy Bypass -File .\start-backend-interactive.ps1
```

### 10.2 后端健康检查失败

检查：

1. MySQL 服务是否启动。
2. 数据库是否叫 `museum_reservation`。
3. 是否已经导入 `database\schema.sql`。
4. 启动后端时输入的 MySQL 密码是否正确。
5. 8080 端口是否被其他程序占用。

### 10.3 前端页面接口报错

先确认后端健康检查是否正常：

```text
http://127.0.0.1:8080/api/health
```

后端正常后，再刷新前端页面。

### 10.4 前端端口不是 5173

如果控制台显示：

```text
Port 5173 is in use, trying another one...
Local: http://127.0.0.1:5174/
```

就访问控制台显示的新地址。

### 10.5 命令行中文乱码

PowerShell 输出中文乱码通常是控制台编码问题，优先以浏览器页面显示为准。

## 11. 推荐演示顺序

1. 首页展示场馆信息和公告。
2. 游客实名登录。
3. 查询并提交预约。
4. 展示预约结果。
5. 查询我的预约。
6. 管理员登录后台。
7. 展示后台统计。
8. 管理公告、活动和时段。
9. 查询预约记录并导出 CSV。
10. 查看系统状态。
