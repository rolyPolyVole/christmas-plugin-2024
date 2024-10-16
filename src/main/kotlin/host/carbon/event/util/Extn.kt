package host.carbon.event.util

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun String.asComponent() = LegacyComponentSerializer.legacyAmpersand().deserialize(this)