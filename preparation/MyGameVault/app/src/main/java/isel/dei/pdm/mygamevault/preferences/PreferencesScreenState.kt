package isel.dei.pdm.mygamevault.preferences

import isel.dei.pdm.mygamevault.ports.PersistenceException

/**
 * Represents the possible states of the Preferences screen.
 * @property clientId The IGDB Client ID.
 * @property clientSecret The IGDB Client Secret.
 */
sealed class PreferencesScreenState(
    val clientId: String,
    val clientSecret: String
) {
    /**
     * When the screen is loading the initial values from the repository.
     */
    data object Loading : PreferencesScreenState("", "")

    /**
     * When the screen contains values that haven't been changed yet.
     */
    class Original(clientId: String = "", clientSecret: String = "") :
        PreferencesScreenState(clientId, clientSecret)

    /**
     * When the user has modified at least one of the values.
     */
    class Modified(clientId: String, clientSecret: String) :
        PreferencesScreenState(clientId, clientSecret)

    /**
     * When the values are being saved to the DataStore.
     */
    class Saving(clientId: String, clientSecret: String) :
        PreferencesScreenState(clientId, clientSecret)

    /**
     * When an error occurred while loading or saving the values.
     */
    class Error(
        val error: PersistenceException,
        clientId: String = "",
        clientSecret: String = ""
    ) : PreferencesScreenState(clientId, clientSecret)

    /**
     * Whether the input fields should be enabled.
     */
    val isInputEnabled: Boolean
        get() = this !is Loading && this !is Saving && this !is Error

    /**
     * Whether the IGDB Client ID is valid.
     */
    val isClientIdValid: Boolean
        get() = clientId.isNotBlank()

    /**
     * Whether the IGDB Client Secret is valid.
     */
    val isClientSecretValid: Boolean
        get() = clientSecret.isNotBlank()

    /**
     * Whether the save button should be enabled.
     */
    val isSaveEnabled: Boolean
        get() = this is Modified && isClientIdValid && isClientSecretValid
}
