# MC Mod 协议审计工具箱（通用版）

一套给 agent / 逆向人员用的**通用方法论 + 工具**，用来在任意 Minecraft mod、任意版本、任意加载器里寻找同一类问题：

> **服务端信任了客户端提交的数据（server-trusts-client）**

Create 的仓储发报机退款包只是其中一个已实测案例（见 `CASES\create-stockticker.md`），**方法论不绑定 Create，也不绑定 1.20.1 / 1.21.1**。换任何 mod、任何新版本，流程一致。

## 目录

```text
docs/ai-bug-hunting\
├── README.md              ← 总览与索引
├── METHODOLOGY.md         ← 核心：通用寻找思路与判定公式
├── CHECKLIST.md           ← 通用逐包审计清单
├── VERSIONING.md          ← 跨版本/跨加载器：判断「新版本是否还通用」
├── TOOLS.md               ← 通用命令：解包、反编译、javap、检索、diff
├── tools\
│   └── vineflower-1.12.0.jar
├── scripts\
│   ├── decompile_jar.sh    ← 反编译任意 mod jar
│   ├── triage_packets.sh   ← 列出包/网络类 + 方法签名
│   ├── find_send_sites.sh  ← 在字节码里找所有网络发送调用点
│   └── audit_grep.sh       ← 扫危险 sink / 反序列化入口 / 校验点
└── CASES\
    ├── create-stockticker.md          ← 案例：Create 6.x 退款包（含 6.0 通用性分析）
    ├── README-CreateFilterBreaker.md  ← 案例原版 README
    ├── forge1201-port-prompt.md       ← 案例移植提示词
    ├── filterbreaker-1.21.1-src\      ← 案例反编译源码
    └── create-6.0.8-1.20.1-decompiled\
```

## 快速开始（任意 mod）

```bash
# 1. 列出所有包类
scripts/triage_packets.sh <mod.jar> scan

# 2. 找客户端发包点
scripts/find_send_sites.sh <mod.jar> send-sites.txt

# 3. 反编译
scripts/decompile_jar.sh <mod.jar> out

# 4. 扫危险 sink
scripts/audit_grep.sh out hits.txt
```

然后按 `CHECKLIST.md` 逐包过：**反序列化字段 → handler → sink → 校验缺口**。

## 核心判定公式

```text
客户端可控字段
+ 直接进入有副作用的 sink（给物品/改方块/写方块实体/权限…）
+ 服务端没有对照自己的真值（归属/存在性/会话/数量/NBT/权限）
= 候选漏洞
```

版本换了、加载器换了、线格式换了（NBT ↔ 组件、SimplePacketBase ↔ CustomPacketPayload）都**不改变**这个公式；只影响复现时怎么构造包。判断方法见 `VERSIONING.md`。

## 本次案例一句话结论

Create 6.x 的 `StockKeeperCategoryRefundPacket` 把客户端构造的完整 ItemStack 交给服务端，服务端只查 `instanceof FilterItem`，不查服务端分类、不查 NBT/组件、不限数量、不绑会话，直接入背包。1.20.1 与 1.21.1 的发包机制不同，**漏洞逻辑完全一致**——这就是「6.0 通用」的含义。

## 免责声明

只用于本地存档、自己搭建的服务器或获得授权的测试环境。测试前备份；此类漏洞会真实改变服务端状态。不要在生产服或他人服务器上使用。
