package gg.flyte.christmas.util

import com.google.common.base.Preconditions
import fr.mrmicky.fastboard.adventure.FastBoard
import gg.flyte.christmas.ChristmasEventPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class SidebarManager {
    private val boardRegistry = mutableMapOf<UUID, FastBoard>()
    var dataSupplier = mutableMapOf<UUID, Int>()

    private val placeDefaultComponent = mapOf(
        0 to createPlaceComponent("➊", TextColor.color(255, 203, 26)),
        1 to createPlaceComponent("➋", TextColor.color(208, 208, 208)),
        2 to createPlaceComponent("➌", TextColor.color(163, 147, 65))
    )

    fun update() = Bukkit.getOnlinePlayers().forEach { update(it) }

    fun update(player: Player) {
        val board = boardRegistry.getOrPut(player.uniqueId) { FastBoard(player) }
        board.updateTitle(Component.text("ᴄʜʀɪsᴛᴍᴀs ᴇᴠᴇɴᴛ", NamedTextColor.RED, TextDecoration.BOLD))
        updateLines(player)
    }

    fun remove(player: Player) {
        boardRegistry.remove(player.uniqueId)?.delete()
    }

    private fun createPlaceComponent(symbol: String, color: TextColor): Component {
        return Component.text()
            .color(color)
            .append(Component.text(symbol))
            .append(Component.text(": ", NamedTextColor.GRAY))
            .build()
    }

    fun updateLines(player: Player, addExtra: List<Component>? = null) {
        val board = boardRegistry[player.uniqueId] ?: return

        val lines = mutableListOf<Component>(
            currentGameLine(),
            Component.empty(),
            getComponentForPositionAt(0, player),
            getComponentForPositionAt(1, player),
            getComponentForPositionAt(2, player)
        )

        if (!isTop3(player)) {
            lines += listOf(
                Component.empty(),
                Component.text("ʏᴏᴜʀ sᴄᴏʀᴇ", TextColor.color(178, 255, 171)).append(Component.text(": ", NamedTextColor.GRAY)),
                Component.text()
                    .append(
                        Component.text(
                            "${getPlacementByUUID(player.uniqueId)}.",
                            NamedTextColor.GRAY
                        )
                    )
                    .append(Component.text(" ʏᴏᴜ", TextColor.color(235, 173, 255), TextDecoration.BOLD))
                    .build()
            )
        }

        if (addExtra != null) lines += addExtra

        lines += listOf(
            Component.empty(),
            Component.text("ꜰʟʏᴛᴇ.ɢɢ/ᴅᴏɴᴀᴛᴇ", NamedTextColor.LIGHT_PURPLE)
        )

        board.lines.clear()
        board.updateLines(lines)
    }

    private fun currentGameLine(): Component {
        val eventController = ChristmasEventPlugin.instance.eventController
        val gameName = eventController.currentGame?.gameConfig?.smallDisplayName ?: Component.text("ɴᴏɴᴇ", NamedTextColor.GRAY)

        return Component.text()
            .append(Component.text("ɢᴀᴍᴇ", NamedTextColor.AQUA))
            .append(Component.text(": ", NamedTextColor.GRAY))
            .append(gameName)
            .build()
    }

    private fun getComponentForPositionAt(position: Int, player: Player): Component {
        Preconditions.checkArgument(position in 0..2, "Position must be between 0 and 2")

        val uniqueIdAtPosition = getUUIDByPlacement(position)
        val base = Component.text().append(placeDefaultComponent[position]!!)

        return when (uniqueIdAtPosition) {
            null -> base.append(Component.text("ɴᴏɴᴇ", NamedTextColor.WHITE)).build()

            player.uniqueId ->
                base.append(Component.text("ʏᴏᴜ", TextColor.color(235, 173, 255), TextDecoration.BOLD))
                    .append(Component.text(" (${dataSupplier[player.uniqueId]})", TextColor.color(252, 179, 179)))
                    .build()

            else -> {
                val playerName = Bukkit.getOfflinePlayer(uniqueIdAtPosition).name ?: "Unknown"
                base.append(Component.text(playerName, TextColor.color(245, 214, 255)))
                    .append(Component.text(" (${dataSupplier[uniqueIdAtPosition]})", TextColor.color(252, 179, 179)))
                    .build()
            }
        }
    }

    private fun isTop3(player: Player): Boolean = (0..2).any { getUUIDByPlacement(it) == player.uniqueId }

    private fun getUUIDByPlacement(position: Int): UUID? {
        if (position >= dataSupplier.size) return null
        return dataSupplier.entries.sortedByDescending { it.value }[position].key
    }

    private fun getPlacementByUUID(uuid: UUID): Int {
        val sorted = dataSupplier.entries.sortedByDescending { it.value }
        return sorted.indexOfFirst { it.key == uuid } + 1
    }
}
