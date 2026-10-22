package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class FilterScreenPacket extends SimplePacketBase {
   private final FilterScreenPacket.Option option;
   private final CompoundTag data;

   public FilterScreenPacket(FilterScreenPacket.Option option) {
      this(option, new CompoundTag());
   }

   public FilterScreenPacket(FilterScreenPacket.Option option, CompoundTag data) {
      this.option = option;
      this.data = data;
   }

   public FilterScreenPacket(FriendlyByteBuf buffer) {
      this.option = FilterScreenPacket.Option.values()[buffer.readInt()];
      this.data = buffer.m_130260_();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.option.ordinal());
      buffer.m_130079_(this.data);
   }

   @Override
   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null) {
            if (player.f_36096_ instanceof FilterMenu c) {
               if (this.option == FilterScreenPacket.Option.WHITELIST) {
                  c.blacklist = false;
               }

               if (this.option == FilterScreenPacket.Option.BLACKLIST) {
                  c.blacklist = true;
               }

               if (this.option == FilterScreenPacket.Option.RESPECT_DATA) {
                  c.respectNBT = true;
               }

               if (this.option == FilterScreenPacket.Option.IGNORE_DATA) {
                  c.respectNBT = false;
               }

               if (this.option == FilterScreenPacket.Option.UPDATE_FILTER_ITEM) {
                  c.ghostInventory.setStackInSlot(this.data.m_128451_("Slot"), ItemStack.m_41712_(this.data.m_128469_("Item")));
               }
            }

            if (player.f_36096_ instanceof AttributeFilterMenu c) {
               if (this.option == FilterScreenPacket.Option.WHITELIST) {
                  c.whitelistMode = AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ;
               }

               if (this.option == FilterScreenPacket.Option.WHITELIST2) {
                  c.whitelistMode = AttributeFilterMenu.WhitelistMode.WHITELIST_CONJ;
               }

               if (this.option == FilterScreenPacket.Option.BLACKLIST) {
                  c.whitelistMode = AttributeFilterMenu.WhitelistMode.BLACKLIST;
               }

               if (this.option == FilterScreenPacket.Option.ADD_TAG) {
                  c.appendSelectedAttribute(ItemAttribute.loadStatic(this.data), false);
               }

               if (this.option == FilterScreenPacket.Option.ADD_INVERTED_TAG) {
                  c.appendSelectedAttribute(ItemAttribute.loadStatic(this.data), true);
               }
            }

            if (player.f_36096_ instanceof PackageFilterMenu c && this.option == FilterScreenPacket.Option.UPDATE_ADDRESS) {
               c.address = this.data.m_128461_("Address");
            }
         }
      });
      return true;
   }

   public enum Option {
      WHITELIST,
      WHITELIST2,
      BLACKLIST,
      RESPECT_DATA,
      IGNORE_DATA,
      UPDATE_FILTER_ITEM,
      ADD_TAG,
      ADD_INVERTED_TAG,
      UPDATE_ADDRESS;
   }
}
