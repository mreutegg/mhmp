/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.people.mreutegg.mhmp.zh;

import org.apache.people.mreutegg.mhmp.Coordinate;
import org.apache.people.mreutegg.mhmp.MHMPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ZHGenerator extends ChunkGenerator implements Closeable {

    private static final String OFFSET_PROPS = "offset.properties";

    private static final int DEFAULT_SPAWN_X = 2696836;

    private static final int DEFAULT_SPAWN_Y = 1261767;

    private static final String SPAWN = System.getProperty("spawn",
            DEFAULT_SPAWN_X + "/" + DEFAULT_SPAWN_Y);

    private final MHMPlugin plugin;

    private final Logger logger;

    private final ExecutorService executor;

    private final ASCRepository repo;

    private final LidarRepository lidarRepo;

    private final ZHBlockPopulator populator;

    private int zOffset = -1;

    public ZHGenerator(MHMPlugin plugin) throws IOException {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.executor = Executors.newWorkStealingPool();
        this.repo = new ASCRepository(plugin.getDataFolder(), logger);
        this.lidarRepo = new LidarRepository(plugin.getDataFolder(), executor, logger);
        this.populator = new ZHBlockPopulator(lidarRepo, logger, () -> zOffset);
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData data = createChunkData(world);
        try {
            generateTerrain(world, data, chunkX, chunkZ);
            populateTerrain(data, chunkX, chunkZ);
        } catch (IOException e) {
            logger.warning(e.toString());
        }
        logger.fine("Generated chunk at " + chunkX + "/" + chunkZ);
        return data;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return new ArrayList<>();
        // return Collections.singletonList(populator);
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        int x = DEFAULT_SPAWN_X;
        int y = DEFAULT_SPAWN_Y;
        String[] xy = SPAWN.split("/");
        if (xy.length == 2) {
            try {
                x = Integer.parseInt(xy[0]);
                y = Integer.parseInt(xy[1]);
            } catch (NumberFormatException e) {
                logger.info("Malformed spawn location, using default.");
                // use default
                x = DEFAULT_SPAWN_X;
                y = DEFAULT_SPAWN_Y;
            }
        }
        return new Location(world, x, 100, -y);
    }

    private void generateTerrain(World world, ChunkData data, int chunkX, int chunkZ) throws IOException {
        int minX = chunkX * 16;
        int minZ = chunkZ * 16;
        for(int x=0; x<16; x++){
            for(int z=0; z<16; z++) {
                int blockX = minX + x;
                int blockZ = minZ + z;
                Tile tile = repo.getTile(blockX, -blockZ);
                if (tile != null) {
                    int y = tile.getZ(blockX, -blockZ);
                    y -= getZOffset(world, y);
                    y = Math.min(Math.max(y , 0), 255);
                    short k = tile.getClassification(blockX, -blockZ);
                    Coordinate c = new Coordinate(blockX, -blockZ, y, k);
                    for (int i = 0; i < 5; i++) {
                        data.setBlock(x, y, z, c.getMaterial());
                        y = Math.max(--y, 0);
                    }
                }
            }
        }
    }

    private void populateTerrain(ChunkData data, int chunkX, int chunkZ) throws IOException {
        int minX = chunkX * 16;
        int minZ = chunkZ * 16;
        for(int x=0; x<16; x++){
            for(int z=0; z<16; z++) {
                int blockX = minX + x;
                int blockZ = minZ + z;
                Tile tile = lidarRepo.getTile(blockX, -blockZ);
                if (tile == null) {
                    continue;
                }
                int y = tile.getZ(blockX, -blockZ) - zOffset;
                if (y < 0 || y > 255) {
                    continue;
                }
                short k = tile.getClassification(blockX, -blockZ);
                Coordinate c = new Coordinate(blockX, -blockZ, y, k);
                BlockData b = data.getBlockData(x, y, z);
                if (c.getMaterial() == Material.STONE) {
                    stoneUntilDirt(data, x, y, z);
                } else if (c.getMaterial() == Material.TALL_GRASS
                        || c.getMaterial() == Material.GRASS) {
                    if (b.getMaterial() != Material.AIR) {
                        // put grass one block higher
                        data.setBlock(x, y + 1, z, c.getMaterial());
                    }
                } else if (c.getMaterial() == Material.OAK_LEAVES) {
                    // tree done by BlockPopulator
                } else {
                    for (int i = 0; i < 3; i++) {
                        data.setBlock(x, y - i, z, c.getMaterial());
                    }
                }
            }
        }
    }

    private void stoneUntilDirt(ChunkData data, int x, int y, int z) {
        while (data.getType(x, y, z) != Material.DIRT) {
            data.setBlock(x, y, z, Material.STONE);
            if (--y < 0) {
                break;
            }
        }
    }

    private int getZOffset(World world, int z) {
        if (zOffset == -1) {
            Properties props = new Properties();
            File f = new File(plugin.getDataFolder(), OFFSET_PROPS);
            if (f.exists()) {
                try (InputStream in = new FileInputStream(f)) {
                    props.load(in);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            Object v = props.get(world.getUID().toString());
            if (v == null) {
                // calculate offset for the first time based on passed z
                zOffset = Math.max(0, z - 50);
                // and store it
                props.put(world.getUID().toString(), String.valueOf(zOffset));
                try (OutputStream out = new FileOutputStream(f)) {
                    props.store(out, null);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                zOffset = Integer.parseInt(v.toString());
            }
        }
        return zOffset;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
