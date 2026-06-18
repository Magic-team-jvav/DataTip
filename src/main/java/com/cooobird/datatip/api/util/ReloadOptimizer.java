package com.cooobird.datatip.api.util;

import com.cooobird.datatip.api.loader.TipContentLoader;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.*;

/**
 * 热重载优化器。
 * 实现增量更新，避免全量重载。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class ReloadOptimizer {

    private static final Logger LOGGER = LogUtils.getLogger();

    // 上次加载的文件哈希
    private static final Map<ResourceLocation, Integer> fileHashes = new HashMap<>();

    // 变更的文件
    private static final Set<ResourceLocation> changedFiles = new HashSet<>();

    // 删除的文件
    private static final Set<ResourceLocation> deletedFiles = new HashSet<>();

    /**
     * 检查文件是否变更。
     */
    public static boolean hasChanged(ResourceLocation location, String content) {
        int newHash = content.hashCode();
        Integer oldHash = fileHashes.get(location);

        if (oldHash == null || oldHash != newHash) {
            fileHashes.put(location, newHash);
            changedFiles.add(location);
            return true;
        }

        return false;
    }

    /**
     * 标记文件已删除。
     */
    public static void markDeleted(ResourceLocation location) {
        fileHashes.remove(location);
        deletedFiles.add(location);
    }

    /**
     * 获取变更的文件。
     */
    public static Set<ResourceLocation> getChangedFiles() {
        return Collections.unmodifiableSet(changedFiles);
    }

    /**
     * 获取删除的文件。
     */
    public static Set<ResourceLocation> getDeletedFiles() {
        return Collections.unmodifiableSet(deletedFiles);
    }

    /**
     * 是否有变更。
     */
    public static boolean hasChanges() {
        return !changedFiles.isEmpty() || !deletedFiles.isEmpty();
    }

    /**
     * 清除变更记录。
     */
    public static void clearChanges() {
        changedFiles.clear();
        deletedFiles.clear();
    }

    /**
     * 执行增量更新。
     */
    public static void performIncrementalUpdate(TipContentLoader loader, Map<ResourceLocation, String> newContents) {
        LOGGER.info("Performing incremental update...");

        int added = 0;
        int updated = 0;
        int deleted = 0;

        // 检查变更
        for (var entry : newContents.entrySet()) {
            ResourceLocation location = entry.getKey();
            String content = entry.getValue();

            if (hasChanged(location, content)) {
                if (fileHashes.containsKey(location)) {
                    updated++;
                } else {
                    added++;
                }
            }
        }

        // 检查删除
        Set<ResourceLocation> currentFiles = newContents.keySet();
        for (ResourceLocation oldFile : fileHashes.keySet()) {
            if (!currentFiles.contains(oldFile)) {
                markDeleted(oldFile);
                deleted++;
            }
        }

        LOGGER.info("Incremental update: {} added, {} updated, {} deleted", added, updated, deleted);

        // 清除变更记录
        clearChanges();
    }

    /**
     * 获取更新摘要。
     */
    public static String getUpdateSummary() {
        if (!hasChanges()) {
            return "No changes detected";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Changes detected:\n");

        if (!changedFiles.isEmpty()) {
            sb.append("  Changed: ").append(changedFiles.size()).append(" files\n");
            for (ResourceLocation file : changedFiles) {
                sb.append("    - ").append(file).append("\n");
            }
        }

        if (!deletedFiles.isEmpty()) {
            sb.append("  Deleted: ").append(deletedFiles.size()).append(" files\n");
            for (ResourceLocation file : deletedFiles) {
                sb.append("    - ").append(file).append("\n");
            }
        }

        return sb.toString();
    }
}
