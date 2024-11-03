package gg.flyte.christmas.listeners

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import dev.shreyasayyengar.menuapi.menu.MenuItem
import dev.shreyasayyengar.menuapi.menu.StandardMenu
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.util.CameraSequence
import gg.flyte.christmas.util.eventController
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.hidePlayer
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.extension.showPlayer
import gg.flyte.twilight.extension.toComponent
import gg.flyte.twilight.scheduler.delay
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCombustEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import kotlin.math.ceil

class HousekeepingEventListener : Listener {
    init {
        event<ServerListPingEvent> {
            // TODO finish sponsors
            val footer = Component.text("       ")
                .append(MiniMessage.miniMessage().deserialize("<gradient:#fffdb8:#ffffff>ᴄᴀʀʙᴏɴ.ʜᴏꜱᴛ</gradient>"))
                .append(text(" • ", NamedTextColor.WHITE))
                .append(text("ʙᴜɪʟᴛʙʏʙɪᴛ.ᴄᴏᴍ ", TextColor.color(72, 133, 190)))

            val motd = Component.empty()
                .append(text("        ||||||  ", NamedTextColor.WHITE, TextDecoration.BOLD, TextDecoration.OBFUSCATED))
                .append("<gradient:#F396E1:#FFFFFF>ꜰʟʏᴛ</gradient><gradient:#FFFFFF:#FFFFFF>ᴇ</gradient> ".toComponent())
                .append("<gradient:#51F651:#FAEDCB>ᴄʜʀɪsᴛᴍ</gradient><gradient:#FAEDCB:#D12020>ᴀs ᴇᴠ</gradient><gradient:#D12020:#D12020>ᴇɴᴛ</gradient>".toComponent())
                .append(text("  ||||||", NamedTextColor.WHITE, TextDecoration.BOLD, TextDecoration.OBFUSCATED))
                .append(text("\n"))
                .append(footer)

            motd(motd)
        }

        event<AsyncChatEvent> {
            renderer(ChatRenderer.viewerUnaware { player, displayName, message ->
                val finalRender = text()

                if (player.isOp) finalRender.append(text("ѕᴛᴀꜰꜰ ", NamedTextColor.RED, TextDecoration.BOLD))

                finalRender.append(text(player.name, TextColor.color(209, 209, 209)))
                    .append(text(" » ", NamedTextColor.GRAY))
                    .append(text(signedMessage().message(), NamedTextColor.WHITE))
                    .build()
            })
        }

        event<PlayerJoinEvent>(priority = EventPriority.LOWEST) {
            fun applyChristmasHat(modelData: Int): ItemStack {
                val map = mapOf(
                    1 to NamedTextColor.RED,
                    2 to NamedTextColor.GREEN,
                    3 to NamedTextColor.BLUE
                )

                return ItemStack(Material.LEATHER).apply {
                    itemMeta = itemMeta.apply {
                        displayName(text("Christmas Hat", map[modelData]))
                        setCustomModelData(modelData)
                    }
                }
            }

            fun applyTag(player: Player) {
                player.scoreboard = ChristmasEventPlugin.instance.scoreBoardTab
                ChristmasEventPlugin.instance.scoreBoardTab.getTeam(if (player.isOp) "a. staff" else "b. player")?.addEntry(player.name)
            }

            joinMessage(null)

            player.apply {
//                TODO change URL/configure pack (uncomment when works)
//                async {
//                    RemoteFile("https://github.com/flytegg/ls-christmas-rp/releases/latest/download/RP.zip").apply {
//                    println("RP Hash = $hash")
//                    setResourcePack(url, hash, true)
//                    }
//                }

                gameMode = GameMode.ADVENTURE

                playSound(Sound.ENTITY_PLAYER_LEVELUP)

                inventory.clear()
                inventory.helmet = applyChristmasHat((1..3).random())

                eventController().onPlayerJoin(this)
                eventController().songPlayer?.addPlayer(this)
                ChristmasEventPlugin.instance.worldNPCs.forEach { it.spawnFor(this) }

                applyTag(this)
            }

            val header = text()
                .append(text("❆ ", TextColor.color(255, 161, 161)))
                .append(text("ᴄʜʀɪsᴛᴍᴀs ᴄʜᴀʀɪᴛʏ ᴇᴠᴇɴᴛ", TextColor.color(170, 230, 135)))
                .append(text(" ❆", TextColor.color(255, 161, 161)))
                .append(text("\n"))
                .append(text("\n(${Bukkit.getOnlinePlayers().size} ᴘʟᴀʏᴇʀꜱ)", NamedTextColor.GRAY))

            // TODO finish sponsors
            val footer = Component.text("\nꜰʟʏᴛᴇ.ɢɢ/ᴅᴏɴᴀᴛᴇ\n\n", NamedTextColor.LIGHT_PURPLE)
                .append(MiniMessage.miniMessage().deserialize(" <gradient:#ff80e8:#ffffff>ꜰʟʏᴛᴇ.ɢɢ</gradient>"))
                .append(text(" • ", NamedTextColor.WHITE))
                .append(MiniMessage.miniMessage().deserialize("<gradient:#fffdb8:#ffffff>ᴄᴀʀʙᴏɴ.ʜᴏꜱᴛ</gradient>"))
                .append(text(" • ", NamedTextColor.WHITE))
                .append(text("ʙᴜɪʟᴛʙʏʙɪᴛ.ᴄᴏᴍ ", TextColor.color(72, 133, 190)))
                .append(text("\n"))
            Bukkit.getOnlinePlayers().forEach { it.sendPlayerListHeaderAndFooter(header, footer) }

            eventController().points.putIfAbsent(player.uniqueId, 0)
            eventController().sidebarManager.update()
        }

        event<PlayerQuitEvent> {
            quitMessage(null)
            delay(1) { eventController().onPlayerQuit(player) } // getOnlinePlayers does not update until the next tick
        }

        event<PlayerMoveEvent> {
            // hide players when they are close to NPCs, they obstruct the view
            val locations = ChristmasEventPlugin.instance.worldNPCs.map {
                SpigotConversionUtil.toBukkitLocation(
                    ChristmasEventPlugin.instance.serverWorld,
                    it.npc.location
                )
            }

            val hide = locations.any { it.distance(player.location) < 3 }
            if (hide) player.hidePlayer() else player.showPlayer()

        }

        // TODO uncomment when pack works
//        event<PlayerResourcePackStatusEvent> {
//            if (status == PlayerResourcePackStatusEvent.Status.ACCEPTED || status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) return@event
//            player.kick("&cYou &f&nmust&c accept the resource pack to play on this server!".asComponent())
//        }

        event<PlayerDropItemEvent> { isCancelled = true }

        event<PlayerSwapHandItemsEvent> { isCancelled = true }

        event<PlayerInteractEvent> {
            if (item?.type == Material.COMPASS) {
                openSpectateMenu(player)
            } // NOTE: if other games use compasses, this method will need adjustment.
        }

        event<PlayerStopSpectatingEntityEvent> {
            if (CameraSequence.ACTIVE_CAMERAS.contains(spectatorTarget.uniqueId)) {
                isCancelled = true
                return@event
            }

            val currentGame = eventController().currentGame
            if (currentGame?.spectateEntities?.values?.map { it.uniqueId }?.contains(spectatorTarget.uniqueId) == true) {
                player.teleport(currentGame.gameConfig.spectatorSpawnLocations.random())
                player.gameMode = GameMode.ADVENTURE
            }
        }

        event<FoodLevelChangeEvent> { isCancelled = true }

        event<InventoryOpenEvent> {
            if (inventory.type == InventoryType.BARREL) isCancelled = true
        }

        event<InventoryClickEvent> {
            if (clickedInventory !is PlayerInventory) return@event
            isCancelled = slotType == InventoryType.SlotType.ARMOR
            if (currentItem?.type == Material.COMPASS) {
                openSpectateMenu(whoClicked as Player) // NOTE: if other games use compasses, this method will need adjustment.
            }
        }

        event<EntityCombustEvent> { if (entity is Player) isCancelled = true }

        event<EntityDamageEvent> { isCancelled = true /* TODO examine later*/ }
    }

    private fun openSpectateMenu(player: Player) {
        var locationSize = eventController().currentGame!!.gameConfig.spectatorCameraLocations.size

        var standardMenu = StandardMenu(
            "Spectate Map:",
            ceil(
                locationSize.div(9.0)
            ).toInt() * 9
        )

        for (i in 0 until locationSize) {

            val menuItem = MenuItem(Material.PLAYER_HEAD)
                .setName("Spectate Point $i")
                .setSkullTexture("66f88107041ff1ad84b0a4ae97298bd3d6b59d0402cbc679bd2f77356d454bc4")
                .onClick { whoClicked, itemStack, clickType, inventoryClickEvent ->
                    val requestedCameraEntity = eventController().currentGame!!.spectateEntities[i]
                    whoClicked.gameMode = GameMode.SPECTATOR
                    whoClicked.spectatorTarget = requestedCameraEntity
                    whoClicked.closeInventory()
                }

            standardMenu.setItem(i, menuItem)
        }

        standardMenu.open(false, player)
    }
}
