# 任务：把 Create FilterBreaker 移植成 Forge 1.20.1 版（替另一个 agent 干活的完整提示词）

## 0. 任务目标

用 Forge 1.20.1 重做一个客户端 mod，功能与已拆解的 1.21.1 NeoForge 版 `Create FilterBreaker` 完全一致：

- 注册客户端指令 `/gainfilter`，参数接受 **/give 风格带 NBT 的 ItemStack**（例如 `create:filter{Items:[...]}`），而不是只接受物品 ID。
- 指令把构造好的 ItemStack 通过反射塞进 Create 的 `StockKeeperCategoryRefundPacket`，发给服务端。
- 利用 Create 6.x 退款包漏洞：服务端不校验物品 NBT，直接把包里的 ItemStack 塞进玩家背包，从而获得任意 NBT 的过滤器。
- 附带聊天长度突破（EditBox 上限 + Mixin 拦截 `ChatScreen.normalizeChatMessage`），否则长 NBT 超过 256 字符会被截断。

**客户端 mod，不注册任何服务端内容。指令在客户端本地执行，只有退款包走网络。**

---

## 0.5 原版 README 给出的行为基准（仓库已删除，以下为原文要点）

- 原项目：GitHub `Adnermo-002/Create-FilterBreaker` —— **现已不可访问（已删除），不要指望能 clone 到源码。**
- 1.21.1 版兼容组合（仅作参考）：MC 1.21.1 / Java 21 / NeoForge 21.1.235 / Create 6.0.10 / FilterBreaker 0.1.0 / Client 端。
- 用法：
  - 在附近放置或找到一个已加载的 Stock Ticker（服务端会检查玩家模式、区块加载、距离、同步方块实体）；
  - 看向它执行 `/gainfilter create:filter 1`，或手动指定坐标；
  - 成功后客户端显示发送位置、数量、物品 ID。
- 参数：`<item>` 是 `/give` 风格物品参数，`[count]` 范围 1–99，默认 1。
- **根物品限制**：退款处理只认 `filter.getItem() instanceof FilterItem`；嵌套数据里的物品能否变成独立物品，取决于后续读取/转换/打开/提取该数据的游戏逻辑 —— 退款本身只给根物品。
- **服务端可见性**：`/gainfilter` 是客户端指令，命令文本通常不会进入服务端命令日志；服务端收到的是 Create 的自定义 serverbound 退款包（BlockPos + ItemStack）。服务端网络审计或对该包的埋点可以记录到。
- 构建：`gradlew clean build`，产物在 `build/libs/`。
- 风险提示：会改变服务端库存状态，测试前备份存档。
- 1.21.1 执行链：`ItemArgument` 解析 ID + DataComponents → `ItemInput.createItemStack(count, false)` → 反射构造包 → `PacketDistributor.sendToServer` → 服务端 `ItemStack.STREAM_CODEC` 重建完整栈 → `instanceof FilterItem` → `placeItemBackInInventory(filter)`。

---

## 1. 目标环境（本机已确认，不要改版本）

