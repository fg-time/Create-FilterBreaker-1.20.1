package com.simibubi.create.content.logistics.filter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PackageFilterScreen extends AbstractFilterScreen<PackageFilterMenu> {
   private EditBox addressBox;
   private boolean deferFocus;

   public PackageFilterScreen(PackageFilterMenu menu, Inventory inv, Component title) {
      super(menu, inv, title, AllGuiTextures.PACKAGE_FILTER);
   }

   @Override
   protected void m_181908_() {
      super.m_181908_();
      if (this.deferFocus) {
         this.deferFocus = false;
         this.m_7522_(this.addressBox);
      }

      this.addressBox.m_94120_();
   }

   @Override
   protected void m_7856_() {
      this.setWindowOffset(-11, 7);
      super.m_7856_();
      int x = this.f_97735_;
      int y = this.f_97736_;
      this.addressBox = new AddressEditBox(this, this.f_96547_, x + 44, y + 28, 129, 9, false);
      this.addressBox.m_94202_(16777215);
      this.addressBox.m_94144_(((PackageFilterMenu)this.f_97732_).address);
      this.addressBox.m_94151_(this::onAddressEdited);
      this.m_142416_(this.addressBox);
      this.m_7522_(this.addressBox);
   }

   public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      super.m_88315_(graphics, mouseX, mouseY, partialTicks);
      PoseStack ms = graphics.m_280168_();
      ms.m_85836_();
      ms.m_252880_(this.f_97735_ + 16, this.f_97736_ + 23, 0.0F);
      GuiGameElement.of(PackageStyles.getDefaultBox()).render(graphics);
      ms.m_85849_();
   }

   public void onAddressEdited(String s) {
      ((PackageFilterMenu)this.f_97732_).address = s;
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("Address", s);
      AllPackets.getChannel().sendToServer(new FilterScreenPacket(FilterScreenPacket.Option.UPDATE_ADDRESS, tag));
   }

   public boolean m_6375_(double pMouseX, double pMouseY, int pButton) {
      return super.m_6375_(pMouseX, pMouseY, pButton);
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      return this.addressBox.m_6050_(mouseX, mouseY, delta) ? true : super.m_6050_(mouseX, mouseY, delta);
   }

   public boolean m_7933_(int pKeyCode, int pScanCode, int pModifiers) {
      if (pKeyCode == 257) {
         this.m_7522_(null);
      }

      return super.m_7933_(pKeyCode, pScanCode, pModifiers);
   }

   public boolean m_5534_(char pCodePoint, int pModifiers) {
      return super.m_5534_(pCodePoint, pModifiers);
   }

   @Override
   protected void contentsCleared() {
      this.addressBox.m_94144_("");
      this.deferFocus = true;
   }

   @Override
   protected boolean isButtonEnabled(IconButton button) {
      return false;
   }

   @Override
   protected int getTitleColor() {
      return 4013128;
   }
}
