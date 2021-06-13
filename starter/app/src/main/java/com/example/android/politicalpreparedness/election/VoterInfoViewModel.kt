package com.example.android.politicalpreparedness.election

import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch

class VoterInfoViewModel(val selectedElection: Election, private val dataSource: ElectionDao) : ViewModel() {

    private val voterInfo = MutableLiveData<VoterInfoResponse>()

    private val _followButtonText = MutableLiveData<String>()
    val followButtonText: LiveData<String>
        get() = _followButtonText


    init {
        getVoterInfoFromNetwork()
        checkIfElectionIsSaved(selectedElection)
    }

    private val _response = MutableLiveData<String>()
    val response: LiveData<String>
        get() = _response

    private fun getVoterInfoFromNetwork() = viewModelScope.launch {
        try {
            var address = selectedElection.division.country
            if (selectedElection.division.state.isNotBlank()) {
                address += "/${selectedElection.division.state}"
            }

            val voterInfoResponse = CivicsApi.retrofitService.getVoterInfo(address, selectedElection.id.toLong())
            voterInfo.value = voterInfoResponse

        } catch (e: Exception) {
            _response.value = "Failed to load voter info ${e.message}"
        }
    }


    // Add var and methods to populate voter info
    val votingLocationUrl: String?
        get() = voterInfo.value?.state?.get(0)?.electionAdministrationBody?.votingLocationFinderUrl


    val ballotInfoUrl: String?
        get() = voterInfo.value?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl


    val correspondenceAddress: LiveData<String?> = Transformations.map(voterInfo) {
        return@map voterInfo.value?.state?.get(0)?.electionAdministrationBody?.correspondenceAddress?.toFormattedString()
    }

    // Add var and methods to save and remove elections to local database
    private fun saveCurrentElection() = viewModelScope.launch {
        dataSource.insertElection(selectedElection)
    }

    private fun removeCurrentElection() = viewModelScope.launch {
        dataSource.deleteElection(selectedElection)
    }

    // Populate initial state of save button to reflect proper action based on election saved status
    private var electionSaved: Boolean = false

    private fun populateFollowButtonText() {
        if (electionSaved)
            _followButtonText.postValue("Unfollow Election")
        else
            _followButtonText.postValue("Follow Election")
    }

    fun onFollowButtonClicked() {
        if (electionSaved) { //if already saved
            removeCurrentElection()
            _followButtonText.postValue("Follow Election")
            electionSaved = false
        } else {
            saveCurrentElection()
            _followButtonText.postValue("Unfollow Election")
            electionSaved = false
        }
    }

    private fun checkIfElectionIsSaved(selectedElection: Election) = viewModelScope.launch {
        val election = dataSource.getElectionById(selectedElection.id)
        electionSaved = election != null

        populateFollowButtonText()
    }
}