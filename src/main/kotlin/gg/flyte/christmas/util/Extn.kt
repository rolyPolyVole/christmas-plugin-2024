package gg.flyte.christmas.util

import gg.flyte.christmas.ChristmasEventPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor

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

fun Component.toLegacyString(): String = LegacyComponentSerializer.legacyAmpersand().serialize(this)

fun eventController() = ChristmasEventPlugin.instance.eventController