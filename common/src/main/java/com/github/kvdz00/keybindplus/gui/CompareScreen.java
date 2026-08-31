package com.github.kvdz00.keybindplus.gui;

import com.github.kvdz00.keybindplus.keybind.KeybindApplier;
import com.github.kvdz00.keybindplus.profile.KeybindProfile;
import com.github.kvdz00.keybindplus.profile.ProfileManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class CompareScreen extends Screen {
    private final Screen parent;
    private final KeybindProfile profileA;
    private final KeybindProfile profileB;
    private final List<CompareRowData> allRows = new ArrayList<>();
    private final Map<String, String> actionCategories = new HashMap<>();

    private CompareListWidget listWidget;
    private EditBox searchField;
    private Button filterButton;
    private boolean diffsOnly;

    public CompareScreen(Screen parent, KeybindProfile profileA, KeybindProfile profileB) {
        super(Component.translatable("keybindplus.compare.title", profileA.getName(), profileB.getName()));
        this.parent = parent;
        this.profileA = profileA;
        this.profileB = profileB;

        var options = Minecraft.getInstance().options;
        if (options != null && options.keyMappings != null) {
            for (KeyMapping km : options.keyMappings) {
                this.actionCategories.put(km.getName(), km.getCategory());
            }
        }

        buildRows();
        long diffCount = allRows.stream().filter(CompareRowData::different).count();
        this.diffsOnly = diffCount > 0;
    }

    private void buildRows() {
        allRows.clear();
        Set<String> allActions = new LinkedHashSet<>();
        allActions.addAll(profileA.getKeybinds().keySet());
        allActions.addAll(profileB.getKeybinds().keySet());

        for (String action : allActions) {
            String valueA = profileA.getKeybinds().getOrDefault(action, "none");
            String valueB = profileB.getKeybinds().getOrDefault(action, "none");
            boolean different = !normalizeKey(valueA).equals(normalizeKey(valueB));
            allRows.add(new CompareRowData(action, valueA, valueB, different));
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        this.searchField = new EditBox(this.font, centerX - 190, 24, 190, 18,
            Component.translatable("keybindplus.compare.search"));
        this.searchField.setResponder(q -> refreshList());
        this.addRenderableWidget(this.searchField);

        this.filterButton = this.addRenderableWidget(new Button(
            centerX + 10, 23, 180, 20,
            getFilterButtonLabel(),
            btn -> {
                this.diffsOnly = !this.diffsOnly;
                this.filterButton.setMessage(getFilterButtonLabel());
                refreshList();
            },
            (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.tooltip.compare_filter"), mx, my)
        ));

        this.listWidget = new CompareListWidget(this.minecraft, this,
            this.width, this.height, 58, this.height - 34, 28);
        this.addRenderableWidget(this.listWidget);

        this.addRenderableWidget(new Button(
            centerX - 50, this.height - 28, 100, 20,
            Component.translatable("keybindplus.screen.done"),
            btn -> this.minecraft.setScreen(parent)
        ));

        refreshList();
    }

    private Component getFilterButtonLabel() {
        long diffCount = allRows.stream().filter(CompareRowData::different).count();
        return diffsOnly
            ? Component.translatable("keybindplus.compare.filter_diffs", diffCount)
            : Component.translatable("keybindplus.compare.filter_all", allRows.size());
    }

    private void refreshList() {
        String query = searchField != null ? searchField.getValue().toLowerCase().trim() : "";
        List<CompareRowData> filtered = allRows.stream()
            .filter(row -> {
                if (diffsOnly && !row.different()) return false;
                if (!query.isEmpty()) {
                    boolean matchAction = row.actionId().toLowerCase().contains(query);
                    boolean matchKeyA = row.valueA().toLowerCase().contains(query);
                    boolean matchKeyB = row.valueB().toLowerCase().contains(query);
                    String category = actionCategories.getOrDefault(row.actionId(), "");
                    boolean matchCat = category.toLowerCase().contains(query);
                    return matchAction || matchKeyA || matchKeyB || matchCat;
                }
                return true;
            })
            .collect(Collectors.toList());

        listWidget.updateEntries(filtered);
    }

    private void syncKey(String actionId, String sourceKey, KeybindProfile targetProfile) {
        targetProfile.putKeybind(actionId, sourceKey);
        targetProfile.setUpdatedAt(Instant.now());
        ProfileManager.get().updateProfile(targetProfile);

        if (ProfileManager.get().isProfileLoaded(targetProfile.getName())) {
            KeybindApplier.apply(targetProfile);
        }

        buildRows();
        if (filterButton != null) {
            filterButton.setMessage(getFilterButtonLabel());
        }
        refreshList();

        ToastNotification.toast(
            "keybindplus.toast.synced_title",
            "keybindplus.toast.synced_desc",
            Component.translatable(actionId).getString(),
            formatKeyName(sourceKey),
            targetProfile.getName()
        );
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        int centerX = this.width / 2;
        int headerY = 46;
        int rowWidth = Math.min(380, this.width - 20);
        int startX = centerX - (rowWidth / 2);

        int colActionX = startX + 6;
        int colA_X = startX + 180;
        int colB_X = startX + 292;

        ChatFormatting colorA = profileA.isLoaded() ? ChatFormatting.GREEN : (profileA.isImported() ? ChatFormatting.AQUA : ChatFormatting.WHITE);
        ChatFormatting colorB = profileB.isLoaded() ? ChatFormatting.GREEN : (profileB.isImported() ? ChatFormatting.AQUA : (profileB.isDefault() ? ChatFormatting.GOLD : ChatFormatting.WHITE));

        this.font.draw(poseStack,
            Component.translatable("keybindplus.compare.action").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD),
            colActionX, headerY, 0xAAAAAA);
        this.font.draw(poseStack,
            Component.literal(truncate(profileA.getName(), 12)).withStyle(colorA, ChatFormatting.BOLD),
            colA_X, headerY, 0xFFFFFF);
        this.font.draw(poseStack,
            Component.literal(truncate(profileB.getName(), 12)).withStyle(colorB, ChatFormatting.BOLD),
            colB_X, headerY, 0xFFFFFF);

        fill(poseStack, startX, headerY + 10, startX + rowWidth, headerY + 11, 0xFF444444);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 1) + "." : text;
    }

    private static String normalizeKey(String key) {
        if (key == null || key.isBlank() || key.equals("key.keyboard.unknown")
            || key.equalsIgnoreCase("none") || key.equals(InputConstants.UNKNOWN.getName())) {
            return "none";
        }
        return key.toLowerCase();
    }

    private static boolean checkUnknownKey(String keyName) {
        return keyName == null || keyName.isBlank()
            || keyName.equals("key.keyboard.unknown")
            || keyName.equalsIgnoreCase("none")
            || keyName.equals(InputConstants.UNKNOWN.getName());
    }

    private static String formatKeyName(String key) {
        if (checkUnknownKey(key)) return "NONE";
        return key
            .replace("key.keyboard.", "")
            .replace("key.mouse.", "M-")
            .replace("left.", "L-")
            .replace("right.", "R-")
            .replace('.', ' ')
            .replace('_', ' ')
            .toUpperCase();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public record CompareRowData(String actionId, String valueA, String valueB, boolean different) {}

    public class CompareListWidget extends ContainerObjectSelectionList<CompareListWidget.Entry> {
        public CompareListWidget(Minecraft minecraft, CompareScreen screen,
                                 int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return Math.min(380, this.width - 20);
        }

        public void updateEntries(List<CompareRowData> rows) {
            this.clearEntries();
            for (CompareRowData row : rows) {
                this.addEntry(new Entry(row));
            }
        }

        public class Entry extends ContainerObjectSelectionList.Entry<Entry> {
            private final CompareRowData data;
            private final Button syncToAButton;
            private final Button syncToBButton;
            private final List<Button> children;

            Entry(CompareRowData data) {
                this.data = data;
                if (data.different()) {
                    this.syncToAButton = new Button(
                        0, 0, 16, 16,
                        Component.literal("<"),
                        btn -> syncKey(data.actionId(), data.valueB(), profileA),
                        (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.compare.copy_to_a", profileA.getName()), mx, my)
                    );

                    this.syncToBButton = new Button(
                        0, 0, 16, 16,
                        Component.literal(">"),
                        btn -> syncKey(data.actionId(), data.valueA(), profileB),
                        (btn, poseStack, mx, my) -> renderTooltip(poseStack, Component.translatable("keybindplus.compare.copy_to_b", profileB.getName()), mx, my)
                    );

                    this.children = List.of(this.syncToAButton, this.syncToBButton);
                } else {
                    this.syncToAButton = null;
                    this.syncToBButton = null;
                    this.children = List.of();
                }
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovering, float partialTick) {
                int baseX = left;
                int rowY = top;
                int rowWidth = CompareListWidget.this.getRowWidth();

                int colActionX = baseX + 6;
                int colA_X = baseX + 180;
                int syncA_X = baseX + 248;
                int syncB_X = baseX + 266;
                int colB_X = baseX + 292;

                if (data.different()) {
                    GuiComponent.fill(poseStack, baseX, rowY + 1, baseX + rowWidth, rowY + 27, 0x25FFAA00);
                    GuiComponent.fill(poseStack, baseX, rowY + 1, baseX + 2, rowY + 27, 0xFFFFAA00);

                    if (syncToAButton != null) {
                        syncToAButton.x = syncA_X;
                        syncToAButton.y = rowY + 6;
                        syncToAButton.render(poseStack, mouseX, mouseY, partialTick);
                    }
                    if (syncToBButton != null) {
                        syncToBButton.x = syncB_X;
                        syncToBButton.y = rowY + 6;
                        syncToBButton.render(poseStack, mouseX, mouseY, partialTick);
                    }
                } else if (hovering) {
                    GuiComponent.fill(poseStack, baseX, rowY + 1, baseX + rowWidth, rowY + 27, 0x12FFFFFF);
                }

                MutableComponent actionText = Component.translatable(data.actionId());
                if (data.different()) {
                    actionText = actionText.withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
                } else {
                    actionText = actionText.withStyle(ChatFormatting.WHITE);
                }
                Minecraft.getInstance().font.draw(poseStack, actionText, colActionX, rowY + 4, 0xFFFFFF);

                String category = actionCategories.getOrDefault(data.actionId(), "");
                if (!category.isEmpty()) {
                    Minecraft.getInstance().font.draw(poseStack,
                        Component.literal(category).withStyle(ChatFormatting.DARK_GRAY), colActionX, rowY + 15, 0x888888);
                }

                String keyA = formatKeyName(data.valueA());
                MutableComponent textA = Component.literal(keyA);
                if (data.different()) {
                    textA = textA.withStyle(checkUnknownKey(data.valueA()) ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE);
                } else {
                    textA = textA.withStyle(checkUnknownKey(data.valueA()) ? ChatFormatting.DARK_GRAY : ChatFormatting.GRAY);
                }
                Minecraft.getInstance().font.draw(poseStack, textA, colA_X, rowY + 8, 0xFFFFFF);

                String keyB = formatKeyName(data.valueB());
                MutableComponent textB = Component.literal(keyB);
                if (data.different()) {
                    textB = textB.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
                } else {
                    textB = textB.withStyle(checkUnknownKey(data.valueB()) ? ChatFormatting.DARK_GRAY : ChatFormatting.GRAY);
                }
                Minecraft.getInstance().font.draw(poseStack, textB, colB_X, rowY + 8, 0xFFFFFF);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return this.children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return this.children;
            }
        }
    }
}
