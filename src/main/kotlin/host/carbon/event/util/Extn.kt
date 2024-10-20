package host.carbon.event.util

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.UUID

fun String.asComponent() = LegacyComponentSerializer.legacyAmpersand().deserialize(this)

fun String.colourise(): String = ChatColor.translateAlternateColorCodes('&', this)

fun UUID.toBukkitPlayer(): Player? {
    Bukkit.getPlayer(this)?.let {
        return it
    }
    return null
}