package com.jawzf.pixeltoolkit.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class LiveWallpaperActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, WallpaperActivity::class.java)
        startActivity(intent)
        finish()
    }
}
