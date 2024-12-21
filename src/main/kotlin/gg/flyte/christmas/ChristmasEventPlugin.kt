package gg.flyte.christmas

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.util.MojangAPIUtil
import dev.shreyasayyengar.menuapi.menu.MenuManager
import gg.flyte.christmas.commands.EventCommand
import gg.flyte.christmas.donation.DonationListener
import gg.flyte.christmas.listeners.HousekeepingEventListener
import gg.flyte.christmas.minigame.engine.EventController
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.npc.WorldNPC
import gg.flyte.christmas.util.Colours
import gg.flyte.christmas.util.Util
import gg.flyte.christmas.util.eventController
import gg.flyte.christmas.util.style
import gg.flyte.twilight.extension.playSound
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.time.TimeUnit
import gg.flyte.twilight.twilight
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import org.bukkit.scoreboard.Team.Option
import revxrsal.commands.bukkit.BukkitLamp
import java.util.*
import kotlin.reflect.full.primaryConstructor

class ChristmasEventPlugin : JavaPlugin() {
    lateinit var serverWorld: World
    lateinit var lobbySpawn: MapSinglePoint
    lateinit var scoreBoardTab: Scoreboard
    var cameraPlayer: UUID = UUID.fromString("a008c892-e7e1-48e1-8235-8aa389318b7a") // "devous" | Josh
    var eventController: EventController = EventController()
    var worldNPCs: MutableSet<WorldNPC> = HashSet()

