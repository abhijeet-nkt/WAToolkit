package rawdermapps.watoolkit.util

import android.content.Context

class PreferenceManager(context : Context) {

    companion object {
        const val PREF_FILE = "rawdermapps.watoolkit.PREFERENCE_FILE"
        const val KEY_INTRO_PLAYED = "rawdermapps.watoolkit.PREFERENCE_FILE.KEY_INTRO_PLAYED"
    }

    private val pref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    val isIntroPlayed :Boolean
        get() = pref.getBoolean(KEY_INTRO_PLAYED, false)

    fun declareIntroPlayed() =
        pref.edit()
            .putBoolean(KEY_INTRO_PLAYED, true)
            .apply()
}