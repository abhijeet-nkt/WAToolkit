package rawdermapps.watoolkit.util

import android.content.Context

class PreferenceManager(context : Context) {

    companion object {
        const val PREF_FILE = "rawdermapps.watoolkit.PREFERENCE_FILE"
        const val KEY_MAIN_WALK_THROUGH_COMPLETED = "rawdermapps.watoolkit.PREFERENCE_FILE.KEY_MAIN_WALK_THROUGH_COMPLETED"
        const val KEY_PREVIEW_WALK_THROUGH_COMPLETED = "rawdermapps.watoolkit.PREFERENCE_FILE.KEY_PREVIEW_WALK_THROUGH_COMPLETED"
        const val KEY_SAVE_WALK_THROUGH_COMPLETED = "rawdermapps.watoolkit.PREFERENCE_FILE.KEY_SAVE_WALK_THROUGH_COMPLETED"
    }

    private val pref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    var isMainWalkThroughCompleted :Boolean
        get() = pref.getBoolean(KEY_MAIN_WALK_THROUGH_COMPLETED, false)
        set(value) {
            pref.edit()
                .putBoolean(KEY_MAIN_WALK_THROUGH_COMPLETED, value)
                .apply()
        }

    var isSaveWalkThroughCompleted :Boolean
        get() = pref.getBoolean(KEY_SAVE_WALK_THROUGH_COMPLETED, false)
        set(value) {
            pref.edit()
                .putBoolean(KEY_SAVE_WALK_THROUGH_COMPLETED, value)
                .apply()
        }

    var isPreviewThroughCompleted :Boolean
        get() = pref.getBoolean(KEY_PREVIEW_WALK_THROUGH_COMPLETED, false)
        set(value) {
            pref.edit()
                .putBoolean(KEY_PREVIEW_WALK_THROUGH_COMPLETED, value)
                .apply()
        }

}