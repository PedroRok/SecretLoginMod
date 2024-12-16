package com.paiique.secretlogin;

import net.minecraft.resources.ResourceLocation;

public final class Secretlogin {
    public static final String MOD_ID = "secretlogin";

    private static ModConfig config;

    public static void init() {
        config = new ModConfig();
    }

    public static ModConfig getConfig() {
        return config;
    }

}
