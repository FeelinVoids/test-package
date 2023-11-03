package space.outbreak.outbreaklib.itemutils.customitems

import io.th0rgal.oraxen.api.OraxenItems
import org.bukkit.inventory.ItemStack

class OraxenCustomItemProvider : ItemProvider {
    override fun getItem(id: String): ItemStack? {
        return OraxenItems.getItemById(id)?.build()
    }
}