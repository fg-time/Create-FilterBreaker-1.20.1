package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.data.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

public class FilterItemStack {
   private final ItemStack filterItemStack;
   private boolean fluidExtracted;
   private FluidStack filterFluidStack;

   public static FilterItemStack of(ItemStack filter) {
      if (filter.m_41782_() && filter.m_41720_() instanceof FilterItem item) {
         trimFilterTag(filter);
         return item.makeStackWrapper(filter);
      } else {
         return new FilterItemStack(filter);
      }
   }

   public static FilterItemStack of(CompoundTag tag) {
      return of(ItemStack.m_41712_(tag));
   }

   public static FilterItemStack empty() {
      return of(ItemStack.f_41583_);
   }

   private static void trimFilterTag(ItemStack filter) {
      CompoundTag stackTag = filter.m_41783_();
      stackTag.m_128473_("Enchantments");
      stackTag.m_128473_("AttributeModifiers");
   }

   public boolean isEmpty() {
      return this.filterItemStack.m_41619_();
   }

   public CompoundTag serializeNBT() {
      return this.filterItemStack.serializeNBT();
   }

   public ItemStack item() {
      return this.filterItemStack;
   }

   public FluidStack fluid(Level level) {
      this.resolveFluid(level);
      return this.filterFluidStack;
   }

   public boolean isFilterItem() {
      return this.filterItemStack.m_41720_() instanceof FilterItem;
   }

   public boolean test(Level world, ItemStack stack) {
      return this.test(world, stack, false);
   }

   public boolean test(Level world, FluidStack stack) {
      return this.test(world, stack, true);
   }

   public boolean test(Level world, ItemStack stack, boolean matchNBT) {
      return this.isEmpty() ? true : FilterItem.testDirect(this.filterItemStack, stack, matchNBT);
   }

   public boolean test(Level world, FluidStack stack, boolean matchNBT) {
      if (this.isEmpty()) {
         return true;
      } else if (stack.isEmpty()) {
         return false;
      } else {
         this.resolveFluid(world);
         if (this.filterFluidStack.isEmpty()) {
            return false;
         } else {
            return !matchNBT ? this.filterFluidStack.getFluid().m_6212_(stack.getFluid()) : this.filterFluidStack.isFluidEqual(stack);
         }
      }
   }

   private void resolveFluid(Level world) {
      if (!this.fluidExtracted) {
         this.fluidExtracted = true;
         if (GenericItemEmptying.canItemBeEmptied(world, this.filterItemStack)) {
            this.filterFluidStack = (FluidStack)GenericItemEmptying.emptyItem(world, this.filterItemStack, true).getFirst();
         }
      }
   }

   protected FilterItemStack(ItemStack filter) {
      this.filterItemStack = filter;
      this.filterFluidStack = FluidStack.EMPTY;
      this.fluidExtracted = false;
   }

   public static class AttributeFilterItemStack extends FilterItemStack {
      public FilterItemStack.AttributeFilterItemStack.WhitelistMode whitelistMode;
      public List<Pair<ItemAttribute, Boolean>> attributeTests;

      public AttributeFilterItemStack(ItemStack filter) {
         super(filter);
         boolean defaults = !filter.m_41782_();
         this.attributeTests = new ArrayList<>();
         this.whitelistMode = FilterItemStack.AttributeFilterItemStack.WhitelistMode.values()[defaults ? 0 : filter.m_41783_().m_128451_("WhitelistMode")];

         for (Tag inbt : defaults ? new ListTag() : filter.m_41783_().m_128437_("MatchedAttributes", 10)) {
            CompoundTag compound = (CompoundTag)inbt;
            ItemAttribute attribute = ItemAttribute.loadStatic(compound);
            if (attribute != null) {
               this.attributeTests.add(Pair.of(attribute, compound.m_128471_("Inverted")));
            }
         }
      }

      @Override
      public boolean test(Level world, FluidStack stack, boolean matchNBT) {
         return false;
      }

      @Override
      public boolean test(Level world, ItemStack stack, boolean matchNBT) {
         if (this.attributeTests.isEmpty()) {
            return super.test(world, stack, matchNBT);
         }

         for (Pair<ItemAttribute, Boolean> test : this.attributeTests) {
            ItemAttribute attribute = (ItemAttribute)test.getFirst();
            boolean inverted = (Boolean)test.getSecond();
            boolean matches = attribute.appliesTo(stack, world) != inverted;
            if (matches) {
               switch (this.whitelistMode) {
                  case BLACKLIST:
                     return false;
                  case WHITELIST_CONJ:
                  default:
                     break;
                  case WHITELIST_DISJ:
                     return true;
               }
            } else {
               switch (this.whitelistMode) {
                  case BLACKLIST:
                  case WHITELIST_DISJ:
                  default:
                     break;
                  case WHITELIST_CONJ:
                     return false;
               }
            }
         }
         return switch (this.whitelistMode) {
            case BLACKLIST, WHITELIST_CONJ -> true;
            case WHITELIST_DISJ -> false;
         };
      }

      public enum WhitelistMode {
         WHITELIST_DISJ,
         WHITELIST_CONJ,
         BLACKLIST;
      }
   }

   public static class ListFilterItemStack extends FilterItemStack {
      public List<FilterItemStack> containedItems;
      public boolean shouldRespectNBT;
      public boolean isBlacklist;

      public ListFilterItemStack(ItemStack filter) {
         super(filter);
         boolean defaults = !filter.m_41782_();
         this.containedItems = new ArrayList<>();
         ItemStackHandler items = ((ListFilterItem)filter.m_41720_()).getFilterItemHandler(filter);

         for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stackInSlot = items.getStackInSlot(i);
            if (!stackInSlot.m_41619_()) {
               this.containedItems.add(FilterItemStack.of(stackInSlot));
            }
         }

         this.shouldRespectNBT = defaults ? false : filter.m_41783_().m_128471_("RespectNBT");
         this.isBlacklist = defaults ? false : filter.m_41783_().m_128471_("Blacklist");
      }

      @Override
      public boolean test(Level world, ItemStack stack, boolean matchNBT) {
         for (FilterItemStack filterItemStack : this.containedItems) {
            if (filterItemStack.test(world, stack, this.shouldRespectNBT)) {
               return !this.isBlacklist;
            }
         }

         return this.isBlacklist;
      }

      @Override
      public boolean test(Level world, FluidStack stack, boolean matchNBT) {
         for (FilterItemStack filterItemStack : this.containedItems) {
            if (filterItemStack.test(world, stack, this.shouldRespectNBT)) {
               return !this.isBlacklist;
            }
         }

         return this.isBlacklist;
      }
   }

   public static class PackageFilterItemStack extends FilterItemStack {
      public String filterString;

      public PackageFilterItemStack(ItemStack filter) {
         super(filter);
         this.filterString = filter.m_41784_().m_128461_("Address");
      }

      @Override
      public boolean test(Level world, ItemStack stack, boolean matchNBT) {
         return this.filterString.isBlank() && super.test(world, stack, matchNBT)
            || PackageItem.isPackage(stack) && PackageItem.matchAddress(stack, this.filterString);
      }

      @Override
      public boolean test(Level world, FluidStack stack, boolean matchNBT) {
         return false;
      }
   }
}
