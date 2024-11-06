package gg.flyte.christmas.listeners

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.InteractionHand
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation
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
import kotlin.apply
import kotlin.math.ceil

class HousekeepingEventListener : Listener, PacketListener {
    init {
        PacketEvents.getAPI().eventManager.registerListener(this, PacketListenerPriority.NORMAL)

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
            val worldNPCs = ChristmasEventPlugin.instance.worldNPCs
            val playerLocation = player.location
            val npcLocations = worldNPCs.map { SpigotConversionUtil.toBukkitLocation(ChristmasEventPlugin.instance.serverWorld, it.npc.location) }

            // hide player if near any NPC (they obstruct view)
            if (npcLocations.any { it.distance(playerLocation) < 3 }) player.hidePlayer() else player.showPlayer()

            // make NPCs look at player if within range
            worldNPCs.forEach { npc ->
                val npcLocation = SpigotConversionUtil.toBukkitLocation(ChristmasEventPlugin.instance.serverWorld, npc.npc.location)
                if (npcLocation.distance(playerLocation) <= 25) {
                    val lookVector = npcLocation.apply { setDirection(playerLocation.toVector().subtract(toVector())) }
                    val playerManager = PacketEvents.getAPI().playerManager.getUser(player)
                    playerManager.sendPacket(WrapperPlayServerEntityHeadLook(npc.npc.id, lookVector.yaw))
                    playerManager.sendPacket(WrapperPlayServerEntityRotation(npc.npc.id, lookVector.yaw, lookVector.pitch, false))
                }
            }
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
            // prevents any camera sequence from being escaped
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

    override fun onPacketReceive(event: PacketReceiveEvent) {
        if (event.packetType != PacketType.Play.Client.INTERACT_ENTITY) return
        WrapperPlayClientInteractEntity(event).apply {
            ChristmasEventPlugin.instance.worldNPCs.find { it.npc.id == entityId }?.let { clickedNPC ->

                if (action == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    event.user.sendPacket(
                        WrapperPlayServerEntityAnimation(clickedNPC.npc.id, WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM)
                    )
                } else if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT) { // fired twice
                    if (hand != InteractionHand.MAIN_HAND) return

                    WrapperPlayServerEntityMetadata(
                        clickedNPC.npc.id,
                        listOf(EntityData(6, EntityDataTypes.ENTITY_POSE, EntityPose.CROUCHING))
                    ).also { event.user.sendPacket(it) }

                    delay(3) {
                        WrapperPlayServerEntityMetadata(
                            clickedNPC.npc.id,
                            listOf(EntityData(6, EntityDataTypes.ENTITY_POSE, EntityPose.STANDING))
                        ).also { event.user.sendPacket(it) }
                    }
                }
            }
        }
    }

    private fun openSpectateMenu(player: Player) {
        var options = eventController().currentGame!!.gameConfig.spectatorCameraLocations.size
        var standardMenu = StandardMenu("Spectate Map:", ceil(options.div(9.0)).toInt() * 9)

        for (i in 0 until options) {

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
