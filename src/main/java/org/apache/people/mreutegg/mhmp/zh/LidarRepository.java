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

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import org.apache.people.mreutegg.mhmp.CoordinateReader;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

class LidarRepository extends TileRepository {

    private static final String BASE_URL = "http://maps.zh.ch/download/hoehen/2014/lidar/";

    private final ExecutorService executor;

    private final Map<String, Future<File>> downloads = Maps.newHashMap();

    LidarRepository(File dir, ExecutorService executor, Logger log) throws IOException {
        super(new File(dir, "lidar"), BASE_URL, log);
        this.executor = executor;
    }

    @Override
    protected int getTileWidth() {
        return 500;
    }

    @Override
    protected Tile readTile(int minX, int minY) throws IOException {
        final String name = nameFromCoordinate(minX, minY);
        File xyzc = new File(downloaded, name + ".xyzc");
        try {
            return tileCache.get(xyzc.getAbsolutePath(), () -> {
                Futures.get(downloadAndUnpack(name), IOException.class);
                triggerDownloadAround(minX, minY);
                log.info("Reading Tile into memory (" + name + ")");
                return new MemoryTile(minX, minY,
                        CoordinateReader.fromFile(xyzc));
            });
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
    }

    private Future<File> downloadAndUnpack(String name) throws IOException {
        Future<File> f;
        synchronized (downloads) {
            // download in progress?
            f = downloads.get(name);
            if (f == null) {
                f = executor.submit(() -> {
                    try {
                        File xyzc = new File(downloaded, name + ".xyzc");
                        if (!xyzc.exists()) {
                            File laz = download(name + ".laz");
                            log.info("Unzipping " + laz.getName());
                            long time = System.currentTimeMillis();
                            LAZ.unzip(laz, xyzc);
                            time = System.currentTimeMillis() - time;
                            log.info("Unzipped " + laz.getName() + " in " + (time / 1000) + " s.");
                        }
                        return xyzc;
                    } finally {
                        synchronized (downloads) {
                            downloads.remove(name);
                        }
                    }
                });
                downloads.put(name, f);
            }
        }
        return f;
    }

    private void triggerDownloadAround(int minX, int minY) throws IOException {
        for (int i = -1; i < 2; i++) {
            int x = minX + i * getTileWidth();
            for (int j = -1; j < 2; j++) {
                int y = minY + j * getTileWidth();
                if (x != minX || y != minY) {
                    downloadAndUnpack(nameFromCoordinate(x, y));
                }
            }
        }
    }
}
