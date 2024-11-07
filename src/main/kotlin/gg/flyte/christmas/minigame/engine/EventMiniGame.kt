package gg.flyte.christmas.minigame.engine

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.npc.WorldNPC
import gg.flyte.christmas.util.CameraSequence
import gg.flyte.christmas.util.Util
import gg.flyte.christmas.util.eventController
import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import java.time.Duration
import java.util.UUID
import kotlin.random.Random

/**
 * Abstract class representing a base for mini-games in the event.
 * This class provides the core game logic and management of players,
 * such as handling elimination, preparing players, and tracking game state.
 *
 * @param gameConfig Configuration for the mini-game, which includes settings like spawn points,
 * game name, spectator camera locations, and instructions for players.
 */
abstract class EventMiniGame(val gameConfig: GameConfig) {
    protected val eliminatedPlayers = mutableListOf<UUID>()
    protected val listeners = mutableListOf<TwilightListener>()
    protected val tasks = mutableListOf<TwilightRunnable?>()
    protected val formattedWinners = linkedMapOf<UUID, String>()
    val spectateEntities = mutableMapOf<Int, Entity>()
    lateinit var state: GameState

    /**
     * Initialises the game's spectator entities, which are used to allow players to spectate the game.
     */
    init {
        for ((index, point) in gameConfig.spectatorCameraLocations.withIndex()) {
            spectateEntities[index] = ChristmasEventPlugin.instance.serverWorld.spawn(point, ItemDisplay::class.java) {
                it.setItemStack(ItemStack(Material.AIR))
            }
        }
    }

    /**
     * Starts the game's map overview. This is done through smoothened camera
     * interpolation from [CameraSequence]. As well as sending instructions ([GameConfig.instructions]),
     * and preparing the player for the game.
     *
     * **Note:** Implementing classes may override (but **must** call super) to add additional cosmetic
     * functionality as the CameraSequence is running.
     */
    open fun startGameOverview() {
        handleGameEvents()

        // send BEFORE textDisplay has rendered in.
        val title = Title.title(
            gameConfig.displayName,
            Component.text("Instructions:", gameConfig.colour),
            Title.Times.times(Duration.ofMillis(1250), Duration.ofMillis(3500), Duration.ofMillis(750))
        )

        Util.handlePlayers(eventPlayerAction = { it.showTitle(title) })

        var displayComponent = Component.text("")
            .append(Component.text("\n   ", null, TextDecoration.STRIKETHROUGH))
            .append(Component.text("> "))
            .append(gameConfig.displayName.decorate(TextDecoration.BOLD))
            .append(Component.text("\n\n"))
            .append(Component.text(gameConfig.instructions, gameConfig.colour))
            .append(Component.text("\n"))
            .color(gameConfig.colour)

        CameraSequence(gameConfig.overviewLocations, Bukkit.getOnlinePlayers(), displayComponent) {
            // when sequence finished:
            Util.handlePlayers(
                eventPlayerAction = {
                    // if player was eliminated during the sequence (left server), don't prepare them.
                    if (!(remainingPlayers().map { it.uniqueId }.contains(it.uniqueId))) return@handlePlayers

                preparePlayer(it)
                it.sendMessage(
                    Component.text("\n------------------[INSTRUCTIONS]------------------\n", gameConfig.colour)
                        .append(Component.text(gameConfig.instructions, NamedTextColor.WHITE))
                        .append(Component.text("\n-------------------------------------------------\n", gameConfig.colour))
                )
            },
            optedOutAction = {
                it.teleport(gameConfig.spectatorSpawnLocations.random())
            }
        )

            startGame()
        }
    }

    /**
     * Prepares the player for the game; state, inventory, location, and other relevant configurations.
     *
     * @param player The player to be prepared for the game.
     */
    abstract fun preparePlayer(player: Player)

    /**
     * Called when a game is ready to start.
     * Exclusive of the countdown or any pre-game setup.
     */
    abstract fun startGame()

