package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel : ViewModel() {

    // Establish live data for representatives and address
    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    val address = MutableLiveData<Address>(Address("", "", "", "", ""))

    fun setCurrentAddress(paramAddress: Address) {
        address.value = paramAddress
    }

    // Create function to fetch representatives from API from a provided address
    private val _response = MutableLiveData<String>()
    val response: LiveData<String>
        get() = _response

    fun getRepresentativesFromNetwork() = viewModelScope.launch {
        try {
            if (address.value != null) {
                val (offices, officials) = CivicsApi.retrofitService.getRepresentativesAsync(address.value!!.toFormattedString()).await()
                _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
            }

        } catch (e: Exception) {
            _response.value = "Failed to load representatives ${e.message}"
        }
    }


}
