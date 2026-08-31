package com.github.kvdz00.keybindplus.profile;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

public final class ProfileSerializer {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Instant.class, new InstantAdapter())
        .create();

    private ProfileSerializer() {}

    public static String serialize(KeybindProfile profile) {
        return GSON.toJson(profile);
    }

    public static KeybindProfile deserialize(String json) {
        try {
            return GSON.fromJson(json, KeybindProfile.class);
        } catch (JsonSyntaxException e) {
            KeybindPlus.LOGGER.error("Failed to parse profile JSON: {}", e.getMessage());
            return null;
        }
    }

    private static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Instant.parse(json.getAsString());
        }
    }
}
