#!/usr/bin/env bash
# 从仓库根 skill/ 目录生成可直接导入 AI 编程工具的技能包 dist/qqbot-sdk-skill.zip。
# 包结构要求：SKILL.md 位于 zip 根目录，references/ 与之同级（Trae / CodeBuddy 导入格式）。
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC_DIR="$REPO_ROOT/skill"
OUT_DIR="$REPO_ROOT/dist"
OUT_ZIP="$OUT_DIR/qqbot-sdk-skill.zip"

[ -f "$SRC_DIR/SKILL.md" ] || { echo "错误：$SRC_DIR/SKILL.md 不存在" >&2; exit 1; }

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# zip -X：不写入本机属性等额外字段，保证不同机器产出内容一致
(cd "$SRC_DIR" && zip -X -q -r "$OUT_ZIP" . -x "*.DS_Store")

echo "已生成 $OUT_ZIP"
unzip -l "$OUT_ZIP"
