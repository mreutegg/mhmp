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

public interface LidarClassification {

    int GROUND = 2;

    int LOW_VEGETATION = 3;

    int MEDIUM_VEGETATION = 4;

    int HIGH_VEGETATION = 5;

    int BUILDING = 6;

    int LOW_POINT = 7;

    int WATER = 9;

    int RAIL = 10;

    int ROAD_SURFACE = 11;

}
