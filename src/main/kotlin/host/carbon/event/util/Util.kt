package host.carbon.event.util

import host.carbon.event.ChristmasEventPlugin
import host.carbon.event.minigame.world.MapSinglePoint
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.spigotmc.SpigotConfig.config

object Util {
    data class Contributor(val ign: String, val contribution: String, val location: MapSinglePoint)

    fun getMaxPlayers(): Int = ChristmasEventPlugin.getInstance().config.getInt("maximum-players")

    fun getEventContributors(): List<Contributor> = config.getStringList("contributors").map { contributor ->
        val (ign, contribution, coords) = contributor.subSequence(1, contributor.length - 1).split("><")
        val (x, y, z) = coords.split(", ").map(String::toDouble)

        Contributor(ign, contribution, MapSinglePoint(x, y, z))
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