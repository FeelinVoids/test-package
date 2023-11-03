package space.outbreak.outbreaklib.itemutils.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import de.tr7zw.nbtapi.NBT
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.ComponentSerializer
import org.bukkit.inventory.ItemStack

class ItemStackJacksonSerializer(
    private val componentSerializer: ComponentSerializer<Component, Component, String> = MiniMessage.miniMessage()
) : JsonSerializer<ItemStack>() {
    override fun serialize(value: ItemStack, gen: JsonGenerator, serializers: SerializerProvider) {
        val nbt = NBT.itemStackToNBT(value)

        if (nbt.keys.isEmpty()) {
            gen.writeString(value.type.toString())
            return
        }

        val displayTag = nbt.getCompound("Display")
        if (displayTag != null) {
            if (displayTag.hasTag("Lore")) {
                gen.writeStartArray()
                val arr = value.lore()!!.map { componentSerializer.serialize(it) }.toTypedArray()
                gen.writeArray(arr, 0, arr.size)
                gen.writeEndArray()
            }
            if (displayTag.hasTag("Name")) {
                gen.writeString(componentSerializer.serialize(value.displayName()))
            }
            nbt.removeKey("Display")
        }

        if (nbt.keys.isNotEmpty()) {
            gen.writeString(nbt.toString())
        }
    }
}