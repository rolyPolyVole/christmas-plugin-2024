package gg.flyte.christmas.listeners

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import dev.shreyasayyengar.menuapi.menu.MenuItem
import dev.shreyasayyengar.menuapi.menu.StandardMenu
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.util.CameraSequence
import gg.flyte.christmas.util.colourise
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.async
import gg.flyte.twilight.scheduler.delay
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
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
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import kotlin.math.ceil

class HousekeepingEventListener : Listener {
    init {
        event<ServerListPingEvent> {
            motd = "             &f&k|||||| &dFlyte #ff1515C#ff2a2ah#ff3f3fr#ff5454i#ff6969s#ff7e7et#ff9393m#ffa8a8a#ffbdbds #e6ca97E#ccd771v#b3e54ce#99f226n#80ff00t &f&k||||||".colourise()
            // TODO Finish second line + maybe add X mas symbols special characters
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
                async {
                    RemoteFile("https://github.com/flytegg/ls-christmas-rp/releases/latest/download/RP.zip").apply { // TODO change URL/configure pack
//                    println("RP Hash = $hash")
//                    setResourcePack(url, hash, true)
                    }
                }

                gameMode = GameMode.ADVENTURE

                playSound(Sound.ENTITY_PLAYER_LEVELUP)

                inventory.clear()
                inventory.helmet = applyChristmasHat((1..3).random())

                ChristmasEventPlugin.instance.eventController.onPlayerJoin(this)
                ChristmasEventPlugin.instance.eventController.songPlayer?.addPlayer(this)
                ChristmasEventPlugin.instance.worldNPCs.forEach { it.spawnFor(this) }

                applyTag(this)
            }
            ChristmasEventPlugin.instance.eventController.points.putIfAbsent(player.uniqueId, 0)
            ChristmasEventPlugin.instance.eventController.sidebarManager.update()
        }

        event<PlayerQuitEvent> {
            quitMessage(null)
            delay(1) { ChristmasEventPlugin.instance.eventController.onPlayerQuit(player) } // getOnlinePlayers does not update until the next tick
        }

        event<PlayerResourcePackStatusEvent> {
            if (status == PlayerResourcePackStatusEvent.Status.ACCEPTED || status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) return@event
//            player.kick("&cYou &f&nmust&c accept the resource pack to play on this server!".asComponent()) // TODO uncomment when pack works
        }

        event<PlayerDropItemEvent> { isCancelled = true }

        event<PlayerSwapHandItemsEvent> { isCancelled = true }

        event<PlayerInteractEvent> {
            if (item?.type == Material.COMPASS) {
                openSpectateMenu(player)
            }
        }

        event<PlayerStopSpectatingEntityEvent> {
            if (CameraSequence.ACTIVE_CAMERA.contains(spectatorTarget.uniqueId)) {
                isCancelled = true
                return@event
            }

            val currentGame = ChristmasEventPlugin.instance.eventController.currentGame
            if (currentGame?.spectateEntities?.values?.map { it.uniqueId }?.contains(spectatorTarget.uniqueId) == true) {
                player.teleport(currentGame.gameConfig.spectatorSpawnLocations.random())
                player.gameMode = GameMode.ADVENTURE
            }
        }

        event<FoodLevelChangeEvent> { isCancelled = true }

        event<InventoryClickEvent> {
            if (clickedInventory !is PlayerInventory) return@event
            isCancelled = slotType == InventoryType.SlotType.ARMOR
            if (currentItem?.type == Material.COMPASS) {
                openSpectateMenu(whoClicked as Player)
            }
        }

        event<EntityCombustEvent> { if (entity is Player) isCancelled = true }

        event<EntityDamageEvent> { isCancelled = true /* TODO examine later*/ }
    }

    private fun openSpectateMenu(player: Player) {
        var locationSize = ChristmasEventPlugin.instance.eventController.currentGame?.gameConfig!!.spectatorCameraLocations.size

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
                    val requestedCameraEntity = ChristmasEventPlugin.instance.eventController.currentGame!!.spectateEntities[i]
                    whoClicked.gameMode = GameMode.SPECTATOR
                    whoClicked.spectatorTarget = requestedCameraEntity
                    whoClicked.closeInventory()
                }

            standardMenu.setItem(i, menuItem)
        }

        standardMenu.open(false, player)
    }
}
