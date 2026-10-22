#!/usr/bin/env bash
# 反编译任意 Minecraft mod jar
# 用法: ./decompile_jar.sh <mod.jar> <输出目录>
set -euo pipefail

JAR=${1:?用法: decompile_jar.sh <mod.jar> <输出目录>}
OUT=${2:?用法: decompile_jar.sh <mod.jar> <输出目录>}
VF=${VINEFLOWER:-"$(dirname "$0")/../tools/vineflower-1.12.0.jar"}
JAVA=${JAVA:-java}

if [ ! -f "$VF" ]; then
  echo "找不到 Vineflower: $VF" >&2
  echo "可设置环境变量 VINEFLOWER=/path/to/vineflower.jar" >&2
  exit 1
fi

mkdir -p "$OUT"
echo "[*] jar   = $JAR"
echo "[*] out   = $OUT"
echo "[*] java  = $JAVA"
echo "[*] vf    = $VF"

"$JAVA" -jar "$VF" "$JAR" "$OUT"
echo "[+] 完成: $OUT"
