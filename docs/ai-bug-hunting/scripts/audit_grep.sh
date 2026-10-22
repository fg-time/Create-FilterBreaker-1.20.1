#!/usr/bin/env bash
# 在反编译目录里扫危险 sink / 反序列化入口 / 校验点（通用）
# 用法: ./audit_grep.sh <反编译输出目录> [结果文件=audit-hits.txt]
set -euo pipefail

DIR=${1:?用法: audit_grep.sh <反编译输出目录> [结果文件]}
OUT=${2:-audit-hits.txt}

pattern='sendToServer|ServerPlayNetworking|PacketDistributor|placeItemBackInInventory|giveItemToPlayer|addItem\(|setItem\(|setStackInSlot|dropItem|setBlock\(|setBlockAndUpdate|destroyBlock|removeBlock|applySettings|readSettings|STREAM_CODEC|fromBytes|readItem\(|readNbt\(|readBlockPos\(|containerMenu|openScreen|teleportTo|addExperience|setGameMode|grantAdvancement|performPrefixedCommand'

grep -rnE "$pattern" "$DIR" --include='*.java' > "$OUT" || true
echo "[+] 命中 $(wc -l < "$OUT") 行 -> $OUT"
echo
echo "--- 分类统计 ---"
for k in sendToServer ServerPlayNetworking PacketDistributor placeItemBackInInventory giveItemToPlayer setItem setStackInSlot setBlock applySettings readSettings STREAM_CODEC fromBytes readItem readNbt containerMenu openScreen teleportTo; do
  n=$(grep -c "$k" "$OUT" || true)
  printf "%-28s %s\n" "$k" "$n"
done
