package gg.flyte.christmas.visual

import com.google.common.base.Preconditions
import fr.mrmicky.fastboard.adventure.FastBoard
import gg.flyte.christmas.util.eventController
import gg.flyte.christmas.util.style
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

class SidebarManager {
    private val boardRegistry = mutableMapOf<UUID, FastBoard>()
    var dataSupplier = mutableMapOf<UUID, Int>()

    fun update() = Bukkit.getOnlinePlayers().forEach { update(it) }

    fun update(player: Player) {
        val board = boardRegistry.getOrPut(player.uniqueId) { FastBoard(player) }
        board.updateTitle("<red><b>ᴄʜʀɪsᴛᴍᴀs ᴇᴠᴇɴᴛ".style())
        updateLines(player)
    }

    fun remove(player: Player) {
        boardRegistry.remove(player.uniqueId)?.delete()
    }

    fun updateLines(player: Player, addExtra: List<Component>? = null) {
        val board = boardRegistry[player.uniqueId] ?: return

        val lines = mutableListOf(
            currentGameLine(),
            Component.empty(),
            getComponentForPositionAt(0, player),
            getComponentForPositionAt(1, player),
            getComponentForPositionAt(2, player)
        )

        if (!isTop3(player)) {
            lines += listOf(
                Component.empty(),
                "<colour:#b2ffab>ʏᴏᴜʀ sᴄᴏʀᴇ<grey>: ".style(),
                "<grey>${getPlacementByUUID(player.uniqueId)}. <colour:#ebadff><b>ʏᴏᴜ <reset><grey>(${dataSupplier[player.uniqueId]})".style()
            )
        }

        if (addExtra != null) lines += addExtra

        lines += listOf(
            Component.empty(),
            "<light_purple>ꜰʟʏᴛᴇ.ɢɢ/ᴅᴏɴᴀᴛᴇ".style()
        )

        board.lines.clear()
        board.updateLines(lines)
    }

    private fun currentGameLine() = "<aqua>ɢᴀᴍᴇ<grey>: <0>".style(eventController().currentGame?.gameConfig?.smallDisplayName ?: "<grey>ɴᴏɴᴇ".style())


    private fun getComponentForPositionAt(position: Int, player: Player): Component {
        Preconditions.checkArgument(position in 0..2, "Position must be between 0 and 2")

        val placeDefaultComponent = mapOf(
            0 to "<colour:#ffcb1a>➊<grey>:",
            1 to "<colour:#d0d0d0>➋<grey>:",
            2 to "<color:#a39341>➌<grey>:"
        )

        val uniqueIdAtPosition = getUUIDByPlacement(position)
        val nameComponent = when (uniqueIdAtPosition) {
            player.uniqueId -> "<colour:#ebadff><b>ʏᴏᴜ <reset><colour:#fcb3b3>(${dataSupplier[player.uniqueId]})".style()
            null -> "<white>ɴᴏɴᴇ".style()
            else -> "<colour:#f5d6ff>${Bukkit.getOfflinePlayer(uniqueIdAtPosition).name ?: "Unknown"} <reset><colour:#fcb3b3>(${dataSupplier[uniqueIdAtPosition]})".style()
        }

        return "${placeDefaultComponent[position]!!} <0>".style(nameComponent)
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
