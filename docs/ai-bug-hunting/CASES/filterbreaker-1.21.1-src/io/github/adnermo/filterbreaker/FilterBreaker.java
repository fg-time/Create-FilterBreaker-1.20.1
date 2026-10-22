package io.github.adnermo.filterbreaker;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod("filter_breaker")
public final class FilterBreaker {
   public static final String MODID = "filter_breaker";

   public FilterBreaker(IEventBus modEventBus) {
      if (FMLEnvironment.dist == Dist.CLIENT) {
         NeoForge.EVENT_BUS.addListener(FilterBreakerClient::registerClientCommands);
         NeoForge.EVENT_BUS.addListener(ChatLimitBreakerClient::onScreenInitPost);
      }
   }
}
