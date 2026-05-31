# 性能 / 并发测试脚本

| 文件 | 作用 |
|---|---|
| `concurrency_test.sh` | 并发预约「不超额/不重复」实测（自动重置名额、并发请求、断言结果） |
| `generate_idcards.sh` | 生成 JMeter 用的唯一身份证/手机号 `idcards.csv` |
| `museum-reservation.jmx` | JMeter 压测计划（核心预约接口，配合 idcards.csv） |

## 快速开始

```bash
# 1) 一致性脚本（需后端已运行 + 已建好测试时段 SLOT_ID=9001）
export DB_PASSWORD=你的业务库密码
./concurrency_test.sh 9001 50 1000 200

# 2) JMeter
./generate_idcards.sh 2000          # 生成 idcards.csv
jmeter -n -t museum-reservation.jmx -l result.jtl   # 命令行运行
#   或用 GUI 打开 museum-reservation.jmx，运行后看「聚合报告」
```

> 测试时段需为 `OPEN` 状态且在预约时间窗内、名额充足。可用管理员后台创建，
> 或参考 `性能测试报告.md` 第 6 节用 SQL 建一个 SLOT_ID=9001 的测试时段。

详细结果与结论见 `project-docs/性能测试报告.md`。
