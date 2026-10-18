package com.entegre.transition;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class TransitionClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        FabricLoader loader = FabricLoader.getInstance();
        if (loader.isDevelopmentEnvironment()) return;
        BootstrapTransition.Result result = BootstrapTransition.apply(loader.getGameDir());
        if (result == BootstrapTransition.Result.INSTALLED) {
            System.out.println("[Entegre] Yeni güncelleme penceresi bir sonraki açılış için hazır.");
        } else if (result == BootstrapTransition.Result.FAILED) {
            System.out.println("[Entegre] Güncelleyici geçişi tamamlanamadı; sonraki açılışta yeniden denenecek.");
        }
    }
}
