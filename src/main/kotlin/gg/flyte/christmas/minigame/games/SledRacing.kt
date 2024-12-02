package gg.flyte.christmas.minigame.games

import gg.flyte.christmas.minigame.engine.EventMiniGame
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.minigame.world.MapRegion
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.util.formatInventory
import gg.flyte.christmas.util.style
import gg.flyte.christmas.util.title
import gg.flyte.christmas.util.titleTimes
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.repeatingTask
import io.papermc.paper.entity.TeleportFlag
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.entity.boat.OakBoat
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.time.Duration
import java.util.*

class SledRacing : EventMiniGame(GameConfig.SLED_RACING) {
    private val finishLine = MapRegion(MapSinglePoint(253, 29, 1908), MapSinglePoint(255, 44, 1925))
    private val checkPointToNext = mutableMapOf<MapRegion, MapRegion>().apply {
        put(gameConfig.spawnPoints.first(), gameConfig.spawnPoints.first()) // first checkpoint is the same as spawn point
        put(
            MapRegion(MapSinglePoint(497, 139, 1764), MapSinglePoint(493, 122, 1794)),
            MapRegion(MapSinglePoint(507, 129, 1774), MapSinglePoint(498, 149, 1783))
        )
        put(
            MapRegion(MapSinglePoint(554, 110, 1774), MapSinglePoint(572, 125, 1778)),
            MapRegion(MapSinglePoint(571, 115, 1758), MapSinglePoint(560, 115, 1769))
        )
        put(
            MapRegion(MapSinglePoint(512, 111, 1891), MapSinglePoint(524, 98, 1912)),
            MapRegion(MapSinglePoint(531, 100, 1890), MapSinglePoint(522, 100, 1899))
        )
        put(
            MapRegion(MapSinglePoint(467, 58, 1827), MapSinglePoint(452, 45, 1835)),
            MapRegion(MapSinglePoint(470, 47, 1832), MapSinglePoint(463, 47, 1839))
        )
    } /* area criteria -> spawn area */
    private val currentCheckPoint = mutableMapOf<UUID, MapRegion>()
    private val lapsCompleted = mutableMapOf<UUID, Int>()
    private val lapsRequired = 3
    private var hasStarted = false

    override fun preparePlayer(player: Player) {
        currentCheckPoint[player.uniqueId] = checkPointToNext.values.first()

        player.formatInventory()
        player.gameMode = GameMode.ADVENTURE
        player.teleport(gameConfig.spawnPoints.random().randomLocation())

        resetPlayerToLastCheckPoint(player)
    }

    override fun startGame() {
        simpleCountdown {
            hasStarted = true

            tasks += repeatingTask(1) {
                for (player in Bukkit.getOnlinePlayers()) {
                    if (player.vehicle is Boat) {
                        (player.passengers.first() as ArmorStand).setRotation(player.vehicle!!.yaw, 0F)
                    }
                }
            } // autocorrect armor stand rotation to boat rotation
        }
    }

    override fun eliminate(player: Player, reason: EliminationReason) {
        super.eliminate(player, reason)
    }

    override fun endGame() {
        super.endGame()
    }

    private fun updatePlayerCheckPoint(player: Player, checkPoint: MapRegion) {
        val playerUUID = player.uniqueId
        val currentCheckpoint = currentCheckPoint[playerUUID] ?: return
        if (currentCheckpoint == checkPoint) return

        currentCheckPoint[playerUUID] = checkPoint
        player.title(
            "<game_colour>ᴄʜᴇᴄᴋᴘᴏɪɴᴛ ʀᴇᴀᴄʜᴇᴅ!".style(), Component.empty(),
            titleTimes(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(500))
        )
    }

    private fun handleFinishLineCross(player: Player) {
        val playerUUID = player.uniqueId
        val lapsCompleted = lapsCompleted.getOrDefault(playerUUID, 0) + 1

        if (lapsCompleted >= lapsRequired) {
            player.sendMessage("<gold>ᴄᴏɴɢʀᴀᴛᴜʟᴀᴛɪᴏɴs! ʏᴏᴜ ʜᴀᴠᴇ ᴄᴏᴍᴘʟᴇᴛᴇᴅ ᴛʜᴇ ʀᴀᴄᴇ!".style())
        } else {
            this.lapsCompleted[playerUUID] = lapsCompleted
            currentCheckPoint[playerUUID] = checkPointToNext.values.first()
            resetPlayerToLastCheckPoint(player)
            player.title(
                "<game_colour>ʟᴀᴘ $lapsCompleted/$lapsRequired ᴄᴏᴍᴘʟᴇᴛᴇᴅ!".style(), Component.empty(),
                titleTimes(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(500))
            )
        }
    }

    private fun resetPlayerToLastCheckPoint(player: Player) {
        val lastCheckPoint = currentCheckPoint[player.uniqueId] ?: return
        println(lastCheckPoint.randomLocation())

        player.vehicle?.remove()
        player.passengers.forEach { it.remove() }

        player.teleport(lastCheckPoint.randomLocation(), TeleportFlag.EntityState.RETAIN_VEHICLE)
        player.world.spawn(player.location, OakBoat::class.java) {
            it.addPassenger(player)
            it.setRotation(127F, 0F)

            player.world.spawn(player.location, ArmorStand::class.java) {
                it.isInvisible = true
                it.isInvulnerable = true
                it.equipment.helmet = ItemStack(Material.PAPER).apply {
                    itemMeta = itemMeta.apply { setCustomModelData(1) }
                }

                player.addPassenger(it)
            }
        }

        player.playSound(Sound.BLOCK_NOTE_BLOCK_BASS)
    }

    override fun handleGameEvents() {
        event<PlayerToggleSneakEvent> {
            if (player.vehicle is Boat) {
                if (this.isSneaking) {
                    isCancelled = true
                    resetPlayerToLastCheckPoint(player)
                }
            }
        }

        event<VehicleMoveEvent> {
            if (vehicle !is Boat) return@event

            if (!hasStarted) vehicle.velocity = Vector(0, 0, 0)

            val player = vehicle.passengers.firstOrNull() as? Player ?: return@event
            val currentLocation = vehicle.location

            // check what checkpoint the player is in
            checkPointToNext.forEach { (areaCriteria, _) ->
                if (areaCriteria.contains(currentLocation)) {
                    updatePlayerCheckPoint(player, areaCriteria)
                    return@forEach
                }
            }

            // check if the player crossed the finish line
            if (finishLine.contains(currentLocation)) handleFinishLineCross(player)
        }
    }
}