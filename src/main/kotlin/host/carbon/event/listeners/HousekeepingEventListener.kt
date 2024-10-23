package host.carbon.event.listeners

import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.RemoteFile
import gg.flyte.twilight.extension.playSound
import host.carbon.event.ChristmasEventPlugin
import host.carbon.event.util.asComponent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class HousekeepingEventListener : Listener {
    init {
        event<ServerListPingEvent> {
            motd("TODO!".asComponent()) // TODO add motd
        }

        event<AsyncPlayerPreLoginEvent> {
            // TODO remove if no player limit \o/
        }

        event<PlayerJoinEvent>(priority = EventPriority.LOWEST) {
            joinMessage(null)
            player.apply {
                RemoteFile("https://github.com/flytegg/ls-christmas-rp/releases/latest/download/RP.zip").apply { // TODO change URL/configure pack
                    println("RP Hash = $hash")
                    setResourcePack(url, hash, true)
                }

                playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP)

                inventory.clear()
                inventory.helmet = when ((1..3).random()) {
                    1 -> applyChristmasHat(NamedTextColor.RED, 1)
                    2 -> applyChristmasHat(NamedTextColor.BLUE, 2)
                    else -> applyChristmasHat(NamedTextColor.GREEN, 3)
                }

                ChristmasEventPlugin.getInstance().eventController.onPlayerJoin(this)
                ChristmasEventPlugin.getInstance().eventController.songPlayer?.addPlayer(this)
            }

        }

        event<EntityCombustEvent> {
            if (entity is Player) isCancelled = true
        }

        event<PlayerQuitEvent> {
            quitMessage(null)
            ChristmasEventPlugin.getInstance().eventController.onPlayerQuit(player)
            // TODO
        }

        event<PlayerResourcePackStatusEvent> {
            if (status == PlayerResourcePackStatusEvent.Status.ACCEPTED || status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) return@event
            player.kick("&cYou &f&nmust &caccept the resource pack to play on this server!".asComponent())
        }

        event<EntityDamageEvent> {
            isCancelled = true // TODO examine later
        }

        event<FoodLevelChangeEvent> {
            isCancelled = true
        }

        event<InventoryClickEvent> {
            if (clickedInventory !is PlayerInventory) return@event
            isCancelled = slotType == InventoryType.SlotType.ARMOR
        }
    }

    private fun applyChristmasHat(color: NamedTextColor, modelData: Int): ItemStack {
        return ItemStack(Material.LEATHER).apply {
            itemMeta = itemMeta.apply {
                displayName(text("Christmas Hat", color))
                setCustomModelData(modelData)
            }
        }
    }
}

// TODO prevent players from exiting spectator mode when preview.