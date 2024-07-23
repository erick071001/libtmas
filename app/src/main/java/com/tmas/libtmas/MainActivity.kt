package com.tmas.libtmas

import android.annotation.SuppressLint
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.tmas.tmas.MainUpdate
import com.tmas.tmas.SignView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mainUpdate = MainUpdate()
        mainUpdate.initGofa(this, this)
        mainUpdate.setupSignView(findViewById<SignView>(R.id.sign),this)

    }
}