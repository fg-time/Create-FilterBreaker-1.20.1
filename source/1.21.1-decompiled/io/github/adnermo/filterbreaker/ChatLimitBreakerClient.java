package io.github.adnermo.filterbreaker;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.neoforge.client.event.ScreenEvent.Init.Post;

final class ChatLimitBreakerClient {
   static final int CHAT_INPUT_MAX_LENGTH = Integer.MAX_VALUE;

   private ChatLimitBreakerClient() {
   }

   static void onScreenInitPost(Post event) {
      if (event.getScreen() instanceof ChatScreen) {
         for (GuiEventListener listener : event.getListenersList()) {
            if (listener instanceof EditBox editBox) {
               editBox.setMaxLength(Integer.MAX_VALUE);
            }
         }
      }
   }
}
