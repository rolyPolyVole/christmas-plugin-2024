package host.carbon.event

import org.bukkit.plugin.java.JavaPlugin

class ChristmasEventPlugin : JavaPlugin() {

    override fun onEnable() {
        registerLamp();
    }

    override fun onDisable() {
    }

    private fun registerLamp() {
        val lamp = BukkitLamp.builder(this).build()
        lamp.register(TeleportCommands())
    }

    fun getInstance(): ChristmasEventPlugin {
        return JavaPlugin.getPlugin(ChristmasEventPlugin::class.java)
    }
}