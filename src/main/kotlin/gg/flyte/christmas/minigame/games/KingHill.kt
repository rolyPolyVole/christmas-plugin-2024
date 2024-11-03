package gg.flyte.christmas.minigame.games

import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.minigame.engine.EventMiniGame
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.minigame.world.MapRegion
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.util.Util
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
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
        eventController.sidebarManager.dataSupplier = timeOnHill
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
                val title = Title.title(
                    Component.text(""),
                    Component.text("PVP Enabled!", gameConfig.colour),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(300))
                )

                it.showTitle(title)
            })

            tasks += repeatingTask(1, TimeUnit.SECONDS) {
                Util.handlePlayers(eventPlayerAction = {
                    if (hillRegion.contains(it.location)) {
                        timeOnHill[it.uniqueId] = timeOnHill[it.uniqueId]!! + 1
                        it.playSound(Sound.ENTITY_ITEM_PICKUP)
                        it.sendMessage(Component.text("+1 second", NamedTextColor.GREEN))
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

    private fun updateScoreboard() {
        tasks += repeatingTask(10) {
            val timeComponent = Component.text("ᴛɪᴍᴇ ʟᴇғᴛ: ", NamedTextColor.AQUA)
                .append(Component.text(gameTime.toString(), NamedTextColor.RED, TextDecoration.BOLD))

            Bukkit.getOnlinePlayers().forEach { eventController.sidebarManager.updateLines(it, listOf(Component.empty(), timeComponent)) }
        }
    }


    override fun eliminate(player: Player, reason: EliminationReason) {
        // Note: this game does not eliminate players, remove call to super
        // super.eliminate(player, reason)
    }

    override fun endGame() {
        val winner = timeOnHill.maxBy { it.value }

        Util.handlePlayers(eventPlayerAction = { it.teleport(gameConfig.spawnPoints.random().randomLocation()) })

        var yaw = 0F
        ChristmasEventPlugin.instance.serverWorld.spawn(MapSinglePoint(827.5, 105, 630.5, 0, 0), ItemDisplay::class.java) {
            it.setItemStack(ItemStack(Material.PLAYER_HEAD).apply {
                val meta = itemMeta as SkullMeta
                meta.owningPlayer = Bukkit.getOfflinePlayer(winner.key)
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