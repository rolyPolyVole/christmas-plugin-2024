package host.carbon.event.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent

class HousekeepingEventListener : Listener {
    @org.bukkit.event.EventHandler
    fun onEntityDamage(event: org.bukkit.event.entity.EntityDamageEvent) {
        event.isCancelled = true
    }


    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }

    @org.bukkit.event.EventHandler
    fun onServerListPing(event: org.bukkit.event.server.ServerListPingEvent) {
        event.motd(Component.text())
    }

}