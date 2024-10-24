package gg.flyte.christmas.commands

import dev.shreyasayyengar.menuapi.menu.MenuItem
import dev.shreyasayyengar.menuapi.menu.StandardMenu
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.minigame.engine.GameConfig
import gg.flyte.christmas.util.Util
import gg.flyte.christmas.util.colourise
import gg.flyte.christmas.util.toLegacyString
import gg.flyte.twilight.extension.playSound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.bukkit.annotation.CommandPermission

class EventCommand(val menu: StandardMenu = StandardMenu("&c☃ Event Menu!".colourise(), 54)) {
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
                .onClick { whoClicked, itemStack, clickType, inventoryClickEvent ->
                    // TODO!
                }
        )
        menu.setItem(
            23, MenuItem(Material.ENDER_PEARL)
                .setName("&cTeleport Everyone to Me!".colourise())
                .setEnchantmentGlint(true)
                .closeWhenClicked(true)
                .onClick { whoClicked, itemStack, clickType, inventoryClickEvent ->
                    for (player in Util.handlePlayers(cameraEntityAction = {
                        // TODO interpolate camera entity to this location.

                    })) {
                        player.teleport(whoClicked.location)
                    }

                }
        )
        menu.setItem(
            38, MenuItem(Material.GREEN_CONCRETE)
                .setName(availableGames[selectedIndex].displayName.toLegacyString().colourise())
                .setLore(
                    "",
                    "&cThis will begin the countdown".colourise(),
                    "&cimmediately, &aand prepare the players".colourise(),
                    "",
                    "&eIf you do not want to start yet, simply".colourise(),
                    "&eexit this menu. The game has already been set.".colourise()
                )
                .setEnchantmentGlint(true)
                .closeWhenClicked(true)
                .onClick { whoClicked, itemStack, clickType, inventoryClickEvent ->
                    if (ChristmasEventPlugin.getInstance().eventController.currentGame == null) {
                        whoClicked.playSound(Sound.ENTITY_VILLAGER_NO)
                        whoClicked.sendMessage(Component.text("No game is currently selected!", NamedTextColor.RED))
                        return@onClick
                    }

                    whoClicked.playSound(Sound.ENTITY_PLAYER_LEVELUP)
                    whoClicked.sendMessage(Component.text("Game starting! Please wait...", NamedTextColor.GREEN))
                    ChristmasEventPlugin.getInstance().eventController.prepareStart()
                }
        )

        menu.setItem(
            42, MenuItem(Material.RED_CONCRETE)
                .setName(
                    "&cTerminate Current Game: " + (ChristmasEventPlugin.getInstance().eventController.currentGame?.gameConfig?.displayName
                        ?: "None".colourise())
                )
                .setLore(
                    "",
                    "&cThis will end the current game".colourise(),
                    "&cand teleport all players back to the lobby.".colourise(),
                )
                .onClick { whoClicked, itemStack, clickType, inventoryClickEvent ->
                    if (ChristmasEventPlugin.getInstance().eventController.currentGame == null) {
                        whoClicked.playSound(Sound.ENTITY_VILLAGER_NO)
                        whoClicked.sendMessage(Component.text("No game is currently running!", NamedTextColor.RED))
                        return@onClick
                    }

                    ChristmasEventPlugin.getInstance().eventController.currentGame!!.endGame()
                    whoClicked.sendMessage(Component.text("Game terminated!", NamedTextColor.RED))
                    whoClicked.playSound(Sound.ENTITY_GENERIC_EXPLODE)
                }
        )
    }

    @Command("event")
    @CommandPermission("event.op")
    @Suppress("unused") // power of lamp!
    fun handleCommand(sender: Player) {
        menu.open(false, sender)
    }

    private fun createGameSwitcher(gameType: GameConfig): MenuItem {
        val menuItem = MenuItem(gameType.menuMaterial).apply {
            setName(PlainTextComponentSerializer.plainText().serialize(gameType.displayName))
            updateRotatingItem(this) // initial lore setup
            onClick { whoClicked, itemStack, clickType, inventoryClickEvent ->
                inventoryClickEvent.isCancelled = true
                selectedIndex = (selectedIndex + 1) % availableGames.size // cycle around
                updateRotatingItem(this)
                @Suppress("DEPRECATION") this.itemStack.type = availableGames[selectedIndex].menuMaterial

                menu.setItem(13, this)
                whoClicked.playSound(Sound.UI_BUTTON_CLICK)

                ChristmasEventPlugin.getInstance().eventController.setMiniGame(availableGames[selectedIndex])
                whoClicked.sendMessage(
                    Component.text("Selected game: ", NamedTextColor.GRAY).append(
                        availableGames[selectedIndex].displayName.color(availableGames[selectedIndex].colour)
                    )
                )
            }
        }

        return menuItem
    }

    private fun updateRotatingItem(menuItem: MenuItem) {
        val lore = availableGames.mapIndexed { index, game ->
            if (index == selectedIndex) {
                val component = Component.text("> ").decorate(TextDecoration.BOLD).color(game.colour).append(game.displayName)
                PlainTextComponentSerializer.plainText().serialize(component)
            } else {
                PlainTextComponentSerializer.plainText().serialize(game.displayName)
            }
        }

        menuItem.setLore(lore)
    }
}
