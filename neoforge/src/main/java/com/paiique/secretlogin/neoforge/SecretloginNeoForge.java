package com.paiique.secretlogin.neoforge;

import com.paiique.secretlogin.Secretlogin;
import net.neoforged.fml.common.Mod;

@Mod(Secretlogin.MOD_ID)
public final class SecretloginNeoForge {
    public SecretloginNeoForge() {
        Secretlogin.init();
    }
}
