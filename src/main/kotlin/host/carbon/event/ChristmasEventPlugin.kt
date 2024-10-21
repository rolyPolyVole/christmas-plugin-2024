package host.carbon.event

import com.github.retrooper.packetevents.PacketEvents
import gg.flyte.twilight.twilight
import host.carbon.event.commands.EventCommand
import host.carbon.event.listeners.HousekeepingEventListener
import host.carbon.event.minigame.engine.EventController
import host.carbon.event.minigame.world.MapSinglePoint
import host.carbon.event.npc.WorldNPC
import host.carbon.event.util.Util
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitLamp
import java.util.UUID

class ChristmasEventPlugin : JavaPlugin() {
    lateinit var serverWorld: World;
    lateinit var lobbySpawn: Location
    var cameraPlayer: UUID = UUID.fromString("a008c892-e7e1-48e1-8235-8aa389318b7a") // "devous" | Josh
    var eventController: EventController = EventController()

    companion object {
        fun getInstance(): ChristmasEventPlugin = getPlugin(ChristmasEventPlugin::class.java)
    }

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        Bukkit.getLogger().info("Christmas Event Plugin has been enabled!")
        twilight(this)

        createConfig();
        fixData()
        registerCommands();
        registerEvents();
        registerPacketAPI()
        loadContributorNPCs();
    }

    override fun onDisable() {
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
            val name = contributor.key
            val contribution = contributor.value.first
            val location = contributor.value.second

            WorldNPC.createFromName(name, location)
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
    }
}