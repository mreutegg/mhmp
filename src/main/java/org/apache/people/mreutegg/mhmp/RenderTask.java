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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

public class RenderTask extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final Player player;
    private final CoordinateReader reader;

    public RenderTask(JavaPlugin plugin, Player player, CoordinateReader reader) {
        this.plugin = plugin;
        this.player = player;
        this.reader = reader;
    }

    public void run() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        World world = player.getWorld();
        Location currentLoc = player.getLocation().add(0, 10, 0);
        plugin.getLogger().info("Player location: " + currentLoc);
        long num = Iterables.size(reader);
        plugin.getLogger().info("Rendering " + num + " coordinates");
        Location origin = null;
        int count = 0;
        long lastP = 0;
        for (Coordinate coord : reader) {
            count++;
            if (origin == null) {
                origin = coord.asLocation(world);
            }
            final Location loc = currentLoc.clone().add(coord.asLocation(world).subtract(origin));
            final Material m = coord.getMaterial();
            scheduler.runTask(plugin, new Runnable() {
                public void run() {
                    for (int i = 0; i < 3; i++) {
                        Block block = loc.getBlock();
                        block.setType(m);
                        loc.subtract(0, 1, 0);
                    }
                }
            });
            if (count % 10000 == 0) {
                long p = 100L * count / num;
                if (p > lastP) {
                    plugin.getLogger().info(p + "% done");
                    lastP = p;
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (lastP < 100) {
            plugin.getLogger().info("100% done");
        }
    }
}
