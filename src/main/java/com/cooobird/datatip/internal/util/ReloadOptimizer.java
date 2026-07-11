package com.cooobird.datatip.internal.util;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 资源重载增量计划器。
 * <p>
 * 记录上一次 datatip 资源内容的稳定摘要，只让加载器重新解析新增、变更和删除的文件。
 * </p>
 */
public final class ReloadOptimizer {
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private final Map<ResourceLocation, String> fileDigests = new HashMap<>();
    private boolean initialized;
    private String environmentSignature = "";

    public ReloadPlan createPlan(Map<ResourceLocation, JsonElement> elements) {
        return createPlan(elements, "");
    }

    public ReloadPlan createPlan(Map<ResourceLocation, JsonElement> elements, String newEnvironmentSignature) {
        List<ChangedFile> changedFiles = new ArrayList<>();
        Set<ResourceLocation> currentFiles = elements.keySet();
        int unchangedFiles = 0;
        String normalizedEnvironment = newEnvironmentSignature != null ? newEnvironmentSignature : "";
        boolean environmentChanged = initialized && !environmentSignature.equals(normalizedEnvironment);

        for (var entry : elements.entrySet()) {
            ResourceLocation location = entry.getKey();
            String newDigest = contentDigest(entry.getValue());
            String oldDigest = fileDigests.get(location);

            if (oldDigest == null) {
                changedFiles.add(new ChangedFile(location, ChangeType.ADDED));
            } else if (environmentChanged || !oldDigest.equals(newDigest)) {
                changedFiles.add(new ChangedFile(location, ChangeType.UPDATED));
            } else {
                unchangedFiles++;
            }
        }

        Set<ResourceLocation> deletedFiles = new HashSet<>();
        for (ResourceLocation oldFile : fileDigests.keySet()) {
            if (!currentFiles.contains(oldFile)) {
                deletedFiles.add(oldFile);
            }
        }

        return new ReloadPlan(!initialized, environmentChanged, normalizedEnvironment,
            changedFiles, deletedFiles, unchangedFiles, elements.size());
    }

    public void commit(ReloadPlan plan, Map<ResourceLocation, JsonElement> elements) {
        for (ChangedFile changedFile : plan.changedFiles()) {
            JsonElement element = elements.get(changedFile.location());
            if (element != null) {
                fileDigests.put(changedFile.location(), contentDigest(element));
            }
        }

        for (ResourceLocation deletedFile : plan.deletedFiles()) {
            fileDigests.remove(deletedFile);
        }

        initialized = true;
        environmentSignature = plan.environmentSignature();
    }

    private static String contentDigest(JsonElement element) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(element.toString().getBytes(StandardCharsets.UTF_8));
            char[] hex = new char[bytes.length * 2];
            for (int i = 0; i < bytes.length; i++) {
                int value = bytes[i] & 0xff;
                hex[i * 2] = HEX[value >>> 4];
                hex[i * 2 + 1] = HEX[value & 0x0f];
            }
            return new String(hex);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is unavailable", e);
        }
    }

    public enum ChangeType {
        ADDED,
        UPDATED
    }

    public record ChangedFile(ResourceLocation location, ChangeType type) {
    }

    public record ReloadPlan(
        boolean firstLoad,
        boolean environmentChanged,
        String environmentSignature,
        List<ChangedFile> changedFiles,
        Set<ResourceLocation> deletedFiles,
        int unchangedFiles,
        int totalFiles
    ) {
        public ReloadPlan {
            environmentSignature = environmentSignature != null ? environmentSignature : "";
            changedFiles = List.copyOf(changedFiles);
            deletedFiles = Set.copyOf(deletedFiles);
        }

        public boolean requiresReload() {
            return firstLoad || !changedFiles.isEmpty() || !deletedFiles.isEmpty();
        }

        public int addedFiles() {
            return count(ChangeType.ADDED);
        }

        public int updatedFiles() {
            return count(ChangeType.UPDATED);
        }

        private int count(ChangeType type) {
            int total = 0;
            for (ChangedFile file : changedFiles) {
                if (file.type() == type) {
                    total++;
                }
            }
            return total;
        }
    }
}
