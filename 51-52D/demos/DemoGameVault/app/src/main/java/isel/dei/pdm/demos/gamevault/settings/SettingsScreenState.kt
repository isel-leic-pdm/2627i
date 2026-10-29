package isel.dei.pdm.demos.gamevault.settings

import isel.dei.pdm.demos.gamevault.domain.PersistenceException

/**
 * Represents the possible states of the settings screen.
 * @property clientId The Client ID.
 * @property clientSecret The Client Secret.
 */
sealed class SettingsScreenState(
    val clientId: String,
    val clientSecret: String
) {
    /**
     * When the screen is loading the initial values from the repository.
     */
    data object Loading : SettingsScreenState("", "")

    /**
     * When the screen contains the initial values.
     */
    class Original(clientId: String = "", clientSecret: String = "") :
        SettingsScreenState(clientId, clientSecret)

    /**
     * When the user has modified at least one of the values.
     */
    class Modified(clientId: String, clientSecret: String) :
        SettingsScreenState(clientId, clientSecret)

    /**
     * When the values are being saved to the repository.
     */
    class Saving(clientId: String, clientSecret: String) :
        SettingsScreenState(clientId, clientSecret)

    /**
     * When an error occurred while loading or saving the values.
     */
    class Error(
        val error: PersistenceException,
        clientId: String = "",
        clientSecret: String = ""
    ) : SettingsScreenState(clientId, clientSecret)

    /**
     * Whether the input fields should be enabled.
     */
    val isInputEnabled: Boolean
        get() = this !is Loading && this !is Saving && this !is Error

    /**
     * Whether the Client ID currently stored is valid.
     */
    val isClientIdValid: Boolean
        get() = clientId.isNotBlank()

    /**
     * Whether the Client Secret currently stored is valid.
     */
    val isClientSecretValid: Boolean
        get() = clientSecret.isNotBlank()

    /**
     * Whether the save button should be enabled.
     */
    val isSaveEnabled: Boolean
        get() = this is Modified && isClientIdValid && isClientSecretValid

    /**
     * Whether back navigation should be enabled.
     */
    val isBackEnabled: Boolean
        get() = this !is Loading && this !is Saving
}
