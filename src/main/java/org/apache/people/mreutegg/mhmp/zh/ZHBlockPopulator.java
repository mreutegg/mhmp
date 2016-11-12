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
import org.apache.people.mreutegg.mhmp.RenderTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.logging.Logger;

class ZHBlockPopulator extends BlockPopulator {

    private final Logger logger;

    private LidarRepository repo;

    private Supplier<Integer> zOffset;

    ZHBlockPopulator(ExecutorService executor, Logger logger, Supplier<Integer> zOffset) throws IOException {
        this.logger = logger;
        this.repo = new LidarRepository(new File("downloaded"), executor, logger);
        this.zOffset = zOffset;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        int minX = chunkX * 16;
        int minZ = chunkZ * 16;

        try {
            for(int x=0; x<16; x++){
                for(int z=0; z<16; z++) {
                    int blockX = minX + x;
                    int blockZ = minZ + z;
                    Tile tile = repo.getTile(blockX, -blockZ);
                    int y = tile.getZ(blockX, -blockZ) - zOffset.get();
                    if (y < 0 || y > 255) {
                        continue;
                    }
                    short k = tile.getClassification(blockX, -blockZ);
                    Coordinate c = new Coordinate(blockX, -blockZ, y, k);
                    Block b = chunk.getBlock(x, y, z);
                    if (c.getMaterial() == Material.STONE) {
                        stoneUntilDirt(b);
                    } else if (c.getMaterial() == Material.LONG_GRASS) {
                        if (b.getType() != Material.AIR) {
                            // put grass one block higher
                            b = b.getLocation().add(0, 1, 0).getBlock();
                        }
                        b.setType(c.getMaterial());
                    } else if (c.getMaterial() == Material.GRASS) {
                        b.setType(c.getMaterial());
                    } else if (c.getMaterial() == Material.LEAVES) {
                        world.generateTree(RenderTask.diveForDirtOrGrass(b.getLocation()), TreeType.TREE);
                    } else {
                        Location loc = b.getLocation();
                        for (int i = 0; i < 3; i++) {
                            Block block = loc.getBlock();
                            block.setType(c.getMaterial());
                            loc.subtract(0, 1, 0);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        logger.info("Populated chunk at " + chunkX + "/" + chunkZ);
    }

    private void stoneUntilDirt(Block b) {
        while (b.getType() != Material.DIRT) {
            b.setType(Material.STONE);
            Location loc = b.getLocation();
            if (loc.getBlockY() <= 0) {
                break;
            }
            b = b.getLocation().subtract(0, 1, 0).getBlock();
        }
    }


}
