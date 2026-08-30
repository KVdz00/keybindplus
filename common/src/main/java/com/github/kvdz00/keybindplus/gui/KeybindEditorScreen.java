package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.keybind.KeybindApplier;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class KeybindEditorScreen extends Screen {
    private final Screen parent;
    private final KeybindProfile profile;
    private final Map<String, String> workingKeybinds;
    private final Map<String, String> actionCategories = new HashMap<>();

    private KeybindEditListWidget listWidget;
    private EditBox searchField;
    private Button filterButton;
    private String activeRebindAction = null;
    private boolean conflictsOnly = false;

    public KeybindEditorScreen(Screen parent, KeybindProfile profile) {
        this(parent, profile, false);
    }

    public KeybindEditorScreen(Screen parent, KeybindProfile profile, boolean startWithConflictsOnly) {
        super(Component.translatable("keybindplus.editor.title", profile.getName()));
        this.parent = parent;
        this.profile = profile;
        this.conflictsOnly = startWithConflictsOnly;
        this.workingKeybinds = new LinkedHashMap<>(profile.getKeybinds());

        // Cache category names from game options
        var options = net.minecraft.client.Minecraft.getInstance().options;
        if (options != null && options.keyMappings != null) {
            for (KeyMapping km : options.keyMappings) {
                this.actionCategories.put(km.getName(), km.getCategory().id().getPath());
            }
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Top controls: Search box + Filter button
        this.searchField = new EditBox(this.font, centerX - 154, 26, 170, 18,
            Component.translatable("keybindplus.editor.search"));
        this.searchField.setHint(Component.translatable("keybindplus.editor.search"));
        this.searchField.setResponder(q -> refreshList());
        this.addRenderableWidget(this.searchField);

        this.filterButton = this.addRenderableWidget(Button.builder(
            getFilterButtonLabel(),
            btn -> {
                this.conflictsOnly = !this.conflictsOnly;
                this.filterButton.setMessage(getFilterButtonLabel());
                refreshList();
            }
        ).bounds(centerX + 24, 25, 130, 20)
        .tooltip(Tooltip.create(Component.translatable("keybindplus.tooltip.editor_filter")))
        .build());

        // Keybind scroll list
        this.listWidget = new KeybindEditListWidget(this.minecraft, this,
            this.width, this.height - 86, 48, 28);
        this.addRenderableWidget(this.listWidget);

        // Bottom action buttons
        int btnY = this.height - 30;
        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.editor.save_apply"),
            btn -> onSaveAndApply()
        ).bounds(centerX - 154, btnY, 100, 20)
        .tooltip(Tooltip.create(Component.translatable("keybindplus.tooltip.editor_save_apply")))
        .build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.save"),
            btn -> onSaveOnly()
        ).bounds(centerX - 50, btnY, 96, 20)
        .tooltip(Tooltip.create(Component.translatable("keybindplus.tooltip.save")))
        .build());

        this.addRenderableWidget(Button.builder(
            Component.translatable("keybindplus.popup.cancel"),
            btn -> this.minecraft.setScreenAndShow(parent)
        ).bounds(centerX + 50, btnY, 104, 20).build());

        refreshList();
    }

    private Component getFilterButtonLabel() {
        int conflictCount = calculateConflictMap().size();
        return conflictsOnly
            ? Component.translatable("keybindplus.editor.filter_conflicts", conflictCount)
            : Component.translatable("keybindplus.editor.filter_all", workingKeybinds.size());
    }

    private Map<String, List<String>> calculateConflictMap() {
        Map<String, List<String>> keyToActions = new HashMap<>();
        for (var e : workingKeybinds.entrySet()) {
            if (isUnknownKey(e.getValue())) continue;
            keyToActions.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }

        Map<String, List<String>> conflictMap = new HashMap<>();
        for (var entry : keyToActions.entrySet()) {
            List<String> actions = entry.getValue();
            if (actions.size() > 1) {
                for (String action : actions) {
                    List<String> others = actions.stream()
                        .filter(a -> !a.equals(action))
                        .collect(Collectors.toList());
                    conflictMap.put(action, others);
                }
            }
        }
        return conflictMap;
    }

    private void refreshList() {
        Map<String, List<String>> conflictMap = calculateConflictMap();
        String query = searchField != null ? searchField.getValue().toLowerCase().trim() : "";

        List<KeybindEditListWidget.KeybindRowData> rows = new ArrayList<>();
        for (var entry : workingKeybinds.entrySet()) {
            String actionId = entry.getKey();
            String keyName = entry.getValue();
            String category = actionCategories.getOrDefault(actionId, "");

            // Filter conflicts
            if (conflictsOnly && !conflictMap.containsKey(actionId)) {
                continue;
            }

            // Filter search query
            if (!query.isEmpty()) {
                boolean matchAction = actionId.toLowerCase().contains(query);
                boolean matchCategory = category.toLowerCase().contains(query);
                boolean matchKey = keyName.toLowerCase().contains(query);
                if (!matchAction && !matchCategory && !matchKey) {
                    continue;
                }
            }

            rows.add(new KeybindEditListWidget.KeybindRowData(actionId, keyName, category));
        }

        if (filterButton != null) {
            filterButton.setMessage(getFilterButtonLabel());
        }

        if (listWidget != null) {
            listWidget.setEntries(rows, conflictMap, activeRebindAction);
        }
    }

    public void setActiveRebindAction(String actionId) {
        this.activeRebindAction = actionId;
        refreshList();
    }

    public String getActiveRebindAction() {
        return this.activeRebindAction;
    }

    public void unbindAction(String actionId) {
        this.workingKeybinds.put(actionId, "key.keyboard.unknown");
        this.activeRebindAction = null;
        refreshList();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.activeRebindAction != null) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                this.workingKeybinds.put(activeRebindAction, "key.keyboard.unknown");
            } else {
                InputConstants.Key key = InputConstants.getKey(event);
                this.workingKeybinds.put(activeRebindAction, key.getName());
            }
            this.activeRebindAction = null;
            refreshList();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.activeRebindAction != null) {
            InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(event.button());
            this.workingKeybinds.put(activeRebindAction, mouseKey.getName());
            this.activeRebindAction = null;
            refreshList();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void onSaveAndApply() {
        saveChanges();
        ProfileManager.get().createAutoBackup();
        KeybindApplier.apply(profile);
        this.minecraft.setScreenAndShow(parent);
    }

    private void onSaveOnly() {
        saveChanges();
        this.minecraft.setScreenAndShow(parent);
    }

    private void saveChanges() {
        profile.setKeybinds(workingKeybinds);
        profile.setUpdatedAt(Instant.now());
        ProfileManager.get().updateProfile(profile);
    }

    private static boolean isUnknownKey(String keyName) {
        return keyName == null || keyName.isBlank()
            || keyName.equals("key.keyboard.unknown")
            || keyName.equals(InputConstants.UNKNOWN.getName());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.textRenderer().accept(TextAlignment.CENTER, this.width / 2, 10, this.title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
