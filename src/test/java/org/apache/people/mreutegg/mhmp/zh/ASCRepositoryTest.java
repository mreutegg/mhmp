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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class ASCRepositoryTest {

    private ASCRepository repo;

    @Before
    public void setup() throws IOException {
        repo = new ASCRepository(
                new File("target"), Logger.getLogger(getClass().getName())
        );
    }

    @Test
    public void nameForCoordinate() throws IOException {
        String name = repo.nameFromCoordinate(2702742, 1264993);
        assertEquals("27020_12640", name);
    }

    @Ignore
    @Test
    public void getTile() throws IOException {
        Tile tile = repo.getTile(2702742, 1264993);
        System.out.println(tile);
    }
}
