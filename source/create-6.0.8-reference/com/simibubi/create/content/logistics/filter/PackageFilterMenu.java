package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.AllMenuTypes;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class PackageFilterMenu extends AbstractFilterMenu {
   String address;
   EditBox addressInput;

   public PackageFilterMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
      super(type, id, inv, extraData);
   }

   public PackageFilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack stack) {
      super(type, id, inv, stack);
   }

   public static PackageFilterMenu create(int id, Inventory inv, ItemStack stack) {
      return new PackageFilterMenu((MenuType<?>)AllMenuTypes.PACKAGE_FILTER.get(), id, inv, stack);
   }

   @Override
   protected int getPlayerInventoryXOffset() {
      return 40;
   }

   @Override
   protected int getPlayerInventoryYOffset() {
      return 101;
   }

   @Override
   protected void addFilterSlots() {
   }

   protected ItemStackHandler createGhostInventory() {
      return new ItemStackHandler();
   }

   public void clearContents() {
      this.address = "";
   }

   protected void initAndReadInventory(ItemStack filterItem) {
      super.initAndReadInventory(filterItem);
      this.address = filterItem.m_41784_().m_128461_("Address");
   }

   @Override
   protected void saveData(ItemStack filterItem) {
      super.saveData(filterItem);
      filterItem.m_41784_().m_128359_("Address", this.address);
   }

   public ItemStack m_7648_(Player playerIn, int index) {
      return ItemStack.f_41583_;
   }
}
