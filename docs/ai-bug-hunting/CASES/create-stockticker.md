# 案例：Create StockKeeperCategoryRefundPacket 退款包（1.20.1 / 1.21.1）

本次审计的完整记录。作为方法论的标准样例：从「客户端正常发送路径」入手，发现服务端直接信任客户端构造的 ItemStack。

## 1. 结论

| 项 | 内容 |
|---|---|
| 漏洞类型 | server-trusts-client（服务端信任客户端物品数据） |
| 受影响 | Create 6.x（实测 1.20.1-6.0.8 与 1.21.1-6.0.10） |
| 包类 | `com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryRefundPacket` |
| 传输字段 | `BlockPos pos` + `ItemStack filter`（完整物品 + NBT/组件） |
| 服务端 sink | `player.getInventory().placeItemBackInInventory(filter)` |
| 唯一门槛 | `filter.getItem() instanceof FilterItem` |
| 缺失校验 | 服务端分类存在性、物品 NBT/组件、数量、菜单会话、一次性事务 |
| 影响 | 凭空获得带任意 NBT 的 Create 过滤器（1.21.1 可借组件进一步构造） |

## 1.5 为什么说它是 Create 6.0 通用（方法论示例）

这个案例的重点不是 Create 本身，而是演示「逻辑层 vs 传输层」的判断：

- **逻辑层（信任边界）两版本一致**：
  - 同名类、同构造器 `(BlockPos, ItemStack)`；
  - 服务端 `applySettings` 都只查 `instanceof FilterItem`，然后 `placeItemBackInInventory`；
  - 基类校验都只做玩家模式/区块加载/距离，不查菜单、归属、一次性事务。
- **传输层不同，只影响复现方式**：
  - 1.20.1-6.0.8：`BlockEntityConfigurationPacket` → `SimplePacketBase`；`AllPackets.getChannel().sendToServer(...)`；物品走旧 NBT。
  - 1.21.1-6.0.10：`CustomPacketPayload`；`CatnipServices.NETWORK.sendToServer(...)`；物品走 data components。

所以：**换 MC 版本、换加载器、换线格式，都不等于漏洞被修**。判断任何新版本（6.1.x 或其他 mod）时，按 `VERSIONING.md` 的五步：类还在吗 → 字段还从网络来吗 → 基类校验变了吗 → handler/sink 变了吗 → 线格式只是复现问题。

## 2. 正常客户端路径（找漏洞的入口）

Create 的 Stock Ticker 分类界面删除分类时：

```java
if (!entry.isEmpty())
    CatnipServices.NETWORK.sendToServer(
        new StockKeeperCategoryRefundPacket(
            menu.contentHolder.getBlockPos(),
            entry
        )
    );
entries.remove(entry);
```

`entry` 是**客户端内存里的完整 ItemStack**。只要客户端能构造这个包，就能把任意 ItemStack 交给服务端。

## 3. 服务端处理（无校验）

1.20.1 Create 6.0.8 反编译（`CASES/create-6.0.8-1.20.1-decompiled/.../StockKeeperCategoryRefundPacket.java`）：

```java
protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
   if (!this.filter.isEmpty() && this.filter.getItem() instanceof FilterItem) {
      player.getInventory().placeItemBackInInventory(this.filter);
   }
}
```

1.21.1 Create 6.0.10（README 引用）：

```java
@Override
protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
    if (!filter.isEmpty() && filter.getItem() instanceof FilterItem)
        player.getInventory().placeItemBackInInventory(filter);
}
```

- 没有读 `be.categories`；
- 没有验证 filter 是否存在于服务端分类；
- 没有从服务端分类删除任何东西；
- 没有校验 NBT/组件；
- 没有固定数量；
- 没有绑定菜单会话/一次性事务。

## 4. 基类校验（1.20.1）

