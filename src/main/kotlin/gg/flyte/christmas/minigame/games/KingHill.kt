package gg.flyte.christmas.minigame.games

import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.minigame.engine.EventMiniGame
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.minigame.world.MapRegion
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.util.Util
import gg.flyte.christmas.util.eventController
import gg.flyte.christmas.util.style
import gg.flyte.christmas.util.title
import gg.flyte.christmas.util.titleTimes
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.time.Duration
import java.util.UUID

class KingHill : EventMiniGame(GameConfig.KING_OF_THE_HILL) {
    private var hillRegion = MapRegion(MapSinglePoint(824, 85, 633), MapSinglePoint(830, 88, 627))
    private var pvpEnabled = false
    private var gameTime = 3
    private val respawnBelow = 71
    private val timeOnHill = mutableMapOf<UUID, Int>()

    override fun startGameOverview() {
        super.startGameOverview()
        eventController().sidebarManager.dataSupplier = timeOnHill
    }

    override fun preparePlayer(player: Player) {
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE
        player.teleport(gameConfig.spawnPoints.random().randomLocation())

        var stick = ItemStack(Material.STICK).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Knockback Stick!", gameConfig.colour))
            }

            addUnsafeEnchantment(Enchantment.KNOCKBACK, 5)
        }
        player.inventory.addItem(stick)
        timeOnHill[player.uniqueId] = 0
    }

    override fun startGame() {
        simpleCountdown {
            pvpEnabled = true
            Util.handlePlayers(eventPlayerAction = {
                it.title(
                    Component.empty(), "<game_colour>PVP Enabled!".style(),
                    titleTimes(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(300))
                )
            })

            tasks += repeatingTask(1, TimeUnit.SECONDS) {
                Util.handlePlayers(eventPlayerAction = {
                    if (hillRegion.contains(it.location)) {
                        timeOnHill[it.uniqueId] = timeOnHill[it.uniqueId]!! + 1
                        it.playSound(Sound.ENTITY_ITEM_PICKUP)
                        it.sendMessage("<green>+1 second".style())
                    }
                })

                gameTime--

                if (gameTime == 0) {
                    pvpEnabled = false
                    cancel()
                    endGame()
                }

                updateScoreboard()
            }
        }
    }

    override fun eliminate(player: Player, reason: EliminationReason) {
        // super.eliminate(player, reason) | Note: this game does not eliminate players, remove call to super
    }

    override fun endGame() {
        Util.handlePlayers(eventPlayerAction = { it.teleport(gameConfig.spawnPoints.random().randomLocation()) })
        for (entry in timeOnHill) eventController().addPoints(entry.key, entry.value)

        val (first) = timeOnHill.entries
            .sortedByDescending { it.value }
            .take(1)
            .also { it.forEach { formattedWinners.put(it.key, it.value.toString() + " seconds") } }

        var yaw = 0F
        ChristmasEventPlugin.instance.serverWorld.spawn(MapSinglePoint(827.5, 105, 630.5, 0, 0), ItemDisplay::class.java) {
            it.setItemStack(ItemStack(Material.PLAYER_HEAD).apply {
                val meta = itemMeta as SkullMeta
                meta.owningPlayer = Bukkit.getOfflinePlayer(first.key)
                itemMeta = meta
            })
            it.interpolationDelay = -1
            it.interpolationDuration = 200
            it.teleportDuration = 15

            tasks += delay(1) {
                it.transformation = it.transformation.apply {
                    scale.mul(25F)
                } // apply scale transformation

                tasks += repeatingTask(0, 14) {
                    val clone = it.location.clone()
                    clone.yaw = yaw
                    it.teleport(clone)

                    yaw += 90
                } // rotate the head lol
            }

            tasks += delay(20, TimeUnit.SECONDS) {
                it.remove()
                super.endGame()
            }
        }
    }

    private fun updateScoreboard() {
        val timeLeft = "<aqua>ᴛɪᴍᴇ ʟᴇғᴛ: <red><b>${gameTime}".style()
        Bukkit.getOnlinePlayers().forEach { eventController().sidebarManager.updateLines(it, listOf(Component.empty(), timeLeft)) }
    }

    override fun handleGameEvents() {
        listeners += event<EntityDamageByEntityEvent> {
            if (entity is Player && damager is Player) {
                if (!pvpEnabled) {
                    isCancelled = true
                    (damager as Player).playSound(Sound.BLOCK_NOTE_BLOCK_BASS)
                }
            }
        }

        listeners += event<PlayerMoveEvent> {
            if (player.location.blockY < respawnBelow) {
                player.teleport(gameConfig.spawnPoints.random().randomLocation())
                player.playSound(Sound.ENTITY_PLAYER_HURT)
            }
        }
    }

    override fun onPlayerJoin(player: Player) {
        super.onPlayerJoin(player)
        preparePlayer(player)
    }
}
