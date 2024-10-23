package gg.flyte.christmas.minigame.engine

import com.xxmicloxx.NoteBlockAPI.model.Playlist
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.util.SongReference
import gg.flyte.christmas.util.Util
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID
import kotlin.reflect.full.primaryConstructor

/**
 * The controller for the event, handling the current game (and its state), countdown, and player management.
 */
class EventController() {
    var currentGame: EventMiniGame? = null
    var countdownTask: TwilightRunnable? = null
    val countdownMap = mapOf(
        5 to Component.text("➎", NamedTextColor.GREEN),
        4 to Component.text("➍", NamedTextColor.GOLD),
        3 to Component.text("➌", NamedTextColor.GOLD),
        2 to Component.text("➋", NamedTextColor.RED),
        1 to Component.text("➊", NamedTextColor.RED)
    )
    val optOut = mutableSetOf<UUID>()
    var songPlayer: RadioSongPlayer? = null

    fun setMiniGame(gameConfig: GameConfig) {
        currentGame = gameConfig.gameClass.primaryConstructor?.call()
        currentGame!!.state = GameState.IDLE
        // TODO update action bar: "Next game is !!!!! {..}
    }

    fun prepareStart() {
        // Note: currentGame asserted to not-null due to previous checks.
        currentGame!!.state = GameState.WAITING_FOR_PLAYERS

        if (enoughPlayers()) {
            currentGame!!.state = GameState.COUNTDOWN
            countdown()
            return
        }
    }

    private fun countdown() {
        var seconds = 0 // TODO CHANGE BACK TO 10 WHEN TESTING IS DONE
        countdownTask = repeatingTask(1, TimeUnit.SECONDS) {
            when (seconds) {
                0 -> {
                    currentGame!!.startGameOverview()
                    currentGame!!.state = GameState.LIVE
                    cancel()
                }

                else -> {
                    val times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1250), Duration.ofMillis(0))

                    // TODO check for any usages of getOnlinePlayers and replace with Util

                    Util.handlePlayers(eventPlayerAction = {
                        countdownMap[seconds]?.let { titleText ->
                            it.showTitle(Title.title(titleText, Component.text(""), times))
                            it.playSound(Sound.UI_BUTTON_CLICK)
                        }
                    })
                    seconds--
                }
            }
        }
    }

    private fun enoughPlayers(): Boolean {
        return Util.handlePlayers().size >= currentGame!!.gameConfig.minPlayers
    }

    fun onPlayerJoin(player: Player) {
        if (currentGame == null) {
            player.teleport(ChristmasEventPlugin.getInstance().lobbySpawn)
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
        if (currentGame == null) return
        when (currentGame!!.state) {

            GameState.COUNTDOWN -> {
                if (!enoughPlayers()) {
                    countdownTask?.cancel()
                    currentGame!!.state = GameState.WAITING_FOR_PLAYERS

                    for (player in Util.handlePlayers()) {
                        player.showTitle(
                            Title.title(
                                Component.text("⦅x⦆", NamedTextColor.GRAY),
                                Component.text("Waiting for more players...", NamedTextColor.DARK_PURPLE)
                            )
                        )

                        player.playSound(Sound.BLOCK_NOTE_BLOCK_BASS)
                    }
                }
            }

            GameState.LIVE -> {
                currentGame!!.onPlayerQuit(player)
            }

            else -> return
        }
    }

    fun startPlaylist(song: SongReference? = null) {
        if (songPlayer != null) songPlayer!!.destroy()

        songPlayer = RadioSongPlayer(Playlist(*SongReference.entries.map { it.song }.toTypedArray()), SoundCategory.RECORDS)
        songPlayer!!.isRandom = true
        songPlayer!!.isPlaying = true

        fun checkAndSkip() {
            if (song == null) return

            if (songPlayer!!.song.path.path == song.name + ".nbs") {
                songPlayer!!.playNextSong()
                delay(1) { checkAndSkip() }
            }
        }

        checkAndSkip()
        Bukkit.getOnlinePlayers().forEach(songPlayer!!::addPlayer)
    }
}
// TODO make sure currentGame is set to null when the game ends