# Create FilterBreaker

**Create 物流退款包的服务端验证回归测试工具**

> 主目标：Minecraft **1.20.1** / Forge / Create **6.0.x**
> 附带：Minecraft 1.21.1 / NeoForge 原版构建（已改名归档）

Create FilterBreaker 是一个**客户端**模组，用于在本地或获得授权的测试环境中，复现与回归测试 Create 物流网络中的服务端验证问题。

它做两件事：

- `/gainfilter`：构造并发送 `StockKeeperCategoryRefundPacket`；
- 突破聊天框 256 字符限制，让长 NBT/组件参数能够完整输入。

> ⚠️ 该工具会改变服务端物品状态。只在你自己的存档、自己搭建的服务器或明确授权的测试环境中使用；测试前请备份。

---

## 兼容性

| 版本 | Minecraft | 加载器 | Create | 仓库内状态 |
|---|---|---|---|---|
| **1.20.1 移植版** | 1.20.1 | Forge 47.x | 6.0.x（实测 6.0.8） | 提供构建提示词与参考源码，构建产物需按 `docs/1.20.1-port-prompt.md` 生成 |
| 1.21.1 原版 | 1.21.1 | NeoForge 21.1.235 | 6.0.10 | `dist/create-filter-breaker-1.21.1-0.1.0.jar` 已附带 |

两个版本的类名与构造器一致：

```text
com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryRefundPacket
(BlockPos, ItemStack)
```

但网络传输层不同：

| 版本 | 发包方式 | 物品数据 |
|---|---|---|
| 1.20.1 Forge | `AllPackets.getChannel().sendToServer(packet)` | 旧 NBT |
| 1.21.1 NeoForge | `CatnipServices.NETWORK.sendToServer(packet)` | data components |

---

## 下载与安装

### 1.21.1

1. 下载 `dist/create-filter-breaker-1.21.1-0.1.0.jar`；
2. 准备 Minecraft 1.21.1、Java 21、NeoForge 21.1.235、Create 6.0.10；
3. 把 jar 放入客户端实例的 `mods` 目录；
4. 启动游戏，在模组列表中确认 **Create FilterBreaker**。

### 1.20.1

本仓库暂未附带 1.20.1 的构建产物。把 `docs/1.20.1-port-prompt.md` 交给 AI 或开发者，按提示词构建；产物命名为 `create-filter-breaker-forge-1.20.1-0.1.0.jar`，放入 `dist/`。

构建目标：

| 项目 | 值 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Create | 6.0.x |
| Java | 17 |

---

## 快速使用

### 1. 准备 Stock Ticker（仓储发报机）

在附近放置或找到一个已加载的 Stock Ticker。服务端仍会检查：

- 玩家模式（非旁观者、非冒险）；
- 目标区块是否已加载；
- 玩家与目标方块的距离（20 格内）；
- 坐标处是否存在对应的同步方块实体。

不需要打开界面，也不需要该 Stock Ticker 上有分类。

### 2. 执行指令

使用准星指向的方块：

```mcfunction
/gainfilter <item> [count]
```

手动指定坐标：

```mcfunction
/gainfilter <x> <y> <z> <item> [count]
```

| 参数 | 说明 |
|---|---|
| `<x> <y> <z>` | Stock Ticker 方块坐标 |
| `<item>` | `/give` 风格物品参数 |
| `[count]` | 1–99，默认 1 |

### 1.20.1 示例（旧 NBT）

```mcfunction
/gainfilter create:filter 1
/gainfilter create:attribute_filter 1
/gainfilter create:package_filter 1

/gainfilter create:filter{Items:[{Slot:0b,id:"minecraft:oak_log",Count:1b}]} 1
/gainfilter create:package_filter{Address:"test"} 1
/gainfilter 12 64 -8 create:filter{display:{Name:'{"text":"Ticker Test","italic":false}'}} 1
```

已实测的过滤器 NBT：

| 物品 | 注册名 | NBT |
|---|---|---|
| 列表过滤器 | `create:filter` | `Items`（ItemStackHandler 列表）、`Blacklist` |
| 属性过滤器 | `create:attribute_filter` | 见 `AttributeFilterItem` |
| 包裹过滤器 | `create:package_filter` | `Address`（string） |

### 1.21.1 示例（data components）

```mcfunction
/gainfilter create:filter 1
/gainfilter create:attribute_filter 1
/gainfilter create:package_filter 1
/gainfilter create:filter[minecraft:custom_name='{"text":"Ticker Test","italic":false}'] 1
```

### 长参数

超过 256 字符的 NBT/组件参数需要聊天长度突破，模组已内置：

