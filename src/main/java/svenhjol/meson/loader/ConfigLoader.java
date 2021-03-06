package svenhjol.meson.loader;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonLoader;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigLoader
{
    private MesonLoader instance;
    private ForgeConfigSpec spec;
    private ModConfig config;
    private List<Runnable> refreshConfig = new ArrayList<>();

    public ConfigLoader(MesonLoader instance)
    {
        this.instance = instance;

        // build the config tree
        this.spec = new Builder().configure(this::build).getRight();

        // register config
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        this.config = new ModConfig(ModConfig.Type.COMMON, spec, container);
        container.addConfig(this.config);

        // actual config is loaded too late to do vanilla overrides, so parse it here
        this.earlyConfigHack();
        this.refreshConfig();
    }

    public void earlyConfigHack()
    {
        List<String> lines;

        Path path = FMLPaths.CONFIGDIR.get();
        if (path == null) {
            Meson.debug("Could not fetch config dir path");
            return;
        }

        String name = this.config.getFileName();
        if (name == null) {
            Meson.debug("Could not fetch mod config filename");
            return;
        }

        Path configPath = Paths.get(path.toString() + File.separator + name);
        if (!Files.exists(path)) {
            Meson.debug("Config file does not exist", path);
            return;
        }

        try {
            lines = Files.readAllLines(configPath);
            for (String line : lines) {
                if (!line.contains("enabled")) continue;
                for (MesonModule mod : this.instance.modules) {
                    if (line.contains(mod.name)) {
                        if (line.contains("false")) {
                            mod.enabled = false;
                        } else if (line.contains("true")) {
                            mod.enabled = true;
                        }
                        break;
                    }
                }
            }
            Meson.debug("Finished early loading config");
        } catch (Exception e) {
            Meson.debug("Exception while trying to read config", e);
        }
    }

    /**
     * Called by the Forge config reload event to reset all the module enabled/disabled flags.
     */
    public void refreshConfig()
    {
        refreshConfig.forEach(Runnable::run);
    }

    private Void build(Builder builder)
    {
        instance.categories.entrySet().forEach(entry -> {
            String category = entry.getKey();
            List<MesonModule> modules = entry.getValue();

            builder.push(category);
            buildCategory(builder, modules);
            builder.pop();
        });

        return null;
    }

    private void buildCategory(Builder builder, List<MesonModule> modules)
    {
        // for each module create a config to enable/disable it
        modules.forEach(module -> {
            if (!module.configureEnabled) return;

            Meson.log("Creating config for module " + module.getName());
            if (!module.description.isEmpty()) builder.comment(module.description);
            ForgeConfigSpec.ConfigValue<Boolean> val = builder.define(module.getName() + " enabled", module.enabledByDefault);

            refreshConfig.add(() -> {
                Boolean configEnabled = val.get();
                module.enabled = configEnabled && module.isEnabled();
                instance.enabledModules.put(module.name, module.enabled);
            });
        });

        // for each module create a sublist of module config values
        modules.forEach(module -> {
            builder.push(module.getName());
            buildModule(builder, module);
            builder.pop();
        });
    }

    private void buildModule(Builder builder, MesonModule module)
    {
        // get the annotated fields
        List<Field> fields = new ArrayList<>(Arrays.asList(module.getClass().getDeclaredFields()));
        fields.forEach(field -> {
            Config config = field.getDeclaredAnnotation(Config.class);
            if (config != null) {
                pushConfig(builder, module, field, config);
            }
        });
    }

    private void pushConfig(Builder builder, MesonModule module, Field field, Config config)
    {
        field.setAccessible(true);

        // get the config name, fallback to the field name
        String name = config.name();
        if (name.isEmpty()) name = field.getName();

        // get config description and add a comment if present
        String description = config.description();
        if (!description.isEmpty()) builder.comment(description);

        // get config field type
        Class<?> type = field.getType();

        try {
            ForgeConfigSpec.ConfigValue<?> value;
            Object defaultValue = field.get(null);

            if (defaultValue instanceof List) {
                value = builder.defineList(name, (List<?>) defaultValue, o -> true);
            } else {
                value = builder.define(name, defaultValue);
            }
            refreshConfig.add(() -> {
                try {
                    field.set(null, value.get());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get config for " + module.getName());
        }
    }
}
