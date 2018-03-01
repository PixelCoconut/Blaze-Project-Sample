package com.example.alex.projectblaze

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import java.io.IOException
import java.util.Locale
import java.util.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mMarker: Marker? = null //Marker is supposed to represent the user's current location
    private var mPolygonLeft: Polygon? = null
    private var mPolygonRight: Polygon? = null
    private var mPolygonMelb: Polygon? = null
    private var mIsInZone: Boolean = false

    //action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.items, menu)
        return true
    }

    //test JSON data grab. Send to main activity
    fun grabJson(view: View) {
        val MainActivity = Intent(this, MainActivity::class.java)
        startActivity(MainActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //enable zoom control buttons
        mMap!!.uiSettings.isZoomControlsEnabled = true

        // Add a marker in Melbourne and move the camera
        val melbourne = LatLng(-37.8136, 144.9631)
        mMarker = mMap!!.addMarker(MarkerOptions().position(melbourne).title("Current position").draggable(true))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(melbourne, 6.0f))

        //Allow the dragging of the marker to simulate the user moving
        //very laggy due to constantly checking the current suburb name
        mMap!!.setOnMarkerDragListener(object:GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(marker: Marker) {
                //getLocation(marker.position)
                collisionDetection()
            }
            override fun onMarkerDragStart(marker: Marker) {}

            override fun onMarkerDragEnd(marker: Marker) {
                getLocation(marker.position)
                collisionDetection()
            }
        })

        //User's can click on the map to simulate movement/teleportation
        mMap!!.setOnMapClickListener(object:GoogleMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {
                val mo = MarkerOptions().position(latLng).title("Current position").draggable(true)

                mMarker!!.remove()
                mMarker = mMap!!.addMarker(mo)

                getLocation(latLng)

                collisionDetection()
            }
        })
    }

    //Test if the marker is inside any of the current polygons, if so alert the user
    //Otherwise inform the user if they have left a threatzone
    fun collisionDetection() {
        if (mPolygonLeft == null || mPolygonMelb == null || mPolygonRight == null) {
            return
        }

        else if (PolyUtil.containsLocation(mMarker!!.position, mPolygonLeft!!.points, true)
                || PolyUtil.containsLocation(mMarker!!.position, mPolygonMelb!!.points, true)
                || PolyUtil.containsLocation(mMarker!!.position, mPolygonRight!!.points, true)
                ) {
            Toast.makeText(applicationContext, resources.getString(R.string.threatzone_detected),
                    Toast.LENGTH_SHORT).show()
            mIsInZone = true
        }

        else if (mIsInZone) {
            Toast.makeText(applicationContext, resources.getString(R.string.threatzone_evacuated),
                    Toast.LENGTH_SHORT).show()
            mIsInZone = false
        }
    }


    fun getLocation(latLng: LatLng) {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        try {
            val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val obj = address[0]

            val locationText: TextView = findViewById(R.id.locationText)
            locationText.text = obj.locality
        } catch (e: IOException) {
            //error
        }
    }


    //create random polygons and then detect if the user is inside one already
    //Lena: This is where it happens after pressing the retrieveThreatzones button
    fun retrieveThreatzones(view: View) {
        getRandomPolygons()

        Toast.makeText(applicationContext, resources.getString(R.string.threatzone_updated),
                Toast.LENGTH_SHORT).show()

        collisionDetection()
    }

    //produce random polygons within limited lat and long ranges
    fun getRandomPolygons() {
        //clear previous outdated polygons if they have been drawn on the map previously
        if (mPolygonLeft != null) {
            mPolygonLeft!!.remove()
        }

        if (mPolygonRight != null) {
            mPolygonRight!!.remove()
        }

        if(mPolygonMelb != null) {
            mPolygonMelb!!.remove()
        }

        //create three triangles across Victoria
        mPolygonLeft = mMap!!.addPolygon(PolygonOptions()
                .add(getRandomPointLeftSide(),
                        getRandomPointLeftSide(),
                        getRandomPointLeftSide()).strokeColor(Color.RED).fillColor(Color.YELLOW))

        mPolygonRight = mMap!!.addPolygon(PolygonOptions()
                .add(getRandomLatLongRightSide(),
                        getRandomLatLongRightSide(),
                        getRandomLatLongRightSide()).strokeColor(Color.RED).fillColor(Color.YELLOW))

        mPolygonMelb = mMap!!.addPolygon(PolygonOptions()
                .add(getRandomLatLongMelb(),
                        getRandomLatLongMelb(),
                        getRandomLatLongMelb()).strokeColor(Color.RED).fillColor(Color.YELLOW))
    }


    //return points for polygon for the left side of Victoria
    fun getRandomPointLeftSide(): LatLng {
        val maxLat = -36.0
        val minLat = -38.0

        val maxLong = 145.0
        val minLong = 141.0

        return randomLatLng(minLat, maxLat, minLong, maxLong)
    }


    //return points for polygon for the right side of Victoria
    fun getRandomLatLongRightSide(): LatLng {
        val maxLat = -36.0
        val minLat = -38.0

        val maxLong = 149.0
        val minLong = 145.5

        return randomLatLng(minLat, maxLat, minLong, maxLong)
    }

    //return points for polygon for the CBD area
    fun getRandomLatLongMelb(): LatLng {
        val maxLat = -37.70
        val minLat = -37.89

        val maxLong = 145.3
        val minLong = 144.9

        return randomLatLng(minLat, maxLat, minLong, maxLong)
    }

    fun randomLatLng(minLat:Double, maxLat:Double, minLong:Double, maxLong:Double):LatLng {

        var r = Random()
        var randomLat = minLat + (maxLat - minLat) * r.nextDouble()
        var randomLong = minLong + (maxLong - minLong) * r.nextDouble()

        return LatLng(randomLat, randomLong)
    }
}
