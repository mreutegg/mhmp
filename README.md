Minecraft Height Model Plugin
=============================

This is a plugin for the Bukkit Minecraft Mod and contains a `ChunkGenerator`
that models the Canton of Zurich in Minecraft including buildings and trees.

The plugin automatically downloads the required digital terrain model (DTM)
from [GIS-ZH](http://maps.zh.ch/download/hoehen/2014/dtm/xyz/).

Installation
------------

1. Build this plugin with Apache Maven:

```
> mvn clean install
```

2. Build a CraftBukkit Minecraft server mod. Instructions are available on
[spigotmc.org](https://www.spigotmc.org/wiki/buildtools/)
3. Copy `craftbukkit.jar` built by spigotmc to a directory where you will run
the server. E.g. `craftbukkit`.
4. Start the server for the first time with:
```
> java -Xmx2g -jar craftbukkit.jar
```
5. The server will stop after a short while and tell you to accept the EULA.
6. Once you have done that, start the server again. It will create a new world
and write a set of configuration files.
7. Stop the server
8. Create a sub directory `craftbukkit/plugins` and copy the mhmp plugin jar
into this directory.
9. Copy the file `src/test/resources/server.properties` to the
`craftbukkit` folder. It contains a few changes to the default values.
10. Open the file `bukkit.yml` with a text editor and add the following lines
at the end. It will tell the server to use the chunk generator from the plugin
instead of the default minecraft one.
```
worlds:
  world:
    generator: Minecraft-Height-Model-Plugin
```
11. Delete the world initialized by the previous startup:
```
> rm -r world*
```
12. Start the server with the plugin and the laszip4j dependency:
```
> java -Xmx2g -cp $HOME/.m2/repository/com/github/mreutegg/laszip4j/0.1/laszip4j-0.1.jar:craftbukkit-1.11.jar org.bukkit.craftbukkit.Main
```
13. Start a Minecraft client and connect to the server (Multiplayer->Direct Connect).
Use `localhost` as server address.

The default spawn point is outside the railway station of Winterthur.

If you want to spawn at another place, remove the world and add a `spawn` system
property to the command that starts the server. The following default spawn
point is in the city of Zurich on the terrace of ETH.
```
> java -Dspawn=2683677/1247844 -Xmx2g -cp $HOME/.m2/repository/com/github/mreutegg/laszip4j/0.1/laszip4j-0.1.jar:craftbukkit-1.11.jar org.bukkit.craftbukkit.Main
```
And if you fly up a bit the view is like this:
![ETH Zurich](screenshot.png)