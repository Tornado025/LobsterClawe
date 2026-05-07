package com.lobsterclawe.data

import android.content.Context
import android.content.SharedPreferences

class PrefsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lobster_prefs", Context.MODE_PRIVATE)

    var cuisines: List<String>
        get() = prefs.getStringSet("cuisines", emptySet())?.toList() ?: emptyList()
        set(value) = prefs.edit().putStringSet("cuisines", value.toSet()).apply()

    var dietaryGoal: String
        get() = prefs.getString("dietary_goal", "") ?: ""
        set(value) = prefs.edit().putString("dietary_goal", value).apply()

    var spiceLevel: String
        get() = prefs.getString("spice_level", "medium") ?: "medium"
        set(value) = prefs.edit().putString("spice_level", value).apply()

    var householdSize: Int
        get() = prefs.getInt("household_size", 2)
        set(value) = prefs.edit().putInt("household_size", value).apply()

    var onboardingDone: Boolean
        get() = prefs.getBoolean("onboarding_done", false)
        set(value) = prefs.edit().putBoolean("onboarding_done", value).apply()
}
