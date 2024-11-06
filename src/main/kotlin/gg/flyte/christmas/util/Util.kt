package gg.flyte.christmas.util

import com.google.common.base.Preconditions
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.minigame.world.MapSinglePoint
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object Util {
    data class Contributor(val uniqueId: UUID, val contribution: String, val location: MapSinglePoint)

    fun getEventContributors(): List<Contributor> {
        val config = ChristmasEventPlugin.instance.config

        return config.getStringList("contributors").map { contributor ->
            val (ign, contribution, coords) = contributor.substring(1, contributor.length - 1).split("><")
            val (x, y, z) = coords.split(",").map { it.trim().toDouble() }
            Contributor(UUID.fromString(ign), contribution, MapSinglePoint(x, y, z))
        }
    }

    /**
     * Handles players based on their status in the event.
     *
     * @param cameraEntityAction The action to perform on the camera player.
     * @param optedOutAction The action to perform on players who have opted out.
     * @param eventPlayerAction The action to perform on players who are participating in the event.
     *
     * @return A collection of players who are **participating** in the event. (excludes camera player and opted out players)
     */
    fun handlePlayers(
        cameraEntityAction: ((Player) -> Unit)? = null,
        optedOutAction: ((Player) -> Unit)? = null,
        eventPlayerAction: ((Player) -> Unit)? = null
    ): Collection<Player> {
        val instance = ChristmasEventPlugin.instance

        return Bukkit.getOnlinePlayers().filter { player ->
            val isCameraPlayer = player.uniqueId == instance.cameraPlayer
            val isOptOut = eventController().optOut.contains(player.uniqueId)

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

    /**
     * Gets the location of where a summary NPC should be, given the placement position.
     * A summary NPC is an NPC that represents a player's position on the leaderboard **after an individual minigame**.
     * @param position The position on the leaderboard. (zero-indexed) (first, second, third)
     * @return The location of the NPC.
     *
     * @throws IllegalArgumentException If the position is not between 0 and 2.
     */
    // TODO write in actual locations when map is finished.
    fun getNPCSummaryLocation(position: Int): MapSinglePoint {
        Preconditions.checkArgument(position in 0..2, "Leaderboard only supports positions between 0 and 2")

        return when (position) {
            0 -> return MapSinglePoint(633, 216, 489, 90, 0)
            1 -> return MapSinglePoint(633, 214, 484, 90, 0)
            2 -> return MapSinglePoint(633, 213, 494, 90, 0)
            else -> {
                throw IllegalStateException("Leaderboard only supports positions between 0 and 2")
            }
        }
    }
}
