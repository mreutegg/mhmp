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

import com.google.common.io.Closer;
import org.apache.people.mreutegg.mhmp.zh.WhereAmICommandExecutor;
import org.apache.people.mreutegg.mhmp.zh.ZHGenerator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.UncheckedIOException;

public class MHMPlugin extends JavaPlugin {

    private final Closer closer = Closer.create();

    @Override
    public void onEnable() {
        this.getCommand("render").setExecutor(new RenderCommandExecutor(this));
        this.getCommand("whereami").setExecutor(new WhereAmICommandExecutor());
    }

    @Override
    public void onDisable() {
        try {
            closer.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        try {
            ZHGenerator generator = new ZHGenerator(this);
            closer.register(generator);
            return generator;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
