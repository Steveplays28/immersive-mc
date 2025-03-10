package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ConfigType;
import com.hammy275.immersivemc.common.config.PlacementMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class ImmersivesCustomizeScreen extends OptionsSubScreen {

    protected static int BUTTON_WIDTH = 256;
    protected static int BUTTON_HEIGHT = 20;


    public ImmersivesCustomizeScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.translatable("screen.immersivemc.immersives_customize.title"));
    }

    @Override
    protected void addOptions() {
        ScreenUtils.addOptionIfClient("disable_vanilla_guis", config -> config.disableVanillaInteractionsForSupportedImmersives, (config, newVal) -> config.disableVanillaInteractionsForSupportedImmersives = newVal, this.list);
        ScreenUtils.addOptionIfClient("return_items", config -> config.returnItemsWhenLeavingImmersives, (config, newVal) -> config.returnItemsWhenLeavingImmersives = newVal, this.list);
        ScreenUtils.addOptionIfClient("do_rumble", config -> config.doVRControllerRumble, (config, newVal) -> config.doVRControllerRumble = newVal, this.list);
        ScreenUtils.addOptionIfClient("center_brewing", config -> config.autoCenterBrewingStandImmersive, (config, newVal) -> config.autoCenterBrewingStandImmersive = newVal, this.list);
        ScreenUtils.addOptionIfClient("center_furnace", config -> config.autoCenterFurnaceImmersive, (config, newVal) -> config.autoCenterFurnaceImmersive = newVal, this.list);
        ScreenUtils.addOptionIfClient("right_click_chest", config -> config.rightClickChestInteractions, (config, newVal) -> config.rightClickChestInteractions = newVal, this.list);
        ScreenUtils.addOptionIfClient("spin_crafting_output", config -> config.spinSomeImmersiveOutputs, (config, newVal) -> config.spinSomeImmersiveOutputs = newVal, this.list);
        ScreenUtils.addOption("pet_any_living", config -> config.allowPettingAnythingLiving, (config, newVal) -> config.allowPettingAnythingLiving = newVal, this.list);
        ScreenUtils.addOptionIfClient("right_click_in_vr", config -> config.rightClickImmersiveInteractionsInVR, (config, newVal) -> config.rightClickImmersiveInteractionsInVR = newVal, this.list);
        ScreenUtils.addOptionIfClient("3d_compat", config -> config.compatFor3dResourcePacks, (config, newVal) -> config.compatFor3dResourcePacks = newVal, this.list);
        ScreenUtils.addOptionIfClient("crouch_bypass_immersion", config -> config.crouchingBypassesImmersives, (config, newVal) -> config.crouchingBypassesImmersives = newVal, this.list);

        if (ConfigScreen.getAdjustingConfigType() == ConfigType.CLIENT) {
            this.list.addBig(
                    ScreenUtils.createEnumOption(PlacementMode.class,
                            "config.immersivemc.placement_mode",
                            (placementMode) -> Component.translatable("config.immersivemc.placement_mode." + placementMode.ordinal()),
                            (placementMode) -> Component.translatable("config.immersivemc.placement_mode.desc",
                                    I18n.get("config.immersivemc.placement_mode." + placementMode.ordinal()).toLowerCase()),
                            () -> ConfigScreen.getClientConfigIfAdjusting().placementMode,
                            (newModeIndex, newMode) -> ConfigScreen.getClientConfigIfAdjusting().placementMode = newMode

                    ));

            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.ranged_grab_range",
                    (val) -> {
                        if (val == -1) {
                            return Component.translatable("config.immersivemc.use_pick_range");
                        }
                        return Component.literal(I18n.get("config.immersivemc.ranged_grab_range") + ": " + val);
                    },
                    -1, 12,
                    () -> ConfigScreen.getClientConfigIfAdjusting().rangedGrabRange, (newVal) -> ConfigScreen.getClientConfigIfAdjusting().rangedGrabRange = newVal
            ));
        }
    }

    @Override
    public void onClose() {
        ConfigScreen.writeAdjustingConfig();
        super.onClose();
    }
}
