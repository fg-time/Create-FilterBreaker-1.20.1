package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public abstract class FilterItem extends Item implements MenuProvider, SupportsItemCopying {
   public static ListFilterItem regular(Properties properties) {
      return new ListFilterItem(properties);
   }

   public static AttributeFilterItem attribute(Properties properties) {
      return new AttributeFilterItem(properties);
   }

   public static PackageFilterItem address(Properties properties) {
      return new PackageFilterItem(properties);
   }

   protected FilterItem(Properties properties) {
      super(properties);
   }

   @Nonnull
   public InteractionResult m_6225_(UseOnContext context) {
      return context.m_43723_() == null ? InteractionResult.PASS : this.m_7203_(context.m_43725_(), context.m_43723_(), context.m_43724_()).m_19089_();
   }

   @OnlyIn(Dist.CLIENT)
   public void m_7373_(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      if (!AllKeys.shiftDown()) {
         List<Component> makeSummary = this.makeSummary(stack);
         if (!makeSummary.isEmpty()) {
            tooltip.add(CommonComponents.f_263701_);
            tooltip.addAll(makeSummary);
         }
      }
   }

   public abstract List<Component> makeSummary(ItemStack var1);

   public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand) {
      ItemStack heldItem = player.m_21120_(hand);
      if (!player.m_6144_() && hand == InteractionHand.MAIN_HAND) {
         if (!world.f_46443_ && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this, buf -> buf.m_130055_(heldItem));
         }

         return InteractionResultHolder.m_19090_(heldItem);
      } else {
         return InteractionResultHolder.m_19098_(heldItem);
      }
   }

   public abstract AbstractContainerMenu m_7208_(int var1, Inventory var2, Player var3);

   public Component m_5446_() {
      return this.m_41466_();
   }

   public static boolean testDirect(ItemStack filter, ItemStack stack, boolean matchNBT) {
      if (matchNBT) {
         return PackageItem.isPackage(filter) && PackageItem.isPackage(stack)
            ? doPackagesHaveSameData(filter, stack)
            : ItemHandlerHelper.canItemStacksStack(filter, stack);
      } else {
         return PackageItem.isPackage(filter) && PackageItem.isPackage(stack) ? true : ItemHelper.sameItem(filter, stack);
      }
   }

   public static boolean doPackagesHaveSameData(@NotNull ItemStack a, @NotNull ItemStack b) {
      if (!a.m_41619_() && a.m_41782_() == b.m_41782_()) {
         if (!a.m_41782_()) {
            return true;
         }

         if (!a.areCapsCompatible(b)) {
            return false;
         }

         for (String key : a.m_41783_().m_128431_()) {
            if (!key.equals("Fragment") && !Objects.equals(a.m_41783_().m_128423_(key), b.m_41783_().m_128423_(key))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public abstract FilterItemStack makeStackWrapper(ItemStack var1);

   public abstract ItemStack[] getFilterItems(ItemStack var1);
}
