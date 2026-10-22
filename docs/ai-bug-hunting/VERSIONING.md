# 跨版本 / 跨加载器：判断「这个 bug 在新版本里还通用吗」

一个漏洞是否在 6.0.x 全版本、1.20.1/1.21.1 双加载器、甚至 6.1.x 上通用，取决于**逻辑层**是否没变，而不是线格式是否没变。

## 1. 先分清两层

| 层 | 内容 | 变了会怎样 |
|---|---|---|
| 逻辑层 | 包语义、客户端提交什么、服务端怎么处理、有没有校验 | **变了才可能修掉漏洞** |
| 传输层 | 包基类、注册方式、线格式（NBT/组件）、发包 API | 变了只影响**怎么构造/发送**，漏洞可能还在 |

Create 案例：

- 1.20.1：`SimplePacketBase` + `AllPackets.getChannel().sendToServer(...)`，物品走旧 NBT；
- 1.21.1：`CustomPacketPayload` + `CatnipServices.NETWORK.sendToServer(...)`，物品走 data components；
- **类名、构造器 `(BlockPos, ItemStack)`、服务端 `applySettings`、缺失的校验完全一致** → 逻辑层没变，漏洞在两个版本都成立。

## 2. 五步判定（拿到新版本后照做）

1. **类是否还在**
   ```bash
   unzip -l mod.jar | grep -i "RefundPacket"
   "$JAVAP" -p -classpath mod.jar <完整类名>
   ```
2. **构造器/字段是否一致**
   - 还接受客户端可控字段吗？（ItemStack/NBT/列表/数量）
   - 字段是否仍然从网络反序列化？
3. **基类校验是否没变**
   - 是否仍只检查距离/区块/模式，而不检查归属/会话？
   - 基类有没有新增 menu/所有权/一次性 token 检查？
4. **handler 与 sink 是否没变**
   - 是否仍然直接用包内数据？
   - 是否仍然 `placeItemBackInInventory` / `setBlock` / 写方块实体？
   - 有没有新增「对照服务端真值」的逻辑？
5. **线格式是否变了**
   - 变了只说明复现代码要改（例如 1.21.1 的组件 vs 1.20.1 的 NBT），不说明漏洞修了。

## 3. 结论矩阵

| 逻辑层 | 传输层 | 结论 |
|---|---|---|
| 相同 | 相同 | 直接通用 |
| 相同 | 不同 | **仍然通用**，改复现构造方式即可 |
| 不同（加了校验） | 任意 | 该版本可能已修；检查同基类兄弟包是否漏修 |
| 类被删/语义改变 | 任意 | 需要重新找新的入口 |

## 4. 找「漏修」的兄弟

修复通常很局部。发现某版本修了一个包，立刻检查：

- 同一基类的所有子类（同一套宽松校验）；
- 同目录/同语义的其他包（Edit/Delete/Lock/Request…）；
- 另一加载器上的同名包（1.20.1 与 1.21.1 常常各自维护）。

## 5. 跨 mod 的通用性

这套判断不只对 Create 成立：

- 任何 mod 换版本后，先 `javap` 目标类三件套：**类存在？字段从网络来？handler 还信任它？**
- 换加载器（Forge ↔ NeoForge ↔ Fabric）时，传输层必然变，逻辑层可能原样保留；
- 换 MC 大版本时，物品/NBT 序列化方式会变（例如 1.20.5+ 的 data components），但「服务端是否校验」是作者代码，通常不会自动修。

## 6. 本次案例的具体证据

见 `CASES/create-stockticker.md`：

- 1.20.1-6.0.8：`StockKeeperCategoryRefundPacket` 继承 `BlockEntityConfigurationPacket` → `SimplePacketBase`；
- 1.21.1-6.0.10：同名类，`applySettings` 逻辑相同；
- 两版本都只检查 `instanceof FilterItem`；
- 基类 1.20.1 只检查距离 20/已加载/非旁观/非冒险，没有菜单/所有权/一次性检查。

→ 该问题属于 **Create 6.0 逻辑层通用**，与 1.20.1 还是 1.21.1 无关。
