package com.simibubi.create.content.logistics.stockTicker;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.GuiGameElement.GuiRenderBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class StockKeeperCategoryScreen extends AbstractSimiContainerScreen<StockKeeperCategoryMenu> {
   private static final int CARD_HEADER = 20;
   private static final int CARD_WIDTH = 160;
   private List<Rect2i> extraAreas = Collections.emptyList();
   private final LerpedFloat scroll = LerpedFloat.linear().startWithValue(0.0);
   private final List<ItemStack> schedule;
   private IconButton confirmButton;
   private ItemStack editingItem;
   private int editingIndex;
   private IconButton editorConfirm;
   private EditBox editorEditBox;
   final int slices = 4;
   private final Component clickToEdit = CreateLang.translateDirect("gui.schedule.lmb_edit", new Object[0])
      .m_130944_(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC});

   public StockKeeperCategoryScreen(StockKeeperCategoryMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
      this.schedule = new ArrayList<>(((StockTickerBlockEntity)menu.contentHolder).categories);
      menu.slotsActive = false;
   }

   protected void m_7856_() {
      AllGuiTextures bg = AllGuiTextures.STOCK_KEEPER_CATEGORY;
      this.setWindowSize(
         bg.getWidth(), bg.getHeight() * 4 + AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight() + AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.getHeight()
      );
      super.m_7856_();
      this.m_169413_();
      this.confirmButton = new IconButton(this.f_97735_ + bg.getWidth() - 25, this.f_97736_ + this.f_97727_ - 25, AllIcons.I_CONFIRM);
      this.confirmButton.withCallback(() -> this.f_96541_.f_91074_.m_6915_());
      this.m_142416_(this.confirmButton);
      this.stopEditing();
      this.extraAreas = ImmutableList.of(new Rect2i(this.f_97735_ + bg.getWidth(), this.f_97736_ + this.f_97727_ - 40, 48, 40));
   }

   protected void startEditing(int index) {
      this.confirmButton.f_93624_ = false;
      this.editorConfirm = new IconButton(this.f_97735_ + 36 + 131, this.f_97736_ + 59, AllIcons.I_CONFIRM);
      ((StockKeeperCategoryMenu)this.f_97732_).slotsActive = true;
      this.editorEditBox = new EditBox(this.f_96547_, this.f_97735_ + 47, this.f_97736_ + 28, 124, 10, Component.m_237119_());
      this.editorEditBox.m_94202_(-1118482);
      this.editorEditBox.m_94182_(false);
      this.editorEditBox.m_93692_(false);
      this.editorEditBox.m_6375_(0.0, 0.0, 0);
      this.editorEditBox.m_94199_(28);
      this.editorEditBox
         .m_94144_(
            index != -1 && !this.schedule.get(index).m_41619_()
               ? this.schedule.get(index).m_41786_().getString()
               : CreateLang.translate("gui.stock_ticker.new_category", new Object[0]).string()
         );
      this.editingIndex = index;
      this.editingItem = index == -1 ? ItemStack.f_41583_ : this.schedule.get(index);
      ((StockKeeperCategoryMenu)this.f_97732_).proxyInventory.setStackInSlot(0, this.editingItem);
      AllPackets.getChannel().sendToServer(new GhostItemSubmitPacket(this.editingItem, 0));
      this.m_142416_(this.editorConfirm);
      this.m_142416_(this.editorEditBox);
   }

   protected void stopEditing() {
      this.confirmButton.f_93624_ = true;
      if (this.editingItem != null) {
         this.playUiSound((SoundEvent)SoundEvents.f_12490_.m_203334_(), 1.0F, 1.0F);
         this.m_169411_(this.editorConfirm);
         this.m_169411_(this.editorEditBox);
         ItemStack stackInSlot = ((StockKeeperCategoryMenu)this.f_97732_).proxyInventory.getStackInSlot(0).m_41777_();
         boolean empty = stackInSlot.m_41619_();
         if (empty && this.editingIndex != -1) {
            this.schedule.remove(this.editingIndex);
         }

         if (!empty) {
            String value = this.editorEditBox.m_94155_();
            stackInSlot.m_41714_(value.isBlank() ? null : Component.m_237113_(value));
            if (this.editingIndex == -1) {
               this.schedule.add(stackInSlot);
            } else {
               this.schedule.set(this.editingIndex, stackInSlot);
            }
         }

         AllPackets.getChannel().sendToServer(new GhostItemSubmitPacket(ItemStack.f_41583_, 0));
         this.editingItem = null;
         this.editorConfirm = null;
         this.editorEditBox = null;
         ((StockKeeperCategoryMenu)this.f_97732_).slotsActive = false;
         this.m_7856_();
      }
   }

   protected void m_181908_() {
      super.m_181908_();
      this.scroll.tickChaser();
      if (this.editorEditBox != null) {
         if (this.editorEditBox.m_94155_().equals(CreateLang.translate("gui.stock_ticker.new_category", new Object[0]).string())) {
            if (((StockKeeperCategoryMenu)this.f_97732_).proxyInventory.getStackInSlot(0).m_41788_()) {
               this.editorEditBox.m_94144_(((StockKeeperCategoryMenu)this.f_97732_).proxyInventory.getStackInSlot(0).m_41786_().getString());
            }
         }
      }
   }

   public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      partialTicks = this.f_96541_.m_91296_();
      if (((StockKeeperCategoryMenu)this.f_97732_).slotsActive) {
         super.m_88315_(graphics, mouseX, mouseY, partialTicks);
      } else {
         this.m_280273_(graphics);
         this.m_7286_(graphics, partialTicks, mouseX, mouseY);

         for (Renderable widget : this.f_169369_) {
            widget.m_88315_(graphics, mouseX, mouseY, partialTicks);
         }

         this.renderForeground(graphics, mouseX, mouseY, partialTicks);
      }
   }

   protected void renderCategories(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      PoseStack matrixStack = graphics.m_280168_();
      int yOffset = 25;
      List<ItemStack> entries = this.schedule;
      float scrollOffset = -this.scroll.getValue(partialTicks);
      graphics.m_280588_(this.f_97735_ + 3, this.f_97736_ + 16, this.f_97735_ + 187, this.f_97736_ + 19 + AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight() * 4);

      for (int i = 0; i <= entries.size(); i++) {
         matrixStack.m_85836_();
         matrixStack.m_252880_(0.0F, scrollOffset, 0.0F);
         if (i == entries.size()) {
            AllGuiTextures.STOCK_KEEPER_CATEGORY_NEW.render(graphics, this.f_97735_ + 7, this.f_97736_ + yOffset);
            matrixStack.m_85849_();
            break;
         }

         ItemStack scheduleEntry = entries.get(i);
         int cardY = yOffset;
         int cardHeight = this.renderScheduleEntry(graphics, i, scheduleEntry, cardY, mouseX, mouseY, partialTicks);
         yOffset += cardHeight;
         matrixStack.m_85849_();
      }

      graphics.m_280618_();
   }

   public int renderScheduleEntry(GuiGraphics graphics, int i, ItemStack entry, int yOffset, int mouseX, int mouseY, float partialTicks) {
      int cardWidth = 160;
      int cardHeader = 20;
      int cardHeight = cardHeader;
      PoseStack matrixStack = graphics.m_280168_();
      matrixStack.m_85836_();
      matrixStack.m_252880_(this.f_97735_ + 7, this.f_97736_ + yOffset, 0.0F);
      AllGuiTextures.STOCK_KEEPER_CATEGORY_ENTRY.render(graphics, 0, 0);
      if (i > 0) {
         AllGuiTextures.STOCK_KEEPER_CATEGORY_UP.render(graphics, cardWidth + 12, cardHeader - 18);
      }

      if (i < this.schedule.size() - 1) {
         AllGuiTextures.STOCK_KEEPER_CATEGORY_DOWN.render(graphics, cardWidth + 12, cardHeader - 9);
      }

      graphics.m_280480_(entry, 14, 1);
      graphics.m_280056_(
         this.f_96547_,
         entry.m_41619_()
            ? CreateLang.translate("gui.stock_ticker.empty_category_name_placeholder", new Object[0]).string()
            : entry.m_41786_().m_130668_(20).stripTrailing() + (entry.m_41786_().getString().length() > 20 ? "..." : ""),
         35,
         5,
         6645093,
         false
      );
      matrixStack.m_85849_();
      return cardHeight;
   }

   public boolean action(@Nullable GuiGraphics graphics, double mouseX, double mouseY, int click) {
      if (mouseX < this.f_97735_ || mouseX >= this.f_97735_ + this.f_97726_ || mouseY < this.f_97736_ + 15 || mouseY >= this.f_97736_ + 99) {
         return false;
      }

      if (this.editingItem != null) {
         return false;
      }

      int mx = (int)mouseX;
      int my = (int)mouseY;
      int x = mx - this.f_97735_ - 20;
      int y = my - this.f_97736_ - 24;
      if (x < 0 || x >= 196) {
         return false;
      }

      if (y >= 0 && y < 143) {
         y = (int)(y + this.scroll.getValue(0.0F));
         List<ItemStack> entries = this.schedule;

         for (int i = 0; i < entries.size(); i++) {
            ItemStack entry = entries.get(i);
            int cardHeight = 20;
            if (y >= cardHeight) {
               y -= cardHeight;
               if (y < 0) {
                  return false;
               }
            } else {
               int fieldSize = 140;
               if (x > 0 && x <= fieldSize && y > 0 && y <= 16) {
                  List<Component> components = new ArrayList<>();
                  components.add(
                     (Component)(entry.m_41619_()
                        ? CreateLang.translate("gui.stock_ticker.empty_category_name_placeholder", new Object[0]).component()
                        : entry.m_41786_())
                  );
                  components.add(this.clickToEdit);
                  this.renderActionTooltip(graphics, components, mx, my);
                  if (click == 0) {
                     this.startEditing(i);
                  }

                  return true;
               }

               if (x > fieldSize && x <= fieldSize + 16 && y > 0 && y <= 16) {
                  this.renderActionTooltip(
                     graphics, ImmutableList.of(CreateLang.translate("gui.stock_ticker.delete_category", new Object[0]).component()), mx, my
                  );
                  if (click == 0) {
                     if (!entry.m_41619_()) {
                        AllPackets.getChannel()
                           .sendToServer(
                              new StockKeeperCategoryRefundPacket(
                                 ((StockTickerBlockEntity)((StockKeeperCategoryMenu)this.f_97732_).contentHolder).m_58899_(), entry
                              )
                           );
                     }

                     entries.remove(entry);
                     this.m_7856_();
                  }

                  return true;
               }

               if (x > 158 && x < 170) {
                  if (y > 2 && y <= 10 && i > 0) {
                     this.renderActionTooltip(
                        graphics,
                        ImmutableList.of(
                           CreateLang.translateDirect("gui.schedule.move_up", new Object[0]),
                           CreateLang.translate("gui.stock_ticker.shift_moves_top", new Object[0])
                              .style(ChatFormatting.DARK_GRAY)
                              .style(ChatFormatting.ITALIC)
                              .component()
                        ),
                        mx,
                        my
                     );
                     if (click == 0) {
                        entries.remove(entry);
                        entries.add(m_96638_() ? 0 : i - 1, entry);
                        this.m_7856_();
                     }

                     return true;
                  }

                  if (y > 10 && y <= 22 && i < entries.size() - 1) {
                     this.renderActionTooltip(
                        graphics,
                        ImmutableList.of(
                           CreateLang.translateDirect("gui.schedule.move_down", new Object[0]),
                           CreateLang.translate("gui.stock_ticker.shift_moves_bottom", new Object[0])
                              .style(ChatFormatting.DARK_GRAY)
                              .style(ChatFormatting.ITALIC)
                              .component()
                        ),
                        mx,
                        my
                     );
                     if (click == 0) {
                        entries.remove(entry);
                        entries.add(m_96638_() ? entries.size() : i + 1, entry);
                        this.m_7856_();
                     }

                     return true;
                  }
               }

               x -= 18;
               y -= 28;
               if (x < 0 || y < 0 || x > 160) {
                  return false;
               }
            }
         }

         if (x > 0 && x <= 16 && y > 0 && y <= 16) {
            this.renderActionTooltip(graphics, ImmutableList.of(CreateLang.translate("gui.stock_ticker.new_category", new Object[0]).component()), mx, my);
            if (click == 0) {
               this.playUiSound((SoundEvent)SoundEvents.f_12490_.m_203334_(), 1.0F, 1.0F);
               this.startEditing(-1);
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void renderActionTooltip(@Nullable GuiGraphics graphics, List<Component> tooltip, int mx, int my) {
      if (graphics != null) {
         graphics.m_280677_(this.f_96547_, tooltip, Optional.empty(), mx, my);
      }
   }

   public boolean m_6375_(double pMouseX, double pMouseY, int pButton) {
      if (this.editorConfirm != null && this.editorConfirm.m_5953_(pMouseX, pMouseY)) {
         this.stopEditing();
         return true;
      }

      if (this.action(null, pMouseX, pMouseY, pButton)) {
         this.playUiSound((SoundEvent)SoundEvents.f_12490_.m_203334_(), 1.0F, 1.0F);
         return true;
      }

      boolean wasNotFocused = this.editorEditBox != null && !this.editorEditBox.m_93696_();
      boolean mouseClicked = super.m_6375_(pMouseX, pMouseY, pButton);
      if (this.editorEditBox != null && this.editorEditBox.m_5953_(pMouseX, pMouseY) && wasNotFocused) {
         this.editorEditBox.m_94201_();
         this.editorEditBox.m_94208_(0);
      }

      return mouseClicked;
   }

   public boolean m_7933_(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.editingItem == null) {
         return super.m_7933_(pKeyCode, pScanCode, pModifiers);
      }

      Key mouseKey = InputConstants.m_84827_(pKeyCode, pScanCode);
      boolean hitEscape = pKeyCode == 256;
      boolean hitEnter = this.m_7222_() instanceof EditBox && (pKeyCode == 257 || pKeyCode == 335);
      boolean hitE = this.m_7222_() == null && this.f_96541_.f_91066_.f_92092_.isActiveAndMatches(mouseKey);
      if (!hitE && !hitEnter && !hitEscape) {
         return super.m_7933_(pKeyCode, pScanCode, pModifiers);
      }

      this.stopEditing();
      return true;
   }

   public boolean m_6050_(double pMouseX, double pMouseY, double pDelta) {
      if (this.editingItem != null) {
         return super.m_6050_(pMouseX, pMouseY, pDelta);
      }

      float chaseTarget = this.scroll.getChaseTarget();
      float max = 40 - (3 + AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight() * 4);
      max += this.schedule.size() * 20 + 24;
      if (max > 0.0F) {
         chaseTarget = (float)(chaseTarget - pDelta * 12.0);
         chaseTarget = Mth.m_14036_(chaseTarget, 0.0F, max);
         this.scroll.chase((int)chaseTarget, 0.7F, Chaser.EXP);
      } else {
         this.scroll.chase(0.0, 0.7F, Chaser.EXP);
      }

      return super.m_6050_(pMouseX, pMouseY, pDelta);
   }

   protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      super.renderForeground(graphics, mouseX, mouseY, partialTicks);
      ((GuiRenderBuilder)GuiGameElement.of(AllBlocks.STOCK_TICKER.asStack())
            .at(this.f_97735_ + AllGuiTextures.STOCK_KEEPER_CATEGORY.getWidth() + 12, this.f_97736_ + this.f_97727_ - 39, -190.0F))
         .scale(3.0)
         .render(graphics);
      this.action(graphics, mouseX, mouseY, -1);
      if (this.editingItem != null) {
         if (this.f_97734_ instanceof SlotItemHandler && this.f_97734_.m_7993_().m_41619_()) {
            graphics.m_280666_(
               this.f_96547_,
               List.of(
                  CreateLang.translate("gui.stock_ticker.category_filter", new Object[0]).color(ScrollInput.HEADER_RGB).component(),
                  CreateLang.translate("gui.stock_ticker.category_filter_tip", new Object[0]).style(ChatFormatting.GRAY).component(),
                  CreateLang.translate("gui.stock_ticker.category_filter_tip_1", new Object[0]).style(ChatFormatting.GRAY).component()
               ),
               mouseX,
               mouseY
            );
         }

         if (this.editorEditBox != null && this.editorEditBox.m_274382_() && !this.editorEditBox.m_93696_()) {
            graphics.m_280666_(
               this.f_96547_,
               List.of(CreateLang.translate("gui.stock_ticker.category_name", new Object[0]).color(ScrollInput.HEADER_RGB).component(), this.clickToEdit),
               mouseX,
               mouseY
            );
         }
      }
   }

   protected void m_7286_(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
      int y = this.f_97736_;
      AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, this.f_97735_, y);
      y += AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();

      for (int i = 0; i < 4; i++) {
         AllGuiTextures.STOCK_KEEPER_CATEGORY.render(graphics, this.f_97735_, y);
         y += AllGuiTextures.STOCK_KEEPER_CATEGORY.getHeight();
      }

      AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, this.f_97735_, y);
      AllGuiTextures.STOCK_KEEPER_CATEGORY_SAYS.render(graphics, this.f_97735_ + this.f_97726_ - 6, y + 7);
      FormattedCharSequence formattedcharsequence = ((StockTickerBlockEntity)((StockKeeperCategoryMenu)this.f_97732_).contentHolder)
         .m_58900_()
         .m_60734_()
         .m_49954_()
         .m_7532_();
      int center = this.f_97735_ + AllGuiTextures.STOCK_KEEPER_CATEGORY.getWidth() / 2;
      graphics.drawString(
         this.f_96547_, formattedcharsequence, center - this.f_96547_.m_92724_(formattedcharsequence) / 2, this.f_97736_ + 4.0F, 4013128, false
      );
      if (this.editingItem == null) {
         this.renderCategories(graphics, pMouseX, pMouseY, pPartialTick);
      } else {
         graphics.m_280024_(0, 0, this.f_96543_, this.f_96544_, -1072689136, -804253680);
         y = this.f_97736_ - 5;
         AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.render(graphics, this.f_97735_, y);
         y += AllGuiTextures.STOCK_KEEPER_CATEGORY_HEADER.getHeight();
         AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.render(graphics, this.f_97735_, y);
         y += AllGuiTextures.STOCK_KEEPER_CATEGORY_EDIT.getHeight();
         AllGuiTextures.STOCK_KEEPER_CATEGORY_FOOTER.render(graphics, this.f_97735_, y);
         this.renderPlayerInventory(graphics, this.f_97735_ + 10, this.f_97736_ + 88);
         formattedcharsequence = CreateLang.translate("gui.stock_ticker.category_editor", new Object[0]).component().m_7532_();
         graphics.drawString(
            this.f_96547_, formattedcharsequence, center - this.f_96547_.m_92724_(formattedcharsequence) / 2, this.f_97736_ - 1.0F, 4013128, false
         );
      }
   }

   public void m_7861_() {
      super.m_7861_();
      AllPackets.getChannel()
         .sendToServer(
            new StockKeeperCategoryEditPacket(((StockTickerBlockEntity)((StockKeeperCategoryMenu)this.f_97732_).contentHolder).m_58899_(), this.schedule)
         );
   }

   protected List<Component> m_280553_(ItemStack pStack) {
      List<Component> tooltip = super.m_280553_(pStack);
      if (!(this.f_97734_ instanceof SlotItemHandler)) {
         return tooltip;
      }

      if (!tooltip.isEmpty()) {
         tooltip.set(0, CreateLang.translate("gui.stock_ticker.category_filter", new Object[0]).color(ScrollInput.HEADER_RGB).component());
      }

      return tooltip;
   }

   public List<Rect2i> getExtraAreas() {
      return this.extraAreas;
   }

   public Font getFont() {
      return this.f_96547_;
   }
}
