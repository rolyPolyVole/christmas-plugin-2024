package host.carbon.event

import org.bukkit.plugin.java.JavaPlugin

class ChristmasEventPlugin : JavaPlugin() {

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    fun getInstance(): ChristmasEventPlugin {
        return JavaPlugin.getPlugin(ChristmasEventPlugin::class.java)
    }
}