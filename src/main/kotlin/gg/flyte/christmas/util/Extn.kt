package gg.flyte.christmas.util

import gg.flyte.christmas.ChristmasEventPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.time.Duration

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

fun Player.title(title: Component, subtitle: Component, times: Times? = null) = this.showTitle(Title.title(title, subtitle, times))

fun titleTimes(fadeIn: Duration, stay: Duration, fadeOut: Duration): Times = Times.times(fadeIn, stay, fadeOut)

fun eventController() = ChristmasEventPlugin.instance.eventController