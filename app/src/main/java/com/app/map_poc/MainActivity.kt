package com.app.map_poc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    var map: GoogleMap? = null
    var input: AutoCompleteTextView? = null
    var placesClient: PlacesClient? = null
    val token = AutocompleteSessionToken.newInstance()
    val list = ArrayList<String>()
    val listPlaceId = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Places.initialize(applicationContext, "AIzaSyAer-qPX9SKSoopp1A8diJl1dvPVNLNPbo")
        placesClient = Places.createClient(this)

        input = findViewById(R.id.input)
        val mapFrag = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFrag.getMapAsync(this)

        input?.onItemClickListener = AdapterView.OnItemClickListener { p0, p1, index, p3 ->
            run {
                hideKeyboard()
                val placeFields: List<Place.Field> =
                    listOf(Place.Field.LAT_LNG)
                val request = FetchPlaceRequest.newInstance(listPlaceId.get(index), placeFields)
                placesClient?.fetchPlace(request)
                    ?.addOnSuccessListener { response: FetchPlaceResponse ->
                        val place = response.place
                        map?.addMarker(place?.latLng?.let {
                            MarkerOptions().position(it).title(list.get(index))
                        })
                        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 15f))

                        map?.setOnMarkerClickListener { marker ->
                            startActivity(
                                Intent(
                                    this,
                                    DetailsActivity::class.java
                                ).putExtra("address", list.get(index))
                            );
                            false
                        }
                    }?.addOnFailureListener { exception: java.lang.Exception ->
                        if (exception is ApiException) {
                            val statusCode = exception.statusCode
                        }
                    }
                input?.dismissDropDown()
            }
        }

        input?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (input?.isPerformingCompletion == false)
                    searchPlace(p0.toString())
            }
        })

    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map
        val sydney = LatLng(-34.00, 151.00)
        // map?.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))
    }

    private fun searchPlace(query: String) {
        val request =
            FindAutocompletePredictionsRequest.builder() // Call either setLocationBias() OR setLocationRestriction().
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .build()

        placesClient?.findAutocompletePredictions(request)
            ?.addOnSuccessListener { response: FindAutocompletePredictionsResponse ->

                for (prediction in response.autocompletePredictions) {
                    list.add(prediction.getFullText(null).toString())
                    listPlaceId.add(prediction.placeId)
                }
                val adapter = ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    list
                )

                input?.setAdapter(adapter)
                input?.showDropDown()
            }?.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    show(exception.message.toString());
                    Log.e("Kartik", "Place not found: " + exception.message)
                }
            }
    }

    private fun show(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show();
    }

    public fun hideKeyboard() {
    val imm:InputMethodManager =getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager;
    //Find the currently focused view, so we can grab the correct window token from it.
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
}
}
