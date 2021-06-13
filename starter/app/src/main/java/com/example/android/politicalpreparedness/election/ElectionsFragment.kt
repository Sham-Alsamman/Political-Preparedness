package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {

    // Declare ViewModel
    private lateinit var viewModel: ElectionsViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val binding = FragmentElectionBinding.inflate(inflater)
        binding.lifecycleOwner = this

        // Add ViewModel values and create ViewModel
        val application = requireNotNull(this.activity).application
        val viewModelFactory = ElectionsViewModelFactory(ElectionDatabase.getInstance(application).electionDao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)

        binding.viewModel = viewModel

        // Initiate recycler adapters
        val upcomingElectionAdapter = ElectionListAdapter(ElectionListener {
            viewModel.onElectionClicked(it)
        })
        val savedElectionsAdapter = ElectionListAdapter(ElectionListener {
            viewModel.onElectionClicked(it)
        })

        // Populate recycler adapters
        binding.upcomingElectionsRecyclerView.adapter = upcomingElectionAdapter
        binding.savedElectionsRecyclerView.adapter = savedElectionsAdapter


//        viewModel.upcomingElections.observe(viewLifecycleOwner, Observer {
//            upcomingElectionAdapter.submitList(it)
//        })
//
//        viewModel.savedElections.observe(viewLifecycleOwner, Observer {
//            savedElectionsAdapter.submitList(it)
//        })


        /******************/
        viewModel.response.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })


        // Link elections to voter info
        viewModel.navigateToVoterInfo.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(it))
                viewModel.onNavigationCompleted()
            }
        })

        return binding.root
    }

}