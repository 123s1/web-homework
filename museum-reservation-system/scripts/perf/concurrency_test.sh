#!/usr/bin/env bash
# 并发预约「不超额 / 不重复」实测脚本
# 原理：对同一个时段，用 N 个不同身份证号高并发提交预约（N 远大于名额 M），
#       验证「成功数 == M」「booked_count == M」「无超额、无重复」。
#
# 用法：
#   export DB_PASSWORD=Museum@123456            # 业务库密码
#   ./concurrency_test.sh [SLOT_ID] [CAPACITY] [REQUESTS] [PARALLEL] [BASE_URL]
# 默认：SLOT_ID=9001 CAPACITY=50 REQUESTS=500 PARALLEL=100 BASE_URL=http://127.0.0.1:8080
#
# 依赖：curl、mysql 客户端、xargs（GNU）

set -euo pipefail

SLOT_ID="${1:-9001}"
CAPACITY="${2:-50}"
REQUESTS="${3:-500}"
PARALLEL="${4:-100}"
BASE_URL="${5:-http://127.0.0.1:8080}"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-museum_reservation}"
DB_USERNAME="${DB_USERNAME:-museum}"
DB_PASSWORD="${DB_PASSWORD:-Museum@123456}"

export MYSQL_PWD="${DB_PASSWORD}"
MYSQL="mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USERNAME} ${DB_NAME} -N -B"

echo "==== 并发预约压测 ===="
echo "时段ID=${SLOT_ID}  名额=${CAPACITY}  请求数=${REQUESTS}  并发=${PARALLEL}  目标=${BASE_URL}"

# 1) 重置时段名额 + 清理本时段历史预约，保证可重复执行
${MYSQL} -e "DELETE FROM reservation_record WHERE slot_id=${SLOT_ID};"
${MYSQL} -e "UPDATE reservation_slot SET booked_count=0, version=0 WHERE id=${SLOT_ID};"

WORKDIR="$(mktemp -d)"
trap 'rm -rf "${WORKDIR}"' EXIT

# 2) 单次请求函数：第 i 个用户用唯一身份证号
one_request() {
  local i="$1"
  local idcard
  local phone
  idcard="$(printf '110101199001%06d' "${i}")"   # 12 + 6 = 18 位
  phone="$(printf '139%08d' "${i}")"              # 11 位
  curl -s -o /dev/null -w '%{http_code}\n' \
    -X POST "${BASE_URL}/api/reservations" \
    -H 'Content-Type: application/json' \
    -d "{\"name\":\"压测用户${i}\",\"idCard\":\"${idcard}\",\"phone\":\"${phone}\",\"slotId\":${SLOT_ID}}"
}
export -f one_request
export BASE_URL SLOT_ID

# 3) 高并发发起请求，记录每个请求的 HTTP 状态码
echo "开始并发请求 ..."
START_TS=$(date +%s.%N)
seq 1 "${REQUESTS}" | xargs -P "${PARALLEL}" -I {} bash -c 'one_request "$@"' _ {} > "${WORKDIR}/codes.txt"
END_TS=$(date +%s.%N)

# 4) 统计结果
SUCCESS=$(grep -c '^200$' "${WORKDIR}/codes.txt" || true)
FAIL=$(grep -vc '^200$' "${WORKDIR}/codes.txt" || true)
ELAPSED=$(awk "BEGIN{printf \"%.2f\", ${END_TS}-${START_TS}}")
TPS=$(awk "BEGIN{printf \"%.1f\", ${REQUESTS}/(${END_TS}-${START_TS})}")

BOOKED=$(${MYSQL} -e "SELECT booked_count FROM reservation_slot WHERE id=${SLOT_ID};")
RECORDS=$(${MYSQL} -e "SELECT COUNT(*) FROM reservation_record WHERE slot_id=${SLOT_ID};")
DISTINCT=$(${MYSQL} -e "SELECT COUNT(DISTINCT id_card) FROM reservation_record WHERE slot_id=${SLOT_ID};")

echo
echo "==== 结果 ===="
echo "总耗时(s):        ${ELAPSED}"
echo "吞吐(req/s):      ${TPS}"
echo "HTTP 200 成功数:  ${SUCCESS}"
echo "被拒绝(名额满等): ${FAIL}"
echo "DB booked_count:  ${BOOKED}"
echo "DB 预约记录数:    ${RECORDS}"
echo "DB 不同身份证数:  ${DISTINCT}  (用于验证无重复)"
echo

# 5) 断言
RC=0
if [[ "${SUCCESS}" == "${CAPACITY}" && "${BOOKED}" == "${CAPACITY}" && "${RECORDS}" == "${CAPACITY}" ]]; then
  echo "[PASS] 成功数 = booked_count = 记录数 = 名额(${CAPACITY})，未超额。"
else
  echo "[FAIL] 数值不一致：成功=${SUCCESS} booked=${BOOKED} 记录=${RECORDS} 名额=${CAPACITY}"
  RC=1
fi
if [[ "${RECORDS}" == "${DISTINCT}" ]]; then
  echo "[PASS] 记录数 = 不同身份证数，无重复预约。"
else
  echo "[FAIL] 存在重复：记录=${RECORDS} 不同身份证=${DISTINCT}"
  RC=1
fi
exit "${RC}"
