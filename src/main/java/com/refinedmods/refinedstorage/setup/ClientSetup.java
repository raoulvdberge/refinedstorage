package com.refinedmods.refinedstorage.setup;

import com.refinedmods.refinedstorage.*;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.container.CrafterContainer;
import com.refinedmods.refinedstorage.container.CrafterManagerContainer;
import com.refinedmods.refinedstorage.container.slot.CrafterManagerSlot;
import com.refinedmods.refinedstorage.item.property.ControllerItemPropertyGetter;
import com.refinedmods.refinedstorage.item.property.NetworkItemPropertyGetter;
import com.refinedmods.refinedstorage.item.property.SecurityCardItemPropertyGetter;
import com.refinedmods.refinedstorage.render.BakedModelOverrideRegistry;
import com.refinedmods.refinedstorage.render.color.PatternItemColor;
import com.refinedmods.refinedstorage.render.model.*;
import com.refinedmods.refinedstorage.render.resourcepack.ResourcePackListener;
import com.refinedmods.refinedstorage.render.tesr.StorageMonitorTileRenderer;
import com.refinedmods.refinedstorage.screen.*;
import com.refinedmods.refinedstorage.screen.factory.CrafterManagerScreenFactory;
import com.refinedmods.refinedstorage.screen.factory.GridScreenFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientSetup {
    private final BakedModelOverrideRegistry bakedModelOverrideRegistry = new BakedModelOverrideRegistry();

    public ClientSetup() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) { // This is null in a runData environment.
            IResourceManager resourceManager = minecraft.getResourceManager();
            if (resourceManager instanceof IReloadableResourceManager) {
                ((IReloadableResourceManager) resourceManager).addReloadListener(new ResourcePackListener());
            }
        }

        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "controller"), (base, registry) -> new FullbrightBakedModel(
            base,
            true,
            new ResourceLocation(RS.ID, "block/controller/cutouts/nearly_off"),
            new ResourceLocation(RS.ID, "block/controller/cutouts/nearly_on"),
            new ResourceLocation(RS.ID, "block/controller/cutouts/on")
        ));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "creative_controller"), (base, registry) -> new FullbrightBakedModel(
            base,
            true,
            new ResourceLocation(RS.ID, "block/controller/cutouts/nearly_off"),
            new ResourceLocation(RS.ID, "block/controller/cutouts/nearly_on"),
            new ResourceLocation(RS.ID, "block/controller/cutouts/on")
        ));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "grid"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/grid/cutouts/front_connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "crafting_grid"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/grid/cutouts/crafting_front_connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "pattern_grid"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/grid/cutouts/pattern_front_connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "fluid_grid"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/grid/cutouts/fluid_front_connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "network_receiver"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/network_receiver/cutouts/connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "network_transmitter"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/network_transmitter/cutouts/connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "relay"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/relay/cutouts/connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "detector"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/detector/cutouts/on")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "security_manager"), (base, registry) -> new FullbrightBakedModel(
            base,
            true,
            new ResourceLocation(RS.ID, "block/security_manager/cutouts/top_connected"),
            new ResourceLocation(RS.ID, "block/security_manager/cutouts/front_connected"),
            new ResourceLocation(RS.ID, "block/security_manager/cutouts/left_connected"),
            new ResourceLocation(RS.ID, "block/security_manager/cutouts/back_connected"),
            new ResourceLocation(RS.ID, "block/security_manager/cutouts/right_connected")
        ));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "wireless_transmitter"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/wireless_transmitter/cutouts/connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "constructor"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/constructor/cutouts/connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "destructor"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/destructor/cutouts/connected")));

        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "disk_drive"), (base, registry) -> new FullbrightBakedModel(
            new DiskDriveBakedModel(
                base,
                registry.get(new ResourceLocation(RS.ID + ":block/disks/disk")),
                registry.get(new ResourceLocation(RS.ID + ":block/disks/disk_near_capacity")),
                registry.get(new ResourceLocation(RS.ID + ":block/disks/disk_full")),
                registry.get(new ResourceLocation(RS.ID + ":block/disks/disk_disconnected"))
            ),
            false,
            new ResourceLocation(RS.ID, "block/disks/leds")
        ));

        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "disk_manipulator"), (base, registry) -> new FullbrightBakedModel(
            new DiskManipulatorBakedModel(
                registry.get(new ResourceLocation(RS.ID + ":block/disk_manipulator_connected")),
                registry.get(new ResourceLocation(RS.ID + ":block/disk_manipulator_disconnected")),
                registry.get(new ResourceLocation(RS.ID + ":block/disks/disk")),
                registry.get(new ResourceLocation(RS.ID + ":block/disks/disk_near_capacity")),
                registry.get(new ResourceLocation(RS.ID + ":block/disks/disk_full")),
                registry.get(new ResourceLocation(RS.ID + ":block/disks/disk_disconnected"))
            ),
            false,
            new ResourceLocation(RS.ID, "block/disk_manipulator/cutouts/connected"),
            new ResourceLocation(RS.ID, "block/disks/leds")
        ));

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

        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "crafter"), (base, registry) -> new FullbrightBakedModel(
            base,
            true,
            new ResourceLocation(RS.ID, "block/crafter/cutouts/side_connected"),
            new ResourceLocation(RS.ID, "block/crafter/cutouts/side_connected_90"),
            new ResourceLocation(RS.ID, "block/crafter/cutouts/side_connected_180"),
            new ResourceLocation(RS.ID, "block/crafter/cutouts/side_connected_270"),
            new ResourceLocation(RS.ID, "block/crafter/cutouts/front_connected")
        ));

        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "crafter_manager"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/crafter_manager/cutouts/front_connected")));
        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "crafting_monitor"), (base, registry) -> new FullbrightBakedModel(base, true, new ResourceLocation(RS.ID, "block/crafting_monitor/cutouts/front_connected")));

        bakedModelOverrideRegistry.add(new ResourceLocation(RS.ID, "pattern"), (base, registry) -> new PatternBakedModel(base));

        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/disk"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/disk_near_capacity"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/disk_full"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/disk_disconnected"));

        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disk_manipulator_disconnected"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disk_manipulator_connected"));

        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/portable_grid_connected"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/portable_grid_disconnected"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk_near_capacity"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk_full"));
        ModelLoader.addSpecialModel(new ResourceLocation(RS.ID + ":block/disks/portable_grid_disk_disconnected"));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onModelBake);

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

        RenderTypeLookup.setRenderLayer(RSBlocks.CONTROLLER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.CREATIVE_CONTROLLER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.CABLE, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.CRAFTER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.CRAFTER_MANAGER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.CRAFTING_MONITOR, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.DETECTOR, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.DISK_MANIPULATOR, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.GRID, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.CRAFTING_GRID, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.PATTERN_GRID, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.FLUID_GRID, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.NETWORK_RECEIVER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.NETWORK_TRANSMITTER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.RELAY, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.SECURITY_MANAGER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.WIRELESS_TRANSMITTER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.IMPORTER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.EXPORTER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.EXTERNAL_STORAGE, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.CONSTRUCTOR, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(RSBlocks.DESTRUCTOR, RenderType.getCutout());

        ClientRegistry.bindTileEntityRenderer(RSTiles.STORAGE_MONITOR, StorageMonitorTileRenderer::new);

        e.getMinecraftSupplier().get().getItemColors().register(new PatternItemColor(), RSItems.PATTERN);

        ItemModelsProperties.func_239418_a_(RSItems.SECURITY_CARD, new ResourceLocation("active"), new SecurityCardItemPropertyGetter());

        ItemModelsProperties.func_239418_a_(RSItems.CONTROLLER, new ResourceLocation("energy_type"), new ControllerItemPropertyGetter());
        ItemModelsProperties.func_239418_a_(RSItems.CREATIVE_CONTROLLER, new ResourceLocation("energy_type"), new ControllerItemPropertyGetter());

        ItemModelsProperties.func_239418_a_(RSItems.WIRELESS_CRAFTING_MONITOR, new ResourceLocation("connected"), new NetworkItemPropertyGetter());
        ItemModelsProperties.func_239418_a_(RSItems.CREATIVE_WIRELESS_CRAFTING_MONITOR, new ResourceLocation("connected"), new NetworkItemPropertyGetter());

        ItemModelsProperties.func_239418_a_(RSItems.WIRELESS_GRID, new ResourceLocation("connected"), new NetworkItemPropertyGetter());
        ItemModelsProperties.func_239418_a_(RSItems.CREATIVE_WIRELESS_GRID, new ResourceLocation("connected"), new NetworkItemPropertyGetter());

        ItemModelsProperties.func_239418_a_(RSItems.WIRELESS_FLUID_GRID, new ResourceLocation("connected"), new NetworkItemPropertyGetter());
        ItemModelsProperties.func_239418_a_(RSItems.CREATIVE_WIRELESS_FLUID_GRID, new ResourceLocation("connected"), new NetworkItemPropertyGetter());
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent e) {
        for (ResourceLocation id : e.getModelRegistry().keySet()) {
            BakedModelOverrideRegistry.BakedModelOverrideFactory factory = this.bakedModelOverrideRegistry.get(new ResourceLocation(id.getNamespace(), id.getPath()));

            if (factory != null) {
                e.getModelRegistry().put(id, factory.create(e.getModelRegistry().get(id), e.getModelRegistry()));
            }
        }
    }
}
