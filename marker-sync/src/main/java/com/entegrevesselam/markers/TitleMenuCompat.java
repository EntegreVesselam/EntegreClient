package com.entegrevesselam.markers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.FriendsButton;
import de.keksuccino.fancymenu.customization.widget.identification.identificationcontext.WidgetIdentificationContextRegistry;

/** Map new vanilla widgets to stable layout identifiers before FancyMenu scans them. */
public final class TitleMenuCompat implements ClientModInitializer {
    private boolean registered;
    @Override public void onInitializeClient() {
        if (!FabricLoader.getInstance().isModLoaded("fancymenu")) return;
        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            if (!registered && screen instanceof TitleScreen) {
                FancyBridge.register();
                registered = true;
            }
        });
    }
    private static final class FancyBridge {
        static void register() {
            var context = WidgetIdentificationContextRegistry.getContextForScreen(TitleScreen.class);
            if (context == null) throw new IllegalStateException("FancyMenu title context missing");
            context.addUniversalIdentifierProvider(meta -> {
                if (meta.getWidget() instanceof FriendsButton) return "entegre_titlescreen_friends_button";
                String key = meta.getWidgetLocalizationKey();
                if ("options.language".equals(key)) return "mc_titlescreen_language_button";
                if ("options.accessibility".equals(key)
                        || "accessibility.onboarding.accessibility.button".equals(key))
                    return "mc_titlescreen_accessibility_button";
                return null;
            });
        }
    }
}
