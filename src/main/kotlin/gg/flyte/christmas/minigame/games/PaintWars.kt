package gg.flyte.christmas.minigame.games

import com.google.common.collect.HashBiMap
import gg.flyte.christmas.donation.DonationTier
import gg.flyte.christmas.minigame.engine.EventMiniGame
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.minigame.engine.PlayerType
import gg.flyte.christmas.util.Util
import gg.flyte.christmas.util.eventController
import gg.flyte.christmas.util.formatInventory
import gg.flyte.christmas.util.style
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.random.Random

class PaintWars : EventMiniGame(GameConfig.PAINT_WARS) {
    private var gameTime = 90
    private val paintMaterials = mutableListOf(
        Material.RED_WOOL,
        Material.ORANGE_WOOL,
        Material.YELLOW_WOOL,
        Material.LIME_WOOL,
        Material.GREEN_WOOL,
        Material.LIGHT_BLUE_WOOL,
        Material.CYAN_WOOL,
        Material.BLUE_WOOL,
        Material.PURPLE_WOOL,
        Material.PINK_WOOL,
        Material.MAGENTA_WOOL,
        Material.LIGHT_GRAY_WOOL,
        Material.GRAY_WOOL,
        Material.BROWN_WOOL,
        Material.BLACK_WOOL,
        Material.WHITE_WOOL,
        Material.RED_CONCRETE,
        Material.ORANGE_CONCRETE,
        Material.YELLOW_CONCRETE,
        Material.LIME_CONCRETE,
        Material.GREEN_CONCRETE,
        Material.LIGHT_BLUE_CONCRETE,
        Material.CYAN_CONCRETE,
        Material.BLUE_CONCRETE,
        Material.PURPLE_CONCRETE,
        Material.PINK_CONCRETE,
        Material.MAGENTA_CONCRETE,
        Material.LIGHT_GRAY_CONCRETE,
        Material.GRAY_CONCRETE,
        Material.BROWN_CONCRETE,
        Material.BLACK_CONCRETE,
        Material.WHITE_CONCRETE,
        Material.RED_GLAZED_TERRACOTTA,
        Material.ORANGE_GLAZED_TERRACOTTA,
        Material.YELLOW_GLAZED_TERRACOTTA,
        Material.LIME_GLAZED_TERRACOTTA,
        Material.GREEN_GLAZED_TERRACOTTA,
        Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
        Material.CYAN_GLAZED_TERRACOTTA,
        Material.BLUE_GLAZED_TERRACOTTA,
        Material.PURPLE_GLAZED_TERRACOTTA,
        Material.PINK_GLAZED_TERRACOTTA,
        Material.MAGENTA_GLAZED_TERRACOTTA,
        Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
        Material.GRAY_GLAZED_TERRACOTTA,
        Material.BROWN_GLAZED_TERRACOTTA,
        Material.BLACK_GLAZED_TERRACOTTA,
        Material.WHITE_GLAZED_TERRACOTTA,
        Material.RED_STAINED_GLASS,
        Material.ORANGE_STAINED_GLASS,
        Material.YELLOW_STAINED_GLASS,
        Material.LIME_STAINED_GLASS,
        Material.GREEN_STAINED_GLASS,
        Material.LIGHT_BLUE_STAINED_GLASS,
        Material.CYAN_STAINED_GLASS,
        Material.BLUE_STAINED_GLASS,
        Material.PURPLE_STAINED_GLASS,
        Material.PINK_STAINED_GLASS,
        Material.MAGENTA_STAINED_GLASS,
        Material.LIGHT_GRAY_STAINED_GLASS,
        Material.GRAY_STAINED_GLASS,
        Material.BROWN_STAINED_GLASS,
        Material.BLACK_STAINED_GLASS,
        Material.WHITE_STAINED_GLASS,
        Material.RED_SHULKER_BOX,
        Material.ORANGE_SHULKER_BOX,
        Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX,
        Material.GREEN_SHULKER_BOX,
        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.CYAN_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX,
        Material.PURPLE_SHULKER_BOX,
        Material.PINK_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX,
        Material.GRAY_SHULKER_BOX,
        Material.BROWN_SHULKER_BOX,
        Material.BLACK_SHULKER_BOX,
        Material.WHITE_SHULKER_BOX
    ) // only *really* supports 79 players...
    private val playerBrushesBiMap = HashBiMap.create<UUID, Material>()
    private val changedBlocks = mutableListOf<Block>()
    private val scores = mutableMapOf<UUID, Int>()
    private var started = false
    private var materialIndex = 0
    private var splashRadius = 1

    // ticks left | total ticks
    private var rapidFireTickData: Pair<Int, Int> = 0 to 0

    // lateinit since <game_colour> is not mapped yet at time of init
    private lateinit var rapidFireBossBar: BossBar

    override fun preparePlayer(player: Player) {
        player.gameMode = GameMode.ADVENTURE
        player.formatInventory()
        player.teleport(gameConfig.spawnPoints.random().randomLocation())

        scores[player.uniqueId] = 0

        ItemStack(Material.BRUSH).apply {
            itemMeta = itemMeta.apply {
                displayName("<!i><game_colour><b>ᴘᴀɪɴᴛ ʙʀᴜѕʜ".style())
                lore(listOf("<grey>ᴜѕᴇ ᴛʜɪѕ ᴛᴏ ᴘᴀɪɴᴛ ᴛʜᴇ ᴍᴀᴘ!".style()))
            }
        }.apply { player.inventory.setItem(0, this) }

        if (paintMaterials.getOrElse(materialIndex) { null } == null) {
            var randomMaterial = Material.entries.toTypedArray().filter { it.isBlock }.random()
            while (playerBrushesBiMap.inverse().containsKey(randomMaterial)) {
                randomMaterial = Material.entries.toTypedArray().random()
            }
        } else {
            playerBrushesBiMap[player.uniqueId] = paintMaterials[materialIndex].also { player.inventory.setItem(1, ItemStack(it)) }
            materialIndex++
        }
    }

    override fun startGame() {
        eventController().sidebarManager.dataSupplier = scores

        simpleCountdown {
            started = true
            donationEventsEnabled = true
            rapidFireBossBar = BossBar.bossBar("<game_colour><b>ʀᴀᴘɪᴅ ꜰɪʀᴇ".style(), 1.0F, BossBar.Color.RED, BossBar.Overlay.PROGRESS)

            tasks += repeatingTask(1, TimeUnit.SECONDS) {
                gameTime--

                if (gameTime == 0) {
                    cancel()
                    endGame()
                }

                updateScoreboard()
            }
        }
    }

    override fun endGame() {
        tasks.forEach { it?.cancel() } // this will cancel all game tasks.
        started = false
        donationEventsEnabled = false

        changedBlocks.forEach { it.type = Material.WHITE_WOOL }
        playerBrushesBiMap.clear()
        changedBlocks.clear()
        for (entry in scores) eventController().addPoints(entry.key, entry.value)

        scores.entries
            .sortedByDescending { it.value }
            .take(3)
            .also {
                it.forEach {
                    formattedWinners[it.key] = "${it.value} ʙʟᴏᴄᴋ${if (it.value == 1) "" else "s"}"
                }
            }

        Util.runAction(PlayerType.PARTICIPANT) {
            it.hideBossBar(rapidFireBossBar)
        }

        super.endGame()
    }

    private fun updateScoreboard() {
        val timeLeft = "<aqua>ᴛɪᴍᴇ ʟᴇꜰᴛ: <red><b>${gameTime}".style()
        Bukkit.getOnlinePlayers().forEach { eventController().sidebarManager.updateLines(it, listOf(Component.empty(), timeLeft)) }
    }

    fun tryUpdateBlock(block: Block, player: Player, overrideRandom: Boolean) {
        if (!overrideRandom && Random.nextDouble() < 0.25) return // 75% chance to paint block
        if (block.type == Material.AIR || block.type == Material.LIGHT) return
        if (block.type == playerBrushesBiMap[player.uniqueId]) return // block already painted by same player

        // if the block has no contact with any air blocks, don't paint it (it's surrounded by other blocks)
        var canChange = false
        BlockFace.entries.forEach {
            if (block.getRelative(it).type == Material.AIR) canChange = true
        }
        if (!canChange) return

        // decrement the score of previous painter:
        playerBrushesBiMap.inverse()[block.type]?.let { previousPlayerId ->
            scores[previousPlayerId]!!.also { scores[previousPlayerId] = it - 1 }
        }

        // increment the score of current painter:
        scores[player.uniqueId] = scores.getOrDefault(player.uniqueId, 0) + 1

        if (!(changedBlocks.contains(block))) changedBlocks.add(block)
        block.type = playerBrushesBiMap[player.uniqueId]!!

        if (!overrideRandom) {
            repeat(10) {
                block.world.spawnParticle(Particle.BLOCK, block.location.add(0.5, 0.3, 0.5), 10, 0.5, 0.5, 0.5, block.type.createBlockData())
            }
        }
    }

    private fun rapidFireEnabled() = rapidFireTickData.first > 0

    override fun handleGameEvents() {
        listeners += event<PlayerInteractEvent> {
            if (!started) return@event

            if (!hasItem()) return@event
            if (item!!.type != Material.BRUSH) return@event
            if (!(action.name.lowercase().contains("right"))) return@event

            if (rapidFireEnabled()) {
                var times = 0 // must reach 4
                repeatingTask(1) {
                    if (times == 4) {
                        cancel()
                        return@repeatingTask
                    }
                    player.launchProjectile(Snowball::class.java).apply { item = ItemStack(playerBrushesBiMap[player.uniqueId]!!) }
                    times++
                }
            } else {
                // singly shoot
                player.launchProjectile(Snowball::class.java).apply { item = ItemStack(playerBrushesBiMap[player.uniqueId]!!) }
            }
        }

        listeners += event<InventoryOpenEvent> { if (inventory.type == InventoryType.SHULKER_BOX) isCancelled = true }

        listeners += event<ProjectileHitEvent> {
            if (!started) return@event

            if (hitBlock == null) return@event
            // if hit block was white wool, or any paintable block
            if (!(hitBlock!!.type == Material.WHITE_WOOL || playerBrushesBiMap.inverse().containsKey(hitBlock!!.type))) return@event

            hitBlock!!.world.playSound(hitBlock!!.location, Sound.ENTITY_PLAYER_SPLASH, 0.5F, 1.0f)
            tryUpdateBlock(hitBlock!!, entity.shooter as Player, false)

            val hitBlockLocation = hitBlock!!.location
            for (x in -splashRadius..splashRadius) {
                for (y in -splashRadius..splashRadius) {
                    for (z in -splashRadius..splashRadius) {
                        tryUpdateBlock(
                            hitBlock!!.world.getBlockAt(
                                Location(
                                    hitBlock!!.world,
                                    hitBlockLocation.x + x,
                                    hitBlockLocation.y + y,
                                    hitBlockLocation.z + z
                                )
                            ),
                            entity.shooter as Player,
                            false,
                        )
                    }
                }
            }
        }

        listeners += event<InventoryClickEvent> { isCancelled = true }
    }

    override fun handleDonation(tier: DonationTier, donorName: String?) {
        when (tier) {
            DonationTier.LOW -> {
                remainingPlayers().forEach {
                    // find the brush in the player's inventory
                    val brush = it.inventory.contents.firstOrNull { item -> item?.type == Material.BRUSH }
                    brush?.addUnsafeEnchantment(Enchantment.UNBREAKING, 1)
                    brush?.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE)

                    it.showBossBar(rapidFireBossBar)
                }

                if (rapidFireEnabled()) {
                    // extend duration if already active
                    rapidFireTickData = rapidFireTickData.let { it.first + (20 * 20) to it.second + (20 * 20) }
                } else {
                    // set initial duration
                    rapidFireTickData = 20 * 20 to 20 * 20
                    tasks += repeatingTask(1) {
                        val (ticksLeft, totalTicks) = rapidFireTickData
                        rapidFireBossBar.progress(Math.clamp(ticksLeft / totalTicks.toFloat(), 0.0F, 1.0F))

                        if (ticksLeft == 0) {
                            cancel()
                            remainingPlayers().forEach {
                                val goldenHoe = it.inventory.contents.firstOrNull { item -> item?.type == Material.BRUSH }
                                goldenHoe?.removeEnchantment(Enchantment.UNBREAKING)
                                it.hideBossBar(rapidFireBossBar)
                            }
                        } else {
                            rapidFireTickData = ticksLeft - 1 to totalTicks
                        }
                    }
                }

                announceDonationEvent("<game_colour>ʀᴀᴘɪᴅ ꜰɪʀᴇ ɪs ᴏɴ! (${if (donorName != null) "<aqua>$donorName's</aqua> ᴅᴏɴᴀᴛɪᴏɴ" else "ᴅᴏɴᴀᴛɪᴏɴ"})".style())
            }

            DonationTier.MEDIUM -> {
                splashRadius++
                announceDonationEvent("<game_colour>ꜱᴘʟᴀꜱʜ ʀᴀᴅɪᴜꜱ ʜᴀꜱ ʙᴇᴇɴ ɪɴᴄʀᴇᴀꜱᴇᴅ! (${if (donorName != null) "<aqua>$donorName's</aqua> ᴅᴏɴᴀᴛɪᴏɴ" else "ᴅᴏɴᴀᴛɪᴏɴ"})".style())
            }

            DonationTier.HIGH -> {
                changedBlocks.forEach { it.type = Material.WHITE_WOOL }.also { changedBlocks.clear() }
                scores.clear()
                remainingPlayers().forEach {
                    scores[it.uniqueId] = 0
                    it.playSound(Sound.ENTITY_WITHER_DEATH)
                }

                announceDonationEvent("<game_colour>ᴄʟᴇᴀʀᴇᴅ ᴀʟʟ ᴘᴀɪɴᴛ ᴀɴᴅ ꜱᴄᴏʀᴇꜱ! (${if (donorName != null) "<aqua>$donorName's</aqua> ᴅᴏɴᴀᴛɪᴏɴ" else "ᴅᴏɴᴀᴛɪᴏɴ"})".style())
            }
        }
    }
}
