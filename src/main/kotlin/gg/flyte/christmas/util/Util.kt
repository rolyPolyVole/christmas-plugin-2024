package gg.flyte.christmas.util

import com.google.common.base.Preconditions
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.minigame.engine.PlayerType
import gg.flyte.christmas.minigame.world.MapSinglePoint
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.sqrt

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
     * Handles players based on their type in the event.
     *
     * @param types The types of players to handle.
     * @param action The action to perform on the player if they match the given types.
     *
     * @return A collection of players who match the given player type.
     */
    fun runAction(vararg types: PlayerType, action: (Player) -> Unit): Collection<Player> {
        return Bukkit.getOnlinePlayers().filter { player ->
            val isCameraPlayer = player.uniqueId == ChristmasEventPlugin.instance.cameraPlayer
            val isOptOut = eventController().optOut.contains(player.uniqueId)

            when {
                PlayerType.PARTICIPANT in types && !isCameraPlayer && !isOptOut -> {
                    action(player)
                    true
                }

                PlayerType.OPTED_OUT in types && isOptOut -> {
                    action(player)
                    true
                }

                PlayerType.CAMERA in types && isCameraPlayer -> {
                    action(player)
                    true
                }

                else -> false
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
    fun getNPCSummaryLocation(position: Int): MapSinglePoint {
        Preconditions.checkArgument(position in 0..2, "Leaderboard only supports positions between 0 and 2")
        return when (position) {
            0 -> return MapSinglePoint(525.5, 215, 548.5)
            1 -> return MapSinglePoint(517.5, 211, 543.5)
            2 -> return MapSinglePoint(533.5, 211, 494.5)
            else -> {
                throw IllegalStateException("Leaderboard only supports positions between 0 and 2")
            }
        }
    }

    /**
     * Applies a Christmas hat to an ItemStack with a random custom model data.
     * @return The corresponding modelled ItemStack.
     */
    fun applyChristmasHat(): ItemStack {
        val randomPair = listOf(Pair(1, "<red>"), Pair(2, "<blue>"), Pair(3, "<green>")).random()

        return ItemStack(Material.LEATHER).apply {
            itemMeta = itemMeta.apply {
                displayName("${randomPair.second}Christmas Hat".style())
                setCustomModelData(randomPair.first)
            }
        }

        runAction(PlayerType.PARTICIPANT, PlayerType.CAMERA) {}
    }

    /**
     * Fills the arena circle (at centre point x: 616, z: 800, radius = 28) with snow blocks at a given level.
     */
    fun fillArena(atLevel: Int, material: Material): List<MapSinglePoint> {
        val locations = mutableListOf<MapSinglePoint>()

        val world = ChristmasEventPlugin.instance.serverWorld
        val centreX = 616
        val centreZ = 800
        val radius = 28

        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val distance = sqrt((x * x + z * z).toDouble())
                if (distance <= radius) {
                    val block = world.getBlockAt(centreX + x, atLevel, centreZ + z)

                    if (block.type != Material.CRIMSON_PLANKS) {
                        block.type = material
                        locations.add(MapSinglePoint(block.x, block.y, block.z))
                    }
                }
            }
        }

        return locations
    }
}
