package host.carbon.event.util

import host.carbon.event.ChristmasEventPlugin
import org.bukkit.Location

class Util {
    companion object {
        fun getMaxPlayers(): Int = ChristmasEventPlugin.getInstance().config.getInt("maximum-players")

        fun getSpawnLocation(): Location = ChristmasEventPlugin.getInstance().config.getLocation("spawn-location")!!

        fun getEventContributors(): Map<String, Pair<String, Location>> {
            val contributors = mutableMapOf<String, Pair<String, Location>>()
            ChristmasEventPlugin.getInstance().config.getStringList("contributors").forEach { contributor ->
                val split = contributor.split("><")
                val ign = split[0].removePrefix("<")
                val contribution = split[1]
                val location = split[2].removeSuffix(">").split(", ")
                val x = location[0].toDouble()
                val y = location[1].toDouble()
                val z = location[2].toDouble()
                contributors[ign] = Pair(contribution, Location(getSpawnLocation().world, x, y, z))
            }
            return contributors
        }
    }
}