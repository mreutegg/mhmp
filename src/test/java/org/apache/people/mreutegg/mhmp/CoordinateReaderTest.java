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

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CoordinateReaderTest {

    @Ignore
    @Test
    public void readFile() throws IOException {
        CoordinateReader reader = CoordinateReader.fromFile(new File("DHM200.xyz"));
        int count = 0;
        for (Coordinate coord : reader) {
            if (++count % 1000 == 0) {
                System.out.println(coord);
            }
        }
    }

    @Ignore
    @Test
    public void readASCFile() throws IOException {
        Iterable<Coordinate> reader = ASCReader.fromFile(new File("7020_2640.asc"));
        int count = 0;
        for (Coordinate coord : reader) {
            if (++count % 1000 == 0) {
                System.out.println(coord);
            }
        }
    }

    @Ignore
    @Test
    public void convertASCToXYZ() throws IOException {
        Iterable<Coordinate> reader = ASCReader.fromFile(new File("7010_2640.asc"));
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(new File("7010_2640.xyz")), "UTF-8"));
        NumberFormat nf = new DecimalFormat("#.##");
        try {
            for (Coordinate coord : reader) {
                writer.write(nf.format(coord.x));
                writer.write(" ");
                writer.write(nf.format(coord.y));
                writer.write(" ");
                writer.write(nf.format(coord.z));
                writer.write(" ");
                writer.write(String.valueOf(coord.k));
                writer.write("\n");
            }
        } finally {
            writer.close();
        }

    }

}
