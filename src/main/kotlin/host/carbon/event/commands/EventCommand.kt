package host.carbon.event.commands

import dev.shreyasayyengar.menuapi.menu.MenuItem
import dev.shreyasayyengar.menuapi.menu.StandardMenu
import gg.flyte.twilight.extension.playSound
import host.carbon.event.ChristmasEventPlugin
import host.carbon.event.minigame.engine.GameConfig
import host.carbon.event.util.Util
import host.carbon.event.util.colourise
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.bukkit.annotation.CommandPermission

class EventCommand(val menu: StandardMenu = StandardMenu("&c☃ Event Menu!".colourise(), 18)) {
    private val availableGames = GameConfig.entries
    private var selectedIndex = 0

    init {
        // Set the initial item in the menu
        menu.setItem(13, createGameSwitcher(availableGames[selectedIndex]))
        menu.setItem(
            21, MenuItem(Material.PAINTING)
                .setName("&cTake a Screenie!".colourise())
                .setEnchantmentGlint(true)
                .closeWhenClicked(true)
                .onClick({ whoClicked, itemStack, clickType, inventoryClickEvent ->
                    // TODO!
                })
        )
        menu.setItem(
            23, MenuItem(Material.ENDER_PEARL)
                .setName("&cTeleport Everyone to Me!".colourise())
                .setEnchantmentGlint(true)
                .closeWhenClicked(true)
                .onClick({ whoClicked, itemStack, clickType, inventoryClickEvent ->
                    for (player in Util.handlePlayers(cameraEntityAction = {
                        // TODO interpolate camera entity to this location.

                    })) {
                        player.teleport(whoClicked.location)
                    }

                })
        )
        menu.setItem(
            38, MenuItem(Material.GREEN_CONCRETE)
                .setName("&aStart " + availableGames[selectedIndex].menuName)
                .setLore(
                    "&aThis will begin the countdown".colourise(),
                    "&cimmediately, &aand prepare the players".colourise(),
                    "&eIf you do not want to start yet, simply".colourise(),
                    "&eexit this menu. The game has already been set.".colourise()
                )
                .setEnchantmentGlint(true)
                .closeWhenClicked(true)
                .onClick({ whoClicked, itemStack, clickType, inventoryClickEvent ->
                    whoClicked.playSound(Sound.ENTITY_PLAYER_LEVELUP)
                    ChristmasEventPlugin.getInstance().eventController.prepareStart()
                })
        )

        menu.setItem(
            42, MenuItem(Material.RED_CONCRETE)
                .setName(
                    "&cTerminate Current Game: " + (ChristmasEventPlugin.getInstance().eventController.currentGame?.gameConfig?.menuName ?: "None")
                )
                .setLore(
                    "&cThis will end the current game".colourise(),
                    "&cand teleport all players back to the lobby.".colourise(),
                )
                .onClick({ whoClicked, itemStack, clickType, inventoryClickEvent ->
                    if (ChristmasEventPlugin.getInstance().eventController.currentGame == null) {
                        whoClicked.playSound(Sound.ENTITY_VILLAGER_NO)
                        whoClicked.sendMessage(Component.text("No game is currently running!", NamedTextColor.RED))
                        return@onClick
                    }

                    ChristmasEventPlugin.getInstance().eventController.currentGame!!.endGame()
                    whoClicked.sendMessage(Component.text("Game terminated!", NamedTextColor.RED))
                    whoClicked.playSound(Sound.ENTITY_GENERIC_EXPLODE)
                })
        )

    }

    @Command("event")
    @CommandPermission("event.op")
    fun handleCommand(sender: Player) {
        menu.open(false, sender)
    }

    private fun createGameSwitcher(gameType: GameConfig): MenuItem {
        val menuItem = MenuItem(gameType.menuMaterial)
        menuItem.setName(PlainTextComponentSerializer.plainText().serialize(gameType.menuName))
        updateRotatingItem(menuItem) // initial lore setup

        menuItem.onClick { whoClicked, itemStack, clickType, inventoryClickEvent ->
            selectedIndex = (selectedIndex + 1) % availableGames.size // cycle around

            updateRotatingItem(menuItem)
            @Suppress("DEPRECATION") menuItem.itemStack.type = availableGames[selectedIndex].menuMaterial

            menu.setItem(13, menuItem)
            whoClicked.playSound(Sound.UI_BUTTON_CLICK)

            ChristmasEventPlugin.getInstance().eventController.setMiniGame(availableGames[selectedIndex])
        }

        return menuItem
    }

    private fun updateRotatingItem(menuItem: MenuItem) {
        val lore = availableGames.mapIndexed { index, game ->
            if (index == selectedIndex) {
                val component = Component.text("> ").decorate(TextDecoration.BOLD).color(game.colour).append(game.menuName)
                PlainTextComponentSerializer.plainText().serialize(component)
            } else {
                PlainTextComponentSerializer.plainText().serialize(game.menuName)
            }
        }

        menuItem.setLore(lore)
    }
}