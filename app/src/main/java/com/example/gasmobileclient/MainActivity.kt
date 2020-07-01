package com.example.gasmobileclient

import ActivityStatusHandler
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gasmobileclient.detection.ActivityStatus
import com.example.gasmobileclient.integration.api.ActivityRestService
import com.example.gasmobileclient.integration.dto.ActivityDto
import com.example.gasmobileclient.integration.dto.ActivityType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import pl.politechnika.lodzka.mobile.applications.service.detection.BackgroundDetectedActivitiesService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    var client: GoogleSignInClient? = null
    var RC_SIGN_IN: Int = 1
    var retrofit: Retrofit? = null
    var account: GoogleSignInAccount? = null
    var adapter: ArrayAdapter<String>? = null
    var activityRecognitionBroadcastReceiver: BroadcastReceiver? = null
    var activityRecognitionBroadcastReceiverFilter = IntentFilter(DETECTED_ACTIVITY_ACTION)
    var activityStatusHandler: ActivityStatusHandler = ActivityStatusHandler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val consoleLog = findViewById<ListView>(R.id.console_log)
        adapter = SimpleAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        consoleLog.adapter = adapter
        title = "Dodaj aktywność"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(this, gso)

        retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        activityRecognitionBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                val activity = intent?.getSerializableExtra(DETECTED_ACTIVITY_INTENT) as ActivityStatus
                adapter?.add("Transition: ${activity.type} (${activity.transition})")

                if (activity.transition == "START") {
                    activityStatusHandler.handleStart(activity.type)
                } else {
                    val handleExit = activityStatusHandler.handleExit(activity.type)
                    if (handleExit == null) {
                        return;
                    }

                    if (activity.type == "RUNNING") {
                        sendActivity(3.05, handleExit, ActivityType.RUNNING.name)
                    } else if (activity.type == "WALKING") {
                        sendActivity(1.4, handleExit, ActivityType.WALKING.name)
                    } else if (activity.type == "ON_BICYCLE") {
                        sendActivity(4.91, handleExit, ActivityType.CYCLING.name)
                    }
                }
            }
        }

    }

    fun signIn(view: View) {
        val signInIntent: Intent = client!!.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            val loggedInTextView = findViewById<TextView>(R.id.user_name)
            loggedInTextView.setText(account?.displayName ?: "Niezalogowany")
            loggedInTextView.invalidate()
            val addButton = findViewById<Button>(R.id.add_activity)
            addButton.isEnabled = true
            startTrackingActivity()

        } catch (e: ApiException) {
            Log.e(TAG, e.message ?: "Empty")
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }

    private fun parseTime(timeText: String) : Long {
        val splitted = timeText.split(":")
        var time = 60L * 60L * 1000L * splitted[0].toLong()
        if (splitted.size >= 2) {
            time += splitted[1].toLong() * 60L * 1000L
        }
        if (splitted.size >= 3) {
            time += splitted[2].toLong() * 1000L
        }
        return time
    }

    fun addActivity(view: View) {
        val activityType = findViewById<Spinner>(R.id.activity_type_input).selectedItem.toString()
        val velocity = findViewById<EditText>(R.id.velocity_input).text.toString().toDouble()
        val timeText = findViewById<EditText>(R.id.time_input).text.toString()

        sendActivity(velocity, parseTime(timeText), activityType)
    }

    fun sendActivity(velocity : Double, time : Long, activityType : String) {
        val create = retrofit?.create(ActivityRestService::class.java)

        val activity = ActivityDto(velocity, time, ActivityType.valueOf(activityType))

        create?.save(account?.id ?: "", activity)?.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>?, t: Throwable?) {
                Log.i(TAG, "FAILURE")
                Toast.makeText(getBaseContext(), t?.message ?: "Empty", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                Log.i(TAG, "OK")
                Toast.makeText(getBaseContext(), "OK!", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun startTrackingActivity() {
        if (shouldRequestActivityRecognitionPermission(this)) {
            adapter?.add("Asking for ActivityRecognitionPermission")
            requestActivityRecognitionPermission(this)
            return
        }
        adapter?.add("ActivityRecognitionPermission granted")

        val intent =
            Intent(this, BackgroundDetectedActivitiesService::class.java)
        adapter?.add("Activity recognition service started.")
        startService(intent)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            ACTIVITY_RECOGNITION_RC -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingActivity()
            } else {
                Toast.makeText(applicationContext, "$ACTIVITY_RECOGNITION_RC not granted", Toast.LENGTH_LONG).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(activityRecognitionBroadcastReceiver, activityRecognitionBroadcastReceiverFilter)
        adapter?.add("Broadcasters registered.")
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(activityRecognitionBroadcastReceiver)
        adapter?.add("Broadcasters unregistered.")
    }

}
