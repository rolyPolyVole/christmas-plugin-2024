package gg.flyte.christmas.minigame.engine

import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.util.CameraSequence
import gg.flyte.christmas.util.Util
import gg.flyte.twilight.event.TwilightListener
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.TwilightRunnable
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
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Duration
import java.util.UUID

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
    val spectateEntities = mutableMapOf<Int, Entity>()
    lateinit var state: GameState
    val eventController get() = ChristmasEventPlugin.instance.eventController

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
     * Ends the game, performing cleanup operations such as cancelling tasks,
     * unregistering listeners, and removing spectator entities. Can be
     * overridden by subclasses for custom end-game behavior.
     */
    open fun endGame() {
        tasks.forEach { it?.cancel() }
        listeners.forEach { it.unregister() }
        spectateEntities.values.forEach { it.remove() }
        eliminatedPlayers.clear()
        eventController.currentGame = null
        eventController.sidebarManager.dataSupplier = eventController.points

        Bukkit.getOnlinePlayers().forEach {
            it.gameMode = GameMode.ADVENTURE // could be spectating camera
            it.inventory.clear()
            it.teleport(ChristmasEventPlugin.instance.lobbySpawn)
            eventController.sidebarManager.update(it)
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
     * As the super-class, this only handles game logic for player elimination.
     * Subclasses should override for cosmetic functionality
     * and call `super.eliminate()` to ensure engine logic is maintained.
     * @param player The player to eliminate.
     * @param reason The reason for elimination.
     */
    open fun eliminate(player: Player, reason: EliminationReason) {
        eliminatedPlayers.add(player.uniqueId)

        if (reason == EliminationReason.LEFT_GAME) {
            player.teleport(gameConfig.spectatorSpawnLocations.random())
            player.clearActivePotionEffects()
        } else {
            // spectate item
            ItemStack(Material.COMPASS).apply {
                itemMeta = itemMeta.apply {
                    displayName(Component.text("Spectate", NamedTextColor.WHITE))
                    editMeta {
                        lore(listOf(Component.text("Click to Spectate!", NamedTextColor.GRAY)))
                    }
                }
            }.let { player.inventory.setItem(8, it) }
        }
    }

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
                    val times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1100), Duration.ofMillis(0))

                    Util.handlePlayers(eventPlayerAction = {
                        eventController.countdownMap[seconds]?.let { titleText ->
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