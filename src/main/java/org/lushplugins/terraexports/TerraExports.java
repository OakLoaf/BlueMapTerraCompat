package org.lushplugins.terraexports;

import com.dfsek.terra.addons.manifest.api.AddonInitializer;
import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.addon.BaseAddon;
import com.dfsek.terra.api.command.CommandSender;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.event.events.platform.CommandRegistrationEvent;
import com.dfsek.terra.api.event.functional.FunctionalEventHandler;
import com.dfsek.terra.api.inject.annotations.Inject;

import com.dfsek.terra.api.world.biome.Biome;
import com.dfsek.terra.bukkit.nms.v1_21_6.config.VanillaBiomeProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.description.Description;
import org.lushplugins.terraexports.config.BiomeInfo;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
public class TerraExports implements AddonInitializer {
    private static final ObjectMapper JACKSON = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    @Inject
    private Platform platform;
    @Inject
    private BaseAddon addon;
    @Inject
    private Logger logger;

    @Override
    public void initialize() {
        platform.getEventManager()
            .getHandler(FunctionalEventHandler.class)
            .register(addon, CommandRegistrationEvent.class)
            .then(event -> {
                CommandManager<CommandSender> manager = event.getCommandManager();
                manager.command(
                    manager.commandBuilder("export", Description.of("Create an export"))
                        .literal("biomes", Description.of("Start a biome export"))
                        .permission("terra.export.biomes")
                        .handler(context -> {
                            platform.getConfigRegistry().entries().forEach(this::exportBiomes);
                            context.sender().sendMessage("Successfully created biome export");
                        })
                );
            });
    }

    private void exportBiomes(ConfigPack pack) {
        String packKey = pack.getNamespace();
        File targetDir = platform.getDataFolder().toPath()
            .resolve("exports")
            .resolve(packKey.toLowerCase())
            .resolve("biomes")
            .toFile();
        targetDir.mkdirs();

        for (Biome biome : pack.getBiomeProvider().getBiomes()) {
            VanillaBiomeProperties biomeProperties = biome.getContext().get(VanillaBiomeProperties.class);

            try {
                JACKSON.writeValue(new File(targetDir, biome.getID().toLowerCase() + ".json"), new BiomeInfo(biomeProperties));
            } catch (IOException e) {
                logger.error("Failed to write biome: ", e);
            }
        }
    }
}