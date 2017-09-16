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

import com.google.common.collect.AbstractIterator;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.io.Closeables.close;

public class ASCReader implements Iterable<Coordinate> {

    public static ASCReader fromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        return new ASCReader(file);

    }

    private final File file;

    private ASCReader(File file) {
        this.file = file;
    }

    @Override
    public Iterator<Coordinate> iterator() {
        try {
            String x = null;
            String y = null;
            String cellSize = null;
            String nrows = null;
            BufferedReader reader = openReader();
            try {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        throw new EOFException();
                    }
                    String[] splits = line.split(" ");
                    String firstValue = splits[0];
                    String lastValue = splits[splits.length -1];
                    if ("xllcorner".equals(firstValue)) {
                        x = lastValue;
                    } else if ("yllcorner".equals(firstValue)) {
                        y = lastValue;
                    } else if ("cellsize".equals(firstValue)) {
                        cellSize = lastValue;
                    } else if ("nrows".equals(firstValue)) {
                        nrows = lastValue;
                    }
                    if (x != null && y != null
                            && cellSize != null && nrows != null) {
                        // got everything from the header
                        break;
                    }
                }
            } finally {
                close(reader, true);
            }
            final int nr = Integer.parseInt(nrows);
            final double cs = Double.parseDouble(cellSize);
            final Coordinate bc = new Coordinate(Double.parseDouble(x),
                    Double.parseDouble(y) + nr * cs, 0.0);
            final Iterable<String> lines = getDataLines();

            return new AbstractIterator<Coordinate>() {
                final Iterator<String> zLines = lines.iterator();
                Iterator<Double> zValues = Collections.emptyIterator();
                int lineCounter = -1;
                int rowCounter = -1;
                @Override
                protected Coordinate computeNext() {
                    Double z = null;
                    if (zValues.hasNext()) {
                        z = zValues.next();
                        rowCounter++;
                    } else if (zLines.hasNext()) {
                        zValues = valuesFromLine(zLines.next());
                        z = zValues.hasNext() ? zValues.next() : null;
                        rowCounter = 0;
                        lineCounter++;
                    }
                    if (z == null) {
                        return endOfData();
                    }
                    return new Coordinate(
                            bc.x + rowCounter * cs,
                            bc.y - lineCounter * cs,
                            z);
                }
            };

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Iterator<Double> valuesFromLine(String line) {
        List<String> values = Arrays.asList(line.split(" "));
        return transform(filter(values, s -> !s.trim().isEmpty()), Double::parseDouble).iterator();
    }

    private Iterable<String> getDataLines() throws IOException {
        Iterable<String> lines = () -> {
            try {
                final BufferedReader reader = openReader();
                return new AbstractIterator<String>() {
                    @Override
                    protected String computeNext() {
                        try {
                            String line = reader.readLine();
                            if (line == null) {
                                reader.close();
                                return endOfData();
                            }
                            return line;
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                };
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
        return filter(lines, s -> {
            return s.startsWith(" ");
        });
    }

    private BufferedReader openReader() throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file), "UTF-8"));
    }
}
