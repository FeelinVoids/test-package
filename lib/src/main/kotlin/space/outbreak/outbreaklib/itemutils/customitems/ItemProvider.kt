package space.outbreak.outbreaklib.itemutils.customitems

import org.bukkit.inventory.ItemStack

interface ItemProvider {
    fun getItem(id: String): ItemStack?
}