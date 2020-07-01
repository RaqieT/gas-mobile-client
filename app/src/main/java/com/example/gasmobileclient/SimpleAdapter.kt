package com.example.gasmobileclient

import android.content.Context
import android.widget.ArrayAdapter


class SimpleAdapter(context: Context, resource: Int, objects : List<String>) : ArrayAdapter<String>(context, resource, objects) {
    override fun add(item: String) {
        super.add("[" + getTime() + "] " + item)
    }

    fun getTime(): CharSequence? {
        return android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", java.util.Date())

    }
}
