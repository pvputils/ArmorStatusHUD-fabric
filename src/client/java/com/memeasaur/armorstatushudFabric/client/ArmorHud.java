package com.memeasaur.armorstatushudFabric.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

final class ArmorHud {
    private record Entry(ItemStack stack, String name, String damage, int width) {}
    static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        HudConfig c = ArmorstatushudFabricClient.config;
        if (!c.enabled || mc.player == null || mc.options.hideGui || mc.player.isSpectator()
                || mc.getDebugOverlay().showDebugScreen()
                || (mc.screen != null && !(mc.screen instanceof ChatScreen && c.showInChat))) return;
        List<Entry> entries = new ArrayList<>();
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET})
            add(entries, mc.player.getItemBySlot(slot), c.showArmorDamage, c, mc);
        if (c.showEquippedItem) add(entries, mc.player.getMainHandItem(), c.showItemDamage, c, mc);
        if (c.showOffhand) add(entries, mc.player.getOffhandItem(), c.showItemDamage, c, mc);
        boolean vertical = c.listMode.equals("vertical"), right = c.alignMode.endsWith("right");
        int rowHeight = c.enableItemName ? 18 : 16;
        int height = rowHeight * (vertical ? entries.size() : 1);
        int totalWidth = entries.stream().mapToInt(Entry::width).sum();
        int y = c.alignMode.startsWith("middle") ? (g.guiHeight() - height) / 2 + (c.applyYOffsetToMiddle ? c.yOffset : 0)
                : c.alignMode.startsWith("bottom") ? g.guiHeight() - height - (c.alignMode.equals("bottomcenter") ? c.yOffsetBottomCenter : c.yOffset) : c.yOffset;
        int cursor = x(c, g.guiWidth(), totalWidth);
        for (Entry entry : entries) {
            int left = vertical ? x(c, g.guiWidth(), entry.width) : cursor;
            int iconX = right ? left + entry.width - 18 : left;
            g.renderItem(entry.stack, iconX, y);
            if (c.showDamageOverlay && entry.stack.isBarVisible()) {
                g.fill(iconX + 2, y + 13, iconX + 15, y + 15, 0xff000000);
                g.fill(iconX + 2, y + 13, iconX + 2 + entry.stack.getBarWidth(), y + 14, 0xff000000 | entry.stack.getBarColor());
            }
            if (c.showItemCount && entry.stack.getCount() > 1) {
                String count = Integer.toString(entry.stack.getCount());
                g.drawString(mc.font, count, iconX + 17 - mc.font.width(count), y + 9, 0xffffffff, true);
            }
            int nameX = right ? iconX - 2 - mc.font.width(entry.name) : iconX + 18;
            int damageX = right ? iconX - 2 - mc.font.width(entry.damage) : iconX + 18;
            g.drawString(mc.font, entry.name, nameX, y, 0xffffffff, true);
            g.drawString(mc.font, entry.damage, damageX, y + (c.enableItemName ? 9 : 4), 0xffffffff, true);
            if (vertical) y += rowHeight; else cursor += entry.width;
        }
    }
    private static int x(HudConfig c, int screen, int width) {
        return c.alignMode.endsWith("center") ? (screen - width) / 2 + (c.applyXOffsetToCenter ? c.xOffset : 0)
                : c.alignMode.endsWith("right") ? screen - width - c.xOffset : c.xOffset;
    }
    private static void add(List<Entry> entries, ItemStack stack, boolean damageEnabled, HudConfig c, Minecraft mc) {
        if (stack.isEmpty()) return;
        String name = c.enableItemName ? stack.getHoverName().getString() : "";
        String damage = "";
        if (damageEnabled && stack.isDamageableItem() && !c.damageDisplayType.equals("none")) {
            int maximum = stack.getMaxDamage();
            int remaining = Math.max(0, maximum - stack.getDamageValue());
            int percent = (int) ((long) remaining * 100 / Math.max(1, maximum));
            int threshold = c.damageThresholdType.equals("percent") ? percent : remaining;
            char color = 'f';
            for (HudConfig.Threshold candidate : c.thresholds()) {
                if (threshold <= candidate.limit()) { color = candidate.color(); break; }
            }
            damage = "\u00a7" + color + (c.damageDisplayType.equals("percent") ? percent + "%" : remaining + (c.showMaxDamage ? "/" + maximum : ""));
        }
        entries.add(new Entry(stack, name, damage, 20 + Math.max(mc.font.width(name), mc.font.width(damage))));
    }
}

