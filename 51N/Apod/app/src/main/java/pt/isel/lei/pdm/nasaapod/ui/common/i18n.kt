package pt.isel.lei.pdm.nasaapod.ui.common

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat


/**
 * Sets the application's locale (language) to the specified tag, **persisting this choice**
 * to override the system's global language setting for this app on subsequent launches.
 *
 * This function uses the modern Per-App Language Preferences API (Android 13+)
 * or the backward-compatible AppCompatDelegate API (pre-Android 13) to store
 * and apply the user's language choice.
 *
 * @param languageTag The BCP 47 language tag (e.g., "en", "es", "zh-CN") for the
 * desired locale.
 *
 * - To set a specific language (e.g., Spanish): Pass **"es"**.
 * - To revert the app's language back to the system default:
 * Pass an **empty string ("")** or **"und"** (undefined). This clears the
 * application's stored locale preference, allowing the system language to be used.
 *
 * @note This function must be called from a Context (usually an Activity Context).
 * Calling it will typically trigger an **Activity recreation (restart)**
 * to immediately apply the new locale to the running app.
 * Ensure your Activity extends `AppCompatActivity` for reliable operation
 * on all versions, especially when relying on `AppCompatDelegate`.


Generated via Gemini 2.5
 */
fun Context.setAppLocale(languageTag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // API 33 (Android 13) and above: Use the platform's LocaleManager
        this.getSystemService(LocaleManager::class.java)
            ?.applicationLocales = LocaleList.forLanguageTags(languageTag)
    } else {
        // Pre-API 33: Use the AppCompat library for backward compatibility
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
    }
    // The Activity will typically be recreated (restarted) automatically.
}

