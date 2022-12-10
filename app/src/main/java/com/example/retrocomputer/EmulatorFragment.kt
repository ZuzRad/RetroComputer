package com.example.retrocomputer

import android.os.Bundle
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.retrocomputer.databinding.FragmentEmulatorBinding
import java.io.*

class EmulatorFragment : Fragment() {

    private var _binding: FragmentEmulatorBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEmulatorBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myButton = view.findViewById<Button>(R.id.butt_uruchom)
        myButton.setOnClickListener{
            val fragment : Fragment = DisassemblerFragment()
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frameLayout, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
            requireActivity().title = "Disassembler"

        }

       val edittext= view.findViewById<EditText>(R.id.textView3)

        val myButton1 = view.findViewById<Button>(R.id.butt1)
        myButton1.setOnClickListener{
            edittext.setText("test1")
        }

        val myButton2 = view.findViewById<Button>(R.id.butt2)
        myButton2.setOnClickListener{
            edittext.setText("test2")
        }

        val myButton3 = view.findViewById<Button>(R.id.butt3)
        myButton3.setOnClickListener{
            edittext.setText("test3")
        }


        val getTxt = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {

                val uri = it
                try {
                    val `in`: InputStream? = uri?.let { it1 ->
                        context?.getContentResolver()?.openInputStream(
                            it1
                        )
                    }
                    val r = BufferedReader(InputStreamReader(`in`))
                    val total = StringBuilder()
                    var line: String?
                    while (r.readLine().also { line = it } != null) {
                        total.append(line).append('\n')
                    }
                    val content = total.toString()
                    edittext.setText(content)
                } catch (e: Exception) {
                }
            }
        )
        val myButtonWczytaj = view.findViewById<Button>(R.id.butt_wczytaj)
        myButtonWczytaj.setOnClickListener{
            getTxt.launch("text/plain")
        }
    }

}