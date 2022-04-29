package com.example.gps

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var client = OkHttpClient()

    private val timer = object: CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }
        override fun onFinish() {
            getLocation()
            startTimer()
        }
    }


    private var array: MutableList<String> = ArrayList()
    private val database = Firebase.database
    private val myRef = database.getReference("data")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.btn_get_location).setOnClickListener {
            startTimer()
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            cancelTimer()
        }
    }


    private fun getLocation() {
        val task = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        task.addOnSuccessListener {
            if (it != null) {
                Toast.makeText(
                    applicationContext,
                    "${it.latitude} ${it.longitude}",
                    Toast.LENGTH_SHORT
                ).show()

//                val str: String = findViewById<TextView>(R.id.tv_locations).text.toString()
//                findViewById<TextView>(R.id.tv_locations).text = "$str \n ${it.latitude}, ${it.longitude}"

                array.add("${it.latitude}, ${it.longitude}")
                myRef.setValue(array)

                run("https://626bb806e5274e6664d09d0c.mockapi.io/v1/add-tracking?${it.latitude}&${it.longitude}")
//                run("http://20.115.96.179:8000/v1/writeonly/add-tracking?latitude=${it.latitude}&longitude=${it.longitude}")
//                run("http://104.248.246.5:8000=")
            }
        }
    }


    private fun startTimer() {
        timer.start()
    }


    private fun cancelTimer() {
        timer.cancel()
    }


    private fun run(url: String) {

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(
                response.body()?.string()
            )
        })

        createOkHttpClient(applicationContext)

    }

    private fun createOkHttpClient(context: Context) {

        client = OkHttpClient.Builder()
            .addInterceptor(
                ChuckerInterceptor.Builder(context)
                    .collector(ChuckerCollector(context))
                    .maxContentLength(250000L)
                    .redactHeaders(emptySet())
                    .alwaysReadResponseBody(false)
                    .build()
            )
            .build()

//        val collector = ChuckerCollector(
//            context = this,
//            showNotification = true,
//            retentionPeriod = RetentionManager.Period.ONE_HOUR
//        )
//
//        val interceptor = ChuckerInterceptor.Builder(context = context)
//            .collector(collector = collector)
//            .maxContentLength(
//                length = 120000L
//            )
//            .redactHeaders(emptySet())
//            .alwaysReadResponseBody(false)
//            .build()
    }

}