package com.refinedmods.refinedstorage.setup;

import com.refinedmods.refinedstorage.*;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverType;
import com.refinedmods.refinedstorage.container.CrafterContainer;
import com.refinedmods.refinedstorage.container.CrafterManagerContainer;
import com.refinedmods.refinedstorage.container.slot.CrafterManagerSlot;
import com.refinedmods.refinedstorage.item.property.ControllerItemPropertyGetter;
import com.refinedmods.refinedstorage.item.property.NetworkItemPropertyGetter;
import com.refinedmods.refinedstorage.item.property.SecurityCardItemPropertyGetter;
import com.refinedmods.refinedstorage.render.BakedModelOverrideRegistry;
import com.refinedmods.refinedstorage.render.ExperimentalLightingPipelineNagger;
import com.refinedmods.refinedstorage.render.color.PatternItemColor;
import com.refinedmods.refinedstorage.render.model.*;
import com.refinedmods.refinedstorage.render.resourcepack.ResourcePackListener;
import com.refinedmods.refinedstorage.render.tesr.StorageMonitorTileRenderer;
import com.refinedmods.refinedstorage.screen.*;
import com.refinedmods.refinedstorage.screen.factory.CrafterManagerScreenFactory;
import com.refinedmods.refinedstorage.screen.factory.GridScreenFactory;
import com.refinedmods.refinedstorage.util.ColorMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class ClientSetup {
    private static final ResourceLocation DISK_RESOURCE = new ResourceLocation(RS.ID + ":block/disks/disk");
    private static final ResourceLocation DISK_NEAR_CAPACITY_RESOURCE = new ResourceLocation(RS.ID + ":block/disks/disk_near_capacity");
    private static final ResourceLocation DISK_FULL_RESOURCE = new ResourceLocation(RS.ID + ":block/disks/disk_full");
    private static final ResourceLocation DISK_DISCONNECTED_RESOURCE = new ResourceLocation(RS.ID + ":block/disks/disk_disconnected");

    private static final ResourceLocation CONNECTED = new ResourceLocation("connected");

    private final BakedModelOverrideRegistry bakedModelOverrideRegistry = new BakedModelOverrideRegistry();

    public ClientSetup() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) { // This is null in a runData environment.
            IResourceManager resourceManager = minecraft.getResourceManager();
            if (resourceManager instanceof IReloadableResourceManager) {
                ((IReloadableResourceManager) resourceManager).addReloadListener(new ResourcePackListener());
            }
        }

        forEachColorApply("controller", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(
            base,
            true,
            new ResourceLocation(RS.ID, "block/controller/cutouts/" + color),
            new ResourceLocation(RS.ID, "block/controller/cutouts/nearly_off"),
            new ResourceLocation(RS.ID, "block/controller/cutouts/nearly_on"))

        ));
        forEachColorApply("creative_controller", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(
            base,
            true,
            new ResourceLocation(RS.ID, "block/controller/cutouts/" + color),
            new ResourceLocation(RS.ID, "block/controller/cutouts/nearly_off"),
            new ResourceLocation(RS.ID, "block/controller/cutouts/nearly_on"))
        ));
        forEachColorApply("grid", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/grid/cutouts/"))));
        forEachColorApply("crafting_grid", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/crafting_grid/cutouts/"))));
        forEachColorApply("pattern_grid", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/pattern_grid/cutouts/"))));
        forEachColorApply("fluid_grid", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/fluid_grid/cutouts/"))));
        forEachColorApply("network_receiver", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/network_receiver/cutouts/"))));
        forEachColorApply("network_transmitter", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/network_transmitter/cutouts/"))));
        forEachColorApply("relay", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/relay/cutouts/"))));
        forEachColorApply("detector", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/detector/cutouts/"))));
        forEachColorApply("security_manager", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(
            base,
            true,
            getMultipleColoredModels(color, "block/security_manager/cutouts/top_",
                "block/security_manager/cutouts/front_",
                "block/security_manager/cutouts/left_",
                "block/security_manager/cutouts/back_",
                "block/security_manager/cutouts/right_")
        )));
        forEachColorApply("wireless_transmitter", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/wireless_transmitter/cutouts/"))));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "constructor"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/constructor/cutouts/connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "destructor"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/destructor/cutouts/connected")));

        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "disk_drive"), (base, registry) -> new FullbrightBakedModel(
            new DiskDriveBakedModel(
                base,
                registry.get(DISK_RESOURCE),
                registry.get(DISK_NEAR_CAPACITY_RESOURCE),
                registry.get(DISK_FULL_RESOURCE),
                registry.get(DISK_DISCONNECTED_RESOURCE)
            ),
            false,
            new ResourceLocation(RS.ID, "block/disks/leds")
        ));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "cable"), (base, registry) -> new BakedModelCableCover(base));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "cover"), (base, registry) -> new BakedModelCover(ItemStack.EMPTY, CoverType.NORMAL));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "hollow_cover"), (base, registry) -> new BakedModelCover(ItemStack.EMPTY, CoverType.HOLLOW));

        forEachColorApply("disk_manipulator", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(
            new DiskManipulatorBakedModel(
                registry.get(new ResourceLocation(RS.ID + ":block/disk_manipulator/" + color)),
                registry.get(new ResourceLocation(RS.ID + ":block/disk_manipulator/disconnected")),
                registry.get(DISK_RESOURCE),
                registry.get(DISK_NEAR_CAPACITY_RESOURCE),
                registry.get(DISK_FULL_RESOURCE),
                registry.get(DISK_DISCONNECTED_RESOURCE)
            ),
            false,
            new ResourceLocation(RS.ID, "block/disks/leds"), new ResourceLocation(RS.ID, "block/disk_manipulator/cutouts/" + color)
        )));

        for (String portableGridName : new String[]{"portable_grid", "creative_portable_grid"}) {
            bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, portableGridName), (base, registry) -> new FullbrightBakedModel(
                new PortableGridBakedModel(
                    registry.get(new ResourceLocation(RS.ID + ":block/portable_grid_connected")),
                    registry.get(new ResourceLocation(RS.ID + ":block/portable_grid_disconnected")),
                    registry.get(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk")),
                    registry.get(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk_near_capacity")),
                    registry.get(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk_full")),
                    registry.get(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk_disconnected"))
                ),
                false,
                new ResourceLocation(RS.ID + ":block/disks/leds")
            ));
        }

        forEachColorApply("crafter", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(
            base,
            true,
            getMultipleColoredModels(color, "block/crafter/cutouts/side_", "block/crafter/cutouts/top_")
        )));

        forEachColorApply("crafter_manager", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/crafter_manager/cutouts/"))));
        forEachColorApply("crafting_monitor", (name, color) -> bakedModelOverrideRegistry.add(name, (base, registry) -> new FullbrightBakedModel(base, true, getColoredModel(color, "block/crafting_monitor/cutouts/"))));

        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "pattern"), (base, registry) -> new PatternBakedModel(base));

        ModelLoader.addSpecialModel(DISK_RESOURCE);
        ModelLoader.addSpecialModel(DISK_NEAR_CAPACITY_RESOURCE);
        ModelLoader.addSpecialModel(DISK_FULL_RESOURCE);
        ModelLoader.addSpecialModel(DISK_DISCONNECTED_RESOURCE);

        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disk_manipulator/disconnected"));

        for (DyeColor color : DyeColor.values()) {
            ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disk_manipulator/" + color));
        }

        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/portable_grid_connected"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/portable_grid_disconnected"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk_near_capacity"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk_full"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk_disconnected"));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onModelBake);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onTextureStitch);
        MinecraftForge.EVENT_BUS.addListener(new ExperimentalLightingPipelineNagger()::onPlayerLoggedIn);

        API.instance().addPatternRenderHandler(pattern -> Screen.hasShiftDown());

        API.instance().addPatternRenderHandler(pattern -> {
            Container container = Minecraft.getInstance().player.openContainer;

            if (container instanceof CrafterManagerContainer) {
                for (Slot slot : container.inventorySlots) {
                    if (slot instanceof CrafterManagerSlot && slot.getStack() == pattern) {
                        return true;
                    }
                }
            }

            return false;
        });

        API.instance().addPatternRenderHandler(pattern -> {
            Container container = Minecraft.getInstance().player.openContainer;

            if (container instanceof CrafterContainer) {
                for (int i = 0; i < 9; ++i) {
                    if (container.getSlot(i).getStack() == pattern) {
                        return true;
                    }
                }
            }

            return false;
        });
    }

    private ResourceLocation[] getMultipleColoredModels(DyeColor color, String... paths) {
        return Arrays.stream(paths).map(path -> getColoredModel(color, path)).toArray(ResourceLocation[]::new);
    }

    private ResourceLocation getColoredModel(DyeColor color, String path) {
        return new ResourceLocation(RS.ID, path + color);
    }

    private void forEachColorApply(String name, BiConsumer<ResourceLocation, DyeColor> consumer) {
        for (DyeColor color : DyeColor.values()) {
            String prefix = color == ColorMap.DEFAULT_COLOR ? "" : color + "_";
            consumer.accept(new ResourceLocation(RS.ID, prefix + name), color);
        }
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent e) {
        MinecraftForge.EVENT_BUS.register(new KeyInputListener());

        ScreenManager.registerFactory(RSContainers.FILTER, FilterScreen::new);
        ScreenManager.registerFactory(RSContainers.CONTROLLER, ControllerScreen::new);
        ScreenManager.registerFactory(RSContainers.DISK_DRIVE, DiskDriveScreen::new);
        ScreenManager.registerFactory(RSContainers.GRID, new GridScreenFactory());
        ScreenManager.registerFactory(RSContainers.STORAGE_BLOCK, StorageBlockScreen::new);
        ScreenManager.registerFactory(RSContainers.FLUID_STORAGE_BLOCK, FluidStorageBlockScreen::new);
        ScreenManager.registerFactory(RSContainers.EXTERNAL_STORAGE, ExternalStorageScreen::new);
        ScreenManager.registerFactory(RSContainers.IMPORTER, ImporterScreen::new);
        ScreenManager.registerFactory(RSContainers.EXPORTER, ExporterScreen::new);
        ScreenManager.registerFactory(RSContainers.NETWORK_TRANSMITTER, NetworkTransmitterScreen::new);
        ScreenManager.registerFactory(RSContainers.RELAY, RelayScreen::new);
        ScreenManager.registerFactory(RSContainers.DETECTOR, DetectorScreen::new);
        ScreenManager.registerFactory(RSContainers.SECURITY_MANAGER, SecurityManagerScreen::new);
        ScreenManager.registerFactory(RSContainers.INTERFACE, InterfaceScreen::new);
        ScreenManager.registerFactory(RSContainers.FLUID_INTERFACE, FluidInterfaceScreen::new);
        ScreenManager.registerFactory(RSContainers.WIRELESS_TRANSMITTER, WirelessTransmitterScreen::new);
        ScreenManager.registerFactory(RSContainers.STORAGE_MONITOR, StorageMonitorScreen::new);
        ScreenManager.registerFactory(RSContainers.CONSTRUCTOR, ConstructorScreen::new);
        ScreenManager.registerFactory(RSContainers.DESTRUCTOR, DestructorScreen::new);
        ScreenManager.registerFactory(RSContainers.DISK_MANIPULATOR, DiskManipulatorScreen::new);
        ScreenManager.registerFactory(RSContainers.CRAFTER, CrafterScreen::new);
        ScreenManager.registerFactory(RSContainers.CRAFTER_MANAGER, new CrafterManagerScreenFactory());
        ScreenManager.registerFactory(RSContainers.CRAFTING_MONITOR, CraftingMonitorScreen::new);
        ScreenManager.registerFactory(RSContainers.WIRELESS_CRAFTING_MONITOR, CraftingMonitorScreen::new);

        ClientRegistry.registerKeyBinding(RSKeyBindings.FOCUS_SEARCH_BAR);
        ClientRegistry.registerKeyBinding(RSKeyBindings.CLEAR_GRID_CRAFTING_MATRIX);
        ClientRegistry.registerKeyBinding(RSKeyBindings.OPEN_WIRELESS_GRID);
        ClientRegistry.registerKeyBinding(RSKeyBindings.OPEN_WIRELESS_FLUID_GRID);
        ClientRegistry.registerKeyBinding(RSKeyBindings.OPEN_WIRELESS_CRAFTING_MONITOR);
        ClientRegistry.registerKeyBinding(RSKeyBindings.OPEN_PORTABLE_GRID);

        RSBlocks.CONTROLLER.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.CREATIVE_CONTROLLER.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.CRAFTER.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.CRAFTER_MANAGER.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.CRAFTING_MONITOR.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.DETECTOR.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.DISK_MANIPULATOR.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.GRID.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.CRAFTING_GRID.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.PATTERN_GRID.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.FLUID_GRID.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.NETWORK_RECEIVER.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.NETWORK_TRANSMITTER.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.RELAY.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.SECURITY_MANAGER.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RSBlocks.WIRELESS_TRANSMITTER.values().forEach(block -> RenderTypeLookup.setRenderLayer(block.get(), RenderType.getCutout()));
        RenderTypeLookup.setRenderLayer(RSBlocks.CABLE.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.IMPORTER.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.EXPORTER.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.EXTERNAL_STORAGE.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.CONSTRUCTOR.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.DESTRUCTOR.get(), RenderType.getCutout());

        ClientRegistry.bindTileEntityRenderer(RSTiles.STORAGE_MONITOR, StorageMonitorTileRenderer::new);

        e.getMinecraftSupplier().get().getItemColors().register(new PatternItemColor(), RSItems.PATTERN.get());

        ItemModelsProperties.func_239418_a_(RSItems.SECURITY_CARD.get(), new ResourceLocation("active"), new SecurityCardItemPropertyGetter());

        RSItems.CONTROLLER.values().forEach(controller -> ItemModelsProperties.func_239418_a_(controller.get(), new ResourceLocation("energy_type"), new ControllerItemPropertyGetter()));
        RSItems.CREATIVE_CONTROLLER.values().forEach(controller -> ItemModelsProperties.func_239418_a_(controller.get(), new ResourceLocation("energy_type"), new ControllerItemPropertyGetter()));

        ItemModelsProperties.func_239418_a_(RSItems.WIRELESS_CRAFTING_MONITOR.get(), CONNECTED, new NetworkItemPropertyGetter());
        ItemModelsProperties.func_239418_a_(RSItems.CREATIVE_WIRELESS_CRAFTING_MONITOR.get(), CONNECTED, new NetworkItemPropertyGetter());

        ItemModelsProperties.func_239418_a_(RSItems.WIRELESS_GRID.get(), CONNECTED, new NetworkItemPropertyGetter());
        ItemModelsProperties.func_239418_a_(RSItems.CREATIVE_WIRELESS_GRID.get(), CONNECTED, new NetworkItemPropertyGetter());

        ItemModelsProperties.func_239418_a_(RSItems.WIRELESS_FLUID_GRID.get(), CONNECTED, new NetworkItemPropertyGetter());
        ItemModelsProperties.func_239418_a_(RSItems.CREATIVE_WIRELESS_FLUID_GRID.get(), CONNECTED, new NetworkItemPropertyGetter());
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent e) {
        FullbrightBakedModel.invalidateCache();

        for (ResourceLocation id : e.getModelRegistry().keySet()) {
            BakedModelOverrideRegistry.BakedModelOverrideFactory factory = this.bakedModelOverrideRegistry.get(new ResourceLocation(id.getNamespace(), id.getPath()));

            if (factory != null) {
                e.getModelRegistry().put(id, factory.create(e.getModelRegistry().get(id), e.getModelRegistry()));
            }
        }
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event){
        if (event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE))
            event.addSprite(new ResourceLocation(RS.ID ,"block/cable_part_border"));
    }
}
