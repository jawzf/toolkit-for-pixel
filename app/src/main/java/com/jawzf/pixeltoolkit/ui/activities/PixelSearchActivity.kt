package com.jawzf.pixeltoolkit
 
import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import com.jawzf.pixeltoolkit.ui.activities.PixelSearchResultsActivity

class PixelSearchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val query = intent.getStringExtra(SearchManager.QUERY)
            ?: intent.getStringExtra("query")
            ?: ""

        val resultsIntent = Intent(this, PixelSearchResultsActivity::class.java).apply {
            putExtra(SearchManager.QUERY, query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(resultsIntent)
        finish()
    }
}
