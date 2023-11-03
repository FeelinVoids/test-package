package space.outbreak.outbreaklib.itemutils

data class ItemRepresentation(
    val material: String,
    val displayname: String = "",
    val lore: String = "",
    val amount: Int = 1,
    val customModelData: Int,
)