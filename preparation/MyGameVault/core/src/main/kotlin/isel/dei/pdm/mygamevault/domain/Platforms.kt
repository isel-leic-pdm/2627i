package isel.dei.pdm.mygamevault.domain

/**
 * Predefined platforms for the application.
 */
object Platforms {
    val PS5 = Platform(167, "PS5", "PlayStation 5")
    val PS4 = Platform(48, "PS4", "PlayStation 4")
    val PS3 = Platform(9, "PS3", "PlayStation 3")
    val PS2 = Platform(8, "PS2", "PlayStation 2")
    val PS = Platform(7, "PS", "PlayStation")
    val XBOX = Platform(169, "XBOX", "Xbox")
    val SWITCH = Platform(130, "SWITCH", "Nintendo Switch")
    val SWITCH_2 = Platform(1302, "SWITCH 2", "Nintendo Switch 2")
    val PC = Platform(6, "PC", "PC")

    /**
     * The list of all predefined platforms.
     */
    val all = listOf(PS5, PS4, PS3, PS2, PS, XBOX, SWITCH, SWITCH_2, PC)
}
