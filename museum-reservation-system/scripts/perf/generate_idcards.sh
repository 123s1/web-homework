#!/usr/bin/env bash
# 生成 JMeter 用的唯一身份证/手机号数据文件 idcards.csv
# 每行：idcard,phone（无表头，配合 CSV Data Set Config）
# 用法：./generate_idcards.sh [数量]   默认 2000
set -euo pipefail
COUNT="${1:-2000}"
OUT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/idcards.csv"
: > "${OUT}"
for ((i=1; i<=COUNT; i++)); do
  printf '110101199001%06d,139%08d\n' "${i}" "${i}" >> "${OUT}"
done
echo "已生成 ${COUNT} 行：${OUT}"
