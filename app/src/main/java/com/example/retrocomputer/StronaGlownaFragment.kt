package com.example.retrocomputer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction


class StronaGlownaFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_strona_glowna, container, false)
        val myButton = view.findViewById<Button>(R.id.button1)
        myButton.setOnClickListener{
            Toast.makeText(getActivity(), "BUTTON WAS PRESSED", Toast.LENGTH_SHORT).show()

        }
        return view
    }

}