| 项 | 值 |
|---|---|
| Minecraft | 1.20.1 |
| 加载器 | Forge 47.4.28（ForgeGradle 6，Java 17 编译目标；运行时兼容 `[47,)`） |
| Create | `create-1.20.1-6.0.8.jar`（放到你的 1.20.1 实例 `mods` 目录） |
| 测试实例 | 任意 Minecraft 1.20.1 + Forge 47.x 实例（安装 Create 6.0.x），构建产物放进它的 `mods\` 目录实测 |
| 构建产物 | 输出到 D 盘（禁止写 C 盘），建议 `D:\filterbreaker-1201\` |
| 产物 jar | `create-filter-breaker-forge-1.20.1-0.1.0.jar` |

---

## 2. 漏洞链路（1.20.1 Create 6.0.8 实测反编译结果，直接按这个写，不要猜）

### 2.1 目标类（1.20.1 与 1.21.1 类名相同，构造器相同）

```
com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryRefundPacket
构造器：(net.minecraft.core.BlockPos, net.minecraft.world.item.ItemStack)
```

1.20.1 里它继承 `BlockEntityConfigurationPacket<StockTickerBlockEntity>` → `SimplePacketBase`（Forge SimpleChannel 体系），**不是** `CustomPacketPayload`。

### 2.2 1.20.1 的发送方式（与 1.21.1 不同！）

Create 自己在 `StockKeeperCategoryScreen` 里是这么发的（javap 实测）：

```java
AllPackets.getChannel().sendToServer(new StockKeeperCategoryRefundPacket(pos, stack));
```

- `AllPackets.getChannel()` 是 `public static`，返回 `net.minecraftforge.network.simple.SimpleChannel`。
- 反射写法（推荐，避免把 Create 加进编译依赖）：

```java
Class<?> allPackets = Class.forName("com.simibubi.create.AllPackets");
Object channel = allPackets.getMethod("getChannel").invoke(null);
Class<?> packetClass = Class.forName(
    "com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryRefundPacket");
Object packet = packetClass.getConstructor(BlockPos.class, ItemStack.class)
                            .newInstance(position, stack);
((net.minecraftforge.network.simple.SimpleChannel) channel).sendToServer(packet);
```

**绝对不要照搬 1.21.1 的 `PacketDistributor.sendToServer(CustomPacketPayload)`，也不要照搬截图里的 `CatnipServices.NETWORK` —— 1.20.1 的 Create 里没有 `CatnipServices` 类，那套只存在于 1.21.1。**

### 2.3 服务端处理（漏洞核心，反编译确认）

```java
protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
   if (!this.filter.isEmpty() && this.filter.getItem() instanceof FilterItem) {
      player.getInventory().placeItemBackInInventory(this.filter);
   }
}
```

- **没有任何 NBT / tag / 组件校验**，收到的 ItemStack 原样给玩家。
- 唯一的门槛：物品必须 `instanceof FilterItem`（否则静默忽略）。

### 2.4 基类校验（`BlockEntityConfigurationPacket.handle`）

发送后服务端只做这些检查，全部可满足：

- 发送者非 null、非旁观者（`isSpectator()`）、非冒险模式；
- `level.isLoaded(pos)` 为真；
- 坐标距离玩家 `< 20` 格；
- 该坐标的方块实体是 `SyncedBlockEntity`（Stock Ticker 满足）。

**不需要打开 Stock Ticker 界面，不需要它上面有分类，也没有权限检查。站在 20 格内任意一个已加载的 Stock Ticker 旁边直接发包即可。**

### 2.5 截图证据

客户端删除分类时，把**客户端自己构造的** `entry` 原样传给服务端：

```
CatnipServices.NETWORK.sendToServer(
    new StockKeeperCategoryRefundPacket(menu.contentHolder.getBlockPos(), entry));