```java
public boolean handle(Context context) {
   context.enqueueWork(() -> {
      ServerPlayer player = context.getSender();
      if (player != null && !player.isSpectator() && !AdventureUtil.isAdventure(player)) {
         Level world = player.level();
         if (world != null && world.isLoaded(this.pos)) {
            if (this.pos.closerThan(player.blockPosition(), this.maxRange())) { // 20
               BlockEntity blockEntity = world.getBlockEntity(this.pos);
               if (blockEntity instanceof SyncedBlockEntity) {
                  this.applySettings(player, (BE) blockEntity);
                  ...
               }
            }
         }
      }
   });
   return true;
}
```

可达条件：非旁观/非冒险、区块已加载、距离 20 格内、坐标上是同步方块实体。**不需要打开界面，也不需要该 Stock Ticker 上有分类。**

## 5. 两版本差异（复现时最容易踩的坑）

| 项目 | 1.20.1 Forge | 1.21.1 NeoForge |
|---|---|---|
| 包基类 | `BlockEntityConfigurationPacket` → `SimplePacketBase` | `CustomPacketPayload` |
| 客户端发包 | `AllPackets.getChannel().sendToServer(packet)` | `CatnipServices.NETWORK.sendToServer(packet)` / `PacketDistributor.sendToServer(payload)` |
| 物品数据 | 旧 NBT（`ItemStack` tag） | data components / `DataComponentPatch` |
| 命令物品语法 | `create:filter{Items:[...]}` | `create:filter[minecraft:custom_name=...]` |
| Java | 17 | 21 |

类名与构造器 `(BlockPos, ItemStack)` 两版本一致；反射可按同名类处理，但发包路径必须按版本区分。

## 6. 复现工具（1.20.1 版思路）

客户端 mod：

1. 注册客户端指令 `/gainfilter <item>[NBT] [count]`（`ItemArgument` 解析 `/give` 风格参数）；
2. `ItemInput.createItemStack(count, false)` 得到完整.stack；
3. 反射：
   ```java
   Object channel = Class.forName("com.simibubi.create.AllPackets")
                           .getMethod("getChannel").invoke(null);
   Class<?> pc = Class.forName(
       "com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryRefundPacket");
   Object packet = pc.getConstructor(BlockPos.class, ItemStack.class)
                      .newInstance(pos, stack);
   ((SimpleChannel) channel).sendToServer(packet);
   ```
4. 站在 Stock Ticker 20 格内执行。

长 NBT 超过 256 字符时，需要客户端侧绕过聊天长度：

- `ScreenEvent.Init.Post` 把 `EditBox.maxLength` 提到 `Integer.MAX_VALUE`；
- Mixin `ChatScreen.normalizeChatMessage` HEAD，指令（`/` 开头）原样返回，聊天仍走 `StringUtil.trimChatMessage`。

完整代码见 `CASES/filterbreaker-1.21.1-src/`（1.21.1 原版反编译）与 `CASES/forge1201-port-prompt.md`（移植提示词）。

## 7. 1.20.1 已验证的过滤器 NBT

| 物品 | 注册名 | NBT |
|---|---|---|
| 列表过滤器 | `create:filter` | `Items:[{Slot:0b,id:"...",Count:1b}]`、`Blacklist:0b` |
| 属性过滤器 | `create:attribute_filter` | 见 `AttributeFilterItem` / `FilterItemStack$AttributeFilterItemStack` |
| 包裹过滤器 | `create:package_filter` | `Address:"..."` |

1.21.1 的 `create:package_filter[minecraft:food={...using_converts_to...}]` 依赖 data components，1.20.1 **不存在**，不要照抄。

## 8. 修复建议（供记录）

- 退款时以服务端分类数据为准，不信任包内 ItemStack；
- 或至少校验该 filter 存在于服务端分类、匹配数量，并在成功后从服务端删除；
- 绑定菜单会话/一次性 token，防止重放；
- 忽略客户端 NBT/组件，或按白名单重建；
- 基类补充 menu/所有权校验。
