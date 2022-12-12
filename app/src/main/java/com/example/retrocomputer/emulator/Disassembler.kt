package com.example.retrocomputer

import android.util.Log
import com.example.retrocomputer.AddressingMode
import com.example.retrocomputer.Disassembly
import java.io.File
import kotlin.reflect.typeOf

class Disassembler : CPU() {
    fun step() {
        while (cycles > 0) {
            clock()
        }
        clock()
        log()
    }

    fun hexHandler(instruction: String) : MutableList<Int> {
        val hex : MutableList<Int> = mutableListOf()
        val stringToHex = instruction.split(" ")
        val opcode = stringToHex[0]
        if (stringToHex.size > 1) {
            val addressingMode = stringToHex[1]
        } else {
            hex.add(lookup.indexOf(lookup.find { it.name == opcode }))
        }
        Log.d("hex", hex.toString())
        return hex
    }

    fun loadMemoryAsembly(assembly: String, startAddress: Int = 0x8000) {
        val lines : MutableList<String> = mutableListOf()
        val rom : MutableList<Int> = mutableListOf()
        assembly.split("\n").forEach { lines.add(it.trim()) }
        lines.forEachIndexed{ _, line -> hexHandler(line).forEachIndexed{_, hex -> rom.add(hex)}}
//        Log.d("rom", rom.toString())
//        loadMemory(rom, startAddress)
    }

    fun loadMemory(rom: MutableList<Int>, startAddress: Int = 0x8000): Map<Int, Disassembly> {
        if(startAddress < 0){
            throw Exception("Start address '$startAddress' out of bounds. [0-${memory.ram.size}]")
        } else if((startAddress + rom.size) > memory.ram.size){
            throw Exception("ROM size too large (${rom.size}) for start address $startAddress")
        }
        memory.write(0xFFFC, 0x00)
        memory.write(0xFFFC + 1, 0x80)
        rom.forEachIndexed{i,_ -> memory.ram[i + startAddress] = rom[i] }
        return disassemble(startAddress, startAddress + rom.size - 1)
    }

    fun disassemble(start: Int = 0x8000, stop: Int = 0xFFFF): Map<Int, Disassembly>{
        val disassembled: MutableMap<Int, Disassembly> = mutableMapOf()
        var currentAddress : Int = start
        var savedAddress: Int

        if(start > stop){
            throw Exception("Cannot start at position greater than stopping position")
        } else if(start < 0 || start > memory.ram.size){
            throw Exception("Start position '$start' out of bounds")
        } else if(stop < 0 || stop > memory.ram.size){
            throw Exception("Stop position '$stop' out of bounds")
        }

        while(currentAddress <= stop){
            savedAddress = currentAddress
            val instruction = lookup[memory.read(currentAddress)]
            val hex: MutableList<Int> = mutableListOf(lookup.indexOf(instruction))
            var asm = "$%04X ${instruction.name} ".format(currentAddress++)

            asm += when (instruction.mode) {
                AddressingMode.IMP -> ""
                AddressingMode.IMM -> {
                    hex.add(memory.read(currentAddress++))
                    "#$%02X".format(hex[1])
                }
                AddressingMode.ZP0 -> {
                    hex.add(memory.read(currentAddress++))
                    "$%02X".format(hex[1])
                }
                AddressingMode.ZPX -> {
                    hex.add(memory.read(currentAddress++))
                    "$%02X".format(hex[1]) + ", X"
                }
                AddressingMode.ZPY -> {
                    hex.add(memory.read(currentAddress++))
                    "$%02X".format(hex[1]) + ", Y"
                }
                AddressingMode.IZX -> {
                    hex.add(memory.read(currentAddress++))
                    "($%02X".format(hex[1]) + ", X)"
                }
                AddressingMode.IZY -> {
                    hex.add(memory.read(currentAddress++))
                    "($%02X".format(hex[1]) + ", Y)"
                }
                AddressingMode.ABS -> {
                    hex.addAll(listOf(memory.read(currentAddress++), memory.read(currentAddress++)))
                    "$%04X".format((hex[2] shl 8) or hex[1])
                }
                AddressingMode.ABX -> {
                    hex.addAll(listOf(memory.read(currentAddress++), memory.read(currentAddress++)))
                    "$%04X".format((hex[2] shl 8) or hex[1]) + ", X"
                }
                AddressingMode.ABY -> {
                    hex.addAll(listOf(memory.read(currentAddress++), memory.read(currentAddress++)))
                    "$%04X".format((hex[2] shl 8) or hex[1]) + ", Y"
                }
                AddressingMode.IND -> {
                    hex.addAll(listOf(memory.read(currentAddress++), memory.read(currentAddress++)))
                    "($%04X".format((hex[2] shl 8) or hex[1]) + ")"
                }
                AddressingMode.REL -> {
                    hex.add(memory.read(currentAddress++))
                    "%02X [$%04X]".format(hex[1], currentAddress + hex[1])
                }
            }
            disassembled[savedAddress] = Disassembly(savedAddress, asm.padEnd(30, ' '),
                instruction, hex.joinToString(" "){"%02X".format(it)})
        }
        outputDisassembly("./src/main/java/com/example/retrocomputer/disassembly.txt", disassembled, start, stop)
        return disassembled
    }

    private fun outputDisassembly(path: String, disassembled: Map<Int, Disassembly>, start: Int, stop: Int){
        File(path).printWriter().use { out ->
            out.println("-".repeat(66))
            out.println("Index     Address   Assembly            Hex Dump     Mode   Cycles")
            out.println("-".repeat(66))
            disassembled.forEach{(k,v) ->
                if(k in start..stop){
                    out.println("${k.toString().padStart(5, '0')}     $v ")
                }
            }
        }
    }

//    init {
//        log(true)
//    }

    fun log(init: Boolean = false, path: String = "./src/main/java/com/example/retrocomputer/log.txt"){
        if(!File(path).exists() || init){
            File(path).printWriter().use{ out ->
                out.println("-".repeat(55))
                out.println("OP    INS    A     X     Y     PC      SP     NVUBDIZC")
                out.println("-".repeat(55))
            }
        } else {
            File(path).appendText(showDebug() + "\n")
        }
    }

    fun showDebug(): String {
        return "%02X    %s    %02X    %02X    %02X    %04X    %02X    ".format(
            opcode, lookup[opcode].name, A, X, Y, PC, SP) +
                " ${status.toString(2).padStart(8,'0')}"
    }

    fun logMemory(page: Int, path: String = "./src/main/java/com/example/retrocomputer/log.txt"){
        var out = "\n" + "-".repeat(55) + " \n\n"
        out += showPage(0x00) + "\n" + showPage(page)
        File(path).appendText(out + "\n")
    }

    fun showPage(page: Int = 0): String {
        var out = ""
        for(i in 0..15) {
            var line = "$%04X:  ".format((i * 16) + page)
            for (j in 0..15) {
                line += "%02X ".format(memory.ram[((i * 16) + j) + page])
            }
            out += line + "\n"
        }
        return out
    }
}