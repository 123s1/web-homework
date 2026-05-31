#!/usr/bin/env bash
#
# build-deliverables.sh
# 一键生成「省博物馆参观预约系统」大作业最终交付物：
#   1) 把全部 Markdown 文档导出为中文 PDF（pandoc + wkhtmltopdf + Noto/文泉驿 中文字体）
#   2) 打包最终压缩包 学号-姓名-博物馆预约系统.zip（源代码 + 文档 PDF + 测试报告 + 部署脚本）
#
# 用法：
#   bash scripts/build-deliverables.sh            # 仅生成 PDF，到 dist/pdf/
#   bash scripts/build-deliverables.sh 2021001 张三  # 生成 PDF 并打包 dist/2021001-张三-博物馆预约系统.zip
#
# 依赖（Ubuntu/Debian）：
#   sudo apt-get install -y pandoc wkhtmltopdf fonts-noto-cjk zip
# CentOS 可用：sudo yum install -y pandoc wkhtmltopdf google-noto-cjk-fonts zip
#
set -euo pipefail

# 仓库内目录定位：脚本位于 museum-reservation-system/scripts/
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MRS_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"          # museum-reservation-system
REPO_DIR="$(cd "$MRS_DIR/.." && pwd)"            # 仓库根
DIST_DIR="$REPO_DIR/dist"
PDF_DIR="$DIST_DIR/pdf"

STUDENT_ID="${1:-}"
STUDENT_NAME="${2:-}"

command -v pandoc >/dev/null     || { echo "[ERR] 缺少 pandoc，请先安装"; exit 1; }
command -v wkhtmltopdf >/dev/null|| { echo "[ERR] 缺少 wkhtmltopdf，请先安装"; exit 1; }

mkdir -p "$PDF_DIR"

# 中文 PDF 样式
CSS_FILE="$DIST_DIR/.pdf-style.css"
cat > "$CSS_FILE" <<'CSS'
body { font-family: "Noto Sans CJK SC", "WenQuanYi Zen Hei", sans-serif; font-size: 12pt; line-height: 1.6; color: #1a1a1a; }
h1 { font-size: 20pt; border-bottom: 2px solid #333; padding-bottom: 6px; }
h2 { font-size: 16pt; border-bottom: 1px solid #ccc; padding-bottom: 4px; margin-top: 22px; }
h3 { font-size: 13pt; margin-top: 16px; }
code, pre { font-family: "WenQuanYi Zen Hei Mono", monospace; background: #f5f5f5; }
pre { padding: 10px; border-radius: 4px; border: 1px solid #e0e0e0; white-space: pre-wrap; word-wrap: break-word; }
table { border-collapse: collapse; width: 100%; margin: 12px 0; }
th, td { border: 1px solid #bbb; padding: 6px 10px; text-align: left; }
th { background: #f0f0f0; }
img { max-width: 100%; height: auto; }
blockquote { color: #555; border-left: 4px solid #ddd; padding-left: 12px; margin-left: 0; }
CSS

# md_to_pdf <源md> <pdf文件名(不含路径)> <标题> <资源目录(图片相对路径基准)>
md_to_pdf() {
  local src="$1" out="$2" title="$3" resdir="${4:-}"
  if [[ ! -f "$src" ]]; then echo "[WARN] 跳过(不存在): $src"; return 0; fi
  # 把临时 HTML 放到资源目录(文档所在目录)，使 Markdown 里 screenshots/xx.png 等相对图片路径能被 wkhtmltopdf 正确解析
  local html_dir="${resdir:-$PDF_DIR}"
  local html="$html_dir/.tmp_build.html"
  pandoc "$src" -f gfm -t html5 -s --metadata title="$title" -c "$CSS_FILE" -o "$html"
  wkhtmltopdf --enable-local-file-access --encoding utf-8 \
    --margin-top 16mm --margin-bottom 16mm --margin-left 14mm --margin-right 14mm \
    "$html" "$PDF_DIR/$out" >/dev/null 2>&1
  rm -f "$html"
  echo "[OK] $out"
}

echo "== 1/2 生成中文 PDF 到 $PDF_DIR =="
RP="$REPO_DIR/project-docs"
MP="$MRS_DIR/project-docs"
md_to_pdf "$RP/系统设计文档.md"   "01-系统设计文档.pdf"   "省博物馆参观预约系统 · 系统设计文档"   "$RP"
md_to_pdf "$RP/需求拆分.md"       "02-需求拆分.pdf"       "省博物馆参观预约系统 · 需求拆分"       "$RP"
md_to_pdf "$RP/数据库设计.md"     "03-数据库设计.pdf"     "省博物馆参观预约系统 · 数据库设计"     "$RP"
md_to_pdf "$RP/后端接口设计.md"   "04-后端接口设计.pdf"   "省博物馆参观预约系统 · 后端接口设计"   "$RP"
md_to_pdf "$RP/前端开发设计.md"   "05-前端开发设计.pdf"   "省博物馆参观预约系统 · 前端开发设计"   "$RP"
md_to_pdf "$MP/功能测试报告.md"   "06-功能测试报告.pdf"   "省博物馆参观预约系统 · 功能测试报告"   "$MP"
md_to_pdf "$MP/性能测试报告.md"   "07-性能测试报告.pdf"   "省博物馆参观预约系统 · 性能测试报告"   "$MP"

if [[ -z "$STUDENT_ID" || -z "$STUDENT_NAME" ]]; then
  echo ""
  echo "PDF 已生成完毕（dist/pdf/）。"
  echo "如需打包最终压缩包，请追加学号与姓名重新运行，例如："
  echo "  bash scripts/build-deliverables.sh 2021001 张三"
  exit 0
fi

echo "== 2/2 打包最终压缩包 =="
command -v zip >/dev/null || { echo "[ERR] 缺少 zip，请先安装"; exit 1; }
PKG_NAME="${STUDENT_ID}-${STUDENT_NAME}-博物馆预约系统"
STAGE="$DIST_DIR/$PKG_NAME"
rm -rf "$STAGE"; mkdir -p "$STAGE"

# 1) 源代码工程（排除构建产物/依赖，保证压缩包精简且可重新构建）
mkdir -p "$STAGE/源代码"
rsync -a --exclude '.git' \
         --exclude 'node_modules' \
         --exclude 'dist' \
         --exclude 'target' \
         --exclude 'frontend/dist' \
         "$MRS_DIR/" "$STAGE/源代码/museum-reservation-system/"

# 2) 文档 PDF
mkdir -p "$STAGE/文档PDF"
cp "$PDF_DIR"/*.pdf "$STAGE/文档PDF/" 2>/dev/null || true

# 3) 测试截图（功能测试报告引用）
if [[ -d "$MP/screenshots" ]]; then
  mkdir -p "$STAGE/测试截图"
  cp -r "$MP/screenshots/." "$STAGE/测试截图/"
fi

# 4) 演示视频占位说明（实际视频请录制后放入此目录）
cat > "$STAGE/演示视频请放这里.txt" <<TXT
请将录制好的系统演示视频（≤5分钟，mp4）放入本目录后再压缩。
若视频较大，可改为放视频链接说明（如网盘链接）。
TXT

(cd "$DIST_DIR" && zip -r -q "${PKG_NAME}.zip" "$PKG_NAME")
rm -rf "$STAGE"
echo "[OK] 已生成：$DIST_DIR/${PKG_NAME}.zip"
