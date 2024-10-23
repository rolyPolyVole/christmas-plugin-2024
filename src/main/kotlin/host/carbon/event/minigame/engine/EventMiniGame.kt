package host.carbon.event.minigame.engine

import gg.flyte.twilight.event.TwilightListener
import host.carbon.event.util.Util
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

abstract class EventMiniGame(val gameConfig: GameConfig) {
    protected val allPlayers = mutableListOf<UUID>()
    protected val eliminatedPlayers = mutableListOf<UUID>()
    protected val listeners = mutableListOf<TwilightListener>()
    protected val tasks = mutableListOf<BukkitTask>()
    lateinit var state: GameState

    init {
        allPlayers.addAll(Util.handlePlayers().map { it.uniqueId })
    }

    /**
     * Starts the game's map overview. This is done through smoothened camera
     * interpolation from [CameraSequence]. As well as sending instructions ([GameConfig.instructions]),
     * and preparing the player for the game.
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

        CameraSequence(
            Bukkit.getOnlinePlayers(),
            Component.text("\n" + gameConfig.instructions + "\n", gameConfig.colour),
            gameConfig.overviewLocations,
            600
        ) {
            // when sequence finished:
            Util.handlePlayers(
                eventPlayerAction = {
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
     * Prepare the player to play the game. This includes setting their inventory, location, etc.
     */
    abstract fun preparePlayer(player: Player)

    /**
     * Called when a game is ready to start.
     * Exclusive of the countdown or any pre-game setup.
     */
    abstract fun startGame()

    /**
     * Called when the game is over, or when a winner has been declared.
     */
    open fun endGame() {
        listeners.forEach { it.unregister() }
    }

    abstract fun handleGameEvents()

    /**
     * Subclasses should override for cosmetic functionality
     * and call `super.eliminate()` to ensure engine logic is maintained.
     */
    open fun eliminate(player: Player, reason: EliminationReason) {
        eliminatedPlayers.add(player.uniqueId)

        if (reason == EliminationReason.LEFT_GAME) player.teleport(gameConfig.spectatorSpawnLocations.random())
    }

    open fun onPlayerJoin(player: Player) {
        if (eliminatedPlayers.contains(player.uniqueId)) return // game logic has already handled their elim

        allPlayers.add(player.uniqueId)

        if (state == GameState.LIVE) {
            eliminate(player, EliminationReason.LEFT_GAME)
        }
    }

    fun onPlayerQuit(player: Player) {
        eliminate(player, EliminationReason.LEFT_GAME)
    }

    /**
     * TODO
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
                    val times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1250), Duration.ofMillis(0))

                    Util.handlePlayers(eventPlayerAction = {
                        getController().countdownMap[seconds]?.let { titleText ->
                            it.showTitle(Title.title(titleText, Component.text(""), times))
                            it.playSound(Sound.UI_BUTTON_CLICK)
                        }
                    })
                    seconds--
                }
            }
        }
    }

    fun remainingPlayers(): List<Player> {
        return Util.handlePlayers().filter { !(eliminatedPlayers.contains(it.uniqueId)) }
    }

    fun getController(): EventController {
        return ChristmasEventPlugin.getInstance().eventController
    }

    /**
     * Used to distinguish between different reasons for elimination.
     *
     * Cosmetic effects can only be applied if the eliminated player is still on the server.
     */
    enum class EliminationReason {
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