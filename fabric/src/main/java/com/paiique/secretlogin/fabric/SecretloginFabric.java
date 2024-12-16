package com.paiique.secretlogin.fabric;

import com.paiique.secretlogin.Secretlogin;
import net.fabricmc.api.ModInitializer;

public final class SecretloginFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Secretlogin.init();
    }
}
