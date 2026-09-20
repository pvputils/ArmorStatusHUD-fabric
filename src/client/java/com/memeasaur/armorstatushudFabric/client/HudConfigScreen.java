package com.memeasaur.armorstatushudFabric.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

final class HudConfigScreen extends Screen {
    private HudConfig draft = ArmorstatushudFabricClient.config.copy();
    private final Field[] fields = HudConfig.class.getFields();
    private final Map<Field, EditBox> editors = new LinkedHashMap<>();
    private int page;
    private int perPage;
    private String error = "";
    HudConfigScreen() { super(Component.literal("ArmorStatusHUD settings")); }
    @Override protected void init() {
        editors.clear();
        perPage = Math.max(1, (height - 110) / 25);
        page = Math.min(page, (fields.length - 1) / perPage);
        int left = Math.max(8, width / 2 - 155);
        for (int index = page * perPage; index < Math.min(fields.length, (page + 1) * perPage); index++) {
            Field field = fields[index];
            int y = 35 + (index % perPage) * 25;
            try {
                if (field.getType() == boolean.class) {
                    addRenderableWidget(Button.builder(Component.literal(label(field) + ": " + field.getBoolean(draft)), button -> {
                        try { field.setBoolean(draft, !field.getBoolean(draft)); button.setMessage(Component.literal(label(field) + ": " + field.getBoolean(draft))); }
                        catch (IllegalAccessException e) { throw new IllegalStateException(e); }
                    }).bounds(left, y, Math.min(310, width - 16), 20).build());
                } else {
                    EditBox editor = new EditBox(font, left + 160, y, Math.min(150, width - left - 168), 20, Component.literal(label(field)));
                    editor.setMaxLength(1024);
                    editor.setValue(String.valueOf(field.get(draft)));
                    editors.put(field, addRenderableWidget(editor));
                }
            } catch (IllegalAccessException e) { throw new IllegalStateException(e); }
        }
        addRenderableWidget(Button.builder(Component.literal("Previous"), b -> changePage(-1)).bounds(left, height - 65, 95, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.literal("Next"), b -> changePage(1)).bounds(left + 105, height - 65, 95, 20).build()).active = (page + 1) * perPage < fields.length;
        addRenderableWidget(Button.builder(Component.literal("Defaults"), b -> { draft = new HudConfig(); error = ""; rebuildWidgets(); }).bounds(left + 210, height - 65, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
            if (!capture()) return;
            try { draft.save(); ArmorstatushudFabricClient.config = draft; onClose(); }
            catch (Exception e) { error = e.getMessage(); }
        }).bounds(left, height - 40, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose()).bounds(left + 160, height - 40, 150, 20).build());
    }
    private boolean capture() {
        HudConfig candidate = draft.copy();
        try {
            for (var entry : editors.entrySet()) {
                Field field = entry.getKey();
                String value = entry.getValue().getValue().trim();
                if (field.getType() == int.class) field.setInt(candidate, Integer.parseInt(value));
                else field.set(candidate, value);
            }
            candidate.validate(); draft = candidate; error = ""; return true;
        } catch (Exception e) { error = e instanceof NumberFormatException ? "Offsets must be whole numbers" : e.getMessage(); return false; }
    }
    private void changePage(int direction) { if (capture()) { page += direction; rebuildWidgets(); } }
    private static String label(Field field) { String s = field.getName().replaceAll("([A-Z])", " $1"); return Character.toUpperCase(s.charAt(0)) + s.substring(1); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xffffffff);
        int y = 35;
        for (int index = page * perPage; index < Math.min(fields.length, (page + 1) * perPage); index++, y += 25)
            if (fields[index].getType() != boolean.class) graphics.drawString(font, label(fields[index]), Math.max(8, width / 2 - 155), y + 6, 0xffffffff);
        if (!error.isEmpty()) graphics.drawWordWrap(font, Component.literal(error), 8, height - 90, width - 16, 0xffff5555);
    }
    @Override public boolean isPauseScreen() { return false; }
}
