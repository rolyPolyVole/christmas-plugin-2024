package host.carbon.event.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.UUID

fun String.asComponent() = LegacyComponentSerializer.legacyAmpersand().deserialize(this)

fun String.colourise(): String {
    val pattern = Regex("#[a-fA-F0-9]{6}")
    var message = this

    pattern.findAll(this).forEach { matchResult ->
        val hexCode = matchResult.value
        val replaceSharp = hexCode.replace("#", "x")

        val builder = StringBuilder()
        replaceSharp.forEach { c ->
            builder.append("&").append(c)
        }

        message = message.replace(hexCode, builder.toString())
    }

    return ChatColor.translateAlternateColorCodes('&', message)
}

fun Component.toLegacyString(): String {
    return LegacyComponentSerializer.legacyAmpersand().serialize(this)
}

fun UUID.toBukkitPlayer(): Player? {
    Bukkit.getPlayer(this)?.let {
        return it
    }
    return null
}