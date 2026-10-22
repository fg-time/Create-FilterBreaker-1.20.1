package com.simibubi.create;

import com.simibubi.create.AllPackets.PacketType;
import com.simibubi.create.compat.computercraft.AttachedComputerPacket;
import com.simibubi.create.compat.trainmap.TrainMapSyncPacket;
import com.simibubi.create.compat.trainmap.TrainMapSyncRequestPacket;
import com.simibubi.create.content.contraptions.ContraptionBlockChangedPacket;
import com.simibubi.create.content.contraptions.ContraptionColliderLockPacket;
import com.simibubi.create.content.contraptions.ContraptionDisassemblyPacket;
import com.simibubi.create.content.contraptions.ContraptionRelocationPacket;
import com.simibubi.create.content.contraptions.ContraptionStallPacket;
import com.simibubi.create.content.contraptions.MountedStorageSyncPacket;
import com.simibubi.create.content.contraptions.TrainCollisionPacket;
import com.simibubi.create.content.contraptions.ContraptionColliderLockPacket.ContraptionColliderLockPacketRequest;
import com.simibubi.create.content.contraptions.actors.contraptionControls.ContraptionDisableActorPacket;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsInputPacket;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsStopControllingPacket;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactEditPacket;
import com.simibubi.create.content.contraptions.elevator.ElevatorFloorListPacket;
import com.simibubi.create.content.contraptions.elevator.ElevatorTargetFloorPacket;
import com.simibubi.create.content.contraptions.elevator.ElevatorFloorListPacket.RequestFloorList;
import com.simibubi.create.content.contraptions.gantry.GantryContraptionUpdatePacket;
import com.simibubi.create.content.contraptions.glue.GlueEffectPacket;
import com.simibubi.create.content.contraptions.glue.SuperGlueRemovalPacket;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionPacket;
import com.simibubi.create.content.contraptions.minecart.CouplingCreationPacket;
import com.simibubi.create.content.contraptions.minecart.capability.MinecartControllerUpdatePacket;
import com.simibubi.create.content.contraptions.sync.ClientMotionPacket;
import com.simibubi.create.content.contraptions.sync.ContraptionInteractionPacket;
import com.simibubi.create.content.contraptions.sync.ContraptionSeatMappingPacket;
import com.simibubi.create.content.contraptions.sync.LimbSwingUpdatePacket;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenuSubmitPacket;
import com.simibubi.create.content.equipment.bell.SoulPulseEffectPacket;
import com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.equipment.clipboard.ClipboardEditPacket;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripInteractionPacket;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonPacket;
import com.simibubi.create.content.equipment.symmetryWand.ConfigureSymmetryWandPacket;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryEffectPacket;
import com.simibubi.create.content.equipment.tool.KnockbackPacket;
import com.simibubi.create.content.equipment.toolbox.ToolboxDisposeAllPacket;
import com.simibubi.create.content.equipment.toolbox.ToolboxEquipPacket;
import com.simibubi.create.content.equipment.zapper.ZapperBeamPacket;
import com.simibubi.create.content.equipment.zapper.terrainzapper.ConfigureWorldshaperPacket;
import com.simibubi.create.content.fluids.transfer.FluidSplashPacket;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionPacket;
import com.simibubi.create.content.kinetics.chainConveyor.ChainPackageInteractionPacket;
import com.simibubi.create.content.kinetics.chainConveyor.ClientboundChainConveyorRidingPacket;
import com.simibubi.create.content.kinetics.chainConveyor.ServerboundChainConveyorRidingPacket;
import com.simibubi.create.content.kinetics.gauge.GaugeObservedPacket;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmPlacementPacket;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmPlacementPacket.ClientBoundRequest;
import com.simibubi.create.content.kinetics.transmission.sequencer.ConfigureSequencedGearshiftPacket;
import com.simibubi.create.content.logistics.box.PackageDestroyPacket;
import com.simibubi.create.content.logistics.depot.EjectorAwardPacket;
import com.simibubi.create.content.logistics.depot.EjectorElytraPacket;
import com.simibubi.create.content.logistics.depot.EjectorPlacementPacket;
import com.simibubi.create.content.logistics.depot.EjectorTriggerPacket;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConfigurationPacket;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnectionPacket;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelEffectPacket;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket;
import com.simibubi.create.content.logistics.funnel.FunnelFlapPacket;
import com.simibubi.create.content.logistics.packagePort.PackagePortConfigurationPacket;
import com.simibubi.create.content.logistics.packagePort.PackagePortPlacementPacket;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterConfigurationPacket;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterEffectPacket;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockResponsePacket;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderRequestPacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryEditPacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryHidingPacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperCategoryRefundPacket;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperLockPacket;
import com.simibubi.create.content.logistics.tableCloth.ShopUpdatePacket;
import com.simibubi.create.content.logistics.tunnel.TunnelFlapPacket;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkConfigurationPacket;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerBindPacket;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerInputPacket;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerStopLecternPacket;
import com.simibubi.create.content.redstone.thresholdSwitch.ConfigureThresholdSwitchPacket;
import com.simibubi.create.content.schematics.cannon.ConfigureSchematicannonPacket;
import com.simibubi.create.content.schematics.packet.InstantSchematicPacket;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.content.schematics.packet.SchematicSyncPacket;
import com.simibubi.create.content.schematics.packet.SchematicUploadPacket;
import com.simibubi.create.content.trains.HonkPacket;
import com.simibubi.create.content.trains.TrainHUDUpdatePacket;
import com.simibubi.create.content.trains.TrainHUDUpdatePacket.Serverbound;
import com.simibubi.create.content.trains.entity.TrainPacket;
import com.simibubi.create.content.trains.entity.TrainPromptPacket;
import com.simibubi.create.content.trains.entity.TrainRelocationPacket;
import com.simibubi.create.content.trains.graph.TrackGraphRequestPacket;
import com.simibubi.create.content.trains.graph.TrackGraphRollCallPacket;
import com.simibubi.create.content.trains.graph.TrackGraphSyncPacket;
import com.simibubi.create.content.trains.schedule.ScheduleEditPacket;
import com.simibubi.create.content.trains.signal.SignalEdgeGroupPacket;
import com.simibubi.create.content.trains.station.StationEditPacket;
import com.simibubi.create.content.trains.station.TrainEditPacket;
import com.simibubi.create.content.trains.station.TrainEditPacket.TrainEditReturnPacket;
import com.simibubi.create.content.trains.track.CurvedTrackDestroyPacket;
import com.simibubi.create.content.trains.track.CurvedTrackSelectionPacket;
import com.simibubi.create.content.trains.track.PlaceExtendedCurvePacket;
import com.simibubi.create.foundation.blockEntity.RemoveBlockEntityPacket;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsPacket;
import com.simibubi.create.foundation.gui.menu.ClearMenuPacket;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import com.simibubi.create.foundation.networking.LeftClickPacket;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.networking.ISyncPersistentData.PersistentDataPacket;
import com.simibubi.create.foundation.utility.ServerSpeedProvider.Packet;
import com.simibubi.create.infrastructure.command.HighlightPacket;
import com.simibubi.create.infrastructure.command.SimpleCreateActions;
import com.simibubi.create.infrastructure.debugInfo.ServerDebugInfoPacket;
import java.util.function.Function;
import net.createmod.catnip.net.ClientboundSimpleActionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import net.minecraftforge.network.simple.SimpleChannel;

public enum AllPackets {
   CONFIGURE_SCHEMATICANNON(ConfigureSchematicannonPacket.class, ConfigureSchematicannonPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_STOCKSWITCH(ConfigureThresholdSwitchPacket.class, ConfigureThresholdSwitchPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_SEQUENCER(ConfigureSequencedGearshiftPacket.class, ConfigureSequencedGearshiftPacket::new, NetworkDirection.PLAY_TO_SERVER),
   PLACE_SCHEMATIC(SchematicPlacePacket.class, SchematicPlacePacket::new, NetworkDirection.PLAY_TO_SERVER),
   UPLOAD_SCHEMATIC(SchematicUploadPacket.class, SchematicUploadPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CLEAR_CONTAINER(ClearMenuPacket.class, ClearMenuPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_FILTER(FilterScreenPacket.class, FilterScreenPacket::new, NetworkDirection.PLAY_TO_SERVER),
   EXTENDO_INTERACT(ExtendoGripInteractionPacket.class, ExtendoGripInteractionPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONTRAPTION_INTERACT(ContraptionInteractionPacket.class, ContraptionInteractionPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CLIENT_MOTION(ClientMotionPacket.class, ClientMotionPacket::new, NetworkDirection.PLAY_TO_SERVER),
   PLACE_ARM(ArmPlacementPacket.class, ArmPlacementPacket::new, NetworkDirection.PLAY_TO_SERVER),
   PLACE_PACKAGE_PORT(PackagePortPlacementPacket.class, PackagePortPlacementPacket::new, NetworkDirection.PLAY_TO_SERVER),
   MINECART_COUPLING_CREATION(CouplingCreationPacket.class, CouplingCreationPacket::new, NetworkDirection.PLAY_TO_SERVER),
   INSTANT_SCHEMATIC(InstantSchematicPacket.class, InstantSchematicPacket::new, NetworkDirection.PLAY_TO_SERVER),
   SYNC_SCHEMATIC(SchematicSyncPacket.class, SchematicSyncPacket::new, NetworkDirection.PLAY_TO_SERVER),
   LEFT_CLICK(LeftClickPacket.class, LeftClickPacket::new, NetworkDirection.PLAY_TO_SERVER),
   PLACE_EJECTOR(EjectorPlacementPacket.class, EjectorPlacementPacket::new, NetworkDirection.PLAY_TO_SERVER),
   TRIGGER_EJECTOR(EjectorTriggerPacket.class, EjectorTriggerPacket::new, NetworkDirection.PLAY_TO_SERVER),
   EJECTOR_ELYTRA(EjectorElytraPacket.class, EjectorElytraPacket::new, NetworkDirection.PLAY_TO_SERVER),
   LINKED_CONTROLLER_INPUT(LinkedControllerInputPacket.class, LinkedControllerInputPacket::new, NetworkDirection.PLAY_TO_SERVER),
   LINKED_CONTROLLER_BIND(LinkedControllerBindPacket.class, LinkedControllerBindPacket::new, NetworkDirection.PLAY_TO_SERVER),
   LINKED_CONTROLLER_USE_LECTERN(LinkedControllerStopLecternPacket.class, LinkedControllerStopLecternPacket::new, NetworkDirection.PLAY_TO_SERVER),
   SUBMIT_GHOST_ITEM(GhostItemSubmitPacket.class, GhostItemSubmitPacket::new, NetworkDirection.PLAY_TO_SERVER),
   BLUEPRINT_COMPLETE_RECIPE(BlueprintAssignCompleteRecipePacket.class, BlueprintAssignCompleteRecipePacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_SYMMETRY_WAND(ConfigureSymmetryWandPacket.class, ConfigureSymmetryWandPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_WORLDSHAPER(ConfigureWorldshaperPacket.class, ConfigureWorldshaperPacket::new, NetworkDirection.PLAY_TO_SERVER),
   TOOLBOX_EQUIP(ToolboxEquipPacket.class, ToolboxEquipPacket::new, NetworkDirection.PLAY_TO_SERVER),
   TOOLBOX_DISPOSE_ALL(ToolboxDisposeAllPacket.class, ToolboxDisposeAllPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_SCHEDULE(ScheduleEditPacket.class, ScheduleEditPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_STATION(StationEditPacket.class, StationEditPacket::new, NetworkDirection.PLAY_TO_SERVER),
   C_CONFIGURE_TRAIN(TrainEditPacket.class, TrainEditPacket::new, NetworkDirection.PLAY_TO_SERVER),
   RELOCATE_TRAIN(TrainRelocationPacket.class, TrainRelocationPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONTROLS_INPUT(ControlsInputPacket.class, ControlsInputPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_DATA_GATHERER(DisplayLinkConfigurationPacket.class, DisplayLinkConfigurationPacket::new, NetworkDirection.PLAY_TO_SERVER),
   DESTROY_CURVED_TRACK(CurvedTrackDestroyPacket.class, CurvedTrackDestroyPacket::new, NetworkDirection.PLAY_TO_SERVER),
   SELECT_CURVED_TRACK(CurvedTrackSelectionPacket.class, CurvedTrackSelectionPacket::new, NetworkDirection.PLAY_TO_SERVER),
   PLACE_CURVED_TRACK(PlaceExtendedCurvePacket.class, PlaceExtendedCurvePacket::new, NetworkDirection.PLAY_TO_SERVER),
   GLUE_IN_AREA(SuperGlueSelectionPacket.class, SuperGlueSelectionPacket::new, NetworkDirection.PLAY_TO_SERVER),
   GLUE_REMOVED(SuperGlueRemovalPacket.class, SuperGlueRemovalPacket::new, NetworkDirection.PLAY_TO_SERVER),
   TRAIN_COLLISION(TrainCollisionPacket.class, TrainCollisionPacket::new, NetworkDirection.PLAY_TO_SERVER),
   C_TRAIN_HUD(Serverbound.class, Serverbound::new, NetworkDirection.PLAY_TO_SERVER),
   C_TRAIN_HONK(
      com.simibubi.create.content.trains.HonkPacket.Serverbound.class,
      com.simibubi.create.content.trains.HonkPacket.Serverbound::new,
      NetworkDirection.PLAY_TO_SERVER
   ),
   OBSERVER_STRESSOMETER(GaugeObservedPacket.class, GaugeObservedPacket::new, NetworkDirection.PLAY_TO_SERVER),
   EJECTOR_AWARD(EjectorAwardPacket.class, EjectorAwardPacket::new, NetworkDirection.PLAY_TO_SERVER),
   TRACK_GRAPH_REQUEST(TrackGraphRequestPacket.class, TrackGraphRequestPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_ELEVATOR_CONTACT(ElevatorContactEditPacket.class, ElevatorContactEditPacket::new, NetworkDirection.PLAY_TO_SERVER),
   REQUEST_FLOOR_LIST(RequestFloorList.class, RequestFloorList::new, NetworkDirection.PLAY_TO_SERVER),
   ELEVATOR_SET_FLOOR(ElevatorTargetFloorPacket.class, ElevatorTargetFloorPacket::new, NetworkDirection.PLAY_TO_SERVER),
   VALUE_SETTINGS(ValueSettingsPacket.class, ValueSettingsPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CLIPBOARD_EDIT(ClipboardEditPacket.class, ClipboardEditPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONTRAPTION_COLLIDER_LOCK_REQUEST(ContraptionColliderLockPacketRequest.class, ContraptionColliderLockPacketRequest::new, NetworkDirection.PLAY_TO_SERVER),
   RADIAL_WRENCH_MENU_SUBMIT(RadialWrenchMenuSubmitPacket.class, RadialWrenchMenuSubmitPacket::new, NetworkDirection.PLAY_TO_SERVER),
   LOGISTICS_STOCK_REQUEST(LogisticalStockRequestPacket.class, LogisticalStockRequestPacket::new, NetworkDirection.PLAY_TO_SERVER),
   LOGISTICS_PACKAGE_REQUEST(PackageOrderRequestPacket.class, PackageOrderRequestPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CHAIN_CONVEYOR_CONNECT(ChainConveyorConnectionPacket.class, ChainConveyorConnectionPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CHAIN_CONVEYOR_RIDING(ServerboundChainConveyorRidingPacket.class, ServerboundChainConveyorRidingPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CHAIN_PACKAGE_INTERACTION(ChainPackageInteractionPacket.class, ChainPackageInteractionPacket::new, NetworkDirection.PLAY_TO_SERVER),
   PACKAGE_PORT_CONFIGURATION(PackagePortConfigurationPacket.class, PackagePortConfigurationPacket::new, NetworkDirection.PLAY_TO_SERVER),
   TRAIN_MAP_REQUEST(TrainMapSyncRequestPacket.class, TrainMapSyncRequestPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONNECT_FACTORY_PANEL(FactoryPanelConnectionPacket.class, FactoryPanelConnectionPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_FACTORY_PANEL(FactoryPanelConfigurationPacket.class, FactoryPanelConfigurationPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_REDSTONE_REQUESTER(RedstoneRequesterConfigurationPacket.class, RedstoneRequesterConfigurationPacket::new, NetworkDirection.PLAY_TO_SERVER),
   CONFIGURE_STOCK_KEEPER_CATEGORIES(StockKeeperCategoryEditPacket.class, StockKeeperCategoryEditPacket::new, NetworkDirection.PLAY_TO_SERVER),
   REFUND_STOCK_KEEPER_CATEGORY(StockKeeperCategoryRefundPacket.class, StockKeeperCategoryRefundPacket::new, NetworkDirection.PLAY_TO_SERVER),
   LOCK_STOCK_KEEPER(StockKeeperLockPacket.class, StockKeeperLockPacket::new, NetworkDirection.PLAY_TO_SERVER),
   STOCK_KEEPER_HIDE_CATEGORY(StockKeeperCategoryHidingPacket.class, StockKeeperCategoryHidingPacket::new, NetworkDirection.PLAY_TO_SERVER),
   SYMMETRY_EFFECT(SymmetryEffectPacket.class, SymmetryEffectPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   SERVER_SPEED(Packet.class, Packet::new, NetworkDirection.PLAY_TO_CLIENT),
   BEAM_EFFECT(ZapperBeamPacket.class, ZapperBeamPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   CONTRAPTION_STALL(ContraptionStallPacket.class, ContraptionStallPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   CONTRAPTION_DISASSEMBLE(ContraptionDisassemblyPacket.class, ContraptionDisassemblyPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   CONTRAPTION_BLOCK_CHANGED(ContraptionBlockChangedPacket.class, ContraptionBlockChangedPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   GLUE_EFFECT(GlueEffectPacket.class, GlueEffectPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   CONTRAPTION_SEAT_MAPPING(ContraptionSeatMappingPacket.class, ContraptionSeatMappingPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   LIMBSWING_UPDATE(LimbSwingUpdatePacket.class, LimbSwingUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT),
   MINECART_CONTROLLER(MinecartControllerUpdatePacket.class, MinecartControllerUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT),
   FLUID_SPLASH(FluidSplashPacket.class, FluidSplashPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   MOUNTED_STORAGE_SYNC(MountedStorageSyncPacket.class, MountedStorageSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   GANTRY_UPDATE(GantryContraptionUpdatePacket.class, GantryContraptionUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT),
   BLOCK_HIGHLIGHT(HighlightPacket.class, HighlightPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   TUNNEL_FLAP(TunnelFlapPacket.class, TunnelFlapPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   FUNNEL_FLAP(FunnelFlapPacket.class, FunnelFlapPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   POTATO_CANNON(PotatoCannonPacket.class, PotatoCannonPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   SOUL_PULSE(SoulPulseEffectPacket.class, SoulPulseEffectPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   PERSISTENT_DATA(PersistentDataPacket.class, PersistentDataPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   SYNC_RAIL_GRAPH(TrackGraphSyncPacket.class, TrackGraphSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   SYNC_EDGE_GROUP(SignalEdgeGroupPacket.class, SignalEdgeGroupPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   SYNC_TRAIN(TrainPacket.class, TrainPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   REMOVE_TE(RemoveBlockEntityPacket.class, RemoveBlockEntityPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   S_CONFIGURE_TRAIN(TrainEditReturnPacket.class, TrainEditReturnPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   CONTROLS_ABORT(ControlsStopControllingPacket.class, ControlsStopControllingPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   S_TRAIN_HUD(TrainHUDUpdatePacket.class, TrainHUDUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT),
   S_TRAIN_HONK(HonkPacket.class, HonkPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   S_TRAIN_PROMPT(TrainPromptPacket.class, TrainPromptPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   CONTRAPTION_RELOCATION(ContraptionRelocationPacket.class, ContraptionRelocationPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   TRACK_GRAPH_ROLL_CALL(TrackGraphRollCallPacket.class, TrackGraphRollCallPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   S_PLACE_EJECTOR(ClientBoundRequest.class, ClientBoundRequest::new, NetworkDirection.PLAY_TO_CLIENT),
   S_PLACE_ARM(
      com.simibubi.create.content.logistics.depot.EjectorPlacementPacket.ClientBoundRequest.class,
      com.simibubi.create.content.logistics.depot.EjectorPlacementPacket.ClientBoundRequest::new,
      NetworkDirection.PLAY_TO_CLIENT
   ),
   S_PLACE_PACKAGE_PORT(
      com.simibubi.create.content.logistics.packagePort.PackagePortPlacementPacket.ClientBoundRequest.class,
      com.simibubi.create.content.logistics.packagePort.PackagePortPlacementPacket.ClientBoundRequest::new,
      NetworkDirection.PLAY_TO_CLIENT
   ),
   UPDATE_ELEVATOR_FLOORS(ElevatorFloorListPacket.class, ElevatorFloorListPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   CONTRAPTION_ACTOR_TOGGLE(ContraptionDisableActorPacket.class, ContraptionDisableActorPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   CONTRAPTION_COLLIDER_LOCK(ContraptionColliderLockPacket.class, ContraptionColliderLockPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   ATTACHED_COMPUTER(AttachedComputerPacket.class, AttachedComputerPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   SERVER_DEBUG_INFO(ServerDebugInfoPacket.class, ServerDebugInfoPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   PACKAGE_DESTROYED(PackageDestroyPacket.class, PackageDestroyPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   LOGISTICS_STOCK_RESPONSE(LogisticalStockResponsePacket.class, LogisticalStockResponsePacket::new, NetworkDirection.PLAY_TO_CLIENT),
   FACTORY_PANEL_EFFECT(FactoryPanelEffectPacket.class, FactoryPanelEffectPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   PACKAGER_LINK_EFFECT(WiFiEffectPacket.class, WiFiEffectPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   REDSTONE_REQUESTER_EFFECT(RedstoneRequesterEffectPacket.class, RedstoneRequesterEffectPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   KNOCKBACK(KnockbackPacket.class, KnockbackPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   TRAIN_MAP_SYNC(TrainMapSyncPacket.class, TrainMapSyncPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   CLIENTBOUND_CHAIN_CONVEYOR(ClientboundChainConveyorRidingPacket.class, ClientboundChainConveyorRidingPacket::new, NetworkDirection.PLAY_TO_CLIENT),
   SHOP_UPDATE(ShopUpdatePacket.class, ShopUpdatePacket::new, NetworkDirection.PLAY_TO_CLIENT);

   public static final ResourceLocation CHANNEL_NAME = Create.asResource("main");
   public static final int NETWORK_VERSION = 3;
   public static final String NETWORK_VERSION_STR = String.valueOf(3);
   private static SimpleChannel channel;
   private PacketType<?> packetType;

   <T extends SimplePacketBase> AllPackets(Class<T> type, Function<FriendlyByteBuf, T> factory, NetworkDirection direction) {
      this.packetType = new PacketType(type, factory, direction);
   }

   public static void registerPackets() {
      channel = ChannelBuilder.named(CHANNEL_NAME)
         .serverAcceptedVersions(NETWORK_VERSION_STR::equals)
         .clientAcceptedVersions(NETWORK_VERSION_STR::equals)
         .networkProtocolVersion(() -> NETWORK_VERSION_STR)
         .simpleChannel();

      for (AllPackets packet : values()) {
         packet.packetType.register();
      }
   }

   public static SimpleChannel getChannel() {
      return channel;
   }

   public static void sendToNear(Level world, BlockPos pos, int range, Object message) {
      getChannel().send(PacketDistributor.NEAR.with(TargetPoint.p(pos.m_123341_(), pos.m_123342_(), pos.m_123343_(), range, world.m_46472_())), message);
   }

   static {
      ClientboundSimpleActionPacket.addAction("rainbowDebug", () -> SimpleCreateActions::rainbowDebug);
      ClientboundSimpleActionPacket.addAction("overlayReset", () -> SimpleCreateActions::overlayReset);
      ClientboundSimpleActionPacket.addAction("overlayScreen", () -> SimpleCreateActions::overlayScreen);
      ClientboundSimpleActionPacket.addAction("experimentalLighting", () -> SimpleCreateActions::experimentalLighting);
      ClientboundSimpleActionPacket.addAction("fabulousWarning", () -> SimpleCreateActions::fabulousWarning);
      ClientboundSimpleActionPacket.addAction("zoomMultiplier", () -> SimpleCreateActions::zoomMultiplier);
      ClientboundSimpleActionPacket.addAction("camAngleYawTarget", () -> value -> SimpleCreateActions.camAngleTarget(value, true));
      ClientboundSimpleActionPacket.addAction("camAnglePitchTarget", () -> value -> SimpleCreateActions.camAngleTarget(value, false));
      ClientboundSimpleActionPacket.addAction("camAngleFunction", () -> SimpleCreateActions::camAngleFunction);
   }
}
