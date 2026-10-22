package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.packagerLink.WiFiParticle.Data;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StockTickerBlockEntity extends StockCheckingBlockEntity implements IHaveHoveringInformation {
   public AbstractComputerBehaviour computerBehaviour;
   protected List<List<BigItemStack>> lastClientsideStockSnapshot;
   protected InventorySummary lastClientsideStockSnapshotAsSummary;
   protected List<BigItemStack> newlyReceivedStockSnapshot;
   protected String previouslyUsedAddress = "";
   protected int activeLinks;
   protected int ticksSinceLastUpdate;
   protected List<ItemStack> categories;
   protected Map<UUID, List<Integer>> hiddenCategoriesByPlayer;
   protected SmartInventory receivedPayments = new SmartInventory(27, this, 64, false);
   protected LazyOptional<IItemHandler> capability = LazyOptional.of(() -> this.receivedPayments);

   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      super.addBehaviours(behaviours);
      behaviours.add(this.computerBehaviour = ComputerCraftProxy.behaviour(this));
   }

   public StockTickerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.categories = new ArrayList<>();
      this.hiddenCategoriesByPlayer = new HashMap<>();
   }

   public void refreshClientStockSnapshot() {
      this.ticksSinceLastUpdate = 0;
      AllPackets.getChannel().sendToServer(new LogisticalStockRequestPacket(this.f_58858_));
   }

   public IItemHandler getReceivedPaymentsHandler() {
      return this.receivedPayments;
   }

   public List<List<BigItemStack>> getClientStockSnapshot() {
      return this.lastClientsideStockSnapshot;
   }

   public InventorySummary getLastClientsideStockSnapshotAsSummary() {
      return this.lastClientsideStockSnapshotAsSummary;
   }

   public int getTicksSinceLastUpdate() {
      return this.ticksSinceLastUpdate;
   }

   public boolean broadcastPackageRequest(RequestType type, PackageOrderWithCrafts order, IdentifiedInventory ignoredHandler, String address) {
      boolean result = super.broadcastPackageRequest(type, order, ignoredHandler, address);
      this.previouslyUsedAddress = address;
      this.notifyUpdate();
      return result;
   }

   public InventorySummary getRecentSummary() {
      InventorySummary recentSummary = super.getRecentSummary();
      int contributingLinks = recentSummary.contributingLinks;
      if (this.activeLinks != contributingLinks && !this.m_58901_()) {
         this.activeLinks = contributingLinks;
         this.sendData();
      }

      return recentSummary;
   }

   public void tick() {
      super.tick();
      if (this.f_58857_.m_5776_()) {
         if (this.ticksSinceLastUpdate < 100) {
            this.ticksSinceLastUpdate++;
         }
      }
   }

   protected void write(CompoundTag tag, boolean clientPacket) {
      super.write(tag, clientPacket);
      tag.m_128359_("PreviousAddress", this.previouslyUsedAddress);
      tag.m_128365_("ReceivedPayments", this.receivedPayments.serializeNBT());
      tag.m_128365_("Categories", NBTHelper.writeItemList(this.categories));
      tag.m_128365_("HiddenCategories", NBTHelper.writeCompoundList(this.hiddenCategoriesByPlayer.entrySet(), e -> {
         CompoundTag c = new CompoundTag();
         c.m_128362_("Id", (UUID)e.getKey());
         c.m_128408_("Indices", (List)e.getValue());
         return c;
      }));
      if (clientPacket) {
         tag.m_128405_("ActiveLinks", this.activeLinks);
      }
   }

   protected void read(CompoundTag tag, boolean clientPacket) {
      super.read(tag, clientPacket);
      this.previouslyUsedAddress = tag.m_128461_("PreviousAddress");
      this.receivedPayments.deserializeNBT(tag.m_128469_("ReceivedPayments"));
      this.categories = NBTHelper.readItemList(tag.m_128437_("Categories", 10));
      this.categories.removeIf(stack -> !stack.m_41619_() && !(stack.m_41720_() instanceof FilterItem));
      this.hiddenCategoriesByPlayer.clear();
      NBTHelper.iterateCompoundList(
         tag.m_128437_("HiddenCategories", 10),
         c -> this.hiddenCategoriesByPlayer.put(c.m_128342_("Id"), IntStream.of(c.m_128465_("Indices")).boxed().toList())
      );
      if (clientPacket) {
         this.activeLinks = tag.m_128451_("ActiveLinks");
      }
   }

   public void receiveStockPacket(List<BigItemStack> stacks, boolean endOfTransmission) {
      if (this.newlyReceivedStockSnapshot == null) {
         this.newlyReceivedStockSnapshot = new ArrayList<>();
      }

      this.newlyReceivedStockSnapshot.addAll(stacks);
      if (endOfTransmission) {
         this.lastClientsideStockSnapshotAsSummary = new InventorySummary();
         this.lastClientsideStockSnapshot = new ArrayList<>();

         for (BigItemStack bigStack : this.newlyReceivedStockSnapshot) {
            this.lastClientsideStockSnapshotAsSummary.add(bigStack);
         }

         for (ItemStack filter : this.categories) {
            List<BigItemStack> inCategory = new ArrayList<>();
            if (!filter.m_41619_()) {
               FilterItemStack filterItemStack = FilterItemStack.of(filter);
               Iterator<BigItemStack> iterator = this.newlyReceivedStockSnapshot.iterator();

               while (iterator.hasNext()) {
                  BigItemStack bigStack = iterator.next();
                  if (filterItemStack.test(this.f_58857_, bigStack.stack)) {
                     inCategory.add(bigStack);
                     iterator.remove();
                  }
               }
            }

            this.lastClientsideStockSnapshot.add(inCategory);
         }

         List<BigItemStack> unsorted = new ArrayList<>(this.newlyReceivedStockSnapshot);
         this.lastClientsideStockSnapshot.add(unsorted);
         this.newlyReceivedStockSnapshot = null;
      }
   }

   public boolean isKeeperPresent() {
      for (int yOffset : Iterate.zeroAndOne) {
         for (Direction side : Iterate.horizontalDirections) {
            BlockPos seatPos = this.f_58858_.m_6625_(yOffset).m_121945_(side);

            for (SeatEntity seatEntity : this.f_58857_.m_45976_(SeatEntity.class, new AABB(seatPos))) {
               if (seatEntity.m_20160_()) {
                  return true;
               }
            }

            if (yOffset == 0 && AllBlockEntityTypes.HEATER.is(this.f_58857_.m_7702_(seatPos))) {
               return true;
            }
         }
      }

      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
      if (this.receivedPayments.m_7983_()) {
         return false;
      }

      if (!this.behaviour.mayAdministrate(Minecraft.m_91087_().f_91074_)) {
         return false;
      }

      CreateLang.translate("stock_ticker.contains_payments", new Object[0]).style(ChatFormatting.WHITE).forGoggles(tooltip);
      InventorySummary summary = new InventorySummary();

      for (int i = 0; i < this.receivedPayments.getSlots(); i++) {
         summary.add(this.receivedPayments.getStackInSlot(i));
      }

      for (BigItemStack entry : summary.getStacksByCount()) {
         CreateLang.builder()
            .text(Component.m_237115_(entry.stack.m_41778_()).getString() + " x" + entry.count)
            .style(ChatFormatting.GREEN)
            .forGoggles(tooltip);
      }

      CreateLang.translate("stock_ticker.click_to_retrieve", new Object[0]).style(ChatFormatting.GRAY).forGoggles(tooltip);
      return true;
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      if (this.isItemHandlerCap(cap)) {
         return this.capability.cast();
      } else {
         return this.computerBehaviour.isPeripheralCap(cap) ? this.computerBehaviour.getPeripheralCapability() : super.getCapability(cap, side);
      }
   }

   public void destroy() {
      ItemHelper.dropContents(this.f_58857_, this.f_58858_, this.receivedPayments);

      for (ItemStack filter : this.categories) {
         if (!filter.m_41619_() && filter.m_41720_() instanceof FilterItem) {
            Containers.m_18992_(this.f_58857_, this.f_58858_.m_123341_(), this.f_58858_.m_123342_(), this.f_58858_.m_123343_(), filter);
         }
      }

      super.destroy();
   }

   public void invalidate() {
      this.capability.invalidate();
      super.invalidate();
   }

   public void playEffect() {
      AllSoundEvents.STOCK_LINK.playAt(this.f_58857_, this.f_58858_, 1.0F, 1.0F, false);
      Vec3 vec3 = Vec3.m_82512_(this.f_58858_);
      this.f_58857_.m_7106_(new Data(), vec3.f_82479_, vec3.f_82480_, vec3.f_82481_, 1.0, 1.0, 1.0);
   }
}
