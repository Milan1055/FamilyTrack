package com.example.familytrack

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlin.reflect.KTypeProjection.Companion.STAR

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {



    private lateinit var map: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            getLocationUpdates()
            startLocationUpdates()
        }
        else
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 20000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation

                    //save each user`s location to firebase database
                    val user = FirebaseAuth.getInstance().getCurrentUser();
                    val databaseRef: DatabaseReference = Firebase.database.reference
                    val locationlogging = LocationLogging(location.latitude, location.longitude)
                    databaseRef.child("users").child(user!!.getUid()).child(
                            "userlocation").setValue(locationlogging)

                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Locations written into the " +
                                    "database", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Error occured while writing your" +
                                    " location to the database", Toast.LENGTH_LONG).show()
                        }

                    //show marker on map
                    val latLng = LatLng(location.latitude, location.longitude)
                    val markerOptions = MarkerOptions().position(latLng).title("My location")
                    map.addMarker(markerOptions)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))

                    //dummy locations to show user locations on map
                    val brother = LatLng(53.7567033, -1.5189467)
                    map.addMarker(MarkerOptions().position(brother).title("Brother"))
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(brother, 10f))

                    val sister = LatLng(53.7924117, -1.74966)
                    map.addMarker(MarkerOptions().position(sister).title("Sister"))
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(sister, 10f))

                    val milan = LatLng(53.7220139, -1.6490746)
                    map.addMarker(MarkerOptions().position(milan).title("Milan"))
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(milan, 10f))
                }
            }
        }
    }




    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, null)
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                map.isMyLocationEnabled = true
            } else { Toast.makeText(this, "User has not granted location access permission", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)


            //set highlighter for nav drawer, then close on click
        navigationView.setNavigationItemSelectedListener {
            it.isChecked = true


            //make hamburger buttons work
            when (it.itemId) {
                R.id.menu_about -> {
                    val intent = Intent(this, AboutPageActivity::class.java)
                    startActivity(intent)
                    d("MapsActivity", "About button was pressed!")
                }


                R.id.menu_contact -> {
                    val intent = Intent(this, ContactPageActivity::class.java)
                    startActivity(intent)
                    d("MapsActivity", "Contact button was pressed!")
                }


                R.id.button_panic -> {
                    Toast.makeText(applicationContext, "Sorry, the Panic function is not yet available!", Toast.LENGTH_SHORT).show()
                    d("MapsActivity", "Panic button was pressed!")
                }


                R.id.menu_sign_out -> {
                    FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                    d("MapsActivity", "Sign out button was pressed!")
                }

            }
            drawerLayout.closeDrawers()
            true
        }

        //hamburger icon
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getLocationAccess()

        }


    // new message menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, LatestMessagesActivity::class.java)
                startActivity(intent)
            }
        }

        drawerLayout.openDrawer(GravityCompat.START)

        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_map, menu)
        return super.onCreateOptionsMenu(menu)
    }




}


