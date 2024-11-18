package gg.flyte.christmas.npc

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.attribute.Attributes
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.npc.NPC
import com.github.retrooper.packetevents.protocol.player.TextureProperty
import com.github.retrooper.packetevents.protocol.player.UserProfile
import com.github.retrooper.packetevents.util.MojangAPIUtil
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes
import gg.flyte.christmas.ChristmasEventPlugin
import gg.flyte.christmas.minigame.world.MapSinglePoint
import gg.flyte.christmas.util.eventController
import gg.flyte.christmas.util.packetObj
import gg.flyte.christmas.util.sendPacket
import gg.flyte.christmas.util.style
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

/**
 * Wrapper for a Packet-based ServerPlayer (NPC) through PacketEvent's [NPC] implementation.
 */
class WorldNPC private constructor(displayName: String, textureProperties: List<TextureProperty?>?, val location: Location) {

    private val userProfile: UserProfile = UserProfile(UUID.randomUUID(), displayName, textureProperties)
    private var id: Int = SpigotReflectionUtil.generateEntityId()
    private val tablistName = "NPC-$id".style()
    var scale = 0.5
    val npc: NPC = NPC(userProfile, id, tablistName)

    /**
     * Spawns this NPC for the given player via packets
     */
    fun spawnFor(player: Player) {
        this.npc.location = location.packetObj()
        this.npc.spawn(PacketEvents.getAPI().playerManager.getUser(player).channel)

        // skin packet
        WrapperPlayServerEntityMetadata(id, listOf(EntityData(17, EntityDataTypes.BYTE, 127.toByte()))).sendPacket(player)

        // scale packet (if needed)
        val modifier = WrapperPlayServerUpdateAttributes.PropertyModifier(
            Attributes.GENERIC_SCALE.name,
            scale,
            WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION
        )
        var property = WrapperPlayServerUpdateAttributes.Property(Attributes.GENERIC_SCALE, scale, listOf(modifier))
        WrapperPlayServerUpdateAttributes(id, listOf(property)).sendPacket(player)

    }

    /**
     * Spawns this NPC for all players via packets
     */
    fun spawnForAll() {
        for (player in Bukkit.getOnlinePlayers()) spawnFor(player)
    }

    /**
     * Despawns this NPC for the given player via packets
     */
    fun despawnFor(player: Player) = this.npc.despawn(PacketEvents.getAPI().playerManager.getUser(player).channel)


    /**
     * Despawns this NPC for all players via packets
     */
    fun despawnForAll() {
        for (player in Bukkit.getOnlinePlayers()) despawnFor(player)
    }

    companion object {
        private val worldNPCs: MutableSet<WorldNPC> = HashSet()
        private val leaderBoardNPCs = HashMap<Int, WorldNPC>()
        private val leaderboardPositionToLocation = mapOf(
            0 to MapSinglePoint(535.5, 108.3, 503.5, -90, 0), // 2.5
            1 to MapSinglePoint(535.5, 106.55, 507.5, -90, 0), // 2.0
            2 to MapSinglePoint(535.5, 105, 499.5, -90, 0) // 1.5
        )

        // TODO add text display with total points (and name?)
        fun refreshLeaderboard() {
            eventController().points.entries
                .sortedByDescending { it.value }
                .take(3)
                .forEachIndexed { index, (uniqueId, points) ->
                    println(index)

                    // remove existing leader, if any, and spawn new leader
                    leaderBoardNPCs[index].apply {
                        worldNPCs.remove(this)
                        ChristmasEventPlugin.instance.worldNPCs.remove(this)
                        leaderBoardNPCs[index]?.despawnForAll()
                    }

                    worldNPCs.remove(leaderBoardNPCs[index])
                    ChristmasEventPlugin.instance.worldNPCs.remove(leaderBoardNPCs[index])

                    leaderBoardNPCs[index] = createFromUniqueId("", uniqueId, leaderboardPositionToLocation[index]!!).apply {
                        this.scale = when (index) {
                            0 -> 2.5
                            1 -> 2.0
                            2 -> 1.5
                            else -> 1.0
                        }

                        worldNPCs += this
                        ChristmasEventPlugin.instance.worldNPCs += this
                    }

                    leaderBoardNPCs[index]?.spawnForAll()
                }
        }

        /**
         * Creates a new [WorldNPC] modelled from an existing [Player] reference.
         */
        fun createFromLive(displayName: String, modelAfter: Player, location: Location): WorldNPC {
            val textureProperties = PacketEvents.getAPI().playerManager.getUser(modelAfter).profile.textureProperties
            return WorldNPC(displayName, textureProperties, location).also { worldNPCs += it }
        }

        /**
         * Creates a new [WorldNPC] modelled from a [UUID] reference.
         */
        fun createFromUniqueId(displayName: String, modelAfter: UUID, location: Location): WorldNPC {
            // fetch texture properties from Mojang using player name
            val textureProperty = MojangAPIUtil.requestPlayerTextureProperties(modelAfter)
            return WorldNPC(displayName, textureProperty, location).also { worldNPCs += it }
        }
    }
}
