package com.example.retrocomputer

import android.os.Bundle
import android.content.Context
import android.util.Log
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
        _binding = FragmentEmulatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       val edittext= view.findViewById<EditText>(R.id.textView_assemble)
        val myButton = view.findViewById<Button>(R.id.butt_uruchom)
        myButton.setOnClickListener{
            if (edittext.text.toString().length > 6) {
                val fragment : Fragment = DisassemblerFragment.newInstance(edittext.text.toString())
                val fragmentManager = requireActivity().supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.frameLayout, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
                requireActivity().title = "Disassembler"
            }
        }


        val myButton1 = view.findViewById<Button>(R.id.butt1)
        myButton1.setOnClickListener{
            val assembly = "LDA #$48\n" +
                    "STA $0000\n" +
                    "LDA #$45\n" +
                    "STA $0001\n" +
                    "LDA #$4C\n" +
                    "STA $0002\n" +
                    "LDA #$4C\n" +
                    "STA $0003\n" +
                    "LDA #$4F\n" +
                    "STA $0004\n" +
                    "LDA #$20\n" +
                    "STA $0005\n" +
                    "LDA #$57\n" +
                    "STA $0006\n" +
                    "LDA #$4F\n" +
                    "STA $0007\n" +
                    "LDA #$52\n" +
                    "STA $0008\n" +
                    "LDA #$4C\n" +
                    "STA $0009\n" +
                    "LDA #$44\n" +
                    "STA $000A"
            edittext.setText(assembly)
        }

        val myButton2 = view.findViewById<Button>(R.id.butt2)
        myButton2.setOnClickListener{
            val assembly ="LDX #$28\n" +
                    "STX $0000\n" +
                    "LDX #$30\n" +
                    "STX $0001\n" +
                    "LDX #$5F\n" +
                    "STX $0002\n" +
                    "LDX #$30\n" +
                    "STX $0003\n" +
                    "LDX #$29\n" +
                    "STX $0004\n" +
                    "LDX #$20\n" +
                    "STX $0005\n" +
                    "LDX #$3C\n" +
                    "STX $0006\n" +
                    "LDX #$28\n" +
                    "STX $0007\n" +
                    "LDX #$5E\n" +
                    "STX $0008\n" +
                    "LDX #$5F\n" +
                    "STX $0009\n" +
                    "LDX #$5E\n" +
                    "STX $000A\n" +
                    "LDX #$29\n" +
                    "STX $000B\n" +
                    "LDX #$3E\n" +
                    "STX $000C\n" +
                    "LDX #$20\n" +
                    "STX $000D\n" +
                    "LDX #$28\n" +
                    "STX $000E\n" +
                    "LDX #$3D\n" +
                    "STX $000F\n" +
                    "LDX #$5F\n" +
                    "STX $0010\n" +
                    "LDX #$3D\n" +
                    "STX $0011\n" +
                    "LDX #$29\n" +
                    "STX $0012"
            edittext.setText(assembly)
        }

        val myButton3 = view.findViewById<Button>(R.id.butt3)
        myButton3.setOnClickListener{
            val assembly = "LDY #\$3A\n" +
                    "STY \$0000\n" +
                    "LDY #\$29\n" +
                    "STY \$0001\n" +
                    "LDY #\$20\n" +
                    "STY \$0002\n" +
                    "LDY #\$3A\n" +
                    "STY \$0003\n" +
                    "LDY #\$2F\n" +
                    "STY \$0004\n" +
                    "LDY #\$20\n" +
                    "STY \$0005\n" +
                    "LDY #\$3A\n" +
                    "STY \$0006\n" +
                    "LDY #\$44\n" +
                    "STY \$0007\n" +
                    "LDY #\$20\n" +
                    "STY \$0008\n" +
                    "LDY #\$2E\n" +
                    "STY \$0009\n" +
                    "LDY #\$5F\n" +
                    "STY \$000A\n" +
                    "LDY #\$2E\n" +
                    "STY \$000B"
            edittext.setText(assembly)
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