package gg.flyte.christmas.minigame.engine

import com.xxmicloxx.NoteBlockAPI.model.Playlist
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.donation.DonateEvent
import gg.flyte.christmas.donation.DonationTier
import gg.flyte.christmas.util.*
import gg.flyte.christmas.visual.SidebarManager
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Sound
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import java.time.Duration
import java.util.*
import kotlin.reflect.full.primaryConstructor

/**
 * The controller for the event, handling the current game (and its state), countdown, player management,
 * and other UI and event-related elements.
 */
class EventController() {
    var currentGame: EventMiniGame? = null
    var countdownTask: TwilightRunnable? = null
    val countdownMap = mapOf(
        10 to "<green>➓".style(),
        5 to "<green>➎".style(),
        4 to "<gold>➍".style(),
        3 to "<gold>➌".style(),
        2 to "<red>➋".style(),
        1 to "<dark_red>➊".style()
    )
    val optOut = mutableSetOf<UUID>()
    val points = mutableMapOf<UUID, Int>()
    var songPlayer: RadioSongPlayer? = null
    val sidebarManager = SidebarManager().also { it.dataSupplier = points }
    val donors = mutableSetOf<UUID>()
    var totalDonations: Int = 0
    var donationGoal = 2000
    var donationBossBar = BossBar.bossBar(
        "<b><gradient:#7EC1EF:#FA62A3>Donation Goal:</gradient></b> <white><b>$<#7EC1EF>${totalDonations}<grey>/<#FA62A3>${donationGoal}".style(),
        0F,
        BossBar.Color.GREEN,
        BossBar.Overlay.PROGRESS
    )

    /**
     * Sets the current game to the provided game configuration.
     * **Note:** This does not start the game, only sets the game to be played.
     *
     * @see prepareStart
     */
    fun setMiniGame(gameConfig: GameConfig) {
        currentGame = gameConfig.gameClass.primaryConstructor?.call()
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
        var seconds = 3 // TODO change back to 10
        countdownTask = repeatingTask(1, TimeUnit.SECONDS) {
            when (seconds) {
                0 -> {
                    currentGame!!.startGameOverview()
                    currentGame!!.state = GameState.LIVE
                    cancel()
                }

                else -> {
                    Util.runAction(PlayerType.PARTICIPANT, PlayerType.OPTED_OUT) {
                        countdownMap[seconds]?.let { number ->
                            it.title(number, Component.empty(), titleTimes(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO))
                            it.playSound(Sound.UI_BUTTON_CLICK)
                        }
                    }
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
        return Util.runAction(PlayerType.PARTICIPANT) {}.size >= currentGame!!.gameConfig.minPlayers
    }

    fun onPlayerJoin(player: Player) {
        if (currentGame == null) {
            player.teleport(ChristmasEventPlugin.instance.lobbySpawn)
        } else {
            if (currentGame!!.state == GameState.WAITING_FOR_PLAYERS) {
                if (enoughPlayers()) {
                    currentGame!!.state = GameState.COUNTDOWN
                    countdown()
                }
            }

            if (currentGame!!.state == GameState.LIVE) currentGame!!.onPlayerJoin(player)
        }
    }

    fun onPlayerQuit(player: Player) {
        sidebarManager.remove(player)

        when (currentGame?.state) {

            GameState.COUNTDOWN -> {
                if (!enoughPlayers()) {
                    countdownTask?.cancel()
                    currentGame!!.state = GameState.WAITING_FOR_PLAYERS

                    Util.runAction(PlayerType.PARTICIPANT, PlayerType.OPTED_OUT) {
                        it.title(
                            "<dark_red>⦅x⦆".style(), "<red>Waiting for more players...".style(),
                            titleTimes(Duration.ZERO, Duration.ofSeconds(5), Duration.ofSeconds(1))
                        )
                        it.playSound(Sound.BLOCK_NOTE_BLOCK_BASS)
                    }
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
        songPlayer?.destroy()

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

    fun handleDonation(event: DonateEvent) {
        if (event.value == null) return // no clue why this would happen, but just in case

        fun updateBossBar() {
            donationBossBar.name(
                "<b><gradient:#7EC1EF:#FA62A3>Donation Goal:</gradient></b> <white><b>$<#7EC1EF>${totalDonations}<grey>/<#FA62A3>${donationGoal}".style()
            )
            donationBossBar.progress((totalDonations / donationGoal).toFloat())
        }

        Bukkit.getOnlinePlayers().forEach {
            // spawn firework
            ChristmasEventPlugin.instance.serverWorld.spawn(it.location, Firework::class.java) {
                it.fireworkMeta = it.fireworkMeta.apply {
                    addEffect(
                        FireworkEffect.builder()
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .withColor(Color.RED, Color.LIME, Color.GREEN)
                            .withFlicker()
                            .withTrail()
                            .build()
                    )
                    power = 1
                }
            }

            // announce donation
            val charitableDonor = event.donorName ?: "mysterious donor"
            val numberValue = event.value
            val currency = event.currency
            it.sendMessage("<grey><gradient:#A3ADFF:#00FFF4>DONATION MADE ––> Thank you,</gradient><#FF72A6> $charitableDonor<gradient:#00FFF4:#00FFF4>, <gradient:#00FFF4:#A3ADFF>for donating $numberValue $currency.</gradient>".style())
        }

        var toDouble = event.value.toDouble()

        totalDonations += toDouble.toInt()
        updateBossBar()

        currentGame?.handleDonation(DonationTier.getTier(toDouble))
    }
}
