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

    // TODO fix doc link.
    /**
     * Starts the game's map overview. This is done through smoothened camera
     * interpolation from [Util.SmoothCamera]. As well as sending instructions ([GameConfig.instructions]),
     * and preparing the player for the game.
     */
    fun startGameOverview() {
        Util.handlePlayers(eventPlayerAction = {
            it.sendMessage(gameConfig.instructions)
        })

        // TODO camera interpolation through gameConfig.overviewLocations. Once done
        // REDO COUNTDOWN and run below

        Util.handlePlayers(
            cameraEntityAction = {
                // TODO move cam
            },
            optedOutAction = {
                it.teleport(gameConfig.spectatorSpawnLocations.random())
            },
            eventPlayerAction = {
                preparePlayer(it)
            }
        )

        startGame()
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

    fun remainingPlayers(): List<Player> {
        return Util.handlePlayers().filter { !(eliminatedPlayers.contains(it.uniqueId)) }
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