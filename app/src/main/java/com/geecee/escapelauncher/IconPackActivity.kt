package com.geecee.escapelauncher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class IconPackActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "This app provides an icon pack.", Toast.LENGTH_SHORT).show()
        finish()
    }
}