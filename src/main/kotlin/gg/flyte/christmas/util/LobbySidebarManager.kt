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

class LobbySidebarManager() {
    private val boardRegistry = mutableMapOf<UUID, FastBoard>()

    val placeDefaultComponent = mapOf<Int, Component>(
        0 to Component.text()
            .color(TextColor.color(255, 203, 26))
            .append(Component.text("➊"))
            .append(Component.text(": ", NamedTextColor.GRAY))
            .build(),
        1 to Component.text()
            .color(TextColor.color(208, 208, 208))
            .append(Component.text("➋"))
            .append(Component.text(": ", NamedTextColor.GRAY))
            .build(),
        2 to Component.text()
            .color(TextColor.color(177, 143, 87))
            .append(Component.text("➌"))
            .append(Component.text(": ", NamedTextColor.GRAY))
            .build()
    )

    fun update(player: Player) {
        if (!boardRegistry.containsKey(player.uniqueId)) {
            boardRegistry[player.uniqueId] = FastBoard(player)
        }

        boardRegistry[player.uniqueId]!!.updateTitle(Component.text("ᴄʜʀɪsᴛᴍᴀs ᴇᴠᴇɴᴛ", NamedTextColor.RED, TextDecoration.BOLD))
        updateLines(player)
    }

    private fun updateLines(player: Player) {
        val board = boardRegistry[player.uniqueId] ?: return

        val lines = mutableListOf<Component>(
            currentGameLine(),
            Component.empty(),
            getPlace(0, player),
            getPlace(1, player),
            getPlace(2, player),
        )

        if (isTop3(player)) {
            lines.add(
                Component.text("ʏᴏᴜʀ sᴄᴏʀᴇ", NamedTextColor.YELLOW).append(Component.text(": ", NamedTextColor.GRAY))
            )

            lines.addAll(
                listOf(
                    Component.empty()
                        .append(Component.text("${ChristmasEventPlugin.instance.eventController.getPlace(player.uniqueId)}.", NamedTextColor.GRAY))
                        .append(Component.text(" ʏᴏᴜ", NamedTextColor.RED, TextDecoration.BOLD))
                )
            )
        }

        lines.addAll(
            listOf(
                Component.empty(),
                Component.text("ꜰʟʏᴛᴇ.ɢɢ/ᴅᴏɴᴀᴛᴇ", NamedTextColor.LIGHT_PURPLE)
            )
        )

        board.updateLines(lines)
    }

    private fun currentGameLine(): Component {
        val titleBase = Component.text()
            .append(Component.text("ɢᴀᴍᴇ", NamedTextColor.AQUA))
            .append(Component.text(": ", NamedTextColor.GRAY))
            .append(
                ChristmasEventPlugin.instance.eventController.currentGame?.gameConfig?.displayName ?: Component.text(
                    "ɴᴏɴᴇ",
                    NamedTextColor.WHITE
                )
            ).build()

        return titleBase
    }

    private fun getPlace(position: Int, player: Player): Component {
        Preconditions.checkArgument(position in 0..2, "Position must be between 0 and 2")

        var uniqueId = ChristmasEventPlugin.instance.eventController.getScorePosition(position)
        val base = placeDefaultComponent[position]!!

        if (uniqueId == null) {
            base.append(Component.text("ɴᴏɴᴇ", NamedTextColor.WHITE))
        } else if (uniqueId == player.uniqueId) {
            base.append(Component.text("ʏᴏᴜ", NamedTextColor.RED, TextDecoration.BOLD))
        } else {
            var content = Bukkit.getOfflinePlayer(uniqueId).name!!
            base.append(Component.text(content, NamedTextColor.WHITE))
        }

        println(uniqueId)

        return base
    }

    private fun isTop3(player: Player): Boolean {
        var eventController = ChristmasEventPlugin.instance.eventController
        return eventController.getScorePosition(0) == player.uniqueId ||
                eventController.getScorePosition(1) == player.uniqueId ||
                eventController.getScorePosition(2) == player.uniqueId
    }
}