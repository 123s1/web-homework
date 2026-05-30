# 博物馆预约系统打开与检查指南

本文档用于快速启动并检查博物馆预约系统网站。

## 1. 项目目录

项目根目录：

```text
E:\web-homework\museum-reservation-system
```

前端目录：

```text
E:\web-homework\museum-reservation-system\frontend
```

后端目录：

```text
E:\web-homework\museum-reservation-system\backend
```

## 2. 启动后端

后端需要连接 MySQL 数据库。

进入后端目录：

```powershell
cd E:\web-homework\museum-reservation-system\backend
```

运行交互式启动脚本：

```powershell
.\start-backend-interactive.ps1
```

如果 PowerShell 提示“禁止运行脚本”，使用下面的一次性绕过命令：

```powershell
powershell.exe -ExecutionPolicy Bypass -File .\start-backend-interactive.ps1
```

看到提示后输入 MySQL 密码：

```text
Input MySQL password:
```

启动成功后，窗口中应出现类似信息：

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

## 3. 启动前端

进入前端目录：

```powershell
cd E:\web-homework\museum-reservation-system\frontend
```

启动前端开发服务器：

```powershell
npm.cmd run dev
```

启动成功后会显示：

```text
Local: http://127.0.0.1:5173/
```

网站访问地址：

```text
http://127.0.0.1:5173
```

## 4. 游客端检查流程

打开网站：

```text
http://127.0.0.1:5173
```

按以下顺序检查：

1. 首页
   - 检查场馆名称、地址、开放信息是否显示。
   - 检查公告列表是否显示。
   - 检查中文是否乱码。

2. 游客实名登录
   - 进入游客登录页面。
   - 输入姓名、身份证号和手机号。
   - 示例测试数据：

```text
姓名：测试游客
身份证号：110101199001010047
手机号：13900000047
```

3. 预约时段
   - 登录后进入预约时段页面。
   - 检查日期筛选是否可用。
   - 检查时段卡片是否显示容量、已预约人数和剩余名额。
   - 选择一个剩余名额大于 0 的时段提交预约。

4. 预约结果
   - 检查预约编号是否显示。
   - 检查游客姓名、参观日期、预约时段是否正确。
   - 检查二维码内容是否显示。

5. 我的预约
   - 进入我的预约页面。
   - 输入身份证号查询。
   - 检查刚提交的预约是否出现在列表中。

## 5. 管理员端检查流程

管理员登录信息：

```text
用户名：admin
密码：admin123
```

按以下顺序检查：

1. 管理员登录
   - 输入管理员账号和密码。
   - 检查登录成功后是否进入后台首页。

2. 后台首页
   - 检查游客数量、预约数量、总容量、已预约人数、剩余名额。
   - 检查各时段预约概览是否显示。

3. 场馆信息管理
   - 检查场馆信息是否能加载。
   - 可尝试修改一处无风险内容后保存。
   - 保存后回首页查看是否同步变化。

4. 公告通知管理
   - 检查公告列表。
   - 可新增一条测试公告。
   - 检查编辑、启用/停用、删除功能。

5. 预约活动管理
   - 检查活动列表。
   - 检查筛选功能。
   - 可新增或编辑测试活动。

6. 预约时段管理
   - 检查时段列表。
   - 检查活动和日期筛选。
   - 检查新增、编辑、启用/停用功能。

7. 预约记录管理
   - 按参观日期、时段、身份证号、状态筛选。
   - 检查预约记录列表字段是否完整。
   - 点击 CSV 导出，检查是否下载文件。

8. 系统状态
   - 检查系统状态是否为 UP。
   - 检查数据库状态是否为 UP。
   - 点击刷新状态按钮。

## 6. 构建检查

前端构建命令：

```powershell
cd E:\web-homework\museum-reservation-system\frontend
npm.cmd run build
```

正常结果：

```text
✓ built successfully
```

## 7. 常见问题

### 7.1 网站打不开

检查前端是否启动：

```text
http://127.0.0.1:5173
```

如果打不开，重新进入前端目录执行：

```powershell
npm.cmd run dev
```

### 7.2 页面接口报错

检查后端是否启动：

```text
http://127.0.0.1:8080/api/health
```

如果打不开，进入后端目录执行：

```powershell
.\start-backend-interactive.ps1
```

### 7.3 数据库状态 DOWN

检查：

1. MySQL 服务是否启动。
2. 数据库名是否为 `museum_reservation`。
3. 启动后端时输入的 MySQL 密码是否正确。

### 7.4 命令行中文乱码

PowerShell 输出中文乱码通常是控制台编码显示问题。

优先以浏览器页面显示为准。

### 7.5 CSV 无法用 PowerShell 检查

PowerShell 的 `Invoke-WebRequest` 有时会对下载响应处理异常。

优先用浏览器点击导出按钮验证 CSV 下载。

## 8. 当前已验证通过的内容

已验证通过：

- 后端健康检查。
- 数据库连接。
- 前端 Vite 启动。
- 前端 `/api` 代理。
- 游客登录。
- 预约提交。
- 我的预约查询。
- 管理员登录。
- 后台统计。
- 系统状态。
- 预约记录查询。
- CSV 导出接口。
- 前端生产构建。

## 9. 推荐演示顺序

正式演示时建议按以下顺序：

1. 打开首页介绍场馆信息和公告。
2. 游客实名登录。
3. 查询并提交预约。
4. 展示预约结果。
5. 查询我的预约。
6. 管理员登录后台。
7. 展示后台统计。
8. 管理公告、活动和时段。
9. 查询预约记录并导出 CSV。
10. 查看系统状态。
