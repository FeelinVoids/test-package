package space.outbreak.outbreaklib.itemutils.customitems

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class VanillaItemProvider : ItemProvider {
    override fun getItem(id: String): ItemStack? {
        var name = id
        if (id.startsWith("minecraft:"))
            name = id.substring(10)

        val spl = name.split(":")
        var customModelData: Int? = null
        if (spl.size == 2) {
            name = spl[0]
            try {
                customModelData = spl[1].toInt()
            } catch (e: NumberFormatException) {
                return null
            }
        }
        val mat = Material.getMaterial(name) ?: return null
        val item = ItemStack(mat)
        if (customModelData != null) {
            val meta = item.itemMeta
            meta?.setCustomModelData(customModelData)
            item.setItemMeta(meta)
        }
        return item
    }
}