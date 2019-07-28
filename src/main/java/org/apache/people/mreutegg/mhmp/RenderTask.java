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
package org.apache.people.mreutegg.mhmp;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RenderTask extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final Player player;
    private final Iterable<Coordinate> coordinates;
    private final Vector scale;
    private final AtomicInteger scheduledTasks = new AtomicInteger(0);

    public RenderTask(JavaPlugin plugin,
                      Player player,
                      Iterable<Coordinate> coordinates,
                      Vector scale) {
        this.plugin = plugin;
        this.player = player;
        this.coordinates = coordinates;
        this.scale = scale;
    }

    public void run() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        World world = player.getWorld();
        Location currentLoc = player.getLocation().add(0, 50, 0);
        plugin.getLogger().info("Player location: " + currentLoc);
        long num = Iterables.size(coordinates);
        plugin.getLogger().info("Rendering " + num + " coordinates");
        Location origin = null;
        int count = 0;
        long lastP = 0;
        Map<Location, Material> locations = Maps.newHashMap();
        for (Coordinate coord : coordinates) {
            count++;
            if (origin == null) {
                origin = coord.asLocation(world, scale);
            }
            final Location loc = currentLoc.clone().add(coord.asLocation(world, scale).subtract(origin));
            final Material m = coord.getMaterial();
            locations.put(loc, m);
            if (count % 10000 == 0) {
                long p = 100L * count / num;
                if (p > lastP) {
                    plugin.getLogger().info(p + "% done");
                    lastP = p;
                }
            }
            if (locations.size() >= 5) {
                scheduleTask(scheduler, locations);
                locations.clear();
                while (scheduledTasks.get() > 10) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }

        }
        if (!locations.isEmpty()) {
            scheduleTask(scheduler, locations);
        }
        if (lastP < 100) {
            plugin.getLogger().info("100% done");
        }
    }

    private void scheduleTask(BukkitScheduler scheduler,
                              Map<Location, Material> locations) {
        final Map<Location, Material> locs = Maps.newHashMap(locations);
        scheduledTasks.incrementAndGet();
        scheduler.runTask(plugin, () -> {
            for (Map.Entry<Location, Material> entry : locs.entrySet()) {
                Location loc = entry.getKey().clone();
                Material m = entry.getValue();
                if (m == Material.LEGACY_LEAVES) {
                    loc.getWorld().generateTree(diveForDirtOrGrass(loc), TreeType.TREE);
                } else if (m == Material.STONE) {
                    // at most 20 blocks deep until we hit dirt
                    for (int i = 0; i < 20; i++) {
                        Block block = loc.getBlock();
                        if (block.getType() != Material.DIRT) {
                            block.setType(m);
                            loc.subtract(0, 1, 0);
                        } else {
                            break;
                        }
                    }
                } else if (m == Material.TALL_GRASS) {
                    Block block = loc.getBlock();
                    if (block.getType() != Material.AIR) {
                        // put grass one block higher
                        block = loc.add(0, 1, 0).getBlock();
                    }
                    block.setType(m);
                } else if (m == Material.GRASS) {
                    loc.getBlock().setType(m);
                } else {
                    for (int i = 0; i < 3; i++) {
                        Block block = loc.getBlock();
                        block.setType(m);
                        loc.subtract(0, 1, 0);
                    }
                }
            }
            scheduledTasks.decrementAndGet();
        });
    }

    public static Location diveForDirtOrGrass(Location loc) {
        Location l = loc.clone();
        for (int i = 0; i < 20; i++) {
            Material m = l.subtract(0, 1, 0).getBlock().getType();
            if (m == Material.DIRT || m == Material.GRASS) {
                return l.add(0, 1, 0);
            }
        }
        return loc;
    }
}
