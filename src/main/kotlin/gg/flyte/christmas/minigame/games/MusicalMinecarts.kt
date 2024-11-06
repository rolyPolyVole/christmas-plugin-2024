package gg.flyte.christmas.minigame.games

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.minigame.engine.EventMiniGame
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.minigame.world.MapRegion
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.npc.WorldNPC
import gg.flyte.christmas.util.SongReference
import gg.flyte.christmas.util.Util
import gg.flyte.christmas.util.colourise
import gg.flyte.christmas.util.eventController
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.hidePlayer
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.extension.showPlayer
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.Rail
import org.bukkit.block.data.Rail.Shape
import org.bukkit.entity.Firework
import org.bukkit.entity.Item
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Minecart
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.time.Duration
import kotlin.math.ceil
import kotlin.random.Random

@Suppress("DuplicatedCode") // nature of this game is very similar to other music-controlled games.
class MusicalMinecarts : EventMiniGame(GameConfig.MUSICAL_MINECARTS) {
    private var overviewTasks: MutableList<TwilightRunnable> = mutableListOf()

    private var floorRegion = MapRegion(MapSinglePoint(540, 202, 578), MapSinglePoint(570, 202, 607))
    private var roundNumber = 0
    private var minecarts = mutableListOf<Minecart>()
    private var secondsForRound = 12
    private var powerUpLocation: MapSinglePoint? = null
    private var currentBossBar: BossBar? = null
    private var bossBarTask: TwilightRunnable? = null
    private var gameLogicTask: TwilightRunnable? = null
    private var isCountdownActive = false
    private var harder = false
    private var hasEnded = false
    private var stunnedPlayers = mutableSetOf<Player>()
    private var canEnter = false

    override fun startGameOverview() {
        repeat(25) {
            summonMinecart().also {
                overviewTasks += repeatingTask((0..8).random(), (2..6).random()) {
                    if (!(it.isOnGround)) return@repeatingTask

                    it.velocity = it.velocity.add(
                        Vector(
                            Random.nextDouble(-0.15, 0.15),
                            Random.nextDouble(0.5, 1.0).toDouble(),
                            Random.nextDouble(-0.15, 0.15)
                        )
                    )
                }
            }
        }

        super.startGameOverview()
    }

    override fun preparePlayer(player: Player) {
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE
        player.teleport(gameConfig.spawnPoints.random().randomLocation())
    }

    override fun startGame() {
        overviewTasks.forEach { it.cancel() }
        minecarts.forEach { it.remove() }.also { minecarts.clear() }

        simpleCountdown { newRound() }
        Util.handlePlayers(eventPlayerAction = {
            it.sendMessage(
                Component.text("Remember, do NOT click the minecarts before the music has STOPPED... you will be stunned!", gameConfig.colour)
            )
        })
    }

    private fun newRound() {
        if (hasEnded) return

        roundNumber++
        if (secondsForRound > 2) secondsForRound--

        minecarts.forEach { it.remove() }.also { minecarts.clear() }

        when {
            remainingPlayers().size == 20 && !harder -> {
                harder = true

                Util.handlePlayers(
                    eventPlayerAction = {
                        it.showTitle(Title.title(Component.text("Hard Mode!", gameConfig.colour), Component.text("")))
                        it.sendMessage(Component.text("The minecarts will only spawn when the music STOPS!", NamedTextColor.RED, TextDecoration.BOLD))
                        it.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL)
                    },
                    optedOutAction = {
                        it.sendMessage(Component.text("The game is getting harder!", gameConfig.colour))
                        it.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL)
                    }
                )
            }
        }

        // let song play for a few rounds
        if (roundNumber % 3 == 0) {
            eventController().startPlaylist(SongReference.ALL_I_WANT_FOR_CHRISTMAS_IS_YOU) // beginning makes it hard to differentiate when it has stopped.
        } else {
            eventController().songPlayer?.isPlaying = true
        }

        if (!harder) {
            canEnter = false
            summonCarts()
        } // easy-mode summons carts during new rounds.
        powerUp()

        val delayBeforePrepareElimination = (8..12).random()
        tasks += delay(delayBeforePrepareElimination, TimeUnit.SECONDS) {
            if (!isCountdownActive) { // prevent double-prepare due to random condition
                if (harder) summonCarts() // hard-mode summons carts when music stops.
                prepareElimination()
            }
        }

        remainingPlayers().forEach { eventController().addPoints(it.uniqueId, 10) }
    }

    private fun prepareElimination() {
        isCountdownActive = true
        canEnter = true

        eventController().songPlayer?.isPlaying = false
        remainingPlayers().forEach { it.playSound(Sound.BLOCK_NOTE_BLOCK_BASEDRUM) }

        val timerBar: BossBar = BossBar.bossBar(
            Component.text("Time left: $secondsForRound", gameConfig.colour).decorate(TextDecoration.BOLD),
            1.0f,
            BossBar.Color.RED,
            BossBar.Overlay.PROGRESS
        )

        currentBossBar = timerBar

        Util.handlePlayers(
            eventPlayerAction = { it.showBossBar(timerBar) },
            optedOutAction = { it.showBossBar(timerBar) } // all can see ticker
        )

        val totalTicks = secondsForRound * 20
        var remainingTicks = totalTicks

        // game logic timer
        gameLogicTask = repeatingTask(5, 5) {
            if (remainingTicks <= 0) {
                this.cancel()
                gameLogicTask = null

                powerUpLocation?.block?.type = Material.AIR
                powerUpLocation = null

                remainingPlayers().forEach { if (it.vehicle == null) eliminate(it, EliminationReason.ELIMINATED) }

                isCountdownActive = false

                if (!hasEnded) tasks += delay(80) { newRound() }
            } else {
                remainingPlayers().forEach { it.playSound(Sound.BLOCK_NOTE_BLOCK_BASEDRUM) }
            }
        }

        // BossBar ticker
        bossBarTask = repeatingTask(1, 1) {
            if (remainingTicks <= 0) {
                this.cancel()

                Util.handlePlayers(eventPlayerAction = { it.hideBossBar(timerBar) }, optedOutAction = { it.hideBossBar(timerBar) })
                bossBarTask = null
                currentBossBar = null
            } else {
                val progress = remainingTicks.toDouble() / totalTicks
                timerBar.progress(progress.toFloat())

                val secondsRemaining = ceil(remainingTicks / 20.0).toInt()
                timerBar.name(Component.text("Time left: $secondsRemaining", gameConfig.colour).decorate(TextDecoration.BOLD))
                remainingTicks--
            }
        }

        tasks += bossBarTask
        tasks += gameLogicTask
    }

    override fun eliminate(player: Player, reason: EliminationReason) {
        if (currentBossBar != null) player.hideBossBar(currentBossBar!!)

        Util.handlePlayers(
            eventPlayerAction = {
                val eliminatedMessage = player.displayName().color(NamedTextColor.RED)
                    .append(Component.text(" has been eliminated!").color(NamedTextColor.GRAY))
                it.sendMessage(eliminatedMessage)
            },
            optedOutAction = {
                val eliminatedMessage = player.displayName().color(NamedTextColor.RED)
                    .append(Component.text(" has been eliminated!").color(NamedTextColor.GRAY))
                it.sendMessage(eliminatedMessage)
            }
        )

        player.apply {
            if (allowFlight) allowFlight = false // if had double-jump

            if (reason == EliminationReason.ELIMINATED) {
                if (gameMode != GameMode.SPECTATOR) {
                    Util.handlePlayers(
                        eventPlayerAction = { it.playSound(Sound.ENTITY_ITEM_BREAK) },
                        optedOutAction = { it.playSound(Sound.ENTITY_ITEM_BREAK) }
                    )
                } // don't apply cosmetics if in camera sequence

                val itemDisplay = world.spawn(location, ItemDisplay::class.java) {
                    it.setItemStack(ItemStack(Material.AIR))
                    it.teleportDuration = 59 // max (minecraft limitation)
                }

                addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 4, 1, false, false, false))
                //  TODO impl
                delay(1) {
                    val randomSpecLocation = gameConfig.spectatorSpawnLocations.random()
                    itemDisplay.teleport(randomSpecLocation)
                    itemDisplay.addPassenger(player)
                    player.hidePlayer()

                    delay(59) {
                        itemDisplay.remove()
                        player.teleport(randomSpecLocation)
                        player.showPlayer()
                    }
                }
            } // animate death
        }
        super.eliminate(player, reason)

        if (remainingPlayers().size == 1) endGame()
    }

    override fun endGame() {
        hasEnded = true
        tasks.forEach { it?.cancel() } // this will cancel all game tasks.

        val winner = remainingPlayers().first()
        eventController().points.put(winner.uniqueId, eventController().points[winner.uniqueId]!! + 15)

        Util.handlePlayers(
            eventPlayerAction = {
                it.hideBossBar(if (currentBossBar != null) currentBossBar!! else return@handlePlayers)
            },
            optedOutAction = {
                it.hideBossBar(if (currentBossBar != null) currentBossBar!! else return@handlePlayers)
            },
        )
        doWinAnimation(winner)
    }

    private fun summonCarts() {
        val numCarts = if (remainingPlayers().size == 2) 1 else ceil(remainingPlayers().size * 2 / 3.0).toInt() // ceil condition fails at 2 players
        repeat(numCarts) { summonMinecart() }

        Util.handlePlayers(
            eventPlayerAction = {
                it.playSound(Sound.ENTITY_ITEM_PICKUP)
            },
            optedOutAction = {
                it.playSound(Sound.ENTITY_ITEM_PICKUP)
            }
        )
    }

    private fun powerUp() {
        var reducedFrequency = remainingPlayers().size < 4 && roundNumber % 4 == 0 // 4 remaining -> every 4th round
        var regularPowerUp = remainingPlayers().size > 4 && roundNumber % 2 == 0 // 5+ remaining -> every 2nd round

        if (reducedFrequency || regularPowerUp) {

            val localLocation = floorRegion.randomLocation()
            powerUpLocation = MapSinglePoint(localLocation.blockX, localLocation.blockY + 1.0, localLocation.blockZ)
            powerUpLocation!!.block.type = Material.BEACON
            powerUpLocation!!.world.spawn(powerUpLocation!!, Firework::class.java) {
                it.fireworkMeta = it.fireworkMeta.apply {
                    addEffect(
                        FireworkEffect.builder()
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .withColor(Color.FUCHSIA, Color.PURPLE, Color.MAROON).withFade(Color.FUCHSIA, Color.PURPLE, Color.MAROON).build()
                    )
                }
                it.detonate()
            }

            val notification = Component.text(">> A mysterious power-up has spawned on the floor! <<", gameConfig.colour, TextDecoration.BOLD)
            Util.handlePlayers(
                eventPlayerAction = {
                    it.sendMessage(notification)
                    it.sendMessage(Component.text("Find the beacon on the map to unlock it!", NamedTextColor.GRAY))
                    it.playSound(Sound.BLOCK_NOTE_BLOCK_PLING)
                },
                optedOutAction = {
                    it.sendMessage(notification)
                }
            )
        }
    }

    private fun stunPlayer(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 25, false, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false))
        player.playSound(Sound.ENTITY_ITEM_BREAK)
        player.showTitle(
            Title.title(
                Component.text("Stunned! Too early!", NamedTextColor.RED),
                Component.text("The music has not stopped...", NamedTextColor.GOLD),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(10), Duration.ofMillis(250))
            )
        )

        stunnedPlayers.add(player)

        delay(10, TimeUnit.SECONDS) {
            stunnedPlayers.remove(player)
            player.removePotionEffect(PotionEffectType.SLOWNESS)
            player.removePotionEffect(PotionEffectType.BLINDNESS)
        }
    }

    private fun summonMinecart(): Minecart {
        return ChristmasEventPlugin.instance.serverWorld.spawn(floorRegion.randomLocation().add(0.0, 1.5, 0.0), Minecart::class.java) {
            it.isInvulnerable = true
            it.isSlowWhenEmpty = false
            minecarts.add(it)
        }
    }

    private fun doWinAnimation(player: Player) {
        val worldNPCs = mutableListOf<WorldNPC>()
        val animationTasks = mutableListOf<TwilightRunnable>()
        val poweredRails = mapOf(
            MapRegion(MapSinglePoint(547, 203, 599), MapSinglePoint(563, 203, 599)) to Pair(Shape.EAST_WEST, "left"),
            MapRegion(MapSinglePoint(547, 203, 588), MapSinglePoint(563, 203, 588)) to Pair(Shape.EAST_WEST, "right"),
            MapRegion(MapSinglePoint(564, 203, 589), MapSinglePoint(564, 203, 598)) to Pair(Shape.NORTH_SOUTH, "right"),
            MapRegion(MapSinglePoint(546, 203, 589), MapSinglePoint(546, 203, 598)) to Pair(Shape.NORTH_SOUTH, "left")
        )
        val connectorRails = mapOf(
            MapSinglePoint(546, 203, 599) to Shape.NORTH_EAST,
            MapSinglePoint(564, 203, 599) to Shape.NORTH_WEST,
            MapSinglePoint(564, 203, 588) to Shape.SOUTH_WEST,
            MapSinglePoint(546, 203, 588) to Shape.SOUTH_EAST
        )
        val droppedItems = mutableListOf<Item>()

        connectorRails.forEach { (location, shape) ->
            location.block.apply {
                type = Material.RAIL
                blockData = (blockData as Rail).apply { this.shape = shape }
            }
        }

        poweredRails.forEach { (region, railData) ->
            region.toSingleBlockLocations().forEach {
                it.block.apply {
                    setType(Material.POWERED_RAIL, false)
                    blockData = Bukkit.createBlockData(Material.POWERED_RAIL).apply {
                        (this as Rail).shape = railData.first
                        (this as Powerable).isPowered = true
                    }
                }
            }
        }

        poweredRails.keys.flatMap { it.toSingleBlockLocations() }.forEachIndexed { index, location ->
            if (index % 6 == 0) {
                val direction = Vector(0, 0, 0)
                var value = poweredRails.entries.first { entry -> entry.key.contains(location) }.value

                direction.apply {
                    if (value.first == Shape.NORTH_SOUTH) {
                        setZ(if (value.second == "left") 1 else -1)
                    } else {
                        setX(if (value.second == "left") 1 else -1)
                    }
                }

                location.world.spawn(location.add(0.0, 1.0, 0.0), Minecart::class.java) { minecart ->
                    minecart.maxSpeed = 0.6
                    minecart.velocity = direction.normalize()
                    minecarts.add(minecart)

                    val displayName = "§${listOf("4", "c", "6", "2", "a", "9").random()}${player.name}".colourise()
                    val npc = WorldNPC.createFromLive(displayName, player, location).also { worldNPCs.add(it) }

                    Bukkit.getOnlinePlayers().forEach { loopedPlayer ->
                        npc.spawnFor(loopedPlayer)
                        npc.npc.mainHand = SpigotConversionUtil.fromBukkitItemStack(ItemStack(Material.MINECART))
                        npc.npc.updateEquipment()

                        val passengerPacket = WrapperPlayServerSetPassengers(minecart.entityId, intArrayOf(npc.npc.id))
                        delay(1) { PacketEvents.getAPI().playerManager.getUser(loopedPlayer).sendPacket(passengerPacket) }

                        animationTasks += repeatingTask((2..10).random(), (1..6).random()) {
                            PacketEvents.getAPI().playerManager.getUser(loopedPlayer)
                                .sendPacket(
                                    WrapperPlayServerEntityAnimation(npc.npc.id, WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM)
                                )

                            minecart.world.dropItemNaturally(minecart.location.add(0.0, 1.0, 0.0), ItemStack(Material.MINECART)) {
                                it.velocity = Vector(Random.nextDouble(-1.0, 1.0), Random.nextDouble(0.0, 0.5), Random.nextDouble(-1.0, 1.0))
                                droppedItems.add(it)
                            }
                        } // NPC swing

                        var yaw = 0
                        animationTasks += repeatingTask((2..10).random(), 4) {
                            PacketEvents.getAPI().playerManager.getUser(loopedPlayer).apply {
                                sendPacket(WrapperPlayServerEntityHeadLook(npc.npc.id, yaw.toFloat()))
                                sendPacket(WrapperPlayServerEntityRotation(npc.npc.id, yaw.toFloat(), 0F, true))
                            }
                            yaw += 10
                        } // NPC look
                    }
                }
            }
        }

        delay(20, TimeUnit.SECONDS) {
            Bukkit.getOnlinePlayers().forEach { player -> worldNPCs.forEach { it.despawnFor(player) } }
            minecarts.forEach { it.remove() }
            droppedItems.forEach { it.remove() }
            animationTasks.forEach { it.cancel() }
            poweredRails.keys.flatMap { it.toSingleBlockLocations() }.forEach { it.block.type = Material.AIR }
            connectorRails.keys.forEach { it.block.type = Material.AIR }
            super.endGame()
        }
    }

    override fun handleGameEvents() {
        listeners += event<InventoryClickEvent> { isCancelled = true }

        listeners += event<PlayerToggleSneakEvent> {
            if (player.vehicle != null) isCancelled = true
        }

        listeners += event<VehicleEnterEvent> {
            if (entered !is Player) return@event
            if (entered.vehicle != null) { // prevent moving between minecarts
                isCancelled = true
                return@event
            }

            if (stunnedPlayers.contains(entered as Player)) {
                isCancelled = true
                return@event
            }

            if (!canEnter) {
                stunPlayer(entered as Player)
                isCancelled = true
            }
        }

        listeners += event<PlayerInteractEvent> {
            if (clickedBlock?.type == Material.BEACON) {
                clickedBlock?.type = Material.AIR
                var randomPowerUp = PowerUp.entries.random()

                player.sendMessage(
                    Component.text("You've found a ${randomPowerUp.displayName} power-up!")
                        .color(NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                )

                Util.handlePlayers(
                    eventPlayerAction = {
                        if (it != player) {
                            it.sendMessage(
                                Component.text(">> ${player.displayName()} has found a {${randomPowerUp.displayName} power-up! <<")
                                    .color(NamedTextColor.GREEN)
                                    .decorate(TextDecoration.BOLD)
                            )
                        }
                    },
                    optedOutAction = {
                        it.sendMessage(
                            Component.text(">> ${player.displayName()} has found a {${randomPowerUp.displayName} power-up! <<")
                                .color(NamedTextColor.GREEN)
                                .decorate(TextDecoration.BOLD)
                        )
                    }
                )

                when (randomPowerUp) {
                    PowerUp.ENDER_PEARL -> player.inventory.setItem(0, ItemStack(Material.ENDER_PEARL, 1))

                    PowerUp.JUMP_BOOST -> player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 8, 3, false, false, false))

                    PowerUp.FISHING_ROD -> player.inventory.setItem(0, ItemStack(Material.FISHING_ROD, 1))

                    PowerUp.SLOWNESS -> player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 20 * 10, 2, false, false, false))

                    PowerUp.BLINDNESS -> player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 10, 2, false, false, false))

                    PowerUp.RANDOM_TP -> player.teleport(floorRegion.randomLocation())

                    PowerUp.PUSH_SELF -> player.velocity = player.location.direction.multiply(2).add(Vector(0.0, 1.5, 0.0))

                    PowerUp.PUSH_RANDOM -> {
                        remainingPlayers().random().apply {
                            velocity = this.location.direction.multiply(2).add(Vector(0.0, 1.5, 0.0))
                            sendMessage(Component.text("You've been pushed by a power-up!").color(gameConfig.colour))
                        }
                    }

                    PowerUp.DOUBLE_JUMP -> {
                        player.allowFlight = true
                    }

                    PowerUp.EXTRA_CART -> {
                        player.inventory.addItem(ItemStack(Material.MINECART, 1))
                    }
                }
            }

            if (item?.type == Material.MINECART) {
                if (player.vehicle == null) {
                    val minecart = summonMinecart()
                    minecart.addPassenger(player)
                    minecarts.add(minecart)
                    item?.amount = item?.amount?.minus(1) ?: 0
                }
            }
        }

        listeners += event<PlayerToggleFlightEvent> {
            if (!(remainingPlayers().contains(player))) return@event // OP'd players need to be able to fly
            isCancelled = true
            player.allowFlight = false
            player.isFlying = false

            player.velocity = player.location.direction.multiply(0.5).add(Vector(0.0, 1.0, 0.0))
        } // double-jump

        listeners += event<BlockPhysicsEvent> {
            if (block.type == Material.POWERED_RAIL) isCancelled = true
        }
    }

    private enum class PowerUp(
        val displayName: String,
    ) {
        ENDER_PEARL("Ender Pearl"),
        JUMP_BOOST("Jump Boost"),
        FISHING_ROD("Fishing Rod"),
        SLOWNESS("Slowness"),
        BLINDNESS("Blindness"),
        RANDOM_TP("Random TP"),
        PUSH_SELF("Random Self-Boost"),
        PUSH_RANDOM("Random Player Boost"),
        DOUBLE_JUMP("Double Jump"),
        EXTRA_CART("Extra Minecart"),
    }
}
