package com.github.kvdz00.keybindplus.profile;

import com.github.kvdz00.keybindplus.KeybindPlus;
import com.github.kvdz00.keybindplus.config.KeybindPlusConfig;
import com.github.kvdz00.keybindplus.keybind.KeybindCapture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class ProfileManager {
    private static final int MAX_BACKUPS = 5;
    private static final DateTimeFormatter BACKUP_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());

    private static ProfileManager instance;
    private final List<KeybindProfile> profiles = new ArrayList<>();
    private String loadedProfile = "";

    private ProfileManager() {}

    public static ProfileManager get() {
        if (instance == null) {
            instance = new ProfileManager();
            instance.loadAllProfiles();
        }
        return instance;
    }

    public void syncFlags() {
        String defaultName = KeybindPlusConfig.get().getDefaultProfile();
        for (KeybindProfile p : profiles) {
            p.setDefault(defaultName != null && !defaultName.isEmpty() && p.getName().equals(defaultName));
            p.setLoaded(loadedProfile != null && !loadedProfile.isEmpty() && p.getName().equals(loadedProfile));
        }
    }

    public void loadAllProfiles() {
        profiles.clear();
        Path dir = KeybindPlusConfig.getProfilesDir();
        if (!Files.exists(dir)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path path : stream) {
                try {
                    String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                    KeybindProfile profile = ProfileSerializer.deserialize(json);
                    if (profile != null && profile.getName() != null) {
                        profiles.add(profile);
                    }
                } catch (IOException e) {
                    KeybindPlus.LOGGER.error("Failed to read profile from {}: {}", path, e.getMessage());
                }
            }
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to list profiles directory: {}", e.getMessage());
        }

        syncFlags();
        KeybindPlus.LOGGER.info("Loaded {} profiles", profiles.size());
    }

    public void createBackup() {
        Map<String, String> current = KeybindCapture.captureAll();
        if (current.isEmpty()) return;

        Path dir = KeybindPlusConfig.getBackupsDir();
        String timestamp = BACKUP_FORMAT.format(Instant.now());
        KeybindProfile backup = new KeybindProfile("backup_" + timestamp, current);
        Path backupPath = dir.resolve(backup.toFileName());

        try {
            Files.createDirectories(dir);
            String json = ProfileSerializer.serialize(backup);
            Files.write(backupPath, json.getBytes(StandardCharsets.UTF_8));
            pruneOldBackups(dir);
            KeybindPlus.LOGGER.info("Created backup: {}", backupPath.getFileName());
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to save backup: {}", e.getMessage());
        }
    }

    private void pruneOldBackups(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "backup_*.json")) {
            List<Path> backups = new ArrayList<>();
            for (Path p : stream) {
                backups.add(p);
            }
            if (backups.size() > MAX_BACKUPS) {
                backups.sort(Comparator.comparing(Path::toString));
                for (int i = 0; i < backups.size() - MAX_BACKUPS; i++) {
                    Files.deleteIfExists(backups.get(i));
                }
            }
        } catch (IOException e) {
            KeybindPlus.LOGGER.warn("Failed to prune old backups: {}", e.getMessage());
        }
    }

    public KeybindProfile createProfileFromCurrent(String name) {
        Map<String, String> binds = KeybindCapture.captureAll();
        KeybindProfile profile = new KeybindProfile(name, binds);

        KeybindProfile existing = getProfile(name);
        if (existing != null) {
            profiles.remove(existing);
        }

        profiles.add(profile);
        syncDefaultFlags();
        writeProfileToFile(profile);
        KeybindPlus.LOGGER.info("Created profile '{}' with {} binds", name, binds.size());
        return profile;
    }

    public KeybindProfile duplicateProfile(String sourceName, String newName) {
        KeybindProfile source = getProfile(sourceName);
        if (source == null) return null;

        KeybindProfile duplicate = new KeybindProfile(newName, source.getKeybinds());
        duplicate.setDefault(false);
        duplicate.setImported(false);

        KeybindProfile existing = getProfile(newName);
        if (existing != null) {
            profiles.remove(existing);
        }

        profiles.add(duplicate);
        syncDefaultFlags();
        writeProfileToFile(duplicate);
        return duplicate;
    }

    public boolean renameProfile(String oldName, String newName) {
        KeybindProfile profile = getProfile(oldName);
        if (profile == null) return false;

        deleteProfileFile(profile);
        profile.setName(newName);
        profile.setUpdatedAt(Instant.now());

        if (profile.isDefault()) {
            KeybindPlusConfig.get().setDefaultProfile(newName);
        }
        if (loadedProfile.equals(oldName)) {
            loadedProfile = newName;
        }

        writeProfileToFile(profile);
        syncFlags();
        return true;
    }

    public boolean deleteProfile(String name) {
        KeybindProfile profile = getProfile(name);
        if (profile == null) return false;

        deleteProfileFile(profile);
        profiles.remove(profile);

        if (profile.isDefault()) {
            KeybindPlusConfig.get().setDefaultProfile("");
        }
        if (loadedProfile.equals(name)) {
            loadedProfile = "";
        }

        syncFlags();
        return true;
    }

    public void setDefaultProfile(String name) {
        KeybindPlusConfig.get().setDefaultProfile(name);
        syncFlags();
    }

    public void clearDefaultProfile() {
        KeybindPlusConfig.get().setDefaultProfile("");
        syncFlags();
    }

    public void setLoadedProfile(String name) {
        this.loadedProfile = name != null ? name : "";
        syncFlags();
    }

    public String getLoadedProfile() {
        return loadedProfile;
    }

    public KeybindProfile getProfile(String name) {
        for (KeybindProfile p : profiles) {
            if (p.getName().equals(name)) return p;
        }
        return null;
    }

    public List<KeybindProfile> listProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    public List<KeybindProfile> searchProfiles(String query) {
        if (query == null || query.trim().isEmpty()) return listProfiles();
        String lower = query.toLowerCase();
        return profiles.stream()
            .filter(p -> p.getName().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    public boolean profileExists(String name) {
        return getProfile(name) != null;
    }

    public Path exportProfile(String name) {
        KeybindProfile profile = getProfile(name);
        if (profile == null) return null;

        Path exportPath = KeybindPlusConfig.getExportsDir().resolve(profile.toFileName());
        try {
            String json = ProfileSerializer.serialize(profile);
            Files.write(exportPath, json.getBytes(StandardCharsets.UTF_8));
            return exportPath;
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to export profile {}: {}", name, e.getMessage());
            return null;
        }
    }

    public KeybindProfile importProfile(Path file) {
        try {
            String json = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            KeybindProfile profile = ProfileSerializer.deserialize(json);
            if (profile == null) {
                KeybindPlus.LOGGER.error("Failed to parse import file: {}", file);
                return null;
            }

            profile.setDefault(false);
            profile.setImported(true);

            KeybindProfile existing = getProfile(profile.getName());
            if (existing != null) {
                profiles.remove(existing);
            }
            profiles.add(profile);
            syncDefaultFlags();
            writeProfileToFile(profile);
            return profile;
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to import profile from {}: {}", file, e.getMessage());
            return null;
        }
    }

    public static boolean isValidProfileFile(Path file) {
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            KeybindProfile p = ProfileSerializer.deserialize(content);
            return p != null && p.getName() != null && p.getKeybinds() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Path> listImportFiles() {
        Path dir = KeybindPlusConfig.getImportsDir();
        try {
            if (!Files.exists(dir)) return Collections.emptyList();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
                List<Path> list = new ArrayList<>();
                for (Path p : stream) {
                    list.add(p);
                }
                return list;
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public void saveProfile(KeybindProfile profile) {
        profile.setUpdatedAt(Instant.now());
        writeProfileToFile(profile);
    }

    private void syncDefaultFlags() {
        String defaultName = KeybindPlusConfig.get().getDefaultProfile();
        for (KeybindProfile p : profiles) {
            p.setDefault(defaultName != null && !defaultName.isEmpty() && p.getName().equals(defaultName));
        }
    }

    private void writeProfileToFile(KeybindProfile profile) {
        Path dir = KeybindPlusConfig.getProfilesDir();
        Path file = dir.resolve(profile.toFileName());
        try {
            Files.createDirectories(dir);
            String json = ProfileSerializer.serialize(profile);
            Files.write(file, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to write profile {}: {}", profile.getName(), e.getMessage());
        }
    }

    private void deleteProfileFile(KeybindProfile profile) {
        Path file = KeybindPlusConfig.getProfilesDir().resolve(profile.toFileName());
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            KeybindPlus.LOGGER.error("Failed to delete profile file {}: {}", file, e.getMessage());
        }
    }
}