```

服务端退款时直接使用客户端传入的 entry，而不是仓储发报机自身保存的数据 —— 这就是可自定义任意过滤器的原因。

---

## 3. 1.20.1 的过滤器 NBT 格式（旧版 NBT，不是 1.21.1 的 data components）

**1.20.1 没有 data components，1.21.1 的 `create:package_filter[minecraft:food={...}]` 语法在这里不存在。指令必须使用 `/give` 风格的旧 NBT：`create:package_filter{...}`。**

反编译 Create 6.0.8 得到的真实键名：

| 物品 | 注册名 | NBT 格式 |
|---|---|---|
| 列表过滤器 | `create:filter` | `Items`（Forge ItemStackHandler 序列化格式的列表）+ `Blacklist`（byte） |
| 属性过滤器 | `create:attribute_filter` | 见 `AttributeFilterItem` / `FilterItemStack$AttributeFilterItemStack` |
| 包裹过滤器 | `create:package_filter` | `Address`（string） |

可直接测试的例子：

```
/gainfilter create:filter{Items:[{Slot:0b,id:"minecraft:oak_log",Count:1b}]} 1
/gainfilter create:filter{Items:[{Slot:0b,id:"minecraft:barrel",Count:1b,tag:{BlockEntityTag:{Items:[{Slot:0b,id:"minecraft:oak_log",Count:1b}]}}}],Blacklist:0b} 1
/gainfilter create:package_filter{Address:"test"} 1
```

说明：`ItemArgument` + `ItemInput.createItemStack(count, false)` 在 1.20.1 会解析并应用 `{...}` NBT，和原版 `/give` 完全一致，不需要自己解析 NBT。

### 3.1 原版指令生成器算法（1.21.1 参考，来自桌面 `gainfilter.py`）

原版生成器最终产出的指令形态：

```
/gainfilter create:package_filter[minecraft:food={nutrition:1,saturation:0.1,can_always_eat:true,eat_seconds:0.05,using_converts_to:{id:"minecraft:barrel",components:{"minecraft:container":[{item:{count:1,id:"minecraft:oak_log"},slot:0}]}}}] 1
```

它的算法（mod 不需要复刻，但要保证兼容这种"整串 /give 风格物品"的输入）：

1. 用户粘贴物品 NBT（F3+I 或 `/data get` 得到），例如 `{components:{...},count:1,id:"minecraft:barrel"}`。
2. 提取最外层物品 id（兜底 `minecraft:barrel`），并抽出每个容器条目 `{item:{...},slot:N}`；**物品 NBT 整块原样搬运，一个字段都不许洗**（附魔、自定义名、属性都要保留）。
3. 条目按 slot 排序后塞进 `using_converts_to.components."minecraft:container"`。
4. 单个物品 id 模式输出 `using_converts_to:{id:"xxx",count:1}`；多个 id 自动打包成木桶/潜影盒。
5. 末尾追加 ` 1`（ItemStack 数量）；整条必须一行，`[` 要紧贴物品 id。

**对 mod 的要求：`item` 参数必须由 Brigadier 的 `ItemArgument` 解析成单个 /give 风格参数，解析出的 ItemStack 原封不动发给服务端，不得清理/重建 NBT。**

### 3.2 到 1.20.1 的翻译（重要）

- 1.20.1 没有 data components：`create:package_filter[...]` 不合法，必须写成 `create:package_filter{...}`。
- `minecraft:food` / `using_converts_to` 是 1.21.1 的组件，1.20.1 不存在；**不要把上面那串组件语法硬塞进 1.20.1**（Brigadier 会直接解析失败）。
- 1.20.1 能携带的是旧 NBT；mod 只负责原样转发。1.20.1 上具体哪种 NBT 能达成"任意过滤器/任意物品"效果，要求 agent 在 你的 1.20.1 实例 实例上实测确认，**不要凭空编造键名**。
- 已实测确认的 1.20.1 过滤器 NBT 键见上表（`Items` / `Blacklist` / `Address`）。
- 原版 README 说 1.21.1 的 data component 必须已注册并通过 Codec 检查；1.20.1 没有这套限制 —— 旧 NBT 的键不需要注册，原版 `ItemParser` 会接受任意 tag（但物品 ID 必须存在）。

---

## 4. 指令规格

```
/gainfilter                                  → 显示中文用法
/gainfilter <item>[<nbt>] [count]            → 对准星指向的方块发包
/gainfilter <x> <y> <z> <item>[<nbt>] [count] → 对指定坐标发包
```

- `count` 范围 `1..99`（`IntegerArgumentType.integer(1, 99)`），默认 1。
- `item` 用 `ItemArgument.item(buildContext)`，必须支持 NBT。
- 准星不是方块时提示并返回 0。
- Create 未加载时提示并返回 0。
- 发包前检测物品是否是三种 Create 过滤器之一（`create:filter` / `create:attribute_filter` / `create:package_filter`），不是则只打警告（服务端会忽略），但仍允许发送 —— 与原 1.21.1 版行为一致。
- 发包成功/失败都要用中文 `Component.literal("[FilterBreaker] ...")` 显示在聊天栏。
- 原版 README 的基础用例也要能跑：
  ```
  /gainfilter create:filter 1
  /gainfilter create:attribute_filter 1
  /gainfilter create:package_filter 1
  ```
  带自定义名的坐标用例（注意 1.20.1 是旧 NBT 写法，不是组件写法）：
  ```
  /gainfilter 12 64 -8 create:filter{display:{Name:'{"text":"Ticker Test","italic":false}'}} 1
  ```
- 服务端可见性提示原版已知：客户端指令文本通常不进服务端命令日志，但 Create 退款包本身可被服务端审计记录。移植版保持"只发包、不注册服务端内容"。

---

## 5. 聊天长度突破（必需，否则长 NBT 会被截断到 256 字符）

两件事都要做，缺一不可：

1. **Forge 事件** `ScreenEvent.Init.Post`：屏幕是 `ChatScreen` 时遍历 `event.getListenersList()`，把 `EditBox` 的 `setMaxLength(Integer.MAX_VALUE)`。
2. **Mixin** 到 `net.minecraft.client.gui.screens.ChatScreen` 的 `normalizeChatMessage`，`@At("HEAD")` + `cancellable = true`：

```java
String normalized = StringUtils.normalizeSpace(message.trim());
if (normalized.startsWith("/")) {
    callback.setReturnValue(normalized);            // 指令：不截断
} else {
    callback.setReturnValue(StringUtil.trimChatMessage(normalized)); // 聊天：保持原逻辑
}
```

注意事项：

- 先用 javap 确认 1.20.1 里 `normalizeChatMessage` 是实例方法还是静态方法（1.21.1 是实例方法，1.20.1 大概率相同），Mixin 处理器签名必须和目标一致，否则 `defaultRequire: 1` 会直接崩游戏。
- Forge 1.20.1 运行时方法名是 SRG（`m_...`），**必须配置 MixinGradle + refmap**，否则注入找不到方法。
- `mixins.json` 的 `compatibilityLevel` 用 `"JAVA_17"`。

---

## 6. 工程结构 / 构建配置

```
D:\filterbreaker-1201\
  build.gradle            (ForgeGradle 6 + MixinGradle，Java 17)
  gradle.properties
  settings.gradle
  src/main/resources/META-INF/mods.toml
  src/main/resources/filter_breaker.mixins.json
  src/main/java/.../FilterBreaker.java
  src/main/java/.../FilterBreakerClient.java
  src/main/java/.../ChatLimitBreakerClient.java
  src/main/java/.../mixin/ChatScreenMixin.java
```

MixinGradle 配置要点：

```gradle
plugins {
    id 'org.spongepowered.mixin' version '0.7.+'
}
mixin {
    add sourceSets.main, 'filter_breaker.refmap.json'
    config 'filter_breaker.mixins.json'
}
dependencies {
    annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'
}
```

`mods.toml`（Forge 1.20.1 用 `mandatory=true`，不是 NeoForge 的 `type="required"`）：

```toml
modLoader="javafml"
loaderVersion="[47,)"
license="MIT"

[[mods]]
modId="filter_breaker"
version="0.1.0"
displayName="Create FilterBreaker (Forge 1.20.1)"
description="Create 物流退款包漏洞的客户端复现/回归测试工具。"

[[dependencies.filter_breaker]]
    modId="forge"
    mandatory=true
    versionRange="[47,)"
    ordering="NONE"
    side="CLIENT"

[[dependencies.filter_breaker]]
    modId="minecraft"
    mandatory=true
    versionRange="[1.20.1]"
    ordering="NONE"
    side="CLIENT"

[[dependencies.filter_breaker]]
    modId="create"
    mandatory=true
    versionRange="[6.0.0,6.1.0)"
    ordering="AFTER"
    side="CLIENT"
```

- **不要把 Create 加进编译依赖**：所有 Create 类都走反射（与原 1.21.1 版一致）。编译只依赖 Forge。
- 入口用 `MinecraftForge.EVENT_BUS`（不是 `NeoForge.EVENT_BUS`），事件是 `net.minecraftforge.client.event.RegisterClientCommandsEvent` 和 `net.minecraftforge.client.event.ScreenEvent.Init.Post`。
- `FMLEnvironment.dist == Dist.CLIENT` 判断照旧（`net.minecraftforge.fml.loading.FMLEnvironment`）。

---

## 7. 1.21.1 → 1.20.1 差异对照（照抄前先看这张表）

| 项目 | 1.21.1 NeoForge | 1.20.1 Forge（本任务） |
|---|---|---|
| 事件总线 | `NeoForge.EVENT_BUS` | `MinecraftForge.EVENT_BUS` |
| 客户端指令事件 | `net.neoforged.neoforge.client.event.RegisterClientCommandsEvent` | `net.minecraftforge.client.event.RegisterClientCommandsEvent` |
| 发包 | `PacketDistributor.sendToServer(CustomPacketPayload)` | `AllPackets.getChannel().sendToServer(SimplePacketBase)` |
| 包体系 | `CustomPacketPayload` | `SimplePacketBase` / Forge `SimpleChannel` |
| 物品 NBT | data components `[minecraft:food={...}]` | 旧 NBT `{...}` |
| 过滤器存储 | components | item tag：`Items` / `Blacklist` / `Address` |
| mixin 兼容级别 | JAVA_21 | JAVA_17 |
| mods.toml 依赖 | `type="required"` | `mandatory=true` |

---

## 8. 验收清单（必须逐条实测，不能只说"写好了"）

1. `gradlew build` 成功，产出 jar。
2. 用你的 Minecraft 1.20.1 + Forge 47.x 实例（Create 6.0.x）启动客户端，把构建产物放进该实例 `mods\` 目录，加载本 mod 不崩。（用户指定的 47.4.28 实例已不存在；若之后重建，优先用 你的 1.20.1 实例。）
3. 站在 Stock Ticker（仓储发报机）20 格内，`/gainfilter create:filter{Items:[{Slot:0b,id:"minecraft:oak_log",Count:1b}]} 1`：
   - 收到 `create:filter`；
   - NBT 与输入完全一致（用 `/data get entity @s SelectedItem` 或过滤器界面验证）。
4. 测一条超过 256 字符的长 NBT 指令，确认没有被截断（Mixin 生效）。
5. 非过滤器物品（如 `minecraft:stone`）发包：服务端静默忽略，客户端只打警告、不崩。
6. 确认指令是客户端本地执行的：服务端日志/聊天里不出现 `/gainfilter`，也不会有未知指令报错。
7. 确认没有旁观者/冒险模式限制误解：普通生存/创造模式均可用。

## 9. 交付物

- 完整可编译工程（D 盘），构建命令 `gradlew clean build`；
- 构建好的 `create-filter-breaker-forge-1.20.1-0.1.0.jar`（对应原版 `build/libs/create-filter-breaker-0.1.0.jar` 的命名习惯）；
- 简短 README：安装方式、指令用法、测试步骤、兼容性表（MC 1.20.1 / Java 17 / Forge 47.4.28 / Create 6.0.8 / Client）。

---

## 附录 A：1.21.1 原版反编译源码（行为基准）

### FilterBreaker.java

```java
package io.github.adnermo.filterbreaker;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod("filter_breaker")
public final class FilterBreaker {
   public static final String MODID = "filter_breaker";

   public FilterBreaker(IEventBus modEventBus) {
      if (FMLEnvironment.dist == Dist.CLIENT) {
         NeoForge.EVENT_BUS.addListener(FilterBreakerClient::registerClientCommands);
         NeoForge.EVENT_BUS.addListener(ChatLimitBreakerClient::onScreenInitPost);
      }
   }
}
```

### ChatLimitBreakerClient.java

```java
package io.github.adnermo.filterbreaker;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;

final class ChatLimitBreakerClient {
   static final int CHAT_INPUT_MAX_LENGTH = Integer.MAX_VALUE;

   private ChatLimitBreakerClient() {
   }

   static void onScreenInitPost(Post event) {
      if (event.getScreen() instanceof ChatScreen) {
         for (GuiEventListener listener : event.getListenersList()) {
            if (listener instanceof EditBox editBox) {
               editBox.setMaxLength(Integer.MAX_VALUE);
            }
         }
      }
   }
}
```

### mixin/ChatScreenMixin.java

```java
package io.github.adnermo.filterbreaker.mixin;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
   @Inject(method = "normalizeChatMessage", at = @At("HEAD"), cancellable = true)
   private void filterbreaker$normalizeLongCommand(String message, CallbackInfoReturnable<String> callback) {
      String normalized = StringUtils.normalizeSpace(message.trim());
      if (normalized.startsWith("/")) {
         callback.setReturnValue(normalized);
      } else {
         callback.setReturnValue(StringUtil.trimChatMessage(normalized));
      }
   }
}
```

### FilterBreakerClient.java

```java
package io.github.adnermo.filterbreaker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.Constructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

