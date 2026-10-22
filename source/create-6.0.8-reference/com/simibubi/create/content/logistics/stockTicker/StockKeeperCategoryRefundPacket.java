package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class StockKeeperCategoryRefundPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {
   private ItemStack filter;

   public StockKeeperCategoryRefundPacket(BlockPos pos, ItemStack filter) {
      super(pos);
      this.filter = filter;
   }

   public StockKeeperCategoryRefundPacket(FriendlyByteBuf buffer) {
      super(buffer);
   }

   @Override
   protected void readSettings(FriendlyByteBuf buffer) {
      this.filter = buffer.m_130267_();
   }

   @Override
   protected void writeSettings(FriendlyByteBuf buffer) {
      buffer.m_130055_(this.filter);
   }

   protected void applySettings(StockTickerBlockEntity be) {
   }

   protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
      if (!this.filter.m_41619_() && this.filter.m_41720_() instanceof FilterItem) {
         player.m_150109_().m_150079_(this.filter);
      }
   }
}
