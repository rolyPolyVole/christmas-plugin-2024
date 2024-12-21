package gg.flyte.christmas.minigame.games

import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.donation.DonationTier
import gg.flyte.christmas.minigame.engine.EventMiniGame
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.minigame.engine.PlayerType
import gg.flyte.christmas.minigame.world.MapRegion
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.util.*
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.repeatingTask
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Boat
import org.bukkit.entity.Player
import org.bukkit.entity.boat.OakBoat
import org.bukkit.event.player.PlayerInteractEntityEvent
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
            MapRegion(MapSinglePoint(503, 129, 1781, 103, 0), MapSinglePoint(498, 129, 1775, 103, 0))
        )
        put(
            MapRegion(MapSinglePoint(554, 110, 1774), MapSinglePoint(572, 125, 1778)),
            MapRegion(MapSinglePoint(571, 115, 1758, 0, 0), MapSinglePoint(560, 115, 1769, 0, 0))
        )
        put(
            MapRegion(MapSinglePoint(512, 111, 1891), MapSinglePoint(524, 98, 1912)),
            MapRegion(MapSinglePoint(531, 101, 1890, 45, 0), MapSinglePoint(522, 101, 1899, 45, 0))
        )
        put(
            MapRegion(MapSinglePoint(467, 58, 1827), MapSinglePoint(452, 45, 1835)),
            MapRegion(MapSinglePoint(470, 48, 1832, 135, 0), MapSinglePoint(463, 48, 1839, 135, 0))
        )
    } /* area criteria -> spawn area */
    private val currentCheckPoint = mutableMapOf<UUID, MapRegion>()
    private val lapsCompleted = mutableMapOf<UUID, Int>()
    private val lapsRequired = 3
    private var hasStarted = false
    private var gameTime = 421
    private var scores = mutableMapOf<UUID, Int>()

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
            donationEventsEnabled = true

            tasks += repeatingTask(1) {
                Util.runAction(PlayerType.PARTICIPANT) {
                    if (it.vehicle is Boat) {
                        (it.passengers.first() as ArmorStand).setRotation(it.vehicle!!.yaw, 0F)
                    }
                }
            } // autocorrect armor stand rotation to boat rotation

            tasks += repeatingTask(40) {
                Util.runAction(PlayerType.PARTICIPANT) { it.sendActionBar("<game_colour>ᴘʀᴇss <#6b6b6b><b><key:key.sneak></b></#6b6b6b> ᴛᴏ ʀᴇsᴇᴛ ᴛᴏ ʏᴏᴜʀ ᴄʜᴇᴄᴋᴘᴏɪɴᴛ".style()) }
            }

            tasks += repeatingTask(20) {
                val timeLeft = "<aqua>ᴛɪᴍᴇ ʟᴇꜰᴛ: <red><b>${gameTime}".style()
                Bukkit.getOnlinePlayers().forEach { eventController().sidebarManager.updateLines(it, listOf(Component.empty(), timeLeft)) }

                gameTime--
                if (gameTime <= 0) endGame()
            }

            ChristmasEventPlugin.instance.eventController.sidebarManager.dataSupplier = scores
        }
    }

    override fun endGame() {
        tasks.forEach { it?.cancel() } // this will cancel all game tasks.
        donationEventsEnabled = false

        scores.entries.sortedBy { it.value }.take(3).forEachIndexed { index, (uuid, _) ->
            if (index == 0) {
                formattedWinners.putIfAbsent(uuid, "1sᴛ ᴘʟᴀᴄᴇ")
            } else if (index == 1) {
                formattedWinners.putIfAbsent(uuid, "2ɴᴅ ᴘʟᴀᴄᴇ")
            } else if (index == 2) {
                formattedWinners.putIfAbsent(uuid, "3ʀᴅ ᴘʟᴀᴄᴇ")
            }
        }

        ChristmasEventPlugin.instance.serverWorld.entities.forEach { if (it is ArmorStand || it is Boat) it.remove() }
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
        player.playSound(Sound.ITEM_LODESTONE_COMPASS_LOCK)
        scores[playerUUID] = scores.getOrDefault(playerUUID, 0) + 1
    }

    private fun handleFinishLineCross(player: Player) {
        val playerUUID = player.uniqueId
        val lapsCompleted = lapsCompleted.getOrDefault(playerUUID, 0) + 1

        if (lapsCompleted >= lapsRequired) {
            Util.runAction(PlayerType.PARTICIPANT) { it.sendMessage("<grey>${player.name} <game_colour>ʜᴀs ᴄᴏᴍᴘʟᴇᴛᴇᴅ ᴛʜᴇ ʀᴀᴄᴇ!".style()) }

            player.title(
                "<game_colour>ᴄᴏɴɢʀᴀᴛᴜʟᴀᴛɪᴏɴs!".style(), "<grey>ʏᴏᴜ ʜᴀᴠᴇ ᴄᴏᴍᴘʟᴇᴛᴇᴅ ᴛʜᴇ ʀᴀᴄᴇ!".style(),
                titleTimes(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(500))
            )
            player.playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE)
            currentCheckPoint.remove(playerUUID)
            scores[playerUUID] = scores.getOrDefault(playerUUID, 0) + 1

        } else {
            this.lapsCompleted[playerUUID] = lapsCompleted
            currentCheckPoint[playerUUID] = checkPointToNext.values.first()
            resetPlayerToLastCheckPoint(player)
            player.title(
                "<game_colour>ʟᴀᴘ $lapsCompleted/$lapsRequired ᴄᴏᴍᴘʟᴇᴛᴇᴅ!".style(), Component.empty(),
                titleTimes(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(500))
            )
            player.playSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH)
        }
    }

    private fun resetPlayerToLastCheckPoint(player: Player) {
        val lastCheckPointLocation = currentCheckPoint[player.uniqueId]?.randomLocation() ?: return

        player.vehicle?.remove()
        player.passengers.forEach { it.remove() }
        player.teleport(lastCheckPointLocation.clone())
        player.playSound(Sound.BLOCK_NOTE_BLOCK_BASS)

        val boat = CollisionlessBoat().getBukkitEntity()
        boat.spawnAt(lastCheckPointLocation)
        boat.addPassenger(player)
        player.world.spawn(player.location, ArmorStand::class.java) { sledModelStand ->
            sledModelStand.isInvisible = true
            sledModelStand.isInvulnerable = true
            sledModelStand.equipment.helmet = ItemStack(Material.PAPER).apply {
                itemMeta = itemMeta.apply { setCustomModelData(1) }
            }
            player.addPassenger(sledModelStand)
        }
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

            val player = vehicle.passengers.firstOrNull() as? Player ?: return@event
            if (!hasStarted || !(currentCheckPoint.contains(player.uniqueId))) vehicle.velocity = Vector(0, 0, 0)

            val currentLocation = vehicle.location

            // check what checkpoint the player is in
            checkPointToNext.forEach { (areaCriteria, spawnArea) ->
                if (areaCriteria.contains(currentLocation)) {
                    updatePlayerCheckPoint(player, spawnArea)
                    return@forEach
                }
            }

            // check if the player crossed the finish line
            if (finishLine.contains(currentLocation)) handleFinishLineCross(player)
        }

        event<PlayerInteractEntityEvent> {
            if (rightClicked is OakBoat) isCancelled = true
        }
    }

    override fun handleDonation(tier: DonationTier, donorName: String?) {} // ignored.

    private class CollisionlessBoat : net.minecraft.world.entity.vehicle.Boat(
        EntityType.OAK_BOAT,
        (ChristmasEventPlugin.instance.serverWorld as CraftWorld).handle.level,
        { Items.OAK_BOAT }
    ) {
        override fun canBeCollidedWith() = false
        override fun canCollideWith(other: Entity) = false
        override fun canCollideWithBukkit(entity: Entity) = false
        override fun isCollidable(ignoreClimbing: Boolean) = false
        override fun isColliding(pos: BlockPos, state: BlockState) = false
    }
}
// TODO add points in this