# Create FilterBreaker

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-62b47a)](https://www.minecraft.net/)
[![NeoForge 21.1.235](https://img.shields.io/badge/NeoForge-21.1.235-ea5c2f)](https://neoforged.net/)
[![Create 6.0.10](https://img.shields.io/badge/Create-6.0.10-f0b36a)](https://github.com/Creators-of-Create/Create)
[![Release](https://img.shields.io/github/v/release/Adnermo-002/Create-FilterBreaker)](https://github.com/Adnermo-002/Create-FilterBreaker/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Create FilterBreaker 是一个 Minecraft 1.21.1 NeoForge 客户端模组，用于复现和回归测试 Create 物流网络数据包中的服务端验证问题。

主要功能：

- `/gainfilter`：构造并发送 `StockKeeperCategoryRefundPacket`；

> 此模组会改变服务端库存状态。测试前建议备份世界，并使用可回滚的测试存档。

## 兼容性

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| Java | 21 |
| NeoForge | 21.1.235 |
| Create | 6.0.10 |
| FilterBreaker | 0.1.0 |
| 加载端 | Client |

上述组合已经完成本地构建验证。反射调用依赖 Create 对应类和构造器签名，其他 Create 版本可能表现不同。

## 下载与安装

1. 从 [Releases](https://github.com/Adnermo-002/Create-FilterBreaker/releases/latest) 下载 `create-filter-breaker-0.1.0.jar`。
2. 准备 Minecraft 1.21.1、Java 21、NeoForge 21.1.235 和 Create 6.0.10。
3. 把 FilterBreaker JAR 放入客户端实例的 `mods` 目录。
4. 启动游戏并确认模组列表中出现 **Create FilterBreaker**。

FilterBreaker 自身只在客户端加载。连接世界时，服务端需要安装协议兼容的 Create。

## 快速使用

### 1. 准备 Stock Ticker

`StockKeeperCategoryRefundPacket` 继承 Create 的 `BlockEntityConfigurationPacket`。服务端仍会检查：

- 玩家模式；
- 目标区块是否已加载；
- 玩家与目标方块的距离；
- 坐标处是否存在对应的同步方块实体。

因此需要在附近放置或找到一个已加载的 Stock Ticker。

### 2. 看向 Stock Ticker 执行指令

```mcfunction
/gainfilter create:filter 1
```

成功后客户端会显示发送位置、数量和物品 ID。

## `/gainfilter` 使用方法

### 指令格式

使用准星指向的方块坐标：

```mcfunction
/gainfilter <item> [count]
```

手动指定 Stock Ticker 坐标：

```mcfunction
/gainfilter <x> <y> <z> <item> [count]
```

参数：

| 参数 | 说明 |
|---|---|
| `<x> <y> <z>` | Stock Ticker 方块坐标 |
| `<item>` | Minecraft 1.21.1 `/give` 风格物品参数 |
| `[count]` | 可选，范围 1–99，默认 1 |

### 基础示例

```mcfunction
/gainfilter create:filter 1
/gainfilter create:attribute_filter 1
/gainfilter create:package_filter 1
```

显式指定坐标：

```mcfunction
/gainfilter 12 64 -8 create:filter[minecraft:custom_name='{"text":"Ticker Test","italic":false}'] 1
```

`<item>` 由原版 `ItemArgument` 解析，所以组件 ID 和组件内容必须已经注册，并通过对应 Codec 的格式检查。

### 根物品限制

退款处理使用：

```java
filter.getItem() instanceof FilterItem
```

Create 6.0.10 中常见的匹配物品包括：

- `create:filter`
- `create:attribute_filter`
- `create:package_filter`

退款包直接产生的根物品仍是 `FilterItem`。嵌套组件中的物品是否进一步形成独立物品，取决于后续读取、转换、打开或提取该组件的游戏逻辑。

## `/gainfilter` 漏洞原理

### 正常客户端把完整 `entry` 放进退款包

Create 的 Stock Ticker 分类界面在删除分类时执行：

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

这里的 `entry` 是客户端内存中的完整 `ItemStack`。

源码：[`StockKeeperCategoryScreen.java`](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/logistics/stockTicker/StockKeeperCategoryScreen.java)

### 网络 Codec 传输完整 ItemStack

```java
public static final StreamCodec<RegistryFriendlyByteBuf,
    StockKeeperCategoryRefundPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, p -> p.pos,
        ItemStack.STREAM_CODEC, p -> p.filter,
        StockKeeperCategoryRefundPacket::new
    );
```

`ItemStack.STREAM_CODEC` 会传输：

- 数量；
- 物品注册 ID；
- 完整 `DataComponentPatch`；
- 组件中的有效嵌套数据。

### 服务端直接使用客户端字段

```java
@Override
protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
    if (!filter.isEmpty() && filter.getItem() instanceof FilterItem)
        player.getInventory()
            .placeItemBackInInventory(filter);
}
```

源码：[`StockKeeperCategoryRefundPacket.java`](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/logistics/stockTicker/StockKeeperCategoryRefundPacket.java)

关键点：

- `filter` 来自客户端数据包；
- `be` 是真实服务端 Stock Ticker；
- 退款函数没有读取 `be.categories`；
- 没有验证该 Filter 是否存在于服务端分类；
- 没有从服务端分类中删除条目；
- 没有把数量固定为 1；
- 没有核对客户端组件与服务端条目；
- 没有把退款绑定到菜单会话或一次性删除事务。

因此，服务器只确认根物品属于 `FilterItem`，随后把客户端提交的完整栈放进玩家库存。

### 执行链

```text
/gainfilter
→ 客户端 ItemArgument 解析 ID 与数据组件
→ ItemInput.createItemStack(count, false)
→ 反射构造 StockKeeperCategoryRefundPacket
→ PacketDistributor.sendToServer
→ 服务端 ItemStack.STREAM_CODEC 重建完整栈
→ instanceof FilterItem
→ placeItemBackInInventory(filter)
```

## 服务端可见性

`/gainfilter` 注册为客户端指令，命令文本通常不会进入服务端命令日志。服务端接收的是 Create 自定义 serverbound payload：

```text
REFUND_STOCK_KEEPER_CATEGORY
BlockPos + ItemStack
```

服务端网络审计、数据包处理日志或对 `StockKeeperCategoryRefundPacket` 的埋点可以记录该请求。

## 从源码构建

需要 Java 21。

Windows：

```powershell
.\gradlew.bat clean build
```

Linux/macOS：

```bash
chmod +x gradlew
./gradlew clean build
```

构建产物：

```text
build/libs/create-filter-breaker-0.1.0.jar
```

## 项目结构

```text
src/main/java/io/github/adnermo/filterbreaker/
├── FilterBreaker.java
├── FilterBreakerClient.java
├── ChatLimitBreakerClient.java
└── mixin/ChatScreenMixin.java
```

## 发布记录

参见 [CHANGELOG.md](CHANGELOG.md) 与 [GitHub Releases](https://github.com/Adnermo-002/Create-FilterBreaker/releases)。

## License
