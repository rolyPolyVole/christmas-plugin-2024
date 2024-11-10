package gg.flyte.christmas.minigame.engine

import com.xxmicloxx.NoteBlockAPI.model.Playlist
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.util.SongReference
import gg.flyte.christmas.util.Util
import gg.flyte.christmas.util.style
import gg.flyte.christmas.util.title
import gg.flyte.christmas.util.titleTimes
import gg.flyte.christmas.visual.SidebarManager
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID
import kotlin.reflect.full.primaryConstructor

/**
 * The controller for the event, handling the current game (and its state), countdown, player management,
 * and other UI and event-related elements.
 */
class EventController() {
    var currentGame: EventMiniGame? = null
    var countdownTask: TwilightRunnable? = null
    val countdownMap = mapOf(
        5 to "<green>➎".style(),
        4 to "<gold>➍".style(),
        3 to "<gold>➌".style(),
        2 to "<red>➋".style(),
        1 to "<dark_red>➊".style()
    )
    val optOut = mutableSetOf<UUID>()
    var songPlayer: RadioSongPlayer? = null
    val points = mutableMapOf<UUID, Int>()
    val sidebarManager = SidebarManager().also { it.dataSupplier = points }

    /**
     * Sets the current game to the provided game configuration.
     * **Note:** This does not start the game, only sets the game to be played.
     *
     * @see prepareStart
     */
    fun setMiniGame(gameConfig: GameConfig) {
        currentGame = gameConfig.gameClass.primaryConstructor?.call()
        currentGame!!.state = GameState.IDLE
    }

    /**
     * Prepares the game to start by setting the state to `WAITING_FOR_PLAYERS` and checking if there are enough players.
     */
    fun prepareStart() {
        // Note: currentGame asserted to not-null due to previous checks.
        currentGame!!.state = GameState.WAITING_FOR_PLAYERS

        if (enoughPlayers()) {
            currentGame!!.state = GameState.COUNTDOWN
            countdown()
            return
        }
    }

    /**
     * Starts the countdown for the game to begin. If there are enough players to start the game
     * by the end of the countdown, [EventMiniGame.startGameOverview] is invoked.
     *
     * Players leaving during the countdown is handled through [onPlayerQuit]. That cancels the
     * countdown task if there are not enough players to start the game.
     */
    private fun countdown() {
        var seconds = 10
        countdownTask = repeatingTask(1, TimeUnit.SECONDS) {
            when (seconds) {
                0 -> {
                    currentGame!!.startGameOverview()
                    currentGame!!.state = GameState.LIVE
                    cancel()
                }

                else -> {
                    val times = Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO)

                    Util.handlePlayers(
                        eventPlayerAction = {
                            countdownMap[seconds]?.let { number ->
                                it.showTitle(Title.title(number, Component.empty(), times))
                                it.playSound(Sound.UI_BUTTON_CLICK)
                            }
                        },
                        optedOutAction = {
                            countdownMap[seconds]?.let { titleText ->
                                it.showTitle(Title.title(titleText, Component.empty(), times))
                                it.playSound(Sound.UI_BUTTON_CLICK)
                            }
                        }
                    )
                    seconds--
                }
            }
        }
    }

    /**
     * @return `true` if there are enough players to start the game, dictated by the minimum player count
     * through [GameConfig.minPlayers].
     */
    private fun enoughPlayers(): Boolean {
        return Util.handlePlayers().size >= currentGame!!.gameConfig.minPlayers
    }

    fun onPlayerJoin(player: Player) {
        if (currentGame == null) {
            player.teleport(ChristmasEventPlugin.instance.lobbySpawn)
        } else {
            currentGame!!.onPlayerJoin(player)

            if (currentGame!!.state == GameState.WAITING_FOR_PLAYERS) {
                if (enoughPlayers()) {
                    currentGame!!.state = GameState.COUNTDOWN
                    countdown()
                }
            }
        }
    }

    fun onPlayerQuit(player: Player) {
        sidebarManager.remove(player)

        if (currentGame == null) return
        when (currentGame!!.state) {

            GameState.COUNTDOWN -> {
                if (!enoughPlayers()) {
                    countdownTask?.cancel()
                    currentGame!!.state = GameState.WAITING_FOR_PLAYERS

                    Util.handlePlayers(
                        eventPlayerAction = {
                            it.showTitle(
                                Title.title(
                                    "<dark_red>⦅x⦆".style(), "<red>Waiting for more players...".style(),
                                    titleTimes(Duration.ZERO, Duration.ofSeconds(5), Duration.ofSeconds(1))
                                )
                            )
                            it.playSound(Sound.BLOCK_NOTE_BLOCK_BASS)
                        },
                        optedOutAction = {
                            it.title(
                                "<dark_red>⦅x⦆".style(), "<red>Waiting for more players...".style(),
                                titleTimes(Duration.ZERO, Duration.ofSeconds(5), Duration.ofSeconds(1))
                            )
                            it.playSound(Sound.BLOCK_NOTE_BLOCK_BASS)
                        }
                    )
                }
            }

            GameState.LIVE -> {
                currentGame!!.onPlayerQuit(player)
            }

            else -> return
        }
    }

    /**
     * Starts the playlist of songs for the event.
     *
     * @param avoid The song to avoid playing, if any.
     */
    fun startPlaylist(avoid: SongReference? = null) {
        if (songPlayer != null) songPlayer!!.destroy()

        songPlayer = RadioSongPlayer(Playlist(*SongReference.entries.map { it.song }.toTypedArray()), SoundCategory.RECORDS)
        songPlayer!!.isRandom = true
        songPlayer!!.isPlaying = true

        fun checkAndSkip() {
            if (avoid == null) return

            if (songPlayer!!.song.path.path == avoid.name + ".nbs") {
                songPlayer!!.playNextSong()
                delay(1) { checkAndSkip() }
            }
        }

        checkAndSkip()
        Bukkit.getOnlinePlayers().forEach(songPlayer!!::addPlayer)
    }

    // get the player at the position of the input (1st, 2nd, 3rd)
    fun getUUIDByPlacement(index: Int): UUID? {
        // if the index is out of bounds, return an empty string
        if (index >= points.size) return null
        return points.entries.sortedByDescending { it.value }[index].key
    }

    // get the place of the player
    fun getPlacementByUUID(uuid: UUID): Int {
        val sorted = points.entries.sortedByDescending { it.value }
        return sorted.indexOfFirst { it.key == uuid } + 1
    }

    fun addPoints(uuid: UUID, amount: Int) {
        points[uuid] = points.getOrDefault(uuid, 0) + amount // will never default to zero. PlayerJoinEvent puts 0 points
    }

    fun serialisePoints() {
        ChristmasEventPlugin.instance.config.set("points", null)
        points.forEach { (uuid, points) -> ChristmasEventPlugin.instance.config.set("points.$uuid", points) }

        ChristmasEventPlugin.instance.saveConfig()
    }
}
