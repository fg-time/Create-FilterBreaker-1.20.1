package com.simibubi.create.foundation.networking;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.utility.AdventureUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public abstract class BlockEntityConfigurationPacket<BE extends SyncedBlockEntity> extends SimplePacketBase {
   protected BlockPos pos;

   public BlockEntityConfigurationPacket(FriendlyByteBuf buffer) {
      this.pos = buffer.m_130135_();
      this.readSettings(buffer);
   }

   public BlockEntityConfigurationPacket(BlockPos pos) {
      this.pos = pos;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.m_130064_(this.pos);
      this.writeSettings(buffer);
   }

   @Override
   public boolean handle(Context context) {
      context.enqueueWork(() -> {
         ServerPlayer player = context.getSender();
         if (player != null && !player.m_5833_() && !AdventureUtil.isAdventure(player)) {
            Level world = player.m_9236_();
            if (world != null && world.m_46749_(this.pos)) {
               if (this.pos.m_123314_(player.m_20183_(), this.maxRange())) {
                  BlockEntity blockEntity = world.m_7702_(this.pos);
                  if (blockEntity instanceof SyncedBlockEntity) {
                     this.applySettings(player, (BE)blockEntity);
                     if (!this.causeUpdate()) {
                        return;
                     }

                     ((SyncedBlockEntity)blockEntity).sendData();
                     blockEntity.m_6596_();
                  }
               }
            }
         }
      });
      return true;
   }

   protected int maxRange() {
      return 20;
   }

   protected abstract void writeSettings(FriendlyByteBuf var1);

   protected abstract void readSettings(FriendlyByteBuf var1);

   protected void applySettings(ServerPlayer player, BE be) {
      this.applySettings(be);
   }

   protected boolean causeUpdate() {
      return true;
   }

   protected abstract void applySettings(BE var1);
}
