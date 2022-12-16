package com.example.retrocomputer

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
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

    private var _binding: FragmentDisassemblerBinding? = null
    private val binding get() = _binding!!
    private lateinit var disassembler : Disassembler
    private lateinit var disassembled : Map<Int, Disassembly>
    private var ROMstate : Boolean = false

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
        disassembler = Disassembler()
        assembly?.let { disassembled = disassembler.loadMemoryAssembly(it) }

        updateUI()

        binding.textViewMemory.text = disassembler.showPage(0x00)

        binding.buttSwitch1.setOnClickListener {
            binding.textViewMemory.text = disassembler.showPage(0x00)
            ROMstate = false
        }
        binding.buttSwitch2.setOnClickListener {
            binding.textViewMemory.text = disassembler.showPage(0x8000)
            ROMstate = true
        }

        binding.buttReset.setOnClickListener {
            disassembler.reset()
            updateUI()
        }
        binding.buttStep.setOnClickListener {
            if (disassembler.PC < disassembler.stopMemory) disassembler.step()
            else Toast.makeText(context, "To continue you must reset the CPU", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        return binding.root
    }

    fun updateUI() {
        val flagN : String; val flagV : String; val flagU : String; val flagB : String
        val flagD : String; val flagI : String; val flagZ : String; val flagC : String

        if (disassembler.getFlag(disassembler.flagShiftN) == 1) flagN = "<font color=${Color.GREEN}>N</font>"
        else flagN = "<font color=${Color.RED}>N</font>"
        if (disassembler.getFlag(disassembler.flagShiftV) == 1) flagV = "<font color=${Color.GREEN}>V</font>"
        else flagV = "<font color=${Color.RED}>V</font>"
        if (disassembler.getFlag(disassembler.flagShiftU) == 1) flagU = "<font color=${Color.GREEN}>U</font>"
        else flagU = "<font color=${Color.RED}>U</font>"
        if (disassembler.getFlag(disassembler.flagShiftB) == 1) flagB = "<font color=${Color.GREEN}>B</font>"
        else flagB = "<font color=${Color.RED}>B</font>"
        if (disassembler.getFlag(disassembler.flagShiftD) == 1) flagD = "<font color=${Color.GREEN}>D</font>"
        else flagD = "<font color=${Color.RED}>D</font>"
        if (disassembler.getFlag(disassembler.flagShiftI) == 1) flagI = "<font color=${Color.GREEN}>I</font>"
        else flagI = "<font color=${Color.RED}>I</font>"
        if (disassembler.getFlag(disassembler.flagShiftZ) == 1) flagZ = "<font color=${Color.GREEN}>Z</font>"
        else flagZ = "<font color=${Color.RED}>Z</font>"
        if (disassembler.getFlag(disassembler.flagShiftC) == 1) flagC = "<font color=${Color.GREEN}>C</font>"
        else flagC = "<font color=${Color.RED}>C</font>"

        binding.textViewFirstLine.text = HtmlCompat.fromHtml("<string>" +
                "A: ${disassembler.A} X: ${disassembler.X} Y: ${disassembler.Y} " +
                "<br>SP: ${disassembler.SP} PC: ${disassembler.PC}" +
                "<br>FLAGS: $flagN $flagV $flagU $flagB $flagD $flagI $flagZ $flagC" +
                "</string>"
            , HtmlCompat.FROM_HTML_MODE_LEGACY)

        binding.textViewSecondLine.text = "$${disassembler.PC.toString(16).uppercase()}: " +
                "${disassembled[disassembler.PC]?.assembly?.removeRange(0,5)?.trim()} " +
                "{${disassembled[disassembler.PC]?.instruction?.mode?.name}} | " +
                "${disassembled[disassembler.PC]?.hex?.trim()}"

        binding.textViewAscii.text = disassembler.displayASCII()

        if (ROMstate) binding.textViewMemory.text = disassembler.showPage(0x8000)
        else if (!ROMstate) binding.textViewMemory.text = disassembler.showPage(0x00)
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