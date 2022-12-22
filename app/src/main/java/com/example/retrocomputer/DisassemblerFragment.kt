package com.example.retrocomputer

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.example.retrocomputer.databinding.FragmentDisassemblerBinding

private const val ARG_ASSEMBLY = "assembly"

class DisassemblerFragment : Fragment() {
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
    ): View {
        _binding = FragmentDisassemblerBinding.inflate(inflater, container, false)

//        Wczytujemy dane z procesora, który emuluje wcześniej zakodowany tekst do wartości szesnatkowych
        disassembler = Disassembler()
        assembly?.let { disassembled = disassembler.loadMemoryAssembly(it) }

        updateUI()

//        Pamięć
        binding.textViewMemory.text = disassembler.displayMemory(0x00)

//        Przycisk RAM
        binding.buttSwitch1.setOnClickListener {
            binding.textViewMemory.text = disassembler.displayMemory(0x00)
            ROMstate = false
        }

//        Przycisk ROM
        binding.buttSwitch2.setOnClickListener {
            binding.textViewMemory.text = disassembler.displayMemory(0x8000)
            ROMstate = true
        }

//        Przycisk Reset
        binding.buttReset.setOnClickListener {
            disassembler.reset()
            updateUI()
        }

//        Przycisk Step
        binding.buttStep.setOnClickListener {
            if (disassembler.PC < disassembler.stopMemory) disassembler.step()
            else Toast.makeText(context, "To continue you must reset the CPU", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        return binding.root
    }

    private fun updateUI() {

//        Sprawdza czy flaga powinna świecić się na zielono lub czerwono
        val flagN : String = if (disassembler.getFlag(disassembler.flagShiftN) == 1) "<font color=${Color.GREEN}>N</font>"
        else "<font color=${Color.RED}>N</font>"
        val flagV : String = if (disassembler.getFlag(disassembler.flagShiftV) == 1) "<font color=${Color.GREEN}>V</font>"
        else "<font color=${Color.RED}>V</font>"
        val flagU : String = if (disassembler.getFlag(disassembler.flagShiftU) == 1) "<font color=${Color.GREEN}>U</font>"
        else "<font color=${Color.RED}>U</font>"
        val flagB : String = if (disassembler.getFlag(disassembler.flagShiftB) == 1) "<font color=${Color.GREEN}>B</font>"
        else "<font color=${Color.RED}>B</font>"
        val flagD : String = if (disassembler.getFlag(disassembler.flagShiftD) == 1) "<font color=${Color.GREEN}>D</font>"
        else "<font color=${Color.RED}>D</font>"
        val flagI : String = if (disassembler.getFlag(disassembler.flagShiftI) == 1) "<font color=${Color.GREEN}>I</font>"
        else "<font color=${Color.RED}>I</font>"
        val flagZ : String = if (disassembler.getFlag(disassembler.flagShiftZ) == 1) "<font color=${Color.GREEN}>Z</font>"
        else "<font color=${Color.RED}>Z</font>"
        val flagC : String = if (disassembler.getFlag(disassembler.flagShiftC) == 1) "<font color=${Color.GREEN}>C</font>"
        else "<font color=${Color.RED}>C</font>"

//        Pole tekstowe z aktualnymi wartościami procesora
        binding.textViewFirstLine.text = HtmlCompat.fromHtml("<string>" +
                "A: ${disassembler.A} X: ${disassembler.X} Y: ${disassembler.Y} " +
                "<br>SP: ${disassembler.SP} PC: ${disassembler.PC}" +
                "<br>FLAGS: $flagN $flagV $flagU $flagB $flagD $flagI $flagZ $flagC" +
                "</string>"
            , HtmlCompat.FROM_HTML_MODE_LEGACY)

//        Pole tekstowe w którym znajduje się następna instrukcja która zostanie wykonana po wciśnięciu przycisku "Step"
        binding.textViewSecondLine.text = "$${disassembler.PC.toString(16).uppercase()}: " +
                "${disassembled[disassembler.PC]?.assembly?.removeRange(0,5)?.trim()} " +
                "{${disassembled[disassembler.PC]?.instruction?.mode?.name}} | " +
                "${disassembled[disassembler.PC]?.hex?.trim()}"

//        Pole tekstowe posiadające przekonwertowane wartości w pamięci RAM na litery za pomocą tablicy ASCII
        binding.textViewAscii.text = disassembler.displayASCII()

//        Sprawdzamy czy powinnyśmy wyświetlić tablicę RAM czy ROM
        if (ROMstate) binding.textViewMemory.text = disassembler.displayMemory(0x8000)
        else if (!ROMstate) binding.textViewMemory.text = disassembler.displayMemory(0x00)
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