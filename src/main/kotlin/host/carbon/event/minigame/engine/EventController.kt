package host.carbon.event.minigame.engine

import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.extension.toComponent
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import host.carbon.event.ChristmasEventPlugin
import host.carbon.event.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID

/**
 * The controller for the event, handling the current game (and its state), countdown, and player management.
 */
class EventController() {
    var currentGame: EventMiniGame? = null
    var currentCountdown: TwilightRunnable? = null
    val countdownMap = mapOf(
        5 to "&c➎ &7seconds",
        4 to "&c➍ &7seconds",
        3 to "&6➌ &7seconds",
        2 to "&6➋ &7seconds",
        1 to "&a➊ &7second"
    )
    val optOut = mutableSetOf<UUID>()

    fun setMiniGame(gameConfig: GameConfig) {
        currentGame = gameConfig.gameClass.constructors.first().call(gameConfig)
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
        var seconds = 10;
        currentCountdown = repeatingTask(1, TimeUnit.SECONDS) {
            when (seconds) {
                0 -> {
                    currentGame!!.startGameOverview()
                    currentGame!!.state = GameState.LIVE
                    cancel()
                }

                else -> {
                    val times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1250), Duration.ofMillis(0))

                    Bukkit.getOnlinePlayers().forEach { player ->
                        countdownMap[seconds]?.let { titleText ->
                            player.showTitle(Title.title(titleText.toComponent(), Component.text(""), times))
                            player.playSound(Sound.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON)
                        }
                    }
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
                    currentCountdown?.cancel()
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
}
// TODO make sure currentGame is set to null when the game ends