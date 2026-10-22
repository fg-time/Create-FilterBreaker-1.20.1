# 通用工具与命令

适用于任意 mod jar。所有命令以 Windows Git Bash 为例；JDK 路径按本机替换。

## 0. 常用路径

```bash
# 含 javap 的完整 JDK（本机示例；找不到就用 `where javap` / ev 搜）
JAVAP="/path/to/jdk/bin/javap"
# 反编译器
VINEFLOWER="tools/vineflower-1.12.0.jar"
# 跑 Vineflower 的 java
JAVA="/path/to/java"
```

> PCL2 自带的通常是 JRE，能跑 Vineflower；`javap` 用 Minecraft runtime 目录里的。

## 1. 确认目标版本

```bash
# Forge / NeoForge 的 mods.toml
unzip -p mod.jar META-INF/mods.toml | head -50
# Fabric 的 fabric.mod.json
unzip -p mod.jar fabric.mod.json | head -50
# 文件名本身通常带 MC 版本
```

## 2. 看网络面

```bash
unzip -l mod.jar | grep -iE "packet|payload|network|message"

# 关键注册/发送类（按 mod 实际命名）
unzip -l mod.jar | grep -iE "AllPackets|Network|PacketHandler|Payload|Networking"
```

## 3. javap 快速看 API（不用反编译）

```bash
"$JAVAP" -p -classpath mod.jar <完整类名>
"$JAVAP" -p -c -classpath mod.jar <完整类名> | grep -n -B6 -A6 "sendToServer"
```

`-p` 显示私有成员，`-c` 显示字节码里的实际调用。

## 4. 反编译

```bash
scripts/decompile_jar.sh mod.jar out
```

手动等价命令：

```bash
mkdir -p out
"$JAVA" -jar "$VINEFLOWER" mod.jar out
```

只反编译关键类（更快）：先把目标 class 从 jar 里 unzip 到临时目录，再对该目录跑 Vineflower。

## 5. 找客户端发包点

```bash
scripts/find_send_sites.sh mod.jar send-sites.txt
# 全量扫描（慢，但最全）：
scripts/find_send_sites.sh mod.jar send-sites.txt '.*'
```

手动检索：

```bash
grep -rnE "sendToServer|ServerPlayNetworking\.send|PacketDistributor|SimpleChannel" out --include='*.java'
```

## 6. 扫危险 sink / 校验点

```bash
scripts/audit_grep.sh out hits.txt
```

手动：

```bash
grep -rnE "placeItemBackInInventory|addItem\(|setItem\(|setStackInSlot|dropItem" out --include='*.java'
grep -rnE "setBlock\(|setBlockAndUpdate|destroyBlock|removeBlock" out --include='*.java'
grep -rnE "applySettings|readSettings|STREAM_CODEC|readItem\(|readNbt\(|fromBytes|decode" out --include='*.java'
grep -rnE "containerMenu|openMenu|NetworkHooks\.openScreen" out --include='*.java'
grep -rnE "teleportTo|addExperience|setGameMode|grantAdvancement" out --include='*.java'
```

## 7. 批量列出包类

```bash
scripts/triage_packets.sh mod.jar scan
# scan/packet-classes.txt  全部包/网络类
# scan/packet-api.txt      每个类的字段、构造器、方法签名
```

## 8. 版本 diff（找修复点 / 找漏修）

```bash
# 两个版本分别反编译后
diff -ru out_v1/com/example/mod/network out_v2/com/example/mod/network
# 或看单个包
diff -u out_v1/.../XxxPacket.java out_v2/.../XxxPacket.java
```

重点：

- 修复是否只加在某个包（一次性 token/服务端真值校验）；
- 同目录其他包是否漏修；
- 基类是否新增检查（会一次修一大片子类）。

## 9. 各框架的发送方式（复现时用）

| 框架 | 客户端 → 服务端 |
|---|---|
| Forge 1.17–1.20.1 | `SimpleChannel.sendToServer(packet)`（mod 常暴露 `AllPackets.getChannel()`） |
| NeoForge 1.20.1+ | `PacketDistributor.sendToServer(payload)` |
| Fabric | `ClientPlayNetworking.send(new XxxPayload(...))` / `ServerPlayNetworking.send` 反向 |
| 自建封装 | 看反编译出的客户端调用点，照抄 |

> 线格式（NBT/组件、payload/SimplePacketBase）随版本变化，复现代码不能跨版本直接复制；漏洞是否存在与线格式无关。

## 10. 服务端线程与重放

- 发包后确认 handler 是否 `enqueueWork`/切主线程；
- 测试重放：同一包连发 2 次，看是否重复生效（缺一次性事务）；
- 测试数量/大小边界：count、列表长度、字符串长度。
