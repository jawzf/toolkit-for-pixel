package com.jawzf.pixeltoolkit.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs

/** Generic access to the app's main preference file. Feature settings classes build on this. */
open class PrefsStore(
    protected val context: Context,
) {
    protected val prefs: SharedPreferences =
        context.getSharedPreferences(PlatformPrefs.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Registers a listener to be notified when shared preference values change.
     *
     * @param listener [SharedPreferences.OnSharedPreferenceChangeListener] The listener instance to register.
     */
    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Unregisters a previously registered shared preference change listener.
     *
     * @param listener [SharedPreferences.OnSharedPreferenceChangeListener] The listener instance to unregister.
     */
    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    // General Getters

    /**
     * Retrieves a boolean preference value by key.
     *
     * @param key [String] Preference key name.
     * @param default [Boolean] Fallback boolean value if key is absent.
     * @return The stored boolean value or default.
     */
    fun getBoolean(
        key: String,
        default: Boolean = false,
    ): Boolean = prefs.getBoolean(key, default)

    /**
     * Retrieves a string preference value by key.
     *
     * @param key [String] Preference key name.
     * @param default [String?] Fallback string value if key is absent.
     * @return The stored string value or default.
     */
    fun getString(
        key: String,
        default: String? = null,
    ): String? = prefs.getString(key, default)

    /**
     * Retrieves an integer preference value by key.
     *
     * @param key [String] Preference key name.
     * @param default [Int] Fallback integer value if key is absent.
     * @return The stored integer value or default.
     */
    fun getInt(
        key: String,
        default: Int = 0,
    ): Int = prefs.getInt(key, default)

    /**
     * Retrieves a float preference value by key, with legacy integer migration fallback.
     *
     * @param key [String] Preference key name.
     * @param default [Float] Fallback float value if key is absent.
     * @return The stored float value or default.
     */
    fun getFloat(
        key: String,
        default: Float = 0f,
    ): Float =
        try {
            prefs.getFloat(key, default)
        } catch (e: ClassCastException) {
            try {
                // Migrate from Int to Float if necessary
                val intValue = prefs.getInt(key, default.toInt())
                val floatValue = intValue.toFloat()
                putFloat(key, floatValue)
                floatValue
            } catch (e2: Exception) {
                default
            }
        }

    /**
     * Retrieves a long integer preference value by key.
     *
     * @param key [String] Preference key name.
     * @param default [Long] Fallback long value if key is absent.
     * @return The stored long value or default.
     */
    fun getLong(
        key: String,
        default: Long = 0L,
    ): Long = prefs.getLong(key, default)

    // General Setters

    /**
     * Checks if a preference key exists in persistent storage.
     *
     * @param key [String] Preference key name.
     * @return True if key exists in shared preferences.
     */
    fun contains(key: String): Boolean = prefs.contains(key)

    /**
     * Asynchronously stores a boolean preference value.
     *
     * @param key [String] Preference key name.
     * @param value [Boolean] Boolean value to store.
     */
    fun putBoolean(
        key: String,
        value: Boolean,
    ) = prefs.edit().putBoolean(key, value).apply()

    fun putBooleanSync(
        key: String,
        value: Boolean,
    ) = prefs.edit().putBoolean(key, value).commit()

    /**
     * Asynchronously stores a string preference value.
     *
     * @param key [String] Preference key name.
     * @param value [String?] String value to store.
     */
    fun putString(
        key: String,
        value: String?,
    ) = prefs.edit().putString(key, value).apply()

    /**
     * Asynchronously stores an integer preference value.
     *
     * @param key [String] Preference key name.
     * @param value [Int] Integer value to store.
     */
    fun putInt(
        key: String,
        value: Int,
    ) = prefs.edit().putInt(key, value).apply()

    /**
     * Asynchronously stores a float preference value.
     *
     * @param key [String] Preference key name.
     * @param value [Float] Float value to store.
     */
    fun putFloat(
        key: String,
        value: Float,
    ) = prefs.edit().putFloat(key, value).apply()

    /**
     * Asynchronously stores a long integer preference value.
     *
     * @param key [String] Preference key name.
     * @param value [Long] Long value to store.
     */
    fun putLong(
        key: String,
        value: Long,
    ) = prefs.edit().putLong(key, value).apply()

    /**
     * Asynchronously removes a preference entry by key.
     *
     * @param key [String] Preference key name to remove.
     */
    fun remove(key: String) = prefs.edit().remove(key).apply()

    // App Selection Helper Generic
    protected fun loadAppSelection(key: String): List<AppSelection> {
        val json = prefs.getString(key, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<AppSelection>::class.java).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    protected fun saveAppSelection(
        key: String,
        apps: List<AppSelection>,
    ) {
        val json = gson.toJson(apps)
        putString(key, json)
    }
}
