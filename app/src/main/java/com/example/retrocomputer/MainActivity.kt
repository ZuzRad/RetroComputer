package com.example.retrocomputer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    val menuItems = arrayOf("Wczytaj plik", "Pliki testowe", "Mapa pamięci", "Ustawienia")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val menuSpinner=findViewById<Spinner>(R.id.menu)
        val arrayAdapter=ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, menuItems)
        menuSpinner.adapter=arrayAdapter
        menuSpinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Toast.makeText(applicationContext,"selected item is = "+menuItems[p2], Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

    }
}
