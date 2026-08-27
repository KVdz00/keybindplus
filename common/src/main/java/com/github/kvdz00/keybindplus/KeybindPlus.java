package com.github.kvdz00.keybindplus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KeybindPlus {
    public static final String MOD_ID = "keybindplus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private KeybindPlus() {}

    public static void init() {
        LOGGER.info("KeybindPlus common initializing");
    }
}
