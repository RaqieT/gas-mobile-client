package pl.politechnika.lodzka.mobile.applications.service.detection

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.example.gasmobileclient.DETECTED_ACTIVITY_ACTION
import com.example.gasmobileclient.DETECTED_ACTIVITY_INTENT
import com.example.gasmobileclient.detection.ActivityStatus
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

class DetectedActivitiesIntentService : IntentService(DetectedActivitiesIntentService::class.java.simpleName) {

    override fun onHandleIntent(intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            val detectedActivities = result?.transitionEvents
            if (detectedActivities != null) {
                for (activity in detectedActivities) {
                    Log.e(DetectedActivitiesIntentService::class.java.simpleName, "Detected activity: " + activity.activityType + ", " + activity.transitionType)
                    broadcastActivity(activity)
                }
            }
        }

    }

    private fun broadcastActivity(activity: ActivityTransitionEvent) {
        val intent = Intent(DETECTED_ACTIVITY_ACTION)
        var type = ""
        var transition = ""
        when (activity.activityType) {
            DetectedActivity.IN_VEHICLE -> {
                type = "IN_VEHICLE"
            }
            DetectedActivity.ON_BICYCLE -> {
                type = "ON_BICYCLE"
            }
            DetectedActivity.RUNNING -> {
                type = "RUNNING"
            }
            DetectedActivity.STILL -> {
                type = "STILL"
            }
            DetectedActivity.WALKING -> {
                type = "WALKING"
            }
        }

        when(activity.transitionType) {
            ACTIVITY_TRANSITION_ENTER -> {
                transition = "ENTER"
            }
            ACTIVITY_TRANSITION_EXIT -> {
                transition = "EXIT"
            }
        }

        intent.putExtra(
            DETECTED_ACTIVITY_INTENT,
            ActivityStatus(type, transition)
        )
        sendBroadcast(intent)
    }
}