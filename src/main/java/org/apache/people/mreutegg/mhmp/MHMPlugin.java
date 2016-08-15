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
import com.google.common.collect.Lists;
import com.google.common.io.PatternFilenameFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

public class MHMPlugin extends JavaPlugin {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("render")) {
            if (args.length < 1) {
                sender.sendMessage("You need to specify a .xyz file.");
                return false;
            }
            Iterable<Coordinate> coordinates;
            try {
                List<File> files = Lists.newArrayList(new File(".").listFiles(
                        new PatternFilenameFilter(args[0])));
                Iterable<Iterable<Coordinate>> allCoords = transform(
                        files, file -> {
                            try {
                                if (file.getName().endsWith(".asc")) {
                                    return ASCReader.fromFile(file);
                                } else {
                                    return CoordinateReader.fromFile(file);
                                }
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                coordinates = Iterables.concat(allCoords);
            } catch (UncheckedIOException e) {
                getLogger().warning("File not found " + args[0]);
                return false;
            }
            Vector scale = scaleFromArgs(args);
            new RenderTask(this, player, coordinates, scale).runTaskAsynchronously(this);
            return true;
        }
        return false;
    }

    private static Vector scaleFromArgs(String[] args) {
        Vector scale = new Vector(1, 1, 1);
        if (args.length > 1) {
            try {
                scale.setX(Double.parseDouble(args[1]));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (args.length > 2) {
            try {
                scale.setY(Double.parseDouble(args[2]));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (args.length > 3) {
            try {
                scale.setZ(Double.parseDouble(args[3]));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return scale;
    }
}
