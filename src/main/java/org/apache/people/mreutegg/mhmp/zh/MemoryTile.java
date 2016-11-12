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

class MemoryTile implements Tile {

    private static final int TILE_WIDTH = 1000;
    
    private final int minX;

    private final int minY;

    private final short[][] z = new short[TILE_WIDTH][];

    private final byte[][] c = new byte[TILE_WIDTH][];

    MemoryTile(int minX, int minY,
               Iterable<Coordinate> coordinates) {
        this.minX = minX;
        this.minY = minY;
        for (int i = 0; i < TILE_WIDTH; i++) {
            z[i] = new short[TILE_WIDTH];
            c[i] = new byte[TILE_WIDTH];
        }
        read(coordinates);
    }

    @Override
    public int getMinX() {
        return minX;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getZ(int x, int y) {
        try {
            return z[x - minX][ y - minY];
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    @Override
    public short getClassification(int x, int y) {
        return c[x - minX][y - minY];
    }

    private void read(Iterable<Coordinate> coordinates) {
        for (Coordinate coord : coordinates) {
            int x = (int) Math.floor(coord.x) - minX;
            int y = (int) Math.floor(coord.y) - minY;
            if (x < TILE_WIDTH && y < TILE_WIDTH) {
                z[x][y] = (short) Math.round(coord.z);
                c[x][y] = (byte) coord.k;
            }
        }
    }
}