    /**
     * As the super-class, this only handles game logic for player elimination.
     * Subclasses should override for cosmetic functionality
     * and call `super.eliminate()` to ensure engine logic is maintained.
     * @param player The player to eliminate.
     * @param reason The reason for elimination.
     */
    open fun eliminate(player: Player, reason: EliminationReason) {
        eliminatedPlayers.add(player.uniqueId)
        player.clearActivePotionEffects()
        player.inventory.storageContents = arrayOf()
        player.inventory.setItemInOffHand(null)

        if (reason == EliminationReason.LEFT_GAME) {
            player.teleport(gameConfig.spectatorSpawnLocations.random())
        } else {
            // spectate item
            ItemStack(Material.COMPASS).apply {
                itemMeta = itemMeta.apply {
                    displayName(Component.text("Spectate", NamedTextColor.WHITE))
                    editMeta {
                        lore(listOf(Component.text("Click to Spectate!", NamedTextColor.GRAY)))
                    }
                }
            }.also { player.inventory.setItem(8, it) }
        }
    }

    /**
     * Ends the game, performing cleanup operations such as cancelling tasks,
     * unregistering listeners, and removing spectator entities. Can be
     * overridden by subclasses for custom end-game behavior.
     */
    open fun endGame() {
        tasks.forEach { it?.cancel() }.also { tasks.clear() }
        listeners.forEach { it.unregister() }.also { listeners.clear() }
        spectateEntities.values.forEach { it.remove() }.also { spectateEntities.clear() }
        eliminatedPlayers.clear()
        eventController().currentGame = null
        eventController().sidebarManager.dataSupplier = eventController().points
        eventController().serialisePoints()

        val npcs = mutableListOf<WorldNPC>()
        val descendingColour = listOf("a", "c", "9")
        var index = 0
        formattedWinners.entries.take(1).forEach {
            var uniqueId = it.key
            var value = it.value
            val displayName = "§${descendingColour[index]}${Bukkit.getPlayer(uniqueId)!!.name}"
            var placeLocation = Util.getNPCSummaryLocation(index)
            val animationTasks = mutableListOf<TwilightRunnable>()

            WorldNPC.createFromUniqueId(displayName, uniqueId, placeLocation).also { npc ->
                Bukkit.getOnlinePlayers().forEach { npc.spawnFor(it) }

                placeLocation.world.spawn(placeLocation.add(0.0, 2.5, 0.0), TextDisplay::class.java).apply {
                    text(Component.text(value, TextColor.color(255, 196, 255)))
                    backgroundColor = Color.fromRGB(84, 72, 84)
                    billboard = Display.Billboard.CENTER
                }

                animationTasks += repeatingTask(1) {
                    val nearestItem = placeLocation.getNearbyEntitiesByType(ItemDisplay::class.java, 30.0).firstOrNull()
                    if (nearestItem == null) {
                        cancel()
                        return@repeatingTask
                    }

                    val playersLocation = nearestItem.location

                    val npcLocation = SpigotConversionUtil.toBukkitLocation(ChristmasEventPlugin.instance.serverWorld, npc.npc.location)
                    if (npcLocation.distance(playersLocation) <= 25) {
                        val lookVector = npcLocation.apply { setDirection(playersLocation.toVector().subtract(toVector())) }

                        Bukkit.getOnlinePlayers().forEach {
                            val playerManager = PacketEvents.getAPI().playerManager.getUser(it)
                            playerManager.sendPacket(WrapperPlayServerEntityHeadLook(npc.npc.id, lookVector.yaw))
                            playerManager.sendPacket(WrapperPlayServerEntityRotation(npc.npc.id, lookVector.yaw, lookVector.pitch, false))
                        }
                    }
                } // NPC Look
                animationTasks += repeatingTask(3) {
                    delay((0..5).random()) {
                        val packetToSend = when (Random.nextBoolean()) {
                            true -> {
                                WrapperPlayServerEntityAnimation(npc.npc.id, WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM)
                            }

                            false -> {
                                WrapperPlayServerEntityMetadata(
                                    npc.npc.id, listOf(
                                        EntityData(
                                            6,
                                            EntityDataTypes.ENTITY_POSE,
                                            (if (Random.nextBoolean()) EntityPose.CROUCHING else EntityPose.STANDING)
                                        )
                                    )
                                )
                            }
                        }

                        Bukkit.getOnlinePlayers().forEach { PacketEvents.getAPI().playerManager.getUser(it).sendPacket(packetToSend) }
                    }
                } // NPC Animation

                npcs.add(npc)
            }

            index++
        } // Spawn NPCs
        formattedWinners.clear()

        // spawn NPCs for game winners:
        val summaryLocations = listOf(
            MapSinglePoint(605, 216, 488, -88.84488F, 3.8515968F),
            MapSinglePoint(613, 216, 488, -88.84488F, 4.4180946F),
            MapSinglePoint(627, 216, 488, -84.55551F, 7.250582F),
            MapSinglePoint(639, 216, 491, 93.891174F, 23.59806F),
            MapSinglePoint(641, 216, 497, 134.7605F, 17.609343F),
            MapSinglePoint(634, 216, 500, -173.60687F, 15.424276F),
            MapSinglePoint(626, 216, 500, -134.6789F, 13.482008F),
            MapSinglePoint(622, 216, 493, -107.72839F, 14.53407F),
            MapSinglePoint(621, 216, 484, -71.38977F, 13.158297F),
            MapSinglePoint(622, 216, 478, -43.791748F, 12.187162F),
            MapSinglePoint(622, 216, 477, -36.265015F, 12.025307F),
            MapSinglePoint(625, 216, 481, -38.61206F, 12.591802F),
            MapSinglePoint(627, 216, 485, -62.487F, 16.314486F),
            MapSinglePoint(626, 216, 488, -87.98065F, 13.239224F)
        ) // TODO put actual points when map is done.

        CameraSequence(summaryLocations, Bukkit.getOnlinePlayers(), null, 8) {
            Bukkit.getOnlinePlayers().forEach { loopedPlayer ->
                loopedPlayer.gameMode = GameMode.ADVENTURE
                loopedPlayer.inventory.clear()
                loopedPlayer.teleport(ChristmasEventPlugin.instance.lobbySpawn)
                eventController().sidebarManager.update(loopedPlayer)
                npcs.forEach { it.despawnFor(loopedPlayer) }

                // TODO remove textdisplays
            }
        }
    }

