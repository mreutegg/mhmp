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

import com.github.mreutegg.laszip4j.lastools.Laszip;

import java.io.File;
import java.io.IOException;

class LAZ {

    static void unzip(File laz, File xyzc) throws IOException {
        String[] args = new String[] {
                "-oparse", "xyzc",
                "-keep_class", "3", "4", "5", "6",
                "-keep_return", "1",
                "-i", laz.getAbsolutePath(),
                "-o", xyzc.getAbsolutePath()};
        Laszip.run(args);
    }
}
