package com.example.android.politicalpreparedness.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
data class RepresentativeResponse(
        val offices: List<Office>,
        val officials: List<Official>
)