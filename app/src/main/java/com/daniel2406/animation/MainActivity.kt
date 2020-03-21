package com.daniel2406.animation

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private lateinit var mainListAdapter: ItemAdapter
    private val loadingDuration: Long
        get() = (resources.getInteger(R.integer.loadingAnimDuration) / animationPlaybackSpeed).toLong()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainListAdapter = ItemAdapter(this)
        recyclerView.adapter = mainListAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        updateRecyclerViewAnimDuration()
    }

    private fun updateRecyclerViewAnimDuration() = recyclerView.itemAnimator?.run {
        removeDuration = loadingDuration * 60 / 100
        addDuration = loadingDuration
    }
}
