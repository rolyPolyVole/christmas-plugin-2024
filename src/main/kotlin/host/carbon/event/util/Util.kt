package host.carbon.event.util

import host.carbon.event.ChristmasEventPlugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

object Util {
    fun getMaxPlayers(): Int = ChristmasEventPlugin.getInstance().config.getInt("maximum-players")

    fun getLobbySpawnLocation(): Location = ChristmasEventPlugin.getInstance().config.getLocation("spawn-location")!!

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
            contributors[ign] = Pair(contribution, Location(getLobbySpawnLocation().world, x, y, z, 0f, 0F))
        }
        return contributors
    }

    /**
     * Handles players based on their status in the event.
     *
     * @param cameraEntityAction The action to perform on the camera player.
     * @param optedOutAction The action to perform on players who have opted out.
     * @param eventPlayerAction The action to perform on players who are participating in the event.
     *
     * @return A collection of players who are participating in the event. (excludes camera player and opted out players))
     */
    fun handlePlayers(
        cameraEntityAction: ((Player) -> Unit)? = null,
        optedOutAction: ((Player) -> Unit)? = null,
        eventPlayerAction: ((Player) -> Unit)? = null
    ): Collection<Player> {
        val instance = ChristmasEventPlugin.getInstance()

        return Bukkit.getOnlinePlayers().filter { player ->
            val isCameraPlayer = player.uniqueId == instance.cameraPlayer
            val isOptOut = instance.eventController.optOut.contains(player.uniqueId)

            when {
                isCameraPlayer -> {
                    cameraEntityAction?.invoke(player)
                    false
                }

                isOptOut -> {
                    optedOutAction?.invoke(player)
                    false
                }

                else -> {
                    eventPlayerAction?.invoke(player)
                    true
                }
            }
        }
    }
}