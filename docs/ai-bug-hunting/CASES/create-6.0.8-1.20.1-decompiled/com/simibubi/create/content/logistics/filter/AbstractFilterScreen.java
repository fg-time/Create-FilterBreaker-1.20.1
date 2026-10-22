package com.simibubi.create.content.logistics.filter;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.item.TooltipHelper;
import java.util.Collections;
import java.util.List;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.GuiGameElement.GuiRenderBuilder;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractFilterScreen<F extends AbstractFilterMenu> extends AbstractSimiContainerScreen<F> {
   protected AllGuiTextures background;
   private List<Rect2i> extraAreas = Collections.emptyList();
   private IconButton resetButton;
   private IconButton confirmButton;

   protected AbstractFilterScreen(F menu, Inventory inv, Component title, AllGuiTextures background) {
      super(menu, inv, title);
      this.background = background;
   }

   protected void m_7856_() {
      this.setWindowSize(
         Math.max(this.background.getWidth(), AllGuiTextures.PLAYER_INVENTORY.getWidth()),
         this.background.getHeight() + 4 + AllGuiTextures.PLAYER_INVENTORY.getHeight()
      );
      super.m_7856_();
      int x = this.f_97735_;
      int y = this.f_97736_;
      this.resetButton = new IconButton(x + this.background.getWidth() - 62, y + this.background.getHeight() - 24, AllIcons.I_TRASH);
      this.resetButton.withCallback(() -> {
         ((AbstractFilterMenu)this.f_97732_).clearContents();
         this.contentsCleared();
         ((AbstractFilterMenu)this.f_97732_).sendClearPacket();
      });
      this.confirmButton = new IconButton(x + this.background.getWidth() - 33, y + this.background.getHeight() - 24, AllIcons.I_CONFIRM);
      this.confirmButton.withCallback(() -> this.f_96541_.f_91074_.m_6915_());
      this.m_142416_(this.resetButton);
      this.m_142416_(this.confirmButton);
      this.extraAreas = ImmutableList.of(new Rect2i(x + this.background.getWidth(), y + this.background.getHeight() - 40, 80, 48));
   }

   protected void m_7286_(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      int invX = this.getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.getWidth());
      int invY = this.f_97736_ + this.background.getHeight() + 4;
      this.renderPlayerInventory(graphics, invX, invY);
      int x = this.f_97735_;
      int y = this.f_97736_;
      this.background.render(graphics, x, y);
      graphics.m_280614_(
         this.f_96547_, this.f_96539_, x + (this.background.getWidth() - 8) / 2 - this.f_96547_.m_92852_(this.f_96539_) / 2, y + 4, this.getTitleColor(), false
      );
      ((GuiRenderBuilder)GuiGameElement.of((ItemStack)((AbstractFilterMenu)this.f_97732_).contentHolder)
            .at(x + this.background.getWidth() + 8, y + this.background.getHeight() - 52, -200.0F))
         .scale(4.0)
         .render(graphics);
   }

   protected int getTitleColor() {
      return 5841956;
   }

   protected void m_181908_() {
      if (!((AbstractFilterMenu)this.f_97732_).player.m_21205_().equals((ItemStack)((AbstractFilterMenu)this.f_97732_).contentHolder, false)) {
         ((AbstractFilterMenu)this.f_97732_).player.m_6915_();
      }

      super.m_181908_();
      this.handleTooltips();
      this.handleIndicators();
   }

   protected void handleTooltips() {
      List<IconButton> tooltipButtons = this.getTooltipButtons();

      for (IconButton button : tooltipButtons) {
         if (!button.getToolTip().isEmpty()) {
            button.setToolTip((Component)button.getToolTip().get(0));
            button.getToolTip().add(TooltipHelper.holdShift(Palette.YELLOW, m_96638_()));
         }
      }

      if (m_96638_()) {
         List<MutableComponent> tooltipDescriptions = this.getTooltipDescriptions();

         for (int i = 0; i < tooltipButtons.size(); i++) {
            this.fillToolTip(tooltipButtons.get(i), (Component)tooltipDescriptions.get(i));
         }
      }
   }

   public void handleIndicators() {
      for (IconButton button : this.getTooltipButtons()) {
         button.green = !this.isButtonEnabled(button);
      }
   }

   protected abstract boolean isButtonEnabled(IconButton var1);

   protected List<IconButton> getTooltipButtons() {
      return Collections.emptyList();
   }

   protected List<MutableComponent> getTooltipDescriptions() {
      return Collections.emptyList();
   }

   private void fillToolTip(IconButton button, Component tooltip) {
      if (button.m_198029_()) {
         List<Component> tip = button.getToolTip();
         tip.addAll(TooltipHelper.cutTextComponent(tooltip, Palette.ALL_GRAY));
      }
   }

   protected void contentsCleared() {
   }

   protected void sendOptionUpdate(FilterScreenPacket.Option option) {
      AllPackets.getChannel().sendToServer(new FilterScreenPacket(option));
   }

   public List<Rect2i> getExtraAreas() {
      return this.extraAreas;
   }
}
