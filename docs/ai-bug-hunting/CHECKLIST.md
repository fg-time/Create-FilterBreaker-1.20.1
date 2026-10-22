# 通用逐包审计清单

适用于任意 mod。复制一份，对每个候选包逐项打勾。

包类：`______________________`　mod/版本：`____________`

## 1. 反序列化面
- [ ] 找到该框架的反序列化入口（`readSettings` / `read` / `STREAM_CODEC` / `PacketByteBuf.readXxx` / `fromBytes`）
- [ ] 列出每个读入字段及类型（ItemStack / CompoundTag / BlockPos / count / list / UUID / bool / string）
- [ ] 标出客户端完全可控的字段（tainted）

## 2. 处理面
- [ ] 找到 handler（`handle` / `applySettings` / `execute` / `onMessage`）
- [ ] 确认调用链与线程（`enqueueWork` / server thread）
- [ ] 确认使用的服务端真值对象（be / menu / inventory / level / player）

## 3. 校验缺口
- [ ] 是否校验条目存在于服务端（分类/库存/槽位/订单/菜单）
- [ ] 是否校验所有权/权限
- [ ] 是否绑定菜单会话（`containerMenu instanceof …`）
- [ ] 是否一次性事务（防重放）
- [ ] 是否限制数量/列表长度/字符串长度
- [ ] 是否校验数据内容（NBT/组件白名单），还是只查根类型
- [ ] 是否检查旁观者/冒险/OP/白名单
- [ ] 距离/维度/区块检查是否只是宽松基类检查
- [ ] 是否有频率限制

## 4. 可达性
- [ ] 类型门槛（`instanceof`）能否满足
- [ ] 前置方块/距离/模式能否满足
- [ ] 线格式能否完整携带要注入的数据
- [ ] 目标服是否有其它 mod/插件二次校验

## 5. 结论
- [ ] 判定：漏洞 / 疑似 / 不成立
- [ ] 复现已记录（本地或授权环境）
- [ ] 同基类 / 同语义兄弟包已排查
- [ ] 跨版本 / 跨加载器已对照（见 `VERSIONING.md`）
- [ ] 修复建议已写

---

## A. 高危语义关键词（用来快速筛包）

```text
refund claim reward give drop loot crate
configure edit update delete remove rename
request order buy sell trade shop
place upload download sync restore
interact use attack open
container inventory backpack storage
economy money bank market
permission claim land team
```

## B. 框架速查

| 框架 | 包类特征 | 反序列化 | 客户端发送 |
|---|---|---|---|
| Forge 1.12–1.16 | `IMessage` 实现 | `fromBytes` | `SimpleImpl.INSTANCE.sendToServer` |
| Forge 1.17–1.20.1 | `IMessage` 或第三方基类 | `decode`/`readSettings` | `SimpleChannel.sendToServer` |
| NeoForge 1.20.1+ | `CustomPacketPayload` | `STREAM_CODEC` | `PacketDistributor.sendToServer` |
| Fabric | 普通类 + 注册处理器 | `PacketByteBuf.readXxx` | `ServerPlayNetworking.send` |

## C. 高危模式速查

1. `readItem()` / `ItemStack.STREAM_CODEC` + `placeItemBackInInventory` / `addItem`
2. `readNbt()` / `CompoundTag` + 写入方块实体且不校验归属
3. `instanceof XxxItem` 作为唯一门槛，之后直接使用完整 NBT/组件
4. `pos` 只做距离检查，不检查该位置的数据是否属于玩家
5. 包内自带列表/分类/订单，服务端不查自己的列表
6. 退款/返还语义却没有「从服务端删除」的对应操作
7. 客户端界面删除/编辑操作直接发包，服务端不回读菜单

## D. 排优先级（任意 mod）

按以下顺序审：

1. 涉及**物品/NBT** 的包（影响最大、最好利用）
2. 涉及**方块实体写入**的包
3. 涉及**菜单/会话**的包
4. 涉及**权限/领地**的包
5. 只涉及**客户端显示**的包（最低）
