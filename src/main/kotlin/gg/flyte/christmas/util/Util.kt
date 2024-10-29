package gg.flyte.christmas.util

import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.minigame.world.MapSinglePoint
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object Util {
    data class Contributor(val playerName: String, val contribution: String, val location: MapSinglePoint)

    fun getEventContributors(): List<Contributor> {
        val config = ChristmasEventPlugin.instance.config

        return config.getStringList("contributors").map { contributor ->
            val (ign, contribution, coords) = contributor.substring(1, contributor.length - 1).split("><")
            val (x, y, z) = coords.split(",").map { it.trim().toDouble() }
            Contributor(ign, contribution, MapSinglePoint(x, y, z))
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

    @Throws(IOException::class)
    fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        // append = false
        FileOutputStream(file, false).use { outputStream ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        }
    }
}
