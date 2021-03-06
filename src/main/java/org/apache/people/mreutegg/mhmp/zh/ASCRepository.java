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

import org.apache.people.mreutegg.mhmp.ASCReader;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

class ASCRepository extends TileRepository {

    private static final String BASE_URL = "http://maps.zh.ch/download/hoehen/2014/dtm/asc/";

    ASCRepository(File dir, Logger log) throws IOException {
        super(new File(dir, "asc"), BASE_URL, log);
    }

    protected int getTileWidth() {
        return 1000;
    }

    protected Tile readTile(int minX, int minY) throws IOException {
        final String name = nameFromCoordinate(minX, minY);
        File asc = new File(downloaded, name + ".asc");
        try {
            return tileCache.get(asc.getAbsolutePath(), () -> {
                if (!asc.exists()) {
                    File ascZip = download(name + ".asc.zip");
                    log.info("Unzipping " + ascZip.getName());
                    unzip(ascZip);
                }
                log.info("Reading Tile into memory (" + name + ")");
                return new MemoryTile(minX, minY,
                        ASCReader.fromFile(asc));
            });
        } catch (ExecutionException e) {
            tileCache.put(asc.getAbsolutePath(), Tile.NULL);
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException(e.getCause());
            }
        }
    }
}
