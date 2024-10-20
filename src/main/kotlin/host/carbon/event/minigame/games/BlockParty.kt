package host.carbon.event.minigame.games

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.extension.removeActivePotionEffects
import host.carbon.event.minigame.engine.EventMiniGame
import host.carbon.event.minigame.engine.GameConfig
import host.carbon.event.minigame.world.MapRegion
import host.carbon.event.minigame.world.MapSinglePoint
import host.carbon.event.util.SongReference
import host.carbon.event.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BlockParty : EventMiniGame(GameConfig.BLOCK_PARTY) {
    private lateinit var radioPlayer: RadioSongPlayer
    private val materials = mapOf(
        Material.WHITE_CONCRETE to NamedTextColor.WHITE,
        Material.ORANGE_CONCRETE to NamedTextColor.GOLD,
        Material.MAGENTA_CONCRETE to NamedTextColor.DARK_PURPLE,
        Material.LIGHT_BLUE_CONCRETE to NamedTextColor.AQUA,
        Material.YELLOW_CONCRETE to NamedTextColor.YELLOW,
        Material.LIME_CONCRETE to NamedTextColor.GREEN,
        Material.PINK_CONCRETE to NamedTextColor.LIGHT_PURPLE,
        Material.GRAY_CONCRETE to NamedTextColor.DARK_GRAY,
        Material.LIGHT_GRAY_CONCRETE to NamedTextColor.GRAY,
        Material.CYAN_CONCRETE to NamedTextColor.DARK_AQUA,
        Material.PURPLE_CONCRETE to NamedTextColor.DARK_PURPLE,
        Material.BLUE_CONCRETE to NamedTextColor.BLUE,
        Material.BROWN_CONCRETE to NamedTextColor.GOLD,
        Material.GREEN_CONCRETE to NamedTextColor.GREEN,
        Material.RED_CONCRETE to NamedTextColor.RED,
        Material.BLACK_CONCRETE to NamedTextColor.BLACK
    )
    private val blockFaces = listOf(
        BlockFace.NORTH,
        BlockFace.SOUTH,
        BlockFace.EAST,
        BlockFace.WEST,
        BlockFace.NORTH_EAST,
        BlockFace.NORTH_WEST,
        BlockFace.SOUTH_EAST,
        BlockFace.SOUTH_WEST
    )
    private val groupedSquares = mutableListOf<Location>() // TODO change to MapRegion/MapSinglePoint?
    private val basePlatform = MapRegion(MapSinglePoint(632, 110, 816), MapSinglePoint(600, 110, 784))
    private val eliminateBelow = 104
    private var roundNumber = 0
    private var powerUpLocation: Location? = null

    private var locationsToRemove = mutableListOf<Location>()
    private var bombedSquares = mutableListOf<Location>()

    override fun preparePlayer(player: Player) {
        player.gameMode = GameMode.ADVENTURE
    }

    override fun startGame() {
        val barrier: Location = MapSinglePoint(601, 111, 785)
        for (x in 0 until 10) {
            for (z in 0 until 10) {
                val squareLocation = barrier.clone().add((x * 3).toDouble(), 0.0, (z * 3).toDouble()).subtract(0.0, 1.0, 0.0)
                groupedSquares.add(squareLocation)
            }
        }

        for (location in groupedSquares) {
            location.block.type == Material.SNOW_BLOCK

            for (face in blockFaces) location.block.getRelative(face).type = Material.SNOW_BLOCK
        }

        radioPlayer = RadioSongPlayer(SongReference.entries.random().song)
        radioPlayer.repeatMode = RepeatMode.ALL

        newRound()

        TODO("Not yet implemented")
    }

    private fun newRound() {
        roundNumber++
        radioPlayer.isPlaying = true
        newFloor()

        // TODO increment score system here

        powerUp()
    }

    private fun newFloor() {
        locationsToRemove.clear()
        bombedSquares.clear()

        val selectedMaterial = materials.keys.random()
        val safeSquare1 = groupedSquares.indices.random()
        val safeSquare2 = groupedSquares.indices.random()

        groupedSquares.forEachIndexed { index, loc ->
            {
                val mat = if (index == safeSquare1 || index == safeSquare2) selectedMaterial
                else materials.keys.random()

                if (mat != selectedMaterial) locationsToRemove.add(loc)
                loc.block.type = mat

                for (face in blockFaces) loc.block.getRelative(face).type = mat
            }
        }
    }

    private fun powerUp() {
        var reduceFrequency = remainingPlayers().size < 4 && roundNumber % 4 == 0 // 4 remaining -> every 4th round
        var regularPowerUp = remainingPlayers().size > 4 && roundNumber % 2 == 0 // 5+ remaining -> every 2nd round

        if (reduceFrequency || regularPowerUp) {
            // spawn power-up

            val announcePowerUp: (Player) -> Unit = { player ->
                {
                    player.sendMessage(Component.text())
                    player.sendMessage(
                        Component.text("A power-up has spawned on the floor!").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                    )
                    player.sendMessage(Component.text("Find the beacon on the map to unlock it!", NamedTextColor.GRAY))
                    player.playSound(Sound.BLOCK_NOTE_BLOCK_PLING)
                }
            }
            Util.handlePlayers(eventPlayerAction = announcePowerUp, optedOutAction = announcePowerUp)

            val localLocation = groupedSquares.random()
            this.powerUpLocation = (blockFaces.map { face ->
                localLocation.block.getRelative(face).location
            } + localLocation).random()

            powerUpLocation!!.block.type = Material.BEACON
            powerUpLocation!!.world.spawn(powerUpLocation!!, Firework::class.java) { firework ->
                {
                    firework.fireworkMeta = firework.fireworkMeta.apply {
                        addEffect(
                            FireworkEffect.builder()
                                .with(FireworkEffect.Type.BALL_LARGE)
                                .withColor(Color.FUCHSIA, Color.PURPLE, Color.MAROON).withFade(Color.FUCHSIA, Color.PURPLE, Color.MAROON).build()
                        )
                    }

                    firework.detonate() // TODO this may detonate before the entity is spawned? check during testing
                }
            }


        }
    }

    override fun endGame() {
        super.endGame()
        Util.handlePlayers(eventPlayerAction = { it.gameMode = GameMode.SURVIVAL })
    }

    override fun eliminate(player: Player, reason: EliminationReason) {
        super.eliminate(player, reason)

        Util.handlePlayers(
            eventPlayerAction = {
                val eliminatedMessage = Component.text("${player.displayName()}!").color(NamedTextColor.RED)
                    .append(Component.text(" has been eliminated!").color(NamedTextColor.GRAY))
                it.sendMessage(eliminatedMessage)
            },

            optedOutAction = {

            }

        )

        player.apply {
            inventory.storageContents = arrayOf()
            inventory.setItemInOffHand(null)
            removeActivePotionEffects()

            world.strikeLightning(location)

            if (reason == EliminationReason.ELIMINATED) {
                // TODO fancy teleport to spectator location
                addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 1, false, false, false))
                playSound(Sound.ENTITY_PLAYER_HURT)
            }
        }

        // TODO check if the game can end.
    }

    override fun onPlayerJoin(player: Player) {
        super.onPlayerJoin(player)
    }

    override fun handleGameEvents() {
        listeners += event<PlayerDropItemEvent> { isCancelled = true }
        listeners += event<InventoryClickEvent> { isCancelled = true }

        listeners += event<PlayerMoveEvent> {
            if (player.location.blockY < eliminateBelow) {
                eliminate(player, EliminationReason.ELIMINATED)
            }
        }

        listeners += event<PlayerInteractEvent> {
            if (clickedBlock?.type == Material.BEACON) {
                // safe to assume this is a power-up; no other reason it wouldn't be.
            }
        }

    }
}