package com.example.gasmobileclient

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val ACTIVITY_RECOGNITION_PERM = "com.google.android.gms.permission.ACTIVITY_RECOGNITION"

const val ACCESS_FINE_LOCATION_RC = 5
const val ACCESS_COARSE_LOCATION_RC = 6
const val FOREGROUND_SERVICE_RC = 7
const val ACTIVITY_RECOGNITION_RC = 8

fun shouldRequestFineLocationPermission(context: Activity) : Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
fun shouldRequestCoarseLocationPermission(context: Activity) : Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
fun shouldRequestForegroundServicePermission(context: Activity) : Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED
fun shouldRequestActivityRecognitionPermission(context: Activity) : Boolean = ContextCompat.checkSelfPermission(context, ACTIVITY_RECOGNITION_PERM) != PackageManager.PERMISSION_GRANTED

fun requestFineLocationPermission(context: Activity) {
    ActivityCompat.requestPermissions(
        context,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        ACCESS_FINE_LOCATION_RC
    )
}

fun requestCoarseLocationPermission(context: Activity) {
    ActivityCompat.requestPermissions(
        context,
        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
        ACCESS_COARSE_LOCATION_RC
    )
}

fun requestForegroundServicePermission(context: Activity) {
    ActivityCompat.requestPermissions(
        context,
        arrayOf(Manifest.permission.FOREGROUND_SERVICE),
        FOREGROUND_SERVICE_RC
    )
}

fun requestActivityRecognitionPermission(context: Activity) {
    ActivityCompat.requestPermissions(
        context,
        arrayOf(ACTIVITY_RECOGNITION_PERM),
        ACTIVITY_RECOGNITION_RC
    )
}