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

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

public class WhereAmICommandExecutor implements CommandExecutor {

    private static final Map<Point, String> LOCATIONS = createLocations();

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player) || !command.getName().equalsIgnoreCase("whereami")) {
            return false;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();
        player.sendRawMessage("You are near " + getNearestLocation(loc));
        return true;
    }

    static Map<Point, String> getLocations() {
        return Collections.unmodifiableMap(LOCATIONS);
    }

    private String getNearestLocation(Location player) {
        double nearestDistance = Integer.MAX_VALUE;
        String location = "";
        for (Map.Entry<Point, String> entry : LOCATIONS.entrySet()) {
            Point p = entry.getKey();
            Location loc = new Location(player.getWorld(), p.getX(), 0, -p.getY());
            double distance = player.distance(loc);
            if (distance < nearestDistance) {
                location = entry.getValue();
                nearestDistance = distance;
            }
        }
        return location;
    }

    private static Map<Point, String> createLocations() {
        try {
            Map<Point, String> map = Maps.newHashMap();
            try (InputStream in = new BufferedInputStream(
                    WhereAmICommandExecutor.class.getResourceAsStream("locations.json"))) {
                JsonParser parser = new JsonParser();
                JsonElement obj = parser.parse(new InputStreamReader(in, Charsets.UTF_8));
                JsonArray features = obj.getAsJsonObject().getAsJsonArray("features");
                for (JsonElement element : features) {
                    JsonObject feature = element.getAsJsonObject();
                    JsonObject props = feature.getAsJsonObject("properties");
                    StringBuilder location = new StringBuilder();
                    String town = props.get("siedlungen").getAsString();
                    location.append(town);
                    String district = props.get("quartiere").getAsString();
                    if (!town.equals(district)) {
                        location.append(" / ");
                        location.append(district);
                    }
                    JsonObject geo = feature.getAsJsonObject("geometry");
                    JsonArray coords = geo.getAsJsonArray("coordinates");
                    int x = coords.get(0).getAsInt();
                    int y = coords.get(1).getAsInt();
                    map.put(new Point(x, y), location.toString());
                }
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
