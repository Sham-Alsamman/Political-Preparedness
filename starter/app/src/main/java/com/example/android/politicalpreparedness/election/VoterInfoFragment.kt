package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    private lateinit var viewModel: VoterInfoViewModel
    private val args: VoterInfoFragmentArgs by navArgs()


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val binding = FragmentVoterInfoBinding.inflate(inflater)
        binding.lifecycleOwner = this

        // Add ViewModel values and create ViewModel
        val application = requireNotNull(this.activity).application
        val viewModelFactory = VoterInfoViewModelFactory(args.argElection, ElectionDatabase.getInstance(application).electionDao)
        viewModel = ViewModelProvider(this, viewModelFactory).get(VoterInfoViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.response.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        binding.stateLocations.setOnClickListener {
            viewModel.votingLocationUrl?.let {
                openURLIntent(it)
            }
        }

        binding.stateBallot.setOnClickListener {
            viewModel.ballotInfoUrl?.let {
                openURLIntent(it)
            }
        }

        viewModel.correspondenceAddress.observe(viewLifecycleOwner, Observer {
            if (it == null){
                binding.addressGroup.visibility = View.INVISIBLE
            } else {
                binding.addressGroup.visibility = View.VISIBLE
                binding.address.text = viewModel.correspondenceAddress.value!!
            }
        })

        return binding.root
    }

    // Create method to load URL intents
    private fun openURLIntent(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context?.startActivity(intent)
    }
}