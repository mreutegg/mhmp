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
import org.bukkit.util.Vector;

public class Coordinate {

    public final double x;
    public final double y;
    public final double z;
    public final int k;

    public static Coordinate fromLine(String line) {
        double[] coord = new double[3];
        int k = LidarClassification.GROUND;
        int idx = 0;
        for (String part : line.split(" ")) {
            if (idx >= coord.length) {
                k = Integer.parseInt(part);
            } else {
                coord[idx++] = Float.parseFloat(part);
            }
        }
        return new Coordinate(coord[0], coord[1], coord[2], k);
    }

    public Coordinate(double x, double y, double z) {
        this(x, y, z, LidarClassification.GROUND);
    }

    private Coordinate(double x, double y, double z, int k) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.k = k;
    }

    public Location asLocation(World world, Vector scale) {
        Vector v = new Vector(x, z, -y).divide(scale);
        return new Location(world, Math.round(v.getX()), Math.round(v.getY()), Math.round(v.getZ()));
    }

    public Material getMaterial() {
        Material m;
        if (k == LidarClassification.LOW_VEGETATION) {
            m = Material.GRASS;
        } else if (k == LidarClassification.MEDIUM_VEGETATION) {
            m = Material.LONG_GRASS;
        } else if (k == LidarClassification.HIGH_VEGETATION) {
            m = Material.LEAVES;
        } else if (k == LidarClassification.BUILDING) {
            m = Material.STONE;
        } else if (k == LidarClassification.LOW_POINT) {
            m = getGroundMaterial();
        } else if (k == LidarClassification.WATER) {
            m = Material.WATER;
        } else if (k == LidarClassification.RAIL) {
            m = Material.RAILS;
        } else if (k == LidarClassification.ROAD_SURFACE) {
            m = Material.GRAVEL;
        } else {
            m = getGroundMaterial();
        }
        return m;
    }

    @Override
    public String toString() {
        return x + "/" + y + "/" + z + "/" + k;
    }

    //--------------------------< internal >------------------------------------

    private Material getGroundMaterial() {
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
}
