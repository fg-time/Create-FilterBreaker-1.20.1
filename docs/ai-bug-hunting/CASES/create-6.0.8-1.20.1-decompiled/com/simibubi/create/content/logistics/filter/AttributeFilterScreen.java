package com.simibubi.create.content.logistics.filter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPackets;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AttributeFilterScreen extends AbstractFilterScreen<AttributeFilterMenu> {
   private static final String PREFIX = "gui.attribute_filter.";
   private Component addDESC = CreateLang.translateDirect("gui.attribute_filter.add_attribute", new Object[0]);
   private Component addInvertedDESC = CreateLang.translateDirect("gui.attribute_filter.add_inverted_attribute", new Object[0]);
   private Component allowDisN = CreateLang.translateDirect("gui.attribute_filter.allow_list_disjunctive", new Object[0]);
   private Component allowDisDESC = CreateLang.translateDirect("gui.attribute_filter.allow_list_disjunctive.description", new Object[0]);
   private Component allowConN = CreateLang.translateDirect("gui.attribute_filter.allow_list_conjunctive", new Object[0]);
   private Component allowConDESC = CreateLang.translateDirect("gui.attribute_filter.allow_list_conjunctive.description", new Object[0]);
   private Component denyN = CreateLang.translateDirect("gui.attribute_filter.deny_list", new Object[0]);
   private Component denyDESC = CreateLang.translateDirect("gui.attribute_filter.deny_list.description", new Object[0]);
   private Component referenceH = CreateLang.translateDirect("gui.attribute_filter.add_reference_item", new Object[0]);
   private Component noSelectedT = CreateLang.translateDirect("gui.attribute_filter.no_selected_attributes", new Object[0]);
   private Component selectedT = CreateLang.translateDirect("gui.attribute_filter.selected_attributes", new Object[0]);
   private IconButton whitelistDis;
   private IconButton whitelistCon;
   private IconButton blacklist;
   private IconButton add;
   private IconButton addInverted;
   private ItemStack lastItemScanned = ItemStack.f_41583_;
   private List<ItemAttribute> attributesOfItem = new ArrayList<>();
   private List<Component> selectedAttributes = new ArrayList<>();
   private SelectionScrollInput attributeSelector;
   private Label attributeSelectorLabel;

   public AttributeFilterScreen(AttributeFilterMenu menu, Inventory inv, Component title) {
      super(menu, inv, title, AllGuiTextures.ATTRIBUTE_FILTER);
   }

   @Override
   protected void m_7856_() {
      this.setWindowOffset(-11, 7);
      super.m_7856_();
      int x = this.f_97735_;
      int y = this.f_97736_;
      this.whitelistDis = new IconButton(x + 38, y + 61, AllIcons.I_WHITELIST_OR);
      this.whitelistDis.withCallback(() -> {
         ((AttributeFilterMenu)this.f_97732_).whitelistMode = AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ;
         this.sendOptionUpdate(FilterScreenPacket.Option.WHITELIST);
      });
      this.whitelistDis.setToolTip(this.allowDisN);
      this.whitelistCon = new IconButton(x + 56, y + 61, AllIcons.I_WHITELIST_AND);
      this.whitelistCon.withCallback(() -> {
         ((AttributeFilterMenu)this.f_97732_).whitelistMode = AttributeFilterMenu.WhitelistMode.WHITELIST_CONJ;
         this.sendOptionUpdate(FilterScreenPacket.Option.WHITELIST2);
      });
      this.whitelistCon.setToolTip(this.allowConN);
      this.blacklist = new IconButton(x + 74, y + 61, AllIcons.I_WHITELIST_NOT);
      this.blacklist.withCallback(() -> {
         ((AttributeFilterMenu)this.f_97732_).whitelistMode = AttributeFilterMenu.WhitelistMode.BLACKLIST;
         this.sendOptionUpdate(FilterScreenPacket.Option.BLACKLIST);
      });
      this.blacklist.setToolTip(this.denyN);
      this.addRenderableWidgets(new IconButton[]{this.blacklist, this.whitelistCon, this.whitelistDis});
      this.m_142416_(this.add = new IconButton(x + 182, y + 26, AllIcons.I_ADD));
      this.m_142416_(this.addInverted = new IconButton(x + 200, y + 26, AllIcons.I_ADD_INVERTED_ATTRIBUTE));
      this.add.withCallback(() -> this.handleAddedAttibute(false));
      this.add.setToolTip(this.addDESC);
      this.addInverted.withCallback(() -> this.handleAddedAttibute(true));
      this.addInverted.setToolTip(this.addInvertedDESC);
      this.handleIndicators();
      this.attributeSelectorLabel = new Label(x + 43, y + 31, CommonComponents.f_237098_).colored(15985630).withShadow();
      this.attributeSelector = new SelectionScrollInput(x + 39, y + 26, 137, 18);
      this.attributeSelector.forOptions(Arrays.asList(CommonComponents.f_237098_));
      this.attributeSelector.removeCallback();
      this.referenceItemChanged(((AttributeFilterMenu)this.f_97732_).ghostInventory.getStackInSlot(0));
      this.m_142416_(this.attributeSelector);
      this.m_142416_(this.attributeSelectorLabel);
      this.selectedAttributes.clear();
      this.selectedAttributes
         .add(
            (((AttributeFilterMenu)this.f_97732_).selectedAttributes.isEmpty() ? this.noSelectedT : this.selectedT).m_6879_().m_130940_(ChatFormatting.YELLOW)
         );
      ((AttributeFilterMenu)this.f_97732_)
         .selectedAttributes
         .forEach(
            at -> this.selectedAttributes
               .add(Component.m_237113_("- ").m_7220_(((ItemAttribute)at.getFirst()).format((Boolean)at.getSecond())).m_130940_(ChatFormatting.GRAY))
         );
   }

   private void referenceItemChanged(ItemStack stack) {
      this.lastItemScanned = stack;
      if (stack.m_41619_()) {
         this.attributeSelector.f_93623_ = false;
         this.attributeSelector.f_93624_ = false;
         this.attributeSelectorLabel.text = this.referenceH.m_6879_().m_130940_(ChatFormatting.ITALIC);
         this.add.f_93623_ = false;
         this.addInverted.f_93623_ = false;
         this.attributeSelector.calling(s -> {});
      } else {
         this.add.f_93623_ = true;
         this.addInverted.f_93623_ = true;
         this.attributeSelector.titled(stack.m_41786_().m_6879_().m_130946_("..."));
         this.attributesOfItem.clear();

         for (ItemAttributeType type : CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE) {
            this.attributesOfItem.addAll(type.getAllAttributes(stack, this.f_96541_.f_91073_));
         }

         List<Component> options = this.attributesOfItem.stream().map(a -> a.format(false)).collect(Collectors.toList());
         this.attributeSelector.forOptions(options);
         this.attributeSelector.f_93623_ = true;
         this.attributeSelector.f_93624_ = true;
         this.attributeSelector.setState(0);
         this.attributeSelector.calling(i -> {
            this.attributeSelectorLabel.setTextAndTrim(options.get(i), true, 112);
            ItemAttribute selected = this.attributesOfItem.get(i);

            for (Pair<ItemAttribute, Boolean> existing : ((AttributeFilterMenu)this.f_97732_).selectedAttributes) {
               CompoundTag testTag = ItemAttribute.saveStatic((ItemAttribute)existing.getFirst());
               CompoundTag testTag2 = ItemAttribute.saveStatic(selected);
               if (testTag.equals(testTag2)) {
                  this.add.f_93623_ = false;
                  this.addInverted.f_93623_ = false;
                  return;
               }
            }

            this.add.f_93623_ = true;
            this.addInverted.f_93623_ = true;
         });
         this.attributeSelector.onChanged();
      }
   }

   public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      ItemStack stack = ((AttributeFilterMenu)this.f_97732_).ghostInventory.getStackInSlot(1);
      PoseStack matrixStack = graphics.m_280168_();
      matrixStack.m_85836_();
      matrixStack.m_252880_(0.0F, 0.0F, 150.0F);
      graphics.m_280302_(this.f_96547_, stack, this.f_97735_ + 16, this.f_97736_ + 62, String.valueOf(this.selectedAttributes.size() - 1));
      matrixStack.m_85849_();
      super.renderForeground(graphics, mouseX, mouseY, partialTicks);
   }

   @Override
   protected void m_181908_() {
      super.m_181908_();
      ItemStack stackInSlot = ((AttributeFilterMenu)this.f_97732_).ghostInventory.getStackInSlot(0);
      if (!stackInSlot.equals(this.lastItemScanned, false)) {
         this.referenceItemChanged(stackInSlot);
      }
   }

   protected void m_280072_(GuiGraphics graphics, int mouseX, int mouseY) {
      if (((AttributeFilterMenu)this.f_97732_).m_142621_().m_41619_() && this.f_97734_ != null && this.f_97734_.m_6657_()) {
         if (this.f_97734_.f_40219_ == 37) {
            graphics.m_280666_(this.f_96547_, this.selectedAttributes, mouseX, mouseY);
            return;
         }

         graphics.m_280153_(this.f_96547_, this.f_97734_.m_7993_(), mouseX, mouseY);
      }

      super.m_280072_(graphics, mouseX, mouseY);
   }

   @Override
   protected List<IconButton> getTooltipButtons() {
      return Arrays.asList(this.blacklist, this.whitelistCon, this.whitelistDis);
   }

   @Override
   protected List<MutableComponent> getTooltipDescriptions() {
      return Arrays.asList(this.denyDESC.m_6879_(), this.allowConDESC.m_6879_(), this.allowDisDESC.m_6879_());
   }

   protected boolean handleAddedAttibute(boolean inverted) {
      int index = this.attributeSelector.getState();
      if (index >= this.attributesOfItem.size()) {
         return false;
      }

      this.add.f_93623_ = false;
      this.addInverted.f_93623_ = false;
      ItemAttribute itemAttribute = this.attributesOfItem.get(index);
      CompoundTag tag = ItemAttribute.saveStatic(itemAttribute);
      AllPackets.getChannel()
         .sendToServer(new FilterScreenPacket(inverted ? FilterScreenPacket.Option.ADD_INVERTED_TAG : FilterScreenPacket.Option.ADD_TAG, tag));
      ((AttributeFilterMenu)this.f_97732_).appendSelectedAttribute(itemAttribute, inverted);
      if (((AttributeFilterMenu)this.f_97732_).selectedAttributes.size() == 1) {
         this.selectedAttributes.set(0, this.selectedT.m_6879_().m_130940_(ChatFormatting.YELLOW));
      }

      this.selectedAttributes.add(Component.m_237113_("- ").m_7220_(itemAttribute.format(inverted)).m_130940_(ChatFormatting.GRAY));
      return true;
   }

   @Override
   protected void contentsCleared() {
      this.selectedAttributes.clear();
      this.selectedAttributes.add(this.noSelectedT.m_6879_().m_130940_(ChatFormatting.YELLOW));
      if (!this.lastItemScanned.m_41619_()) {
         this.add.f_93623_ = true;
         this.addInverted.f_93623_ = true;
      }
   }

   @Override
   protected boolean isButtonEnabled(IconButton button) {
      if (button == this.blacklist) {
         return ((AttributeFilterMenu)this.f_97732_).whitelistMode != AttributeFilterMenu.WhitelistMode.BLACKLIST;
      } else if (button == this.whitelistCon) {
         return ((AttributeFilterMenu)this.f_97732_).whitelistMode != AttributeFilterMenu.WhitelistMode.WHITELIST_CONJ;
      } else {
         return button == this.whitelistDis ? ((AttributeFilterMenu)this.f_97732_).whitelistMode != AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ : true;
      }
   }
}