- 把聊天输入框上限提升到 `Integer.MAX_VALUE`；
- 拦截 `ChatScreen.normalizeChatMessage`，`/` 开头的指令原样放行。

---

## 原理（简述）

正常客户端在删除 Stock Ticker 分类时，会把界面里的完整 `entry` 放入退款包：

```java
CatnipServices.NETWORK.sendToServer(
    new StockKeeperCategoryRefundPacket(
        menu.contentHolder.getBlockPos(),
        entry
    )
);
```

服务端处理退款时：

```java
if (!filter.isEmpty() && filter.getItem() instanceof FilterItem)
    player.getInventory().placeItemBackInInventory(filter);
```

关键点：

- `filter` 完全来自客户端；
- 只检查根物品是 `FilterItem`；
- 不检查该过滤器是否存在于服务端分类；
- 不校验 NBT/组件；
- 不限制数量；
- 不绑定菜单会话或一次性事务。

1.20.1 的基类 `BlockEntityConfigurationPacket` 只检查玩家模式、区块加载、距离和方块实体类型，不检查菜单或数据归属。

因此，客户端可以提交任意 NBT/组件的过滤器，服务端会原样放进玩家背包。

---

## 仓库内容

```text
.
├── README.md
├── LICENSE
├── CHANGELOG.md
├── dist\
│   ├── create-filter-breaker-1.21.1-0.1.0.jar
│   └── README.md
├── source\
│   ├── 1.21.1-decompiled\          1.21.1 原版 mod 反编译源码
│   ├── create-6.0.8-reference\     Create 6.0.8 (1.20.1) 关键类反编译
│   └── README.md
└── docs\
    ├── 1.20.1-port-prompt.md       1.20.1 移植提示词
    ├── original-mod-README.md      原版 mod README
    └── ai-bug-hunting\             给 AI 的协议审计工具箱
        ├── METHODOLOGY.md
        ├── VERSIONING.md
        ├── CHECKLIST.md
        ├── TOOLS.md
        ├── scripts\
        └── CASES\
```

---

## 给你的 AI 这个文件，让 AI 找寻新机械动力 bug

把 **`docs/ai-bug-hunting/`** 整个文件夹交给你的 AI。

它是一个通用的 Minecraft mod 协议审计工具箱，不绑定 Create、也不绑定某一个版本。里面包含：

- `METHODOLOGY.md`：如何系统性地找「服务端信任客户端」漏洞；
- `VERSIONING.md`：如何判断漏洞在新版本 / 另一加载器上是否仍然存在；
- `CHECKLIST.md`：逐包审计清单；
- `TOOLS.md`：解包、反编译、javap、检索、版本 diff 的具体命令；
- `scripts/`：可直接运行的 `triage_packets.sh`、`find_send_sites.sh`、`audit_grep.sh`、`decompile_jar.sh`；
- `CASES/create-stockticker.md`：Create 6.x 退款包案例，演示整套流程。

可以直接这样对你的 AI 说：

> 按 `docs/ai-bug-hunting/METHODOLOGY.md` 审计 Create 6.x 的网络包。
> 先用 `scripts/triage_packets.sh` 列出所有包类，再用 `scripts/find_send_sites.sh` 找客户端发包点，
> 按 `CHECKLIST.md` 逐个分析「客户端可控字段 → 危险 sink → 缺失的服务端校验」。
> 用 `VERSIONING.md` 判断问题在 1.20.1 与 1.21.1 是否同样成立。

核心判定公式：

```text
客户端可控字段
+ 直接进入有副作用的 sink（给物品/改方块/写方块实体/权限…）
+ 服务端没有对照自己的真值（归属/存在性/会话/数量/NBT/权限）
= 候选漏洞
```

换版本、换加载器、换线格式（NBT ↔ 组件、SimplePacketBase ↔ CustomPacketPayload）都不会自动修掉这类问题；先比对逻辑层，再看传输层。

---

## 注意事项

- 只在本地存档、自建服务器或获得授权的测试环境使用；
- 测试前备份世界；
- 不要在公共服务器或他人的服务器上使用；
- 发现漏洞请优先报告给 Create 维护者，并给修复留出时间；
- 本工具不注册任何服务端内容，命令在客户端本地执行，只有退款包会发送到服务端。

## License 与致谢

- 原版 **Create FilterBreaker** 作者：Adnermo，MIT License（原 README 声明）。原仓库已删除，本仓库保存了原构建与反编译参考。
- Create 模组：Creators-of-Create，MIT License。
- 反编译工具：Vineflower 1.12.0。
- 本项目与 Create 官方无关。
