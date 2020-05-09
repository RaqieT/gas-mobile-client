package com.example.gasmobileclient

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gasmobileclient.integration.api.ActivityRestService
import com.example.gasmobileclient.integration.dto.ActivityDto
import com.example.gasmobileclient.integration.dto.ActivityType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Dodaj aktywność"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestProfile()
            .requestEmail()
            .build()

        client = GoogleSignIn.getClient(this, gso)

        retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.8.11:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
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
        val create = retrofit?.create(ActivityRestService::class.java)
        val activityType = findViewById<Spinner>(R.id.activity_type_input).selectedItem.toString()
        val distance = findViewById<EditText>(R.id.distance_input).text.toString().toDouble()
        val timeText = findViewById<EditText>(R.id.time_input).text.toString()

        val activity = ActivityDto(distance, parseTime(timeText), ActivityType.valueOf(activityType))

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


}
