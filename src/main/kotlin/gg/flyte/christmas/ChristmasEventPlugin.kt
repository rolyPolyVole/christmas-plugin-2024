package gg.flyte.christmas

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.util.MojangAPIUtil
import dev.shreyasayyengar.menuapi.menu.MenuManager
import gg.flyte.christmas.commands.EventCommand
import gg.flyte.christmas.donation.DonationListener
import gg.flyte.christmas.donation.RefreshToken
import gg.flyte.christmas.listeners.HousekeepingEventListener
import gg.flyte.christmas.minigame.engine.EventController
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.npc.WorldNPC
import gg.flyte.christmas.util.Util
import gg.flyte.christmas.util.eventController
import gg.flyte.christmas.util.style
import gg.flyte.twilight.twilight
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
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
        fixData()
        registerCommands()
        registerEvents()
        registerPacketAPI()
        handleDonations()
        loadContributorNPCs()
    }

    override fun onDisable() {
        for (npc in worldNPCs) {
            npc.location.getNearbyEntitiesByType(TextDisplay::class.java, 5.0).forEach { it.remove() }
        }
    }

    private fun initDependencies() {
        twilight(this)
        MenuManager(this)
    }

    private fun createConfig() {
        config.options().configuration()
        saveDefaultConfig()
    }

    private fun registerCommands() {
        val lamp = BukkitLamp.builder(this).build()
        lamp.register(EventCommand())
    }

    private fun registerEvents() {
        HousekeepingEventListener()
    }

    private fun registerPacketAPI() {
        PacketEvents.getAPI().init()
    }

    private fun handleDonations() {
        RefreshToken(
            config.getString("donations.clientId") ?: throw IllegalArgumentException("clientId cannot be empty"),
            config.getString("donations.clientSecret") ?: throw IllegalArgumentException("clientSecret cannot be empty")
        )
        DonationListener(config.getString("donations.campaignId") ?: throw IllegalArgumentException("campaignId cannot be empty"))
    }

    private fun loadContributorNPCs() {
        for (contributor in Util.getEventContributors()) {
            val uniqueId = contributor.uniqueId
            val contribution = contributor.contribution
            val location = contributor.location

            var randomColour = mapOf(
                "<dark_red>" to "4",
                "<red>" to "c",
                "<gold>" to "6",
                "<dark_green>" to "2",
                "<green>" to "a",
                "<blue>" to "9",
            ).entries.random()

            val displayName = "§${randomColour.value}${MojangAPIUtil.requestPlayerName(uniqueId)}"

            var contributorNPC = WorldNPC.createFromUniqueId(displayName, uniqueId, location).also { worldNPCs += it }
            contributorNPC.npc.prefixName = "${randomColour.key}<obf>W ".style()
            contributorNPC.npc.suffixName = " ${randomColour.key}<obf>W".style()
            contributorNPC.spawnForAll()

            location.world.spawn(location.clone().add(0.0, 2.5, 0.0), TextDisplay::class.java).apply {
                text("<colour:#ffc4ff>$contribution".style())
                backgroundColor = Color.fromRGB(84, 72, 84)
                billboard = Display.Billboard.CENTER
            }
        }
    }

    private fun fixData() {
        serverWorld = Bukkit.getWorld("world")!!.apply {
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            setGameRule(GameRule.DO_FIRE_TICK, false)
            setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true)
            setStorm(false)
            time = 6000
        }

        // player list displays entries by alphabetical order of the team they have entries with
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

        lobbySpawn = MapSinglePoint(559.5, 103, 518.5, 135, 0)

        GameConfig.entries.forEach { it.gameClass.primaryConstructor } // preload/cache classes for reflection
        eventController().startPlaylist()
    }
}
