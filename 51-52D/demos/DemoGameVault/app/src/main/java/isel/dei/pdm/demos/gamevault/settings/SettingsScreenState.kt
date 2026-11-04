package isel.dei.pdm.demos.gamevault.settings

import isel.dei.pdm.demos.gamevault.domain.PersistenceException

/**
 * Represents the possible states of the settings screen.
 */
sealed class SettingsScreenState {

    /**
     * When the screen is loading the initial values from the repository.
     * @property isIndicatorVisible Whether the loading indicator should be displayed. It is only
     * displayed if loading takes long enough, so that it does not flash on screen when loading is
     * almost instantaneous.
     */
    data class Loading(val isIndicatorVisible: Boolean = false) : SettingsScreenState()

    /**
     * When an error occurred while loading the values. There are no values to display and the
     * screen can only be left.
     * @property error The error that occurred.
     */
    class LoadError(val error: PersistenceException) : SettingsScreenState()

    /**
     * The states in which the screen holds values for the fields.
     * @property clientId The Client ID.
     * @property clientSecret The Client Secret.
     */
    sealed class WithValues(
        val clientId: String,
        val clientSecret: String
    ) : SettingsScreenState() {

        /**
         * Whether the Client ID is valid.
         */
        val isClientIdValid: Boolean
            get() = clientId.isNotBlank()

        /**
         * Whether the Client Secret is valid.
         */
        val isClientSecretValid: Boolean
            get() = clientSecret.isNotBlank()
    }

    /**
     * When the screen contains the initial values, i.e. the ones stored in the repository.
     * @property showSaveConfirmation Whether the user should be informed that the values have
     * just been saved. Once the confirmation is shown, the view model must be notified.
     */
    class Original(
        clientId: String = "",
        clientSecret: String = "",
        val showSaveConfirmation: Boolean = false
    ) : WithValues(clientId, clientSecret)

    /**
     * When the user has modified at least one of the values.
     */
    class Modified(clientId: String, clientSecret: String) : WithValues(clientId, clientSecret)

    /**
     * When the values are being saved to the repository.
     */
    class Saving(clientId: String, clientSecret: String) : WithValues(clientId, clientSecret)

    /**
     * When an error occurred while saving the values.
     * @property error The error that occurred.
     */
    class SaveError(
        val error: PersistenceException,
        clientId: String,
        clientSecret: String
    ) : WithValues(clientId, clientSecret)

    /**
     * Whether the input fields should be enabled.
     */
    val isInputEnabled: Boolean
        get() = this is Original || this is Modified

    /**
     * Whether validation errors should be displayed. They are only displayed after the user has
     * modified the values, so that the screen does not start with error messages (e.g. when no
     * values have been stored yet).
     */
    val isValidationFeedbackVisible: Boolean
        get() = this is Modified

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
