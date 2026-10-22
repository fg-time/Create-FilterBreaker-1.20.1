package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryMenu.InactiveItemHandlerSlot;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryMenu.InactiveSlot;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;

public class StockKeeperCategoryMenu extends MenuBase<StockTickerBlockEntity> {
   public boolean slotsActive = true;
   public ItemStackHandler proxyInventory;

   public StockKeeperCategoryMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
      super(type, id, inv, extraData);
   }

   public static AbstractContainerMenu create(int pContainerId, Inventory pPlayerInventory, StockTickerBlockEntity stockTickerBlockEntity) {
      return new StockKeeperCategoryMenu((MenuType<?>)AllMenuTypes.STOCK_KEEPER_CATEGORY.get(), pContainerId, pPlayerInventory, stockTickerBlockEntity);
   }

   public StockKeeperCategoryMenu(MenuType<?> type, int id, Inventory inv, StockTickerBlockEntity contentHolder) {
      super(type, id, inv, contentHolder);
   }

   protected void initAndReadInventory(StockTickerBlockEntity contentHolder) {
      this.proxyInventory = new ItemStackHandler(1);
   }

   protected StockTickerBlockEntity createOnClient(FriendlyByteBuf extraData) {
      BlockPos blockPos = extraData.m_130135_();
      return (StockTickerBlockEntity)((StockTickerBlock)AllBlocks.STOCK_TICKER.get()).getBlockEntity(Minecraft.m_91087_().f_91073_, blockPos);
   }

   protected void addSlots() {
      this.m_38897_(new InactiveItemHandlerSlot(this, this.proxyInventory, 0, 16, 24));
      this.addPlayerSlots(18, 106);
   }

   protected Slot createPlayerSlot(Inventory inventory, int index, int x, int y) {
      return new InactiveSlot(this, inventory, index, x, y);
   }

   protected void saveData(StockTickerBlockEntity contentHolder) {
   }

   public boolean m_6875_(Player player) {
      return !((StockTickerBlockEntity)this.contentHolder).m_58901_()
         && player.m_20182_().m_82509_(Vec3.m_82512_(((StockTickerBlockEntity)this.contentHolder).m_58899_()), player.getBlockReach() + 4.0);
   }

   public ItemStack m_7648_(Player pPlayer, int index) {
      Slot clickedSlot = this.m_38853_(index);
      if (!clickedSlot.m_6657_()) {
         return ItemStack.f_41583_;
      }

      ItemStack stack = clickedSlot.m_7993_();
      int size = 1;
      boolean success = false;
      if (index < size) {
         success = !this.m_38903_(stack, size, this.f_38839_.size(), true);
      } else {
         success = !this.m_38903_(stack, 0, size, false);
      }

      return success ? ItemStack.f_41583_ : stack;
   }
}
