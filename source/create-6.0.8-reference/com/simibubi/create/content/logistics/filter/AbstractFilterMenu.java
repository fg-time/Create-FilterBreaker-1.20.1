package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.foundation.gui.menu.HeldItemGhostItemMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractFilterMenu extends HeldItemGhostItemMenu {
   protected AbstractFilterMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
      super(type, id, inv, extraData);
   }

   protected AbstractFilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder) {
      super(type, id, inv, contentHolder);
   }

   protected boolean allowRepeats() {
      return false;
   }

   protected abstract int getPlayerInventoryXOffset();

   protected abstract int getPlayerInventoryYOffset();

   protected abstract void addFilterSlots();

   protected void addSlots() {
      this.addPlayerSlots(this.getPlayerInventoryXOffset(), this.getPlayerInventoryYOffset());
      this.addFilterSlots();
   }

   protected void saveData(ItemStack contentHolder) {
      contentHolder.m_41784_().m_128365_("Items", this.ghostInventory.serializeNBT());
   }
}
