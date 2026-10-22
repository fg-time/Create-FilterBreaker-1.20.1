package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

public class FilterScreen extends AbstractFilterScreen<FilterMenu> {
   private static final String PREFIX = "gui.filter.";
   private Component allowN = CreateLang.translateDirect("gui.filter.allow_list", new Object[0]);
   private Component allowDESC = CreateLang.translateDirect("gui.filter.allow_list.description", new Object[0]);
   private Component denyN = CreateLang.translateDirect("gui.filter.deny_list", new Object[0]);
   private Component denyDESC = CreateLang.translateDirect("gui.filter.deny_list.description", new Object[0]);
   private Component respectDataN = CreateLang.translateDirect("gui.filter.respect_data", new Object[0]);
   private Component respectDataDESC = CreateLang.translateDirect("gui.filter.respect_data.description", new Object[0]);
   private Component ignoreDataN = CreateLang.translateDirect("gui.filter.ignore_data", new Object[0]);
   private Component ignoreDataDESC = CreateLang.translateDirect("gui.filter.ignore_data.description", new Object[0]);
   private IconButton whitelist;
   private IconButton blacklist;
   private IconButton respectNBT;
   private IconButton ignoreNBT;

   public FilterScreen(FilterMenu menu, Inventory inv, Component title) {
      super(menu, inv, title, AllGuiTextures.FILTER);
   }

   @Override
   protected void m_7856_() {
      this.setWindowOffset(-11, 5);
      super.m_7856_();
      int x = this.f_97735_;
      int y = this.f_97736_;
      this.blacklist = new IconButton(x + 18, y + 75, AllIcons.I_BLACKLIST);
      this.blacklist.withCallback(() -> {
         ((FilterMenu)this.f_97732_).blacklist = true;
         this.sendOptionUpdate(FilterScreenPacket.Option.BLACKLIST);
      });
      this.blacklist.setToolTip(this.denyN);
      this.whitelist = new IconButton(x + 36, y + 75, AllIcons.I_WHITELIST);
      this.whitelist.withCallback(() -> {
         ((FilterMenu)this.f_97732_).blacklist = false;
         this.sendOptionUpdate(FilterScreenPacket.Option.WHITELIST);
      });
      this.whitelist.setToolTip(this.allowN);
      this.addRenderableWidgets(new IconButton[]{this.blacklist, this.whitelist});
      this.respectNBT = new IconButton(x + 60, y + 75, AllIcons.I_RESPECT_NBT);
      this.respectNBT.withCallback(() -> {
         ((FilterMenu)this.f_97732_).respectNBT = true;
         this.sendOptionUpdate(FilterScreenPacket.Option.RESPECT_DATA);
      });
      this.respectNBT.setToolTip(this.respectDataN);
      this.ignoreNBT = new IconButton(x + 78, y + 75, AllIcons.I_IGNORE_NBT);
      this.ignoreNBT.withCallback(() -> {
         ((FilterMenu)this.f_97732_).respectNBT = false;
         this.sendOptionUpdate(FilterScreenPacket.Option.IGNORE_DATA);
      });
      this.ignoreNBT.setToolTip(this.ignoreDataN);
      this.addRenderableWidgets(new IconButton[]{this.respectNBT, this.ignoreNBT});
      this.handleIndicators();
   }

   @Override
   protected List<IconButton> getTooltipButtons() {
      return Arrays.asList(this.blacklist, this.whitelist, this.respectNBT, this.ignoreNBT);
   }

   @Override
   protected List<MutableComponent> getTooltipDescriptions() {
      return Arrays.asList(this.denyDESC.m_6879_(), this.allowDESC.m_6879_(), this.respectDataDESC.m_6879_(), this.ignoreDataDESC.m_6879_());
   }

   @Override
   protected boolean isButtonEnabled(IconButton button) {
      if (button == this.blacklist) {
         return !((FilterMenu)this.f_97732_).blacklist;
      } else if (button == this.whitelist) {
         return ((FilterMenu)this.f_97732_).blacklist;
      } else if (button == this.respectNBT) {
         return !((FilterMenu)this.f_97732_).respectNBT;
      } else {
         return button == this.ignoreNBT ? ((FilterMenu)this.f_97732_).respectNBT : true;
      }
   }

   @Override
   protected int getTitleColor() {
      return 3158064;
   }
}
