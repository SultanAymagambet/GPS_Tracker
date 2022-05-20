package com.example.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.CellInfoGsm
import android.telephony.CellSignalStrengthGsm
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var client = OkHttpClient()

    private val timer = object: CountDownTimer(2000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }
        override fun onFinish() {
            getLocation()
            startTimer()
        }
    }


//    private var array: MutableList<String> = ArrayList()
//    private val database = Firebase.database
//    private val myRef = database.getReference("data")


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

//                array.add("${it.latitude}, ${it.longitude}")
//                myRef.setValue(array)

                run("http://164.92.153.55:8080/get?${it.latitude + it.longitude}")
//                run("http://20.115.96.179:8000/v1/writeonly/add-tracking?latitude=${it.latitude}&longitude=${it.longitude}")
            }
        }
    }


    private fun startTimer() {
        timer.start()
    }


    private fun cancelTimer() {
        timer.cancel()
    }


    @SuppressLint("MissingPermission")
    private fun run(url: String) {
        val telephonyManager: TelephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val cellInfoGsm: CellInfoGsm = telephonyManager.allCellInfo[0] as CellInfoGsm
        val cellSignalStrengthGsm: CellSignalStrengthGsm = cellInfoGsm.cellSignalStrength
        val  dbm = cellSignalStrengthGsm.dbm

        val request = Request.Builder()
            .addHeader("X-DBM", dbm.toString())
            .addHeader("X-GSMTYPE", networkTypeClass(telephonyManager.networkType))
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

    /** Usage: networkTypeClass(telephonyManager.networkType) */
    fun networkTypeClass(networkType: Int): String {
        when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN,
            TelephonyManager.NETWORK_TYPE_GSM
            -> return "2G"
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_TD_SCDMA
            -> return "3G"
            TelephonyManager.NETWORK_TYPE_LTE
            -> return "4G"
            TelephonyManager.NETWORK_TYPE_NR
            -> return "5G"
            else -> return "Unknown"
        }
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

    }

}