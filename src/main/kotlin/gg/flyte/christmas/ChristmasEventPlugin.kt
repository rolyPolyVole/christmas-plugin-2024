package gg.flyte.christmas

import com.github.retrooper.packetevents.PacketEvents
import dev.shreyasayyengar.menuapi.menu.MenuManager
import gg.flyte.christmas.commands.EventCommand
import gg.flyte.christmas.listeners.HousekeepingEventListener
import gg.flyte.christmas.minigame.engine.EventController
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.npc.WorldNPC
import gg.flyte.christmas.util.Util
import gg.flyte.twilight.twilight
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitLamp
import java.util.UUID
import kotlin.reflect.full.primaryConstructor

class ChristmasEventPlugin : JavaPlugin() {
    lateinit var serverWorld: World;
    lateinit var lobbySpawn: Location
    var cameraPlayer: UUID = UUID.fromString("a008c892-e7e1-48e1-8235-8aa389318b7a") // "devous" | Josh
    var eventController: EventController = EventController()
    var worldNPCs: MutableSet<WorldNPC> = HashSet()

    companion object {
        fun getInstance(): ChristmasEventPlugin = getPlugin(ChristmasEventPlugin::class.java)
    }

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        logger.info("ChristmasEventPlugin has been enabled!")

        initDependencies()
        createConfig();
        fixData()
        registerCommands();
        registerEvents();
        registerPacketAPI()
        loadContributorNPCs();
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
        config.options().configuration();
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
        PacketEvents.getAPI().init();
    }

    private fun loadContributorNPCs() {
        for (contributor in Util.getEventContributors()) {
            val ign = contributor.ign
            val contribution = contributor.contribution
            val location = contributor.location // TODO configure pitch and yaw

            var createFromName = WorldNPC.createFromName(ign, location).also { worldNPCs += it }
            Bukkit.getOnlinePlayers().forEach { createFromName.spawnFor(it) }

            location.world.spawn(location.clone().add(0.0, 2.5, 0.0), TextDisplay::class.java).apply {
                text(Component.text(contribution, TextColor.color(255, 196, 255)))
                backgroundColor = Color.fromRGB(84, 72, 84)
                billboard = Display.Billboard.CENTER
                isSeeThrough = false
            }
        }
    }

    private fun fixData() {
        serverWorld = Bukkit.getWorld("world")!!
        serverWorld.apply {
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            time = 6000
            setStorm(false)
        }

        // TODO tree world?

        lobbySpawn = MapSinglePoint(559.5, 105, 554.5, 180, 0)


        GameConfig.entries.forEach { it.gameClass.primaryConstructor } // pre-load classes for reflection
        eventController.startPlaylist()
    }
}