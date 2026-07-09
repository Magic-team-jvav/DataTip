package com.cooobird.datatip.internal.legacy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

/**
 * 转换后的旧格式 JSON 写出工具。
 */
final class LegacyConvertedJsonWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger("datatip");

    private LegacyConvertedJsonWriter() {
    }

    static void write(ResourceLocation location, JsonObject json) {
        try {
            File outputDir = new File(
                Minecraft.getInstance().gameDirectory,
                "datatip_converted"
            );
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                LOGGER.error("Failed to create output directory: {}", outputDir.getAbsolutePath());
                return;
            }

            File namespaceDir = new File(outputDir, location.getNamespace());
            if (!namespaceDir.exists() && !namespaceDir.mkdirs()) {
                LOGGER.error("Failed to create namespace directory: {}", namespaceDir.getAbsolutePath());
                return;
            }

            File outputFile = new File(namespaceDir, location.getPath() + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String convertedJson = gson.toJson(json);
            if (outputFile.exists() && Files.readString(outputFile.toPath()).equals(convertedJson)) {
                return;
            }

            Files.writeString(outputFile.toPath(), convertedJson);

            LOGGER.info("Converted legacy format saved to: {}", outputFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Failed to write converted JSON for {}", location, e);
        }
    }
}
