package com.example.android.politicalpreparedness.election

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.models.Election

// Create Factory to generate VoterInfoViewModel with provided election datasource
class VoterInfoViewModelFactory(private val selectedElection: Election, private val dataSource: ElectionDao): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoterInfoViewModel::class.java)) {
            return VoterInfoViewModel(selectedElection, dataSource) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }
}