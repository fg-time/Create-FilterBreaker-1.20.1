package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.items.ItemStackHandler;

public class ListFilterItem extends FilterItem {
   protected ListFilterItem(Properties properties) {
      super(properties);
   }

   @Override
   public List<Component> makeSummary(ItemStack filter) {
      if (!filter.m_41782_()) {
         return Collections.emptyList();
      }

      List<Component> list = new ArrayList<>();
      ItemStackHandler filterItems = this.getFilterItemHandler(filter);
      boolean blacklist = filter.m_41784_().m_128471_("Blacklist");
      list.add(
         (blacklist ? CreateLang.translateDirect("gui.filter.deny_list", new Object[0]) : CreateLang.translateDirect("gui.filter.allow_list", new Object[0]))
            .m_130940_(ChatFormatting.GOLD)
      );
      int count = 0;

      for (int i = 0; i < filterItems.getSlots(); i++) {
         if (count > 3) {
            list.add(Component.m_237113_("- ...").m_130940_(ChatFormatting.DARK_GRAY));
            break;
         }

         ItemStack filterStack = filterItems.getStackInSlot(i);
         if (!filterStack.m_41619_()) {
            list.add(Component.m_237113_("- ").m_7220_(filterStack.m_41786_()).m_130940_(ChatFormatting.GRAY));
            count++;
         }
      }

      return count == 0 ? Collections.emptyList() : list;
   }

   @Override
   public AbstractContainerMenu m_7208_(int id, Inventory inv, Player player) {
      return FilterMenu.create(id, inv, player.m_21205_());
   }

   @Override
   public FilterItemStack makeStackWrapper(ItemStack filter) {
      return new FilterItemStack.ListFilterItemStack(filter);
   }

   public ItemStackHandler getFilterItemHandler(ItemStack stack) {
      ItemStackHandler newInv = new ItemStackHandler(18);
      CompoundTag invNBT = stack.m_41698_("Items");
      if (!invNBT.m_128456_()) {
         newInv.deserializeNBT(invNBT);
      }

      return newInv;
   }

   @Override
   public ItemStack[] getFilterItems(ItemStack itemStack) {
      return itemStack.m_41782_() && itemStack.m_41784_().m_128471_("Blacklist")
         ? new ItemStack[0]
         : ItemHelper.getNonEmptyStacks(this.getFilterItemHandler(itemStack)).toArray(ItemStack[]::new);
   }
}
