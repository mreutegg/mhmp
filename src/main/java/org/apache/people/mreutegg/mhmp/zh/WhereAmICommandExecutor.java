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
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(new InputStreamReader(in, Charsets.UTF_8));
                JSONArray features = (JSONArray) obj.get("features");
                for (Object element : features) {
                    JSONObject feature = (JSONObject) element;
                    JSONObject props = (JSONObject) feature.get("properties");
                    StringBuilder location = new StringBuilder();
                    String town = String.valueOf(props.get("siedlungen"));
                    location.append(town);
                    String district = String.valueOf(props.get("quartiere"));
                    if (!town.equals(district)) {
                        location.append(" / ");
                        location.append(district);
                    }
                    JSONObject geo = (JSONObject) feature.get("geometry");
                    JSONArray coords = (JSONArray) geo.get("coordinates");
                    int x = ((Number) coords.get(0)).intValue();
                    int y = ((Number) coords.get(1)).intValue();
                    map.put(new Point(x, y), location.toString());
                }
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
