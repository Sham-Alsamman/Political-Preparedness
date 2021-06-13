package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch
import java.lang.Exception

// Construct ViewModel and provide election datasource
class ElectionsViewModel(val dataSource: ElectionDao): ViewModel() {

    // Create live data val for upcoming elections
    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    // Create live data val for saved elections
    private lateinit var _savedElections: LiveData<List<Election>>
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    // Create functions to navigate to saved or upcoming election voter info
    private val _navigateToVoterInfo = MutableLiveData<Election>()
    val navigateToVoterInfo: LiveData<Election>
        get() = _navigateToVoterInfo


    // Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
    init {
        getUpcomingElectionsFromNetwork()
        getSavedElectionsFromNetwork()
    }

    fun onElectionClicked(election: Election) {
        _navigateToVoterInfo.value = election
    }

    fun onNavigationCompleted() {
        _navigateToVoterInfo.value = null
    }


    private val _response = MutableLiveData<String>()
    val response: LiveData<String>
        get() = _response

    private fun getUpcomingElectionsFromNetwork() {
        viewModelScope.launch {
            try {
                val electionResponse = CivicsApi.retrofitService.getElections()
                if (electionResponse.elections.isNotEmpty())
                    _upcomingElections.value = electionResponse.elections
            } catch (e: Exception) {
                _response.value = "Failed ${e.message}"
            }
        }
    }


    private fun getSavedElectionsFromNetwork() {
        _savedElections = dataSource.getAllElections()
    }
}