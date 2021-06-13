package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class DetailFragment : Fragment() {

    companion object {
        // Add Constant for Location request
        private const val FINE_LOCATION_ACCESS_REQUEST_CODE = 1
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val TAG = "RepresentativeFragment"

    }

    // Declare ViewModel
    private val viewModel: RepresentativeViewModel by lazy {
        ViewModelProvider(this).get(RepresentativeViewModel::class.java)
    }

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentRepresentativeBinding.inflate(inflater)
        binding.lifecycleOwner = this

        // Establish bindings
        binding.viewModel = viewModel

        // Define and assign Representative adapter
        val adapter = RepresentativeListAdapter()
        binding.representativesRecyclerView.adapter = adapter

        // Establish button listeners for field and location search
        binding.buttonLocation.setOnClickListener {
            mFusedLocationProviderClient = LocationServices
                    .getFusedLocationProviderClient(requireActivity())
            checkLocationPermissionsAndStartGettingLocation()
        }

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            viewModel.address.value?.state = binding.state.selectedItem.toString()
            viewModel.getRepresentativesFromNetwork()
        }

        viewModel.response.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        return binding.root
    }


    private fun setAddressAndGetRepresentatives(address: Address) {
        viewModel.setCurrentAddress(address)
        viewModel.getRepresentativesFromNetwork()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Handle location permission result to get location on permission granted
        //If the grantResults array is empty, then the interaction was interrupted
        if (grantResults.isEmpty() ||
                grantResults[0] == PackageManager.PERMISSION_DENIED)
                 {
            Snackbar.make(
                    this.view!!,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
            )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
        } else {
            checkDeviceLocationSettingsAndGetLocation()
        }
    }

    private fun checkDeviceLocationSettingsAndGetLocation(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        //use LocationServices to get the Settings Client and create a
        //val called locationSettingsResponseTask to check the location settings.
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    //try calling the startResolutionForResult()
                    //method in order to prompt the user to turn on device location.
                    // this method starts an intent (new activity) and requires onActivityResult()
                    startIntentSenderForResult(exception.resolution.intentSender, REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                //If the exception is not of type ResolvableApiException, present
                //a snackbar that alerts the user that location needs to be enabled
                Snackbar.make(
                        this.view!!,
                        R.string.location_required_error, Snackbar.LENGTH_LONG
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndGetLocation()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnSuccessListener {
            getLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // to check that the user turned on their device location
        // and ask again if they did not
        Log.i(TAG, "location permission")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndGetLocation(false)
        }
    }

    private fun checkLocationPermissionsAndStartGettingLocation(): Boolean {
        return if (isPermissionGranted()) {
            checkDeviceLocationSettingsAndGetLocation()
            true
        } else {
            requestLocationPermission()
            false
        }
    }

    private fun requestLocationPermission() {
        //If the permissions have already been approved
        if (isPermissionGranted())
            return

        //The permissionsArray contains the permissions that are going to be requested.
        val permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        requestPermissions(
                permissionArray,
                FINE_LOCATION_ACCESS_REQUEST_CODE
        )
    }

    private fun isPermissionGranted() : Boolean {
        //check if the ACCESS_FINE_LOCATION permission is granted.
        return ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocation() {
        try {
            val locationResult: Task<Location> = mFusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val location: Location = task.result!!
                    setAddressAndGetRepresentatives(geoCodeLocation(location))
                }
            }
        } catch (e: SecurityException) {
            e.message?.let { Log.e("Exception: %s", it) }
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
                .map { address ->
                    Address(address.thoroughfare, address.subThoroughfare, address.locality, address.adminArea, address.postalCode)
                }
                .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

}