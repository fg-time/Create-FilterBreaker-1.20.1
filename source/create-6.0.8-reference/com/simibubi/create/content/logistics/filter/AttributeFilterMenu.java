package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AttributeFilterMenu extends AbstractFilterMenu {
   AttributeFilterMenu.WhitelistMode whitelistMode;
   List<Pair<ItemAttribute, Boolean>> selectedAttributes;

   public AttributeFilterMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
      super(type, id, inv, extraData);
   }

   public AttributeFilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack stack) {
      super(type, id, inv, stack);
   }

   public static AttributeFilterMenu create(int id, Inventory inv, ItemStack stack) {
      return new AttributeFilterMenu((MenuType<?>)AllMenuTypes.ATTRIBUTE_FILTER.get(), id, inv, stack);
   }

   public void appendSelectedAttribute(ItemAttribute itemAttribute, boolean inverted) {
      this.selectedAttributes.add(Pair.of(itemAttribute, inverted));
   }

   protected void init(Inventory inv, ItemStack contentHolder) {
      super.init(inv, contentHolder);
      ItemStack stack = new ItemStack(Items.f_42656_);
      stack.m_41714_(Component.m_237113_("Selected Tags").m_130944_(new ChatFormatting[]{ChatFormatting.RESET, ChatFormatting.BLUE}));
      this.ghostInventory.setStackInSlot(1, stack);
   }

   @Override
   protected int getPlayerInventoryXOffset() {
      return 51;
   }

   @Override
   protected int getPlayerInventoryYOffset() {
      return 107;
   }

   @Override
   protected void addFilterSlots() {
      this.m_38897_(new SlotItemHandler(this.ghostInventory, 0, 16, 27));
      this.m_38897_(new SlotItemHandler(this.ghostInventory, 1, 16, 62) {
         public boolean m_8010_(Player playerIn) {
            return false;
         }
      });
   }

   protected ItemStackHandler createGhostInventory() {
      return new ItemStackHandler(2);
   }

   public void clearContents() {
      this.selectedAttributes.clear();
   }

   public void m_150399_(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId != 37) {
         super.m_150399_(slotId, dragType, clickTypeIn, player);
      }
   }

   public boolean m_5622_(Slot slotIn) {
      return slotIn.f_40219_ == 37 ? false : super.m_5622_(slotIn);
   }

   public boolean m_5882_(ItemStack stack, Slot slotIn) {
      return slotIn.f_40219_ == 37 ? false : super.m_5882_(stack, slotIn);
   }

   public ItemStack m_7648_(Player playerIn, int index) {
      if (index == 37) {
         return ItemStack.f_41583_;
      }

      if (index == 36) {
         this.ghostInventory.setStackInSlot(37, ItemStack.f_41583_);
         return ItemStack.f_41583_;
      }

      if (index < 36) {
         ItemStack stackToInsert = this.playerInventory.m_8020_(index);
         ItemStack copy = stackToInsert.m_41777_();
         copy.m_41764_(1);
         this.ghostInventory.setStackInSlot(0, copy);
      }

      return ItemStack.f_41583_;
   }

   protected void initAndReadInventory(ItemStack filterItem) {
      super.initAndReadInventory(filterItem);
      this.selectedAttributes = new ArrayList<>();
      this.whitelistMode = AttributeFilterMenu.WhitelistMode.values()[filterItem.m_41784_().m_128451_("WhitelistMode")];
      ListTag attributes = filterItem.m_41784_().m_128437_("MatchedAttributes", 10);
      attributes.forEach(inbt -> {
         CompoundTag compound = (CompoundTag)inbt;
         ItemAttribute attribute = ItemAttribute.loadStatic(compound);
         if (attribute != null) {
            this.selectedAttributes.add(Pair.of(attribute, compound.m_128471_("Inverted")));
         }
      });
   }

   @Override
   protected void saveData(ItemStack filterItem) {
      filterItem.m_41784_().m_128405_("WhitelistMode", this.whitelistMode.ordinal());
      ListTag attributes = new ListTag();
      this.selectedAttributes.forEach(at -> {
         if (at != null) {
            CompoundTag compoundNBT = ItemAttribute.saveStatic((ItemAttribute)at.getFirst());
            compoundNBT.m_128379_("Inverted", (Boolean)at.getSecond());
            attributes.add(compoundNBT);
         }
      });
      filterItem.m_41784_().m_128365_("MatchedAttributes", attributes);
      if (attributes.isEmpty() && this.whitelistMode == AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ) {
         filterItem.m_41751_(null);
      }
   }

   public enum WhitelistMode {
      WHITELIST_DISJ,
      WHITELIST_CONJ,
      BLACKLIST;
   }
}
