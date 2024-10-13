package host.carbon.event

import org.bukkit.plugin.java.JavaPlugin

class Event : JavaPlugin() {

    override fun onEnable() {
        println("Wow, it worked!")
    }

    override fun onDisable() {

    }

}