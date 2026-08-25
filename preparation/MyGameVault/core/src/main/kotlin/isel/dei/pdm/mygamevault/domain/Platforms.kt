package isel.dei.pdm.mygamevault.domain

/**
 * Predefined platforms for the application.
 */
object Platforms {
    val PS5 = Platform("PS5", "PlayStation 5")
    val PS4 = Platform("PS4", "PlayStation 4")
    val PS3 = Platform("PS3", "PlayStation 3")
    val PS2 = Platform("PS2", "PlayStation 2")
    val PS = Platform("PS", "PlayStation")
    val XBOX = Platform("XBOX", "Xbox")
    val SWITCH = Platform("SWITCH", "Nintendo Switch")
    val SWITCH_2 = Platform("SWITCH 2", "Nintendo Switch 2")
    val PC = Platform("PC", "PC")

    /**
     * The list of all predefined platforms.
     */
    val all = listOf(PS5, PS4, PS3, PS2, PS, XBOX, SWITCH, SWITCH_2, PC)
}
