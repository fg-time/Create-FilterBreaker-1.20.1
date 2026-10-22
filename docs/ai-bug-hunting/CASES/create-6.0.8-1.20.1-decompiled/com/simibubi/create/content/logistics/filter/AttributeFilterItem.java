package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

public class AttributeFilterItem extends FilterItem {
   protected AttributeFilterItem(Properties properties) {
      super(properties);
   }

   @Override
   public List<Component> makeSummary(ItemStack filter) {
      if (!filter.m_41782_()) {
         return Collections.emptyList();
      }

      List<Component> list = new ArrayList<>();
      AttributeFilterMenu.WhitelistMode whitelistMode = AttributeFilterMenu.WhitelistMode.values()[filter.m_41784_().m_128451_("WhitelistMode")];
      list.add(
         (whitelistMode == AttributeFilterMenu.WhitelistMode.WHITELIST_CONJ
               ? CreateLang.translateDirect("gui.attribute_filter.allow_list_conjunctive", new Object[0])
               : (
                  whitelistMode == AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ
                     ? CreateLang.translateDirect("gui.attribute_filter.allow_list_disjunctive", new Object[0])
                     : CreateLang.translateDirect("gui.attribute_filter.deny_list", new Object[0])
               ))
            .m_130940_(ChatFormatting.GOLD)
      );
      int count = 0;

      for (Tag inbt : filter.m_41784_().m_128437_("MatchedAttributes", 10)) {
         CompoundTag compound = (CompoundTag)inbt;
         ItemAttribute attribute = ItemAttribute.loadStatic(compound);
         if (attribute != null) {
            boolean inverted = compound.m_128471_("Inverted");
            if (count > 3) {
               list.add(Component.m_237113_("- ...").m_130940_(ChatFormatting.DARK_GRAY));
               break;
            }

            list.add(Component.m_237113_("- ").m_7220_(attribute.format(inverted)));
            count++;
         }
      }

      return count == 0 ? Collections.emptyList() : list;
   }

   @Override
   public AbstractContainerMenu m_7208_(int id, Inventory inv, Player player) {
      return AttributeFilterMenu.create(id, inv, player.m_21205_());
   }

   @Override
   public FilterItemStack makeStackWrapper(ItemStack filter) {
      return new FilterItemStack.AttributeFilterItemStack(filter);
   }

   @Override
   public ItemStack[] getFilterItems(ItemStack itemStack) {
      CompoundTag tag = itemStack.m_41784_();
      AttributeFilterMenu.WhitelistMode whitelistMode = AttributeFilterMenu.WhitelistMode.values()[tag.m_128451_("WhitelistMode")];
      ListTag attributes = tag.m_128437_("MatchedAttributes", 10);
      if (whitelistMode == AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ
         && attributes.size() == 1
         && ItemAttribute.loadStatic((CompoundTag)attributes.get(0)) instanceof InTagAttribute inTag) {
         ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
         if (tagManager.isKnownTagName(inTag.tag)) {
            ITag<Item> taggedItems = tagManager.getTag(inTag.tag);
            if (!taggedItems.isEmpty()) {
               ItemStack[] stacks = new ItemStack[taggedItems.size()];
               int i = 0;

               for (Item item : taggedItems) {
                  stacks[i] = new ItemStack(item);
                  i++;
               }

               return stacks;
            }
         }
      }

      return new ItemStack[0];
   }
}