    /**
     * Handles game events
     *
     * **Note**: Registered events in subclasses (preferably through Twilight) should be added to [listeners]
     *
     * ```
     *  override fun handleGameEvents() {
     *    listeners += event<BlockPlaceEvent> {...}
     *  }
     *  ```
     */
    abstract fun handleGameEvents()

    /**
     * Handles player joining the game.
     */
    open fun onPlayerJoin(player: Player) {
        if (eliminatedPlayers.contains(player.uniqueId)) return // game logic has already handled their elim

        if (state == GameState.LIVE) {
            eliminate(player, EliminationReason.LEFT_GAME)
        }
    }

    /**
     * Eliminates the player from the game upon quitting.
     */
    fun onPlayerQuit(player: Player) {
        eliminate(player, EliminationReason.LEFT_GAME)
    }

    /**
     * Starts a simple countdown before the game begins.
     * Displays countdown messages to all players and triggers [onCountdownEnd] when the countdown finishes.
     *
     * @param onCountdownEnd The action to perform when the countdown ends.
     */
    fun simpleCountdown(onCountdownEnd: () -> Unit) {
        var seconds = 5
        repeatingTask(1, TimeUnit.SECONDS) {
            when (seconds) {
                0 -> {
                    cancel()
                    onCountdownEnd()
                }

                else -> {
                    val times = Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO)

                    Util.handlePlayers(eventPlayerAction = {
                        eventController().countdownMap[seconds]?.let { titleText ->
                            it.showTitle(Title.title(titleText, Component.text(""), times))
                            it.playSound(Sound.UI_BUTTON_CLICK)
                        }
                    })
                    seconds--
                }
            }
        }
    }

    /**
     * @return A list of players who have not been eliminated from the game.
     */
    fun remainingPlayers(): List<Player> {
        return Util.handlePlayers().filter { !(eliminatedPlayers.contains(it.uniqueId)) }
    }

    /**
     * Used to distinguish between different reasons for elimination.
     *
     * Cosmetic effects can only be applied if the eliminated player is still on the server.
     */
    enum class EliminationReason {
        // TODO change to boolean?
        /**
         * Player left the game (left the server) OR player joined after the game started. (and canPlayAfterStart is false)
         */
        LEFT_GAME,

        /**
         * Player was terrible and was eliminated by game logic.
         */
        ELIMINATED,
    }
}

// TODO make sure currentGame is set to null when the game ends
// TODO clear inventory contents after each game (because shit/compass might be in it)