#!/usr/bin/env bash
# 在 jar 的字节码里查找网络发送调用点（不需要反编译）
# 用法: ./find_send_sites.sh <mod.jar> [输出文件=send-sites.txt] [类名过滤正则]
#   类名过滤默认扫 Packet/Payload/Network/Message/Handler/Screen/Menu/Client 类；传 '.*' 则全量扫描（慢但最全）
set -euo pipefail

JAR=${1:?用法: find_send_sites.sh <mod.jar> [输出文件] [类名过滤正则]}
OUT=${2:-send-sites.txt}
FILTER=${3:-'Packet|Payload|Network|Message|Handler|Screen|Menu|Client'}
JAVAP=${JAVAP:-javap}

if ! command -v "$JAVAP" >/dev/null 2>&1; then
  echo "javap 不可用: $JAVAP" >&2
  echo "请设置 JAVAP=/path/to/javap（需要能读目标 class 版本的 JDK）" >&2
  exit 1
fi

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

unzip -l "$JAR" \
  | awk '{print $4}' \
  | grep -E '\.class$' \
  | grep -vE 'META-INF|^module-info' \
  | sed 's/\.class$//' \
  | tr '/' '.' \
  | grep -E "$FILTER" \
  | sort -u > "$TMP/classes.txt"

: > "$OUT"
while IFS= read -r cls; do
  hits=$("$JAVAP" -p -c -classpath "$JAR" "$cls" 2>/dev/null \
    | grep -nE "sendToServer|ServerPlayNetworking\.send|ClientPlayNetworking\.send|PacketDistributor|SimpleChannel|NetworkRegistry|PayloadRegistrar" || true)
  if [ -n "$hits" ]; then
    echo "===== $cls" >> "$OUT"
    echo "$hits" >> "$OUT"
    echo "" >> "$OUT"
  fi
done < "$TMP/classes.txt"

echo "[+] 扫描 $(wc -l < "$TMP/classes.txt") 个类，命中 $(grep -c '^=====' "$OUT" || true) 个 -> $OUT"
