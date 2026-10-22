package io.github.adnermo.filterbreaker.mixin;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
   @Inject(method = "normalizeChatMessage", at = @At("HEAD"), cancellable = true)
   private void filterbreaker$normalizeLongCommand(String message, CallbackInfoReturnable<String> callback) {
      String normalized = StringUtils.normalizeSpace(message.trim());
      if (normalized.startsWith("/")) {
         callback.setReturnValue(normalized);
      } else {
         callback.setReturnValue(StringUtil.trimChatMessage(normalized));
      }
   }
}
