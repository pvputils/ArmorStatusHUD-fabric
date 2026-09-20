package com.memeasaur.armorstatushudFabric.client;

public final class HudConfigTest {
    public static void main(String[] args) {
        HudConfig config = new HudConfig();
        config.validate();
        check(config.enabled && config.alignMode.equals("bottomleft") && !config.showOffhand, "Legacy defaults");
        check(config.thresholds().getFirst().limit() == 10, "Thresholds sorted ascending");
        check(config.thresholds().getFirst().color() == '4', "Critical durability color");
        config.xOffset = -123;
        config.showOffhand = true;
        HudConfig copy = config.copy();
        check(copy.xOffset == -123 && copy.showOffhand, "JSON round trip");
        copy.xOffset = 50;
        check(config.xOffset == -123, "Draft isolation");
        String[] invalidColors = {"", "100,z", "bad", "-1,f", "100,ff", "999999999999999,f"};
        for (String value : invalidColors) {
            HudConfig bad = new HudConfig(); bad.damageColorList = value; rejects(bad);
        }
        HudConfig bad = new HudConfig(); bad.alignMode = null; rejects(bad);
        bad = new HudConfig(); bad.damageDisplayType = "invalid"; rejects(bad);
        bad = new HudConfig(); bad.listMode = "diagonal"; rejects(bad);
        bad = new HudConfig(); bad.damageThresholdType = "invalid"; rejects(bad);
        System.out.println("ArmorStatusHUD configuration checks passed");
    }
    private static void rejects(HudConfig config) {
        try { config.validate(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Invalid config accepted");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
