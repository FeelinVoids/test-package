package space.outbreak.outbreaklib.itemutils

import com.fasterxml.jackson.databind.module.SimpleModule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.outbreak.outbreaklib.OutbreakPlugin
import space.outbreak.outbreaklib.itemutils.customitems.ItemProvider
import space.outbreak.outbreaklib.itemutils.customitems.OraxenCustomItemProvider
import space.outbreak.outbreaklib.itemutils.customitems.VanillaItemProvider
import space.outbreak.outbreaklib.itemutils.serializers.ItemStackJacksonDeserializer
import space.outbreak.outbreaklib.itemutils.serializers.ItemStackJacksonSerializer

class ItemUtils(
    private val plugin: OutbreakPlugin,
    private val componentSerializer: ComponentSerializer<Component, Component, String>
) {
    fun registerItemRepresentationSerializer() {
        val module = SimpleModule()
        module.addDeserializer(
            ItemStack::class.java, ItemStackJacksonDeserializer(
                this,
                ItemStack(Material.BARRIER),
                componentSerializer
            )
        )
        module.addSerializer(ItemStack::class.java, ItemStackJacksonSerializer(componentSerializer))
        plugin.mapper.registerModule(module)
    }

    val itemProviders = mutableSetOf<ItemProvider>(
        VanillaItemProvider()
    )

    init {
        if (Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
            itemProviders.add(OraxenCustomItemProvider())
            plugin.logger.info("Oraxen detected. Enabling Oraxen custom items provider.")
        }
    }

    fun getItemByName(name: String): ItemStack? {
        var itemStack: ItemStack? = null

        for (provider in itemProviders) {
            val item = provider.getItem(name)
            if (item != null) {
                itemStack = item
                break
            }
        }

        return itemStack
    }
}