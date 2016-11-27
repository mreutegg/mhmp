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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

abstract class TileRepository {

    private final String baseURL;

    final Logger log;

    final File downloaded;

    Cache<String, Tile> tileCache;

    TileRepository(File dir, String baseURL, Logger log) throws IOException {
        this.log = log;
        this.baseURL = baseURL;
        this.downloaded = mkdirs(dir);
        this.tileCache = CacheBuilder.newBuilder()
                .maximumSize(16).build();
    }

    abstract protected int getTileWidth();

    abstract protected Tile readTile(int minX, int minY) throws IOException;

    Tile getTile(int x, int y) throws IOException {
        int minX = getTileMin(x);
        int minY = getTileMin(y);
        Tile t = readTile(minX, minY);
        if (t == Tile.NULL) {
            t = null;
        }
        return t;
    }

    String nameFromCoordinate(int x, int y) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTileMin(x) / 100);
        sb.append("_");
        sb.append(getTileMin(y) / 100);
        return sb.toString();
    }

    File download(String name) throws IOException {
        File file = new File(downloaded, name);
        if (!file.exists()) {
            log.info("Downloading " + name);
            long time = System.currentTimeMillis();
            URLConnection c = new URL(baseURL + name).openConnection();
            try (InputStream in = c.getInputStream()) {
                File tmp = new File(file.getParent(), file.getName() + ".tmp");
                if (tmp.exists() && ! tmp.delete()) {
                    throw new IOException("Cannot delete " + tmp.getAbsolutePath());
                }
                try (OutputStream out = new FileOutputStream(tmp)) {
                    ByteStreams.copy(in, out);
                }
                if (!tmp.renameTo(file)) {
                    throw new IOException("Cannot rename " + tmp.getAbsolutePath());
                }
            }
            time = System.currentTimeMillis() - time;
            log.info("Downloaded " + name + " in " + (time / 1000) + " s.");
        }
        return file;
    }

    static void unzip(File zip) throws IOException {
        try (ZipFile file = new ZipFile(zip)) {
            file.stream().forEach(o -> {
                File tmp = new File(zip.getParent(), o.getName() + ".tmp");
                try {
                    try (InputStream in = file.getInputStream(o)) {
                        try (OutputStream out = new FileOutputStream(tmp)) {
                            ByteStreams.copy(in, out);
                        }
                    }
                    if (!tmp.renameTo(new File(zip.getParent(), o.getName()))) {
                        throw new IOException("Cannot rename file" + tmp.getAbsolutePath());
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    private static File mkdirs(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create directory " + dir.getAbsolutePath());
        }
        return dir;
    }

    private int getTileMin(int value) {
        return (int) ((long) value) / getTileWidth() * getTileWidth();
    }
}
