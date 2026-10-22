package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.foundation.utility.CreateLang;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class PackageFilterItem extends FilterItem {
   protected PackageFilterItem(Properties properties) {
      super(properties);
   }

   @Override
   public List<Component> makeSummary(ItemStack filter) {
      if (!filter.m_41782_()) {
         return Collections.emptyList();
      }

      String address = filter.m_41784_().m_128461_("Address");
      return address.isBlank()
         ? Collections.emptyList()
         : List.of(CreateLang.text("-> ").style(ChatFormatting.GRAY).add(CreateLang.text(address).style(ChatFormatting.GOLD)).component());
   }

   @Override
   public AbstractContainerMenu m_7208_(int id, Inventory inv, Player player) {
      return PackageFilterMenu.create(id, inv, player.m_21205_());
   }

   @Override
   public FilterItemStack makeStackWrapper(ItemStack filter) {
      return new FilterItemStack.PackageFilterItemStack(filter);
   }

   @Override
   public ItemStack[] getFilterItems(ItemStack itemStack) {
      return new ItemStack[0];
   }
}
