package com.github.kvdz00.keybindplus;

import com.github.kvdz00.keybindplus.config.KeybindPlusConfig;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = KeybindPlus.MOD_ID, name = KeybindPlus.MOD_NAME, version = KeybindPlus.MOD_VERSION, clientSideOnly = true)
public class KeybindPlus {
    public static final String MOD_ID = "keybindplus";
    public static final String MOD_NAME = "KeybindPlus";
    public static final String MOD_VERSION = "1.2.0";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        KeybindPlusConfig.load();
        ProfileManager.get().loadAllProfiles();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        KeybindPlusClient.init();
    }
}
