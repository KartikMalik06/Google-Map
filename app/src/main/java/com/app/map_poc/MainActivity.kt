package com.app.map_poc

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    var map: GoogleMap? = null
    var input: EditText? = null
    var placesClient: PlacesClient? = null
    val token = AutocompleteSessionToken.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Places.initialize(applicationContext, "AIzaSyBUNRCx95MqqU2m-cl4Tncxt7nUCQfgCmM")
        placesClient = Places.createClient(this)

        input = findViewById(R.id.input)
        val mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFrag.getMapAsync(this)

        input?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchPlace(p0.toString());
            }
        })

    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map
        val sydney = LatLng(-34.00, 151.00)
        map?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))
    }

    private fun searchPlace(query:String) {
        val request =
            FindAutocompletePredictionsRequest.builder() // Call either setLocationBias() OR setLocationRestriction().
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .build()

        placesClient?.findAutocompletePredictions(request)
            ?.addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                for (prediction in response.autocompletePredictions) {
                    show(prediction.getPrimaryText(null).toString());
                    Log.i("Kartik", prediction.placeId)
                    Log.i("Kartik", prediction.getPrimaryText(null).toString())
                }
            }?.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                   show(exception.message.toString());
                    Log.e("Kartik", "Place not found: " + exception.message)
                }
            }
    }

    private fun show(msg:String){
        Toast.makeText(applicationContext,msg,Toast.LENGTH_LONG).show();
    }
}
