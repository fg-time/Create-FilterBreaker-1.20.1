#!/usr/bin/env bash
# 列出 jar 里的包/网络/载荷类，并导出每个类的方法签名（javap -p）
# 用法: ./triage_packets.sh <mod.jar> [输出目录=scan]
set -euo pipefail

JAR=${1:?用法: triage_packets.sh <mod.jar> [输出目录]}
OUT=${2:-scan}
JAVAP=${JAVAP:-javap}

if ! command -v "$JAVAP" >/dev/null 2>&1; then
  echo "javap 不可用: $JAVAP" >&2
  echo "请设置 JAVAP=/path/to/javap（需要能读目标 class 版本的 JDK）" >&2
  exit 1
fi

mkdir -p "$OUT"

unzip -l "$JAR" \
  | awk '{print $4}' \
  | grep -E '\.class$' \
  | grep -iE 'packet|payload|network|message' \
  | sed 's/\.class$//' \
  | tr '/' '.' \
  | sort -u > "$OUT/packet-classes.txt"

: > "$OUT/packet-api.txt"
while IFS= read -r cls; do
  echo "===== $cls" >> "$OUT/packet-api.txt"
  "$JAVAP" -p -classpath "$JAR" "$cls" >> "$OUT/packet-api.txt" 2>&1 || true
  echo "" >> "$OUT/packet-api.txt"
done < "$OUT/packet-classes.txt"

echo "[+] 类清单: $OUT/packet-classes.txt ($(wc -l < "$OUT/packet-classes.txt") 个)"
echo "[+] API:    $OUT/packet-api.txt"
