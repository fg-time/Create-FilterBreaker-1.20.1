# 反编译源码（仅供学习与回归测试参考）

## 1.21.1-decompiled

由 `create-filter-breaker-1.21.1-0.1.0.jar`（原 `111bugmod.jar`）反编译得到。

- 工具：Vineflower 1.12.0
- 原始 jar SHA-256：`b583becad6cac63828722487560e8e2ab49191980c679d756b28cadb5b663335`
- 内容：`FilterBreaker.java`、`FilterBreakerClient.java`、`ChatLimitBreakerClient.java`、`mixin/ChatScreenMixin.java`，以及 `META-INF`、`filter_breaker.mixins.json`、`pack.mcmeta`
- 该 mod 没有混淆，类名、字符串、中文提示均完整保留。

## create-6.0.8-reference

从 Create `create-1.20.1-6.0.8.jar` 中提取并反编译的关键类，仅作为 1.20.1 移植与漏洞分析参考：

- `StockKeeperCategoryRefundPacket`（退款包与服务端处理）
- `BlockEntityConfigurationPacket`（基类校验）
- `AllPackets`（SimpleChannel / 发包）
- filter 目录（`FilterItem`、`ListFilterItem`、`PackageFilterItem` 等）
- `StockKeeperCategoryScreen` / `StockTickerBlockEntity` 等

Create 为 MIT License，版权归 Creators-of-Create 所有。此目录仅保留分析所需的最小类集合。

## 注意

反编译结果用于学习、兼容性研究与回归测试；请遵守原作者许可证，不要用于未授权的服务器。
