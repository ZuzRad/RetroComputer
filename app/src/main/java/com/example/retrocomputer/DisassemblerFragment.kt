package com.example.retrocomputer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.retrocomputer.databinding.FragmentDisassemblerBinding
import com.example.retrocomputer.databinding.FragmentEmulatorBinding
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_ASSEMBLY = "assembly"

/**
 * A simple [Fragment] subclass.
 * Use the [action.newInstance] factory method to
 * create an instance of this fragment.
 */
class DisassemblerFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var assembly: String? = null
    private lateinit var hex: String

    private var _binding: FragmentDisassemblerBinding? = null
    private val binding get() = _binding!!
    private val disassembler : Disassembler = Disassembler()
    private lateinit var disassembled : Map<Int, Disassembly>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            assembly = it.getString(ARG_ASSEMBLY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDisassemblerBinding.inflate(inflater, container, false)
        assembly?.let { disassembled = disassembler.loadMemoryAssembly(it) }
//        disassembled.forEach{(k,v) ->
//            if(k in 0x8000..(0x8000 + disassembled.size)){
//                out.println("${k.toString().padStart(5, '0')}     $v ")
//            }
//        }

        binding.textViewFirstLine.text = "A: ${disassembler.A} X: ${disassembler.X} Y: ${disassembler.Y}" +
                "SP: ${disassembler.SP} PC: ${disassembler.PC}"
        binding.textViewMainEmul.text = assembly
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(assembly: String) =
            DisassemblerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ASSEMBLY, assembly)
                }
            }
    }
}