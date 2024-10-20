package host.carbon.event.minigame.world

import host.carbon.event.ChristmasEventPlugin
import org.bukkit.Location

// (represents a single point)
data class MapSinglePoint(
    val x: Number,
    val y: Number,
    val z: Number,
    val yaw: Number = 0,
    val pitch: Number = 0
) : Location(ChristmasEventPlugin.getInstance().serverWorld, x.toDouble(), y.toDouble(), z.toDouble(), yaw.toFloat(), pitch.toFloat())

// (represents a region between two points)
data class MapRegion(val minPoint: MapSinglePoint, val maxPoint: MapSinglePoint) {

    fun contains(location: Location): Boolean =
        location.x in minPoint.x.toDouble()..maxPoint.x.toDouble() && location.y in minPoint.y.toDouble()..maxPoint.y.toDouble() && location.z in minPoint.z.toDouble()..maxPoint.z.toDouble()

    // Optional helper method to get the center of the region
    fun getCenter(): Location {
        val centerX = (minPoint.x.toDouble() + maxPoint.x.toDouble()) / 2
        val centerY = (minPoint.y.toDouble() + maxPoint.y.toDouble()) / 2
        val centerZ = (minPoint.z.toDouble() + maxPoint.z.toDouble()) / 2
        return Location(minPoint.world, centerX, centerY, centerZ)
    }

    fun randomLocation(): Location {
        val randomX = minPoint.x.toDouble() + Math.random() * (maxPoint.x.toDouble() - minPoint.x.toDouble())
        val randomY = minPoint.y.toDouble() + Math.random() * (maxPoint.y.toDouble() - minPoint.y.toDouble())
        val randomZ = minPoint.z.toDouble() + Math.random() * (maxPoint.z.toDouble() - minPoint.z.toDouble())
        return Location(minPoint.world, randomX, randomY, randomZ)
    }

    fun toSinglePoints(): List<MapSinglePoint> {
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

