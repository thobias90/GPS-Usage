package com.stahlt.gps_usage

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.stahlt.gps_usage.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URL

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btShowMap.setOnClickListener {
            btShowMapOnClick()
        }
        binding.btAddress.setOnClickListener {
        binding.btStaticMap.setOnClickListener {
            btStaticMapOnClick()
        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)
    }

    private fun btShowMapOnClick() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("latitude",binding.tvLatitude.text.toString().toDouble())
        intent.putExtra("longitude", binding.tvLongitude.text.toString().toDouble())
        startActivity(intent)
    }

    private fun btnAddressOnClick() {
        Thread {
            val encodedURL =
                "https://maps.googleapis.com/maps/api/geocode/xml?latlng=${binding.tvLatitude.text},${binding.tvLongitude.text}&key=YOUR_API_KEY"
            val url = URL(encodedURL)
            val urlConnection = url.openConnection()

            val inputStream = urlConnection.getInputStream()

            val input = BufferedReader(InputStreamReader(inputStream))
            val output = StringBuilder()

            var row = input.readLine()
            while (row != null) {
                output.append(row)
                row = input.readLine()
            }

            runOnUiThread {
                val formattedAddress = output.substring(
                    output.indexOf("<formatted_address>") + 19,
                    output.indexOf("/formatted_address"))
                showAlert(formattedAddress)
            }
        }.start()
    }

    private fun showAlert(msg: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Address")
            setMessage(msg)
            setNeutralButton("OK", null)
            show()
        }
    }

    private fun btStaticMapOnClick() {
    }
    override fun onLocationChanged(location: Location) {
        binding.tvLatitude.text = location.latitude.toString()
        binding.tvLongitude.text = location.longitude.toString()
    }
}