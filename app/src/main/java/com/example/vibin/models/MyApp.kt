package com.example.vibin.models


import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MyApp : Application(), Application.ActivityLifecycleCallbacks {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        registerActivityLifecycleCallbacks(this)
    }

    private fun setUserPresence(status: String) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val presenceRef = FirebaseFirestore.getInstance().collection("users").document(uid)

        val data = hashMapOf(
            "status" to status,
            "lastSeen" to System.currentTimeMillis()
        )

        presenceRef.set(data, SetOptions.merge())
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App moved to foreground
            setUserPresence("online")
        }
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App moved to background
            setUserPresence("offline")
        }
    }

    // Required but unused lifecycle methods
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