    companion object {
        val instance: ChristmasEventPlugin by lazy { getPlugin(ChristmasEventPlugin::class.java) }
    }

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this).also { it.load() })
    }

    override fun onEnable() {
        logger.info("ChristmasEventPlugin has been enabled!")

        initDependencies()
        createConfig()
        configureWorld()
        registerCommands()
        registerEvents()
        registerPacketAPI()
        loadNPCs()
    }

    override fun onDisable() {
        serverWorld.entities.forEach {
            val kickMessage = Component.empty()
                .append("<colour:#FF3737>ᴛʜᴀɴᴋ ʏᴏᴜ ꜰᴏʀ ᴊᴏɪɴɪɴɢ ᴜs!\n".style())
                .append("<colour:#D6EBFF>❆ <gradient:${Colours.LIGHT_PURPLE.asHexString()}:${Colours.PINK.asHexString()}>ꜰʟʏᴛᴇ</gradient> <gradient:#A5FF7E:#FF1212>ᴡɪsʜᴇs ʏᴏᴜ ᴀ ᴠᴇʀʏ ᴍᴇʀʀʏ ᴄʜʀɪsᴛᴍᴀs!</gradient> <colour:#D6EBFF>❆".style())

            if (it is Player) it.kick(kickMessage) else it.remove()
        } // clean up podium, spectate points, misc entities.

        for (npc in worldNPCs) {
            npc.location.getNearbyEntitiesByType(TextDisplay::class.java, 5.0).forEach { it.remove() }
        }
    }

    private fun initDependencies() {
        twilight(this)
        MenuManager(this)
    }

    private fun createConfig() {
        config.options().copyDefaults(true)
        saveDefaultConfig()
    }

    private fun registerCommands() {
        val lamp = BukkitLamp.builder(this).build()
        lamp.register(EventCommand())
    }

    private fun registerEvents() {
        HousekeepingEventListener()
        DonationListener()
    }

    private fun registerPacketAPI() {
        PacketEvents.getAPI().init()
    }

    private fun loadNPCs() {
        for (contributor in Util.getEventContributors()) {
            val (uniqueId, contribution, location, colour) = contributor
            val displayName = "$colour${MojangAPIUtil.requestPlayerName(uniqueId)}".style()

            val contributorNPC = WorldNPC.createFromUniqueId(displayName, uniqueId, location).also { worldNPCs += it }
            contributorNPC.spawnForAll()

            location.world.spawn(location.clone().add(0.0, 2.5, 0.0), TextDisplay::class.java).apply {
                text("<pink>$contribution".style())
                backgroundColor = Color.fromARGB(150, 0, 0, 0)
                billboard = Display.Billboard.CENTER
            }
        }
    }

    private fun configureWorld() {
        serverWorld = Bukkit.getWorld("world")!!.apply {
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            setGameRule(GameRule.DO_FIRE_TICK, false)
            setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true)
            setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            setStorm(false)
            difficulty = Difficulty.PEACEFUL
            time = 6000
        }
        lobbySpawn = MapSinglePoint(559.5, 103, 518.5, 135, 0)

        // Create Podium Model
        serverWorld.spawn(MapSinglePoint(535.5, 105.0, 503.5), ItemDisplay::class.java) {
            it.setItemStack(ItemStack(Material.PAPER).apply {
                itemMeta = itemMeta?.apply { setCustomModelData(2) }
            })
            it.transformation = it.transformation.apply { this.scale.mul(4F) }
        }

        // Create Event Leaderboard Display
        serverWorld.spawn(MapSinglePoint(535.5, 121, 503.5, -90, 0), TextDisplay::class.java) {
            it.text("<#269123>ᴇᴠᴇɴᴛ\nʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ".style())
            it.transformation = it.transformation.apply { this.scale.mul(10F) }
            it.billboard = Display.Billboard.CENTER
            it.isDefaultBackground = false
            it.backgroundColor = Color.fromARGB(255, 175, 255, 173)
            it.isSeeThrough = false
        }

        // Create Event Contributors Display
        serverWorld.spawn(MapSinglePoint(544.5, 108, 457.5, 0, 30), TextDisplay::class.java) {
            it.text("<colour:#d45757>ᴇᴠᴇɴᴛ\nᴄᴏɴᴛʀɪʙᴜᴛᴏʀꜱ".style())
            it.transformation = it.transformation.apply { this.scale.mul(7F) }
            it.billboard = Display.Billboard.CENTER
            it.isDefaultBackground = false
            it.backgroundColor = Color.fromARGB(255, 255, 207, 207)
            it.brightness = Display.Brightness(15, 15)
            it.isSeeThrough = false
        }

        // Create Donation Info
        serverWorld.spawn(MapSinglePoint(555, 105, 502, 43, 25), TextDisplay::class.java) {
            it.text(
                Component.empty()
                    .append("<gray>    <b>ᴀʙᴏᴜᴛ <colour:#ec8339>ʙᴇsᴛ ꜰʀɪᴇɴᴅs ᴀɴɪᴍᴀʟ sᴏᴄɪᴇᴛʏ</b>\n".style())
                    .append("\n".style())
                    .append("<gray> ᴛʜɪs ᴄʜʀɪsᴛᴍᴀs ᴄʜᴀʀɪᴛʏ ᴇᴠᴇɴᴛ sᴜᴘᴘᴏʀᴛs\n".style())
                    .append("<gray> ᴛʜᴇ <colour:#ec8339>ʙᴇsᴛ ꜰʀɪᴇɴᴅs ᴀɴɪᴍᴀʟ sᴏᴄɪᴇᴛʏ</colour>, ᴀ ʟᴇᴀᴅɪɴɢ\n".style())
                    .append("<gray> ɴᴀᴛɪᴏɴᴀʟ ᴀɴɪᴍᴀʟ ᴡᴇʟꜰᴀʀᴇ ᴏʀɢᴀɴɪsᴀᴛɪᴏɴ\n".style())
                    .append("<gray> ᴅᴇᴅɪᴄᴀᴛᴇᴅ ᴛᴏ ᴇɴᴅɪɴɢ ᴛʜᴇ ᴋɪʟʟɪɴɢ ᴏꜰ ᴄᴀᴛs\n".style())
                    .append("<gray> ᴀɴᴅ ᴅᴏɢs ɪɴ ᴀᴍᴇʀɪᴄᴀ's sʜᴇʟᴛᴇʀs <dark_gray>(ᴅᴜᴇ ᴛᴏ\n".style())
                    .append("<dark_gray> ʜᴏᴍᴇʟᴇssɴᴇss ᴀɴᴅ ᴏᴠᴇʀᴘᴏᴘᴜʟᴀᴛɪᴏɴ)</dark_gray><gray>.\n".style())
                    .append("\n".style())
                    .append("<gray> ᴛʜᴇʏ ᴀʀᴇ ᴀ ɴᴏɴ-ᴘʀᴏꜰɪᴛ ᴛʜᴀᴛ ʀᴜɴs ᴛʜᴇ ʟᴀʀɢᴇsᴛ \n".style())
                    .append("<gray> ɴᴏ-ᴋɪʟʟ ᴍᴏᴠᴇᴍᴇɴᴛ & ᴀɴɪᴍᴀʟ sᴀɴᴄᴛᴜᴀʀʏ ɪɴ\n".style())
                    .append("<gray> ᴛʜᴇ ᴜ.s.\n".style())
                    .append("\n".style())
                    .append("<colour:#ff3d9b> ᴇᴠᴇʀʏ ᴅᴏʟʟᴀʀ ʏᴏᴜ ᴘᴜᴛ ꜰᴏʀᴛʜ ᴛᴏ ᴛʜɪs ɢʀᴇᴀᴛ\n".style())
                    .append("<colour:#ff3d9b> ᴄᴀᴜsᴇ ᴡɪʟʟ ʙᴇ <b><colour:#ae61f2>ᴛʀɪᴘʟᴇ ᴍᴀᴛᴄʜᴇᴅ</colour></b>!\n\n".style())
                    .append("<gray> ʜᴇᴀᴅ ᴏᴠᴇʀ ᴛᴏ <gradient:#ff80e8:#ffffff>ꜰʟʏᴛᴇ.ɢɢ/ᴅᴏɴᴀᴛᴇ</gradient> ᴛᴏ ᴅᴏɴᴀᴛᴇ ɴᴏᴡ!\n\n".style())
                    .append("<gray>          ʟᴇᴀʀɴ ᴍᴏʀᴇ @ <colour:#ec8339>ʙᴇsᴛꜰʀɪᴇɴᴅs.ᴏʀɢ\n".style())
                    .append("<red>           ᴛʜᴀɴᴋ ʏᴏᴜ ꜰᴏʀ ᴊᴏɪɴɪɴɢ ᴜs ❤".style())
            )
            it.lineWidth = 500 // doesn't matter if it's too big, I'm manually adding line breaks
            it.alignment = TextDisplay.TextAlignment.LEFT
            it.transformation = it.transformation.apply { this.scale.mul(2F) }
            it.billboard = Display.Billboard.FIXED
            it.isDefaultBackground = false
            it.backgroundColor = Color.fromARGB(255, 40, 40, 40)
            it.brightness = Display.Brightness(15, 15)
            it.isSeeThrough = false
        }

        WorldNPC.refreshPodium()

        // Create scoreboard:
        // note: player list displays entries by alphabetical order of the team they have entries with
        scoreBoardTab = Bukkit.getScoreboardManager().newScoreboard.apply {
            registerNewTeam("a. staff").apply {
                setOption(Option.COLLISION_RULE, Team.OptionStatus.NEVER)
                setOption(Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
                color(NamedTextColor.GRAY)
                prefix("<red><b>ѕᴛᴀꜰꜰ ".style())
            }
            registerNewTeam("b. player").apply {
                color(NamedTextColor.GRAY)
                setOption(Option.COLLISION_RULE, Team.OptionStatus.NEVER)
                setOption(Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
            }
        }

        eventController.updateDonationBar()

        GameConfig.entries.forEach { it.gameClass.primaryConstructor } // preload/cache classes for reflection
        eventController().startPlaylist()

        repeatingTask(5, TimeUnit.MINUTES) {
            val message = listOf(
                "<#edd900>ꜰᴇᴇʟɪɴɢ ɢᴇɴᴇʀᴏᴜs? <gradient:#A3ADFF:#00FFF4>ᴅᴏɴᴀᴛɪᴏɴs ᴄᴀɴ ᴄʜᴀɴɢᴇ ᴛʜᴇ ɢᴀᴍᴇ - ᴀɴᴅ ɪᴛ's ꜰᴏʀ ᴀ ɢʀᴇᴀᴛ ᴄᴀᴜsᴇ!",
                "<#edd900>ᴄᴀɴ'ᴛ ᴘʟᴀʏ ᴀɴʏᴍᴏʀᴇ? <gradient:#A3ADFF:#00FFF4>ʏᴏᴜ ᴄᴀɴ sᴛɪʟʟ ᴘʟᴀʏ ᴀ ᴘᴀʀᴛ – ᴅᴏɴᴀᴛᴇ ɴᴏᴡ!",
                "<#edd900>ᴇᴠᴇʀʏ ᴅᴏɴᴀᴛɪᴏɴ ɪs ᴛʀɪᴘʟᴇ ᴍᴀᴛᴄʜᴇᴅ! <gradient:#A3ADFF:#00FFF4>sᴏ $10 ᴍᴇᴀɴs $40 ꜰᴏʀ ᴀ ɢʀᴇᴀᴛ ᴄᴀᴜsᴇ.",
                "<#edd900>ᴡᴀɴᴛ ʀᴇᴠᴇɴɢᴇ? <gradient:#A3ADFF:#00FFF4>ᴀ ᴅᴏɴᴀᴛɪᴏɴ ᴍɪɢʜᴛ ᴊᴜsᴛ sʜᴀᴋᴇ ᴛʜɪɴɢs ᴜᴘ ꜰᴏʀ ᴛʜᴏsᴇ sᴛɪʟʟ ᴀʟɪᴠᴇ!",
                "<#edd900>ᴍᴀᴋᴇ ᴀ ᴅᴏɴᴀᴛɪᴏɴ. <gradient:#A3ADFF:#00FFF4>ᴍᴀᴋᴇ sᴏᴍᴇ ᴍᴀʏʜᴇᴍ. ᴍᴀᴋᴇ ᴀ ᴅɪꜰꜰᴇʀᴇɴᴄᴇ!",
                "<#edd900>ᴇᴠᴇʀʏ ᴅᴏʟʟᴀʀ ɢᴏᴇs ᴛᴏ ᴀ ɢᴏᴏᴅ ᴄᴀᴜsᴇ... <gradient:#A3ADFF:#00FFF4>ᴀɴᴅ ᴍᴀʏʙᴇ ᴄᴀᴜsᴇs sᴏᴍᴇ ᴛʀᴏᴜʙʟᴇ ɪɴ-ɢᴀᴍᴇ ᴛᴏᴏ.",
                "<#edd900>ʙᴇ ᴀ ɢᴀᴍᴇ-ᴄʜᴀɴɢᴇʀ. ʟɪᴛᴇʀᴀʟʟʏ. <gradient:#A3ADFF:#00FFF4>ᴅᴏɴᴀᴛɪᴏɴs ᴅᴏ ᴇᴘɪᴄ ɪɴ-ɢᴀᴍᴇ sᴛᴜꜰꜰ!",
                "<#edd900>ɪꜰ ʏᴏᴜ ᴄᴀɴ'ᴛ ᴡɪɴ ᴛʜᴇ ɢᴀᴍᴇ, ᴍᴀᴋᴇ sᴜʀᴇ ᴏᴛʜᴇʀs ᴄᴀɴ'ᴛ ᴇɪᴛʜᴇʀ! <gradient:#A3ADFF:#00FFF4>ᴅᴏɴᴀᴛᴇ ɴᴏᴡ ᴛᴏ ᴄʀᴇᴀᴛᴇ ᴄʜᴀᴏs.",
                "<#edd900>ᴡᴀɴᴛ ᴛᴏ ᴇʟɪᴍɪɴᴀᴛᴇ sᴛᴇᴘʜᴇɴ ʀɪɢʜᴛ ɴᴏᴡ? <gradient:#A3ADFF:#00FFF4>ᴅᴏɴᴀᴛᴇ.",
                "<colour:#ec8339>sᴜᴘᴘᴏʀᴛ ʙᴇsᴛ ꜰʀɪᴇɴᴅs ᴀɴɪᴍᴀʟ sᴏᴄɪᴇᴛʏ. <gradient:#A3ADFF:#00FFF4>ᴅᴏɴᴀᴛᴇ ɴᴏᴡ!",
            ).random()

            val stylised = ("$message [ᴄʟɪᴄᴋ]").style()
                .clickEvent(ClickEvent.openUrl("https://flyte.gg/donate"))
                .hoverEvent(HoverEvent.showText("<gradient:#A3ADFF:#00FFF4>[ᴄʟɪᴄᴋ ᴛᴏ ᴅᴏɴᴀᴛᴇ ɴᴏᴡ!]</gradient><#FF72A6>".style()))

            Bukkit.getOnlinePlayers().forEach {
                it.sendMessage(stylised)
                it.playSound(Sound.BLOCK_NOTE_BLOCK_PLING)
            }
        }
    }
}
