package com.mapswitch.map;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.nio.file.Path;

public record MapContext(
        String name,
        String id,
        Path mapDirectory,
        RegistryKey<World> overworldKey,
        RegistryKey<World> netherKey,
        RegistryKey<World> endKey
) {
}
