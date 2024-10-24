package gg.flyte.christmas.minigame.world

import gg.flyte.christmas.ChristmasEventPlugin
import org.bukkit.Location

/**
 * Represents a single point in the map, with the location's world
 * defined by the server's world defined [ChristmasEventPlugin.serverWorld]
 *
 * @param x The x coordinate
 * @param y The y coordinate
 * @param z The z coordinate
 * @param yaw The yaw rotation (default 0)
 * @param pitch The pitch rotation (default 0)
 *
 * @see Location
 * @return A [org.bukkit.Location] object with the given coordinates
 */
data class MapSinglePoint(
    val x: Number,
    val y: Number,
    val z: Number,
    val yaw: Number = 0,
    val pitch: Number = 0
) : Location(ChristmasEventPlugin.getInstance().serverWorld, x.toDouble(), y.toDouble(), z.toDouble(), yaw.toFloat(), pitch.toFloat())

/**
 * Represents a region in the map, with the location's world
 * defined by the server's world defined [ChristmasEventPlugin.serverWorld]
 *
 * @param minPoint The minimum point of the region
 * @param maxPoint The maximum point of the region
 *
 * @see MapSinglePoint
 */
data class MapRegion(val minPoint: MapSinglePoint, val maxPoint: MapSinglePoint) {

    /**
     * Checks if a location is within the region
     */
    fun contains(location: Location): Boolean =
        location.x in minPoint.x.toDouble()..maxPoint.x.toDouble() && location.y in minPoint.y.toDouble()..maxPoint.y.toDouble() && location.z in minPoint.z.toDouble()..maxPoint.z.toDouble()

    /**
     * Gets the center of the region
     *
     * @return The center of the region as an [org.bukkit.Location] object
     */
    fun getCenter(): Location {
        val centerX = (minPoint.x.toDouble() + maxPoint.x.toDouble()) / 2
        val centerY = (minPoint.y.toDouble() + maxPoint.y.toDouble()) / 2
        val centerZ = (minPoint.z.toDouble() + maxPoint.z.toDouble()) / 2
        return Location(minPoint.world, centerX, centerY, centerZ)
    }

    /**
     * Gets a random location within the region
     *
     * @return A random location within the region as an [org.bukkit.Location] object
     */
    fun randomLocation(): Location {
        val randomX = minPoint.x.toDouble() + Math.random() * (maxPoint.x.toDouble() - minPoint.x.toDouble())
        val randomY = minPoint.y.toDouble() + Math.random() * (maxPoint.y.toDouble() - minPoint.y.toDouble())
        val randomZ = minPoint.z.toDouble() + Math.random() * (maxPoint.z.toDouble() - minPoint.z.toDouble())
        return Location(minPoint.world, randomX, randomY, randomZ)
    }

    /**
     * Converts the region to a list of single block locations
     *
     * @return A list of [MapSinglePoint] objects representing each block in the region
     */
    fun toSingleBlockLocations(): List<MapSinglePoint> {
        val points = mutableListOf<MapSinglePoint>()
        for (x in minPoint.x.toInt()..maxPoint.x.toInt()) {
            for (y in minPoint.y.toInt()..maxPoint.y.toInt()) {
                for (z in minPoint.z.toInt()..maxPoint.z.toInt()) {
                    points.add(MapSinglePoint(x, y, z))
                }
            }
        }
        return points
    }
}

