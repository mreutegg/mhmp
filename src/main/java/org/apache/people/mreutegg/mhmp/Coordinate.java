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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class Coordinate {

    public final float x;
    public final float y;
    public final float z;

    public static Coordinate fromLine(String line) {
        float[] coord = new float[3];
        int idx = 0;
        for (String part : line.split(" ")) {
            coord[idx++] = Float.parseFloat(part);
        }
        return new Coordinate(coord[0], coord[1], coord[2]);
    }

    private Coordinate(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location asLocation(World world) {
        return new Location(world, -x / 200, z / 100, y / 200);
    }

    public Material getMaterial() {
        Material m;
        if (z > 1800) {
            double d = z - 1800;
            if (Math.random() * 800 < d) {
                m = Material.SNOW_BLOCK;
            } else {
                m = Material.STONE;
            }

        } else if (z > 1000) {
            double d = z - 1000;
            if (Math.random() * 800 < d) {
                m = Material.STONE;
            } else {
                m = Material.DIRT;
            }
        } else {
            m = Material.DIRT;
        }
        return m;
    }

    @Override
    public String toString() {
        return x + "/" + y + "/" + z;
    }
}
