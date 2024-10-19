package host.carbon.event

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitLamp
import java.util.stream.Stream

class ChristmasEventPlugin : JavaPlugin() {

    override fun onEnable() {
        println("test commit")
        Bukkit.getLogger().info("Christmas Event Plugin has been enabled!")

        registerCommands();
        registerEvents();
    }

    override fun onDisable() {
    }


    private fun registerCommands() {
        val lamp = BukkitLamp.builder(this).build()
    }

    private fun registerEvents() {
        Stream.of<>(

        ).forEach(
    }

    fun getInstance(): ChristmasEventPlugin {
        return JavaPlugin.getPlugin(ChristmasEventPlugin::class.java)
    }
}