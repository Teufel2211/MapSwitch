package com.mapswitch.map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.nio.file.Path;

public record MapContext(
        String name,
        String id,
        Path mapDirectory,
        ResourceKey<Level> overworldKey,
        ResourceKey<Level> netherKey,
        ResourceKey<Level> endKey
) {
}

