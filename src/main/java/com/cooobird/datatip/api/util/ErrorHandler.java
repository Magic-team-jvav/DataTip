package com.cooobird.datatip.api.util;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 错误处理器。
 * 提供友好的错误信息和错误统计。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class ErrorHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    // 错误统计
    private static final Map<String, ErrorStats> errorStats = new ConcurrentHashMap<>();

    // 最近的错误
    private static final List<RecentError> recentErrors = new ArrayList<>();
    private static final int MAX_RECENT_ERRORS = 50;

    /**
     * 错误统计。
     */
    private record ErrorStats(String type, int count, long lastOccurrence) {
    }

    /**
     * 最近的错误。
     */
    private record RecentError(long timestamp, String type, String message, String location) {
    }

    /**
     * 报告解析错误。
     */
    public static void reportParseError(String location, String message) {
        reportError("PARSE", message, location);
    }

    /**
     * 报告验证错误。
     */
    public static void reportValidationError(String location, String message) {
        reportError("VALIDATION", message, location);
    }

    /**
     * 报告渲染错误。
     */
    public static void reportRenderError(String contentType, String message) {
        reportError("RENDER", message, contentType);
    }

    /**
     * 报告条件错误。
     */
    public static void reportConditionError(String conditionType, String message) {
        reportError("CONDITION", message, conditionType);
    }

    /**
     * 报告变量错误。
     */
    public static void reportVariableError(String variableName, String message) {
        reportError("VARIABLE", message, variableName);
    }

    /**
     * 报告错误。
     */
    private static void reportError(String type, String message, String location) {
        // 记录日志
        LOGGER.warn("[DataTip {}] {} at {}", type, message, location);

        // 更新统计
        errorStats.merge(type,
            new ErrorStats(type, 1, System.currentTimeMillis()),
            (old, stats) -> new ErrorStats(type, old.count() + 1, System.currentTimeMillis()));

        // 添加到最近错误
        synchronized (recentErrors) {
            recentErrors.add(0, new RecentError(System.currentTimeMillis(), type, message, location));
            if (recentErrors.size() > MAX_RECENT_ERRORS) {
                recentErrors.remove(recentErrors.size() - 1);
            }
        }
    }

    /**
     * Show error toast (chat message).
     */
    public static void showErrorToast(String title, String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§c[DataTip] §f" + title + ": " + message),
                false
            );
        }
    }

    /**
     * Show warning toast.
     */
    public static void showWarningToast(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§e[DataTip] §f" + message),
                false
            );
        }
    }

    /**
     * 获取错误统计。
     */
    public static Map<String, ErrorStats> getErrorStats() {
        return Map.copyOf(errorStats);
    }

    /**
     * 获取最近的错误。
     */
    public static List<RecentError> getRecentErrors() {
        synchronized (recentErrors) {
            return List.copyOf(recentErrors);
        }
    }

    /**
     * 清除错误统计。
     */
    public static void clearStats() {
        errorStats.clear();
        synchronized (recentErrors) {
            recentErrors.clear();
        }
    }

    /**
     * 获取错误摘要。
     */
    public static String getErrorSummary() {
        if (errorStats.isEmpty()) {
            return "No errors recorded";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Error Summary:\n");

        for (ErrorStats stats : errorStats.values()) {
            sb.append(String.format("  %s: %d occurrences\n", stats.type(), stats.count()));
        }

        return sb.toString();
    }

    /**
     * Format error message for user-friendly display.
     */
    public static String formatUserFriendlyError(String error) {
        if (error.contains("Expected object")) {
            return "JSON format error: expected object format {}";
        }
        if (error.contains("Expected string")) {
            return "JSON format error: expected string format";
        }
        if (error.contains("Invalid key format")) {
            return "Invalid key format: expected item ID, tag (#), or wildcard (*, ?)";
        }
        if (error.contains("must have")) {
            return "Missing required field: " + error.substring(error.indexOf("must have") + 10);
        }

        return error;
    }
}