final class FilterBreakerClient {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static Constructor<?> refundPacketConstructor;

   private FilterBreakerClient() {
   }

   static void registerClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
      ItemArgument itemArgument = ItemArgument.item(event.getBuildContext());
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("gainfilter").executes(FilterBreakerClient::help))
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("item", itemArgument).executes(context -> gainAtLookedBlock(context, 1)))
                     .then(
                        Commands.argument("count", IntegerArgumentType.integer(1, 99))
                           .executes(context -> gainAtLookedBlock(context, IntegerArgumentType.getInteger(context, "count")))
                     )
               ))
            .then(
               Commands.argument("pos", BlockPosArgument.blockPos())
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("item", itemArgument)
                           .executes(context -> gainAtPosition(context, BlockPosArgument.getBlockPos(context, "pos"), 1)))
                        .then(
                           Commands.argument("count", IntegerArgumentType.integer(1, 99))
                              .executes(
                                 context -> gainAtPosition(
                                    context, BlockPosArgument.getBlockPos(context, "pos"), IntegerArgumentType.getInteger(context, "count")
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static int help(CommandContext<CommandSourceStack> context) {
      say("用法：/gainfilter <item> [count]（使用准星指向的 Stock Ticker）");
      say("用法：/gainfilter <x> <y> <z> <item> [count]（手动指定 Stock Ticker 坐标）");
      say("item 使用 Minecraft 1.21.1 /give 风格，可包含数据组件。");
      return 1;
   }

   private static int gainAtLookedBlock(CommandContext<CommandSourceStack> context, int count) throws CommandSyntaxException {
      BlockPos position = lookedBlockPosition();
      if (position == null) {
         say("准星没有指向方块；请看向 Stock Ticker，或在指令中填写坐标。");
         return 0;
      } else {
         return gainAtPosition(context, position, count);
      }
   }

   private static int gainAtPosition(CommandContext<CommandSourceStack> context, BlockPos position, int count) throws CommandSyntaxException {
      if (!ModList.get().isLoaded("create")) {
         say("Create 尚未加载。");
         return 0;
      }

      LocalPlayer player = MINECRAFT.player;
      if (player != null && player.connection != null) {
         ItemInput input = ItemArgument.getItem(context, "item");
         ItemStack stack = input.createItemStack(count, false);
         ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
         if (!isKnownCreateFilter(itemId)) {
            say("提示：退款包只处理 FilterItem；当前 item=" + itemId + "，服务端可能忽略。");
         }

         if (!sendRefundPacket(position, stack)) {
            say("退款包发送失败。");
            return 0;
         } else {
            say("已发送退款包：pos=" + format(position) + ", stack=" + stack.getCount() + "x " + stack.getHoverName().getString() + " / " + itemId);
            return 1;
         }
      } else {
         say("当前没有连接到世界或服务器。");
         return 0;
      }
   }

   private static boolean isKnownCreateFilter(ResourceLocation itemId) {
      if (itemId != null && "create".equals(itemId.getNamespace())) {
         return switch (itemId.getPath()) {
            case "filter", "attribute_filter", "package_filter" -> true;
            default -> false;
         };
      } else {
         return false;
      }
   }

   private static boolean sendRefundPacket(BlockPos position, ItemStack stack) {
      try {
         Constructor<?> constructor = getRefundPacketConstructor();
         Object payload = constructor.newInstance(position, stack);
         PacketDistributor.sendToServer((CustomPacketPayload)payload, new CustomPacketPayload[0]);
         return true;
      } catch (ReflectiveOperationException | RuntimeException exception) {
         say("StockKeeperCategoryRefundPacket 异常：" + exception.getClass().getSimpleName() + ": " + exception.getMessage());
         return false;
      }
   }

   private static Constructor<?> getRefundPacketConstructor() throws ReflectiveOperationException {
      if (refundPacketConstructor == null) {
         Class<?> packetClass = Class.forName("com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryRefundPacket");
         refundPacketConstructor = packetClass.getConstructor(BlockPos.class, ItemStack.class);
      }

      return refundPacketConstructor;
   }

   private static BlockPos lookedBlockPosition() {
      HitResult hitResult = MINECRAFT.hitResult;
      return hitResult instanceof BlockHitResult blockHit && hitResult.getType() == Type.BLOCK ? blockHit.getBlockPos() : null;
   }

   private static String format(BlockPos position) {
      return position.getX() + " " + position.getY() + " " + position.getZ();
   }

   private static void say(String message) {
      LocalPlayer player = MINECRAFT.player;
      if (player != null) {
         player.displayClientMessage(Component.literal("[FilterBreaker] " + message), false);
      }
   }
}
```

---

## 附录 B：1.20.1 Create 6.0.8 关键反编译摘录（本次实测）

### StockKeeperCategoryRefundPacket.java

```java
public class StockKeeperCategoryRefundPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {
   private ItemStack filter;

   public StockKeeperCategoryRefundPacket(BlockPos pos, ItemStack filter) {
      super(pos);
      this.filter = filter;
   }

   public StockKeeperCategoryRefundPacket(FriendlyByteBuf buffer) {
      super(buffer);
   }

   protected void readSettings(FriendlyByteBuf buffer) {
      this.filter = buffer.readItem();
   }

   protected void writeSettings(FriendlyByteBuf buffer) {
      buffer.writeItem(this.filter);
   }

   protected void applySettings(StockTickerBlockEntity be) {
   }

   protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
      if (!this.filter.isEmpty() && this.filter.getItem() instanceof FilterItem) {
         player.getInventory().placeItemBackInInventory(this.filter);
      }
   }
}
```

### BlockEntityConfigurationPacket.handle（服务端校验）

```java
public boolean handle(Context context) {
   context.enqueueWork(() -> {
      ServerPlayer player = context.getSender();
      if (player != null && !player.isSpectator() && !AdventureUtil.isAdventure(player)) {
         Level world = player.level();
         if (world != null && world.isLoaded(this.pos)) {
            if (this.pos.closerThan(player.blockPosition(), this.maxRange())) { // maxRange = 20
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

### 列表过滤器 NBT 读取（ListFilterItem）

```java
public ItemStackHandler getFilterItemHandler(ItemStack stack) {
   ItemStackHandler newInv = new ItemStackHandler(18);
   CompoundTag invNBT = stack.getOrCreateTagElement("Items");
   if (!invNBT.isEmpty()) {
      newInv.deserializeNBT(invNBT);
   }
   return newInv;
}
```

`Blacklist` 为 byte；包裹过滤器读字符串键 `Address`。
