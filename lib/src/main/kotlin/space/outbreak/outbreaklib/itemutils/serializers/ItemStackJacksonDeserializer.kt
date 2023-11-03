package space.outbreak.outbreaklib.itemutils.serializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import de.tr7zw.nbtapi.NBT
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.ComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.outbreak.outbreaklib.itemutils.ItemUtils

class ItemStackJacksonDeserializer(
    private val itemUtils: ItemUtils,
    private val defaultItem: ItemStack = ItemStack(Material.BARRIER),
    private val serializer: ComponentSerializer<Component, Component, String> = MiniMessage.miniMessage()
) : JsonDeserializer<ItemStack>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ItemStack {
        val node: JsonNode = p.codec.readTree(p)

        if (node.isTextual) {
            return itemUtils.getItemByName(node.asText()) ?: defaultItem
        }

        val item = itemUtils.getItemByName(node.get("material").asText()) ?: defaultItem
        if (node.has("amount"))
            item.amount = node.get("amount").asInt()

        val displayNameRaw = if (node.has("displayname")) node.get("displayname").asText() else null
        val loreRaw = if (node.has("lore")) node.get("lore").asText() else null
        val customModelData = if (node.has("custom-model-data")) node.get("custom-model-data").asInt() else null
        val nbt = if (node.has("nbt")) node.get("nbt").asText() else null

        val meta = item.itemMeta ?: return item

        if (displayNameRaw != null)
            meta.displayName(serializer.deserialize(displayNameRaw))

        if (loreRaw != null) {
            meta.lore(loreRaw.split("\r?\n|\r".toRegex()).map { serializer.deserialize(it) })
        }

        if (customModelData != null) {
            meta.setCustomModelData(customModelData)
        }

        if (nbt != null) {
            val itemNbt = NBT.itemStackToNBT(item)
            itemNbt.mergeCompound(NBT.parseNBT(nbt))
        }

        return item
    }
}