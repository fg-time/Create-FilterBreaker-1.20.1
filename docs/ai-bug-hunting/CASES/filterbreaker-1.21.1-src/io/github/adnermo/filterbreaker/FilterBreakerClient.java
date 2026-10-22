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
