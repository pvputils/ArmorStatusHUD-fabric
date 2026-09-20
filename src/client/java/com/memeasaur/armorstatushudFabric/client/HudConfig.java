package com.memeasaur.armorstatushudFabric.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

public class HudConfig {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("armorstatushud.json"); }
    public boolean enabled = true;
    public String alignMode = "bottomleft";
    public String listMode = "horizontal";
    public boolean enableItemName = false;
    public boolean showDamageOverlay = true;
    public boolean showItemCount = true;
    public String damageColorList = "100,f; 80,7; 60,e; 40,6; 25,c; 10,4";
    public String damageDisplayType = "value";
    public String damageThresholdType = "percent";
    public boolean showItemDamage = true;
    public boolean showArmorDamage = true;
    public boolean showMaxDamage = false;
    public boolean showEquippedItem = true;
    public boolean showOffhand = false;
    public int xOffset = 2;
    public int yOffset = 2;
    public int yOffsetBottomCenter = 41;
    public boolean applyXOffsetToCenter = false;
    public boolean applyYOffsetToMiddle = false;
    public boolean showInChat = false;

    void validate() {
        choice(alignMode, "topleft", "topcenter", "topright", "middleleft", "middlecenter", "middleright", "bottomleft", "bottomcenter", "bottomright");
        choice(listMode, "horizontal", "vertical");
        choice(damageDisplayType, "value", "percent", "none");
        choice(damageThresholdType, "value", "percent");
        thresholds();
    }
    private static void choice(String value, String... choices) {
        if (!Arrays.asList(choices).contains(value)) throw new IllegalArgumentException("Expected one of: " + String.join(", ", choices));
    }
    record Threshold(int limit, char color) {}
    List<Threshold> thresholds() {
        List<Threshold> result = new ArrayList<>();
        if (damageColorList == null || damageColorList.isBlank()) throw new IllegalArgumentException("Color list cannot be empty");
        for (String entry : damageColorList.split(";")) {
            String[] parts = entry.trim().split(",");
            if (parts.length != 2 || !parts[0].trim().matches("[0-9]+") || !parts[1].trim().matches("[0-9a-f]"))
                throw new IllegalArgumentException("Use colors such as 100,f; 25,c; 10,4");
            result.add(new Threshold(Integer.parseInt(parts[0].trim()), parts[1].trim().charAt(0)));
        }
        result.sort(Comparator.comparingInt(Threshold::limit));
        return result;
    }
    static HudConfig load() throws IOException {
        Path PATH = path();
        if (!Files.exists(PATH)) { HudConfig config = new HudConfig(); config.save(); return config; }
        HudConfig config = GSON.fromJson(Files.readString(PATH), HudConfig.class);
        if (config == null) throw new IllegalArgumentException("Config is empty");
        config.validate();
        return config;
    }
    void save() throws IOException {
        Path PATH = path();
        validate();
        Files.createDirectories(PATH.getParent());
        Path temporary = PATH.resolveSibling(PATH.getFileName() + ".tmp");
        Files.writeString(temporary, GSON.toJson(this));
        Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING);
    }
    HudConfig copy() { return GSON.fromJson(GSON.toJson(this), HudConfig.class); }
}

