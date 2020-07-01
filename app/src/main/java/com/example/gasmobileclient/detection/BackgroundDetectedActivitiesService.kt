package pl.politechnika.lodzka.mobile.applications.service.detection

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

class BackgroundDetectedActivitiesService : Service() {
    private var mIntentService: Intent? = null
    private var mPendingIntent: PendingIntent? = null
    private var mActivityRecognitionClient: ActivityRecognitionClient? = null
    var mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val serverInstance: BackgroundDetectedActivitiesService
            get() = this@BackgroundDetectedActivitiesService
    }

    override fun onCreate() {
        super.onCreate()
        mActivityRecognitionClient = ActivityRecognitionClient(this)
        mIntentService = Intent(this, DetectedActivitiesIntentService::class.java)
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT)
        requestActivityUpdatesButtonHandler()
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    fun getRequest() : ActivityTransitionRequest {
        val transitions = ArrayList<ActivityTransition>()

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.WALKING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.RUNNING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.RUNNING)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.ON_BICYCLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())

        transitions.add(ActivityTransition.Builder()
            .setActivityType(DetectedActivity.ON_BICYCLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build())

        return ActivityTransitionRequest(transitions)
    }

    fun requestActivityUpdatesButtonHandler() {
        val task =
            mActivityRecognitionClient?.requestActivityTransitionUpdates(
                getRequest(), // detection interval
                mPendingIntent
            )
        task?.addOnSuccessListener {
            Toast.makeText(
                applicationContext,
                "Successfully requested activity updates",
                Toast.LENGTH_SHORT
            ).show()
        }
        task?.addOnFailureListener {
            Toast.makeText(
                applicationContext,
                "Requesting activity updates failed to start",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    fun removeActivityUpdatesButtonHandler() {
        val task =
            mActivityRecognitionClient!!.removeActivityTransitionUpdates(
                mPendingIntent
            )
        task.addOnSuccessListener {
            Toast.makeText(applicationContext,
                "Removed activity updates successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }
        task.addOnFailureListener {
            Toast.makeText(
                applicationContext, "Failed to remove activity updates!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeActivityUpdatesButtonHandler()
    }

    companion object {
        private val TAG =
            BackgroundDetectedActivitiesService::class.java.simpleName
    }
}