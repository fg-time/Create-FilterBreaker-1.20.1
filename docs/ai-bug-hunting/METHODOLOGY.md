# 通用方法论：在任意 Minecraft mod 中寻找「服务端信任客户端」协议漏洞

适用于 **任意 mod、任意 MC 版本、任意加载器**。Create 仓储发报机只是案例；下面的流程换成任何 mod 都成立。

---

## 0. 漏洞本质

Minecraft mod 的网络协议是**信任边界**：客户端可以发送任意字节，服务端必须假设客户端可能伪造。

要找的是：

> 客户端可控数据 → 服务端**没有对照自己的真值** → 直接产生副作用

副作用不限于物品：

| 类型 | 例子 |
|---|---|
| 物品/库存 | 凭空生成、复制、任意 NBT/组件 |
| 世界 | 任意方块放置/破坏、结构上传 |
| 方块实体 | 篡改容器内容、机器参数、频率绑定 |
| 能力 | 能源/流体/物品 handler 直接接收包内数值 |
| 玩家状态 | 经验、传送、模式、权限、进度 |
| 会话/菜单 | 不属于自己的容器/交易/商店 |
| 经济 | 商店/任务/奖励/货币 mod |

---

## 1. 准备

记录目标的三件套：**MC 版本 / 加载器 / mod 版本**。

- mod jar（整合包 mods 目录、CurseForge、Modrinth、自己编译）；
- JDK 的 `javap`（看类结构和字节码调用点，最快）；
- Vineflower（反编译成 Java，便于读业务逻辑）；
- ripgrep / grep（扫 sink 和校验点）。

**至少准备相邻两个版本**（例如 1.20.1 与 1.21.1，或 6.0.x 与 6.1.x），用于判断漏洞是否跨版本通用（见 `VERSIONING.md`）。

---

## 2. 画出网络攻击面（按框架，不按 mod）

先确认 mod 用的是哪套网络框架：

| 框架 | 注册/发送 | 包载体 | 反序列化入口 |
|---|---|---|---|
| Forge 1.12–1.16 | `SimpleImpl` / `NetworkRegistry` | `IMessage` | `fromBytes` |
| Forge 1.17–1.20.1 | `SimpleChannel` | `IMessage` 或第三方基类 | `decode` / `fromBytes` / `readSettings` |
| NeoForge 1.20.1+ | `PayloadRegistrar` | `CustomPacketPayload` | `STREAM_CODEC` / `read` |
| Fabric | `ServerPlayNetworking.registerGlobalReceiver` | 无统一基类 | `PacketByteBuf` 读取 |
| 自建封装 | mod 自己的 `AllPackets` / `Network` / `PacketHandler` | 各种基类 | 各种 |

### 快速枚举

```bash
unzip -l mod.jar | grep -iE "packet|payload|network|message"
scripts/triage_packets.sh mod.jar scan
scripts/find_send_sites.sh mod.jar send-sites.txt
```

### 找「客户端发包点」

- 反编译后搜：`sendToServer`、`ServerPlayNetworking.send`、`PacketDistributor`、`SimpleChannel`；
- 这些调用点告诉你**客户端在什么操作下会发什么包**，也是构造复现的入口。

---

## 3. 按语义给包分类（高危优先级）

不是所有包都值得细看。优先审计这些语义：

```text
refund / claim / reward / give / drop / loot / crate
configure / edit / update / delete / remove / rename
request / order / buy / sell / trade / shop
place / upload / download / sync / restore
interact / use / attack / open
container / inventory / backpack / storage
economy / money / bank / market
permission / claim / land / team
```

原因：这些包天然涉及「客户端提交一个对象，服务端应用它」。Create 的退款包属于 `refund` + `container` 组合。

---

## 4. 标记客户端可控字段（taint）

对每个候选包：

1. **反序列化面**
   - Forge：`readSettings(FriendlyByteBuf)` / `read`
   - NeoForge：`STREAM_CODEC`、构造器 `(FriendlyByteBuf)`
   - Fabric：`PacketByteBuf.readXxx`
   - 记录字段类型：`ItemStack`、`CompoundTag`、`BlockPos`、`List`、`count`、UUID、布尔、字符串。
2. **可控性**
   - 该字段是否完全由客户端决定（物品/NBT/坐标/数量/列表）；
   - 客户端界面状态（screen 内存里的 entry）是否原样进包。
3. **handler 路径**
   - `handle(...)` / `applySettings(...)` / `execute(...)` / `onMessage(...)`；
   - 是否切到服务端线程（`enqueueWork` / `server.execute`）；
   - 使用了哪个服务端对象（方块实体、菜单、玩家、世界）。
4. **标记**
   - 「客户端可控且未校验」→ **tainted**。

---

## 5. 危险 sink（通用）

**物品/库存**
```text
placeItemBackInInventory / addItem / add / setItem / setStackInSlot
ItemHandlerHelper.giveItemToPlayer / dropItem / spawnItem
```

**世界/方块**
```text
setBlock / setBlockAndUpdate / destroyBlock / removeBlock
```

**方块实体/能力**
```text
setChanged / sendData / getCapability
energy / fluid / item handler 的接收端
```

**玩家/会话**
```text
containerMenu / openMenu / NetworkHooks.openScreen
teleportTo / addExperience / setGameMode / grantAdvancement
```

**命令/权限**
```text
performPrefixedCommand / CommandSourceStack / permission checks
```

脚本：`scripts/audit_grep.sh <反编译目录> hits.txt`。

---

## 6. 缺失校验清单（决定是不是真漏洞）

- [ ] **服务端真值**：handler 用的是包里的值，还是回读了服务端数据（方块实体/菜单/库存）？
- [ ] **存在性**：客户端提交的条目是否必须存在于服务端（分类、槽位、订单、容器）？
- [ ] **所有权**：该方块/容器/菜单是否属于该玩家，或有权限？
- [ ] **会话绑定**：是否要求对应 menu 处于打开状态？
- [ ] **一次性事务**：同一请求能否重放？服务端是否删除对应条目？
- [ ] **数量/大小**：`count`、列表长度、字符串长度是否有上限？
- [ ] **数据内容**：只检查根类型（`instanceof`），还是也校验 NBT/组件/白名单？
- [ ] **模式/权限**：旁观者、冒险、OP、白名单。
- [ ] **距离/维度/区块**：基类检查是否只是宽松的 20 格/已加载。
- [ ] **频率**：能否高频刷。

> 还要往上层看：菜单事件、能力层、tick 逻辑里是否有二次校验。只看到「tainted → sink」还不够。

---

## 7. 判定公式与可达性

```text
客户端可控字段 + 危险 sink + 无服务端真值校验 = 候选
```

再确认：

1. **类型门槛**：`instanceof XxxItem` / `instanceof XxxMenu` 能否满足；
2. **前置条件**：距离、区块、模式、目标方块是否存在；
3. **数据能否完整传输**：线格式能不能携带你想注入的内容（NBT/组件/列表）；
4. **服务端是否有其它 mod/插件二次校验**（测试服要贴近目标服环境）。

---

## 8. 复现（受控）

- 只在本机存档 / 自己搭的服务器 / 授权环境；先备份。
- 最小方式：写一个**客户端指令 mod**，反射或直接构造目标包并发送。
- 各框架发送方式见 `TOOLS.md`；案例代码见 `CASES\create-stockticker.md`。
- 记录：版本、坐标、前置条件、完整命令、结果、服务端日志。

---

## 9. 跨版本 / 跨加载器通用性

发现一个漏洞后，不要只停在当前版本。判断它是否在新版本/另一加载器上仍然成立：

1. 目标类是否仍然存在（javap）；
2. 构造器/字段是否一致；
3. 基类校验是否没变；
4. handler 与 sink 是否没变；
5. 线格式是否变了（只影响复现方式，不影响漏洞是否存在）。

详细流程和本次案例的证据见 `VERSIONING.md` 与 `CASES\create-stockticker.md`。

---

## 10. 跨 mod 迁移（同一套思路）

任何 mod 都可以按下面三问快速筛：

1. **它有没有「客户端提交对象 → 服务端应用」的包？**（第 3 节语义关键词）
2. **应用前有没有回读服务端真值？**（第 6 节）
3. **sink 有没有实际副作用？**（第 5 节）

高发领域：物流/仓储、商店/经济、背包/容器、领地/权限、任务/奖励、载具/机械配置、蓝图/结构上传。

---

## 11. 记录模板

| 字段 | 内容 |
|---|---|
| 版本 | MC / loader / mod 版本 |
| 包类 | 完整类名 |
| 传输字段 | 反序列化字段与类型 |
| 客户端可控性 | 哪些完全可控 |
| handler | 方法名与调用链 |
| sink | 具体 API |
| 缺失校验 | 服务端真值/存在性/所有权/会话/一次性/数量/内容/权限/距离/频率 |
| 前置条件 | 距离、方块、模式、菜单 |
| 影响 | 可获得什么、能否重复、是否持久化 |
| 复现 | 命令/脚本/截图 |
| 修复建议 | 以服务端真值为准、绑定会话、限数量、忽略客户端数据 |
| 通用性 | 同基类/同语义包/跨版本/跨加载器是否同样成立 |
