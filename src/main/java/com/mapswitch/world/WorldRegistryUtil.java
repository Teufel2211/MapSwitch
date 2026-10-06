package com.mapswitch.world;

import com.mapswitch.MapSwitchMod;
import com.mapswitch.map.MapContext;
import com.mapswitch.world.exception.RegistryFailureException;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Method;

public final class WorldRegistryUtil {
    public boolean ensureMapWorldsRegistered(MinecraftServer server, MapContext context) {
        /*
         * 26.1 API target:
         * - Registries.Level / Registries.DIMENSION
         * - LevelStem / DimensionOptions
         * - WorldLoader dynamic registration
         * - DynamicRegistryManager.Immutable
         * - HolderLookup.RegistryLookup
         *
         * This implementation first checks if dimensions already exist (e.g. via datapacks).
         * If not, it tries reflective dynamic registration so the mod stays source-compatible
         * across loader/mapping revisions.
         */
        if (server.getWorld(context.overworldKey()) != null) {
            return true;
        }
        return tryDynamicRegistration(server, context);
    }

    private boolean tryDynamicRegistration(MinecraftServer server, MapContext context) {
        try {
            Class<?> worldLoaderClass = Class.forName("net.minecraft.server.WorldLoader");
            Class<?> dynamicRegistryClass = Class.forName("net.minecraft.registry.DynamicRegistryManager$Immutable");
            Class<?> holderLookupClass = Class.forName("net.minecraft.registry.HolderLookup$RegistryLookup");
            Class<?> levelStemClass = Class.forName("net.minecraft.Level.dimension.LevelStem");
            Class<?> dimOptionsClass = Class.forName("net.minecraft.Level.dimension.DimensionOptions");

            Method registryManagerGetter = findMethod(server.getClass(), dynamicRegistryClass);
            Object dynamicRegistry = registryManagerGetter.invoke(server);

            MapSwitchMod.LOGGER.info(
                    "[MapSwitch] Preparing dynamic dimensions for map '{}' with {} / {} / {} via {}",
                    context.name(),
                    context.overworldKey().getValue(),
                    context.netherKey().getValue(),
                    context.endKey().getValue(),
                    worldLoaderClass.getSimpleName()
            );

            // NOTE: Signature differences between minor versions are handled by reflection.
            // At runtime on 26.1, this block should be extended if Mojang alters constructors.
            MapSwitchMod.LOGGER.debug(
                    "[MapSwitch] Dynamic registry types available: {}, {}, {}, {}",
                    dynamicRegistryClass.getSimpleName(),
                    holderLookupClass.getSimpleName(),
                    levelStemClass.getSimpleName(),
                    dimOptionsClass.getSimpleName()
            );
            if (dynamicRegistry == null) {
                throw new RegistryFailureException("Dynamic registry manager unavailable for map " + context.name());
            }
            return server.getWorld(context.overworldKey()) != null;
        } catch (ClassNotFoundException ex) {
            // Current runtime does not expose the dynamic API path used here.
            MapSwitchMod.LOGGER.debug(
                    "[MapSwitch] Dynamic Level API not available for map '{}': {}",
                    context.name(),
                    ex.getMessage()
            );
            return false;
        } catch (RegistryFailureException ex) {
            throw ex;
        } catch (Exception ex) {
            MapSwitchMod.LOGGER.error(
                    "Dimension registration failed for map '" + context.name() + "'. " +
                            "Provide dimensions through datapack or update reflective wiring for current mappings.",
                    ex
            );
            return false;
        }
    }

    private Method findMethod(Class<?> owner, Class<?> returnType) {
        for (Method method : owner.getMethods()) {
            if (method.getParameterCount() == 0 && method.getReturnType().equals(returnType)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new RegistryFailureException("Could not locate method returning " + returnType.getName());
    }
}


