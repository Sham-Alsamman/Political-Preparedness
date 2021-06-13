package com.example.android.politicalpreparedness.representative.adapter

import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.representative.model.Representative

@BindingAdapter("representativeListData")
fun bindRepresentativeRecyclerView(recyclerView: RecyclerView, data: List<Representative>?) {
    val adapter = recyclerView.adapter as RepresentativeListAdapter
    adapter.submitList(data)
}

@BindingAdapter("profileImage")
fun fetchImage(view: ImageView, src: String?) {
    src?.let {
        val uri = src.toUri().buildUpon().scheme("https").build()
        // Add Glide call to load image and circle crop - user ic_profile as a placeholder and for errors.
        Glide.with(view.context)
                .load(uri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_profile))
                .circleCrop()
                .into(view)
    }
}

@BindingAdapter("stateValue")
fun Spinner.setNewValue(value: String?) {
    val adapter = toTypedAdapter<String>(this.adapter as ArrayAdapter<*>)
    val position = when (adapter.getItem(0)) {
        is String -> adapter.getPosition(value)
        else -> this.selectedItemPosition
    }
    if (position >= 0) {
        setSelection(position)
    }
}

inline fun <reified T> toTypedAdapter(adapter: ArrayAdapter<*>): ArrayAdapter<T>{
    return adapter as ArrayAdapter<T>
}
