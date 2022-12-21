package com.example.retrocomputer

import android.util.Log
import java.io.File
import kotlin.math.log

class Disassembler : CPU() {

    private val labels : MutableList<String> = mutableListOf()
    private var branchOutOfRange : Boolean = false
    private var branchPC : Int = 0
    var stopMemory : Int = 0

    fun findLabel(label : String) : Boolean {
        labels.forEach {
            val nameAndAddr = it.split("|")
            if(label == nameAndAddr[0]){
                return true
            }
        }
        return false
    }

    fun getPCfromLabel(parameter: String) : Int {
        labels.forEach {
            val nameAndAddr = it.split("|")
            if(parameter == nameAndAddr[0]){
                return (nameAndAddr[1]).toInt()
            }
        }
        return -1
    }

    private fun parseByteOperand(parameter: String) : Int {
        var value : Int = -1
        val checkRegexDecimal = Regex("^([0-9]{1,3})\$")
        if (parameter.matches(checkRegexDecimal)) value = checkRegexDecimal.find(parameter)?.groupValues?.get(1)!!.toInt(10)
        val checkRegexHex = Regex("^\\\$([0-9a-f]{1,2})\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegexHex)) value = checkRegexHex.find(parameter)?.groupValues?.get(1)!!.toInt(16)
        val checkRegexBinary = Regex("^%([0-1]{1,8})\$")
        if (parameter.matches(checkRegexBinary)) value = checkRegexBinary.find(parameter)?.groupValues?.get(1)!!.toInt(2)
        return if (value in 0..0xff) value
        else -1
    }

    private fun parseWordOperand(parameter: String) : Int {
        var value = -1
        var checkRegex = Regex("^\\\$([0-9a-f]{3,4})\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            value = checkRegex.find(parameter)?.groupValues?.get(1)!!.toInt(16)
        } else {
            checkRegex = Regex("^([0-9]{1,5})\$", RegexOption.IGNORE_CASE)
            if (parameter.matches(checkRegex)) value = checkRegex.find(parameter)?.groupValues?.get(1)!!.toInt(10)
        }
        return if (value in 0..0xffff) value
        else -1
    }

    private fun checkImplied(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }
        if (parameter != "" && parameter != "A") { return Pair(false, hex) }
        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.IMP })
        if (opcodeIndex != -1) {
            hex.add(opcodeIndex)
            branchPC++
            return Pair(true, hex)
        }
        return Pair(false, hex)
    }
    private fun checkZeroPage(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }
        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.ZP0 })
        val operand = parseByteOperand(parameter)
        if (operand >= 0 && opcodeIndex != -1) {
            hex.add(opcodeIndex)
            branchPC++
            hex.add(operand and 0xff)
            branchPC++
            return Pair(true, hex)
        }
        return Pair(false, hex)
    }
    private fun checkZeroPageX(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }
        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.ZPY })
        val checkRegex = Regex("^([\\w$]+),X\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex) && opcodeIndex != -1) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(opcodeIndex)
                branchPC++
                hex.add(operand and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }
        return Pair(false, hex)
    }
    private fun checkZeroPageY(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }
        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.ZPY })
        val checkRegex = Regex("^([\\w$]+),Y\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex) && opcodeIndex != -1) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(opcodeIndex)
                branchPC++
                hex.add(operand and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }
        return Pair(false, hex)
    }
    private fun checkAbsolute(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }

        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.ABS })
        val checkRegex = Regex("^([\\w$]+)\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex) && opcodeIndex != -1) {
            val operand = parseWordOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(opcodeIndex)
                branchPC++
                hex.add(operand and 0xff)
                branchPC++
                hex.add((operand shr 8)  and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }

        if (parameter.matches(Regex("^\\w+\$")) && opcodeIndex != -1) {
            hex.add(opcodeIndex)
            branchPC++
            if (findLabel(parameter)) {
                val addr = getPCfromLabel(parameter)
                if (addr < 0 || addr > 0xffff) return Pair(false, hex)
                hex.add(addr  and 0xff)
                branchPC++
                hex.add((addr shr 8) and 0xff)
                branchPC++
                return Pair(true, hex)
            } else {
                hex.add(0xffff and 0xff)
                branchPC++
                hex.add((0xffff shr 8) and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }
        return Pair(false, hex)
    }
    private fun checkAbsoluteX(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }

        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.ABX })
        val checkRegex = Regex("^([\\w$]+),X\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex) && opcodeIndex != -1) {
            val operand = parseWordOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(opcodeIndex)
                branchPC++
                hex.add(operand and 0xff)
                branchPC++
                hex.add((operand shr 8) and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }

        if (parameter.matches(Regex("^\\w+,X\$", RegexOption.IGNORE_CASE)) && opcodeIndex != -1) {
            val labelParameter = parameter.replace(Regex(",X\$", RegexOption.IGNORE_CASE), "")
            hex.add(opcodeIndex)
            branchPC++
            if (findLabel(labelParameter)) {
                val addr = getPCfromLabel(labelParameter)
                if (addr < 0 || addr > 0xffff) return Pair(false, hex)
                hex.add(addr and 0xff)
                branchPC++
                hex.add((addr shr 8) and 0xff)
                branchPC++
                return Pair(true, hex)
            } else {
                hex.add(0xffff and 0xff)
                branchPC++
                hex.add((0xffff shr 8) and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }
        return Pair(false, hex)
    }
    private fun checkAbsoluteY(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }

        val checkRegex = Regex("^([\\w$]+),Y\$", RegexOption.IGNORE_CASE)
        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.ABY })
        if (parameter.matches(checkRegex) && opcodeIndex != -1) {
            val operand = parseWordOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(opcodeIndex)
                branchPC++
                hex.add(operand and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }

        if (parameter.matches(Regex("^\\w+,Y\$", RegexOption.IGNORE_CASE)) && opcodeIndex != -1) {
            val labelParameter = parameter.replace(Regex(",Y\$", RegexOption.IGNORE_CASE), "")
            hex.add(opcodeIndex)
            branchPC++
            if (findLabel(labelParameter)) {
                val addr = getPCfromLabel(labelParameter)
                if (addr < 0 || addr > 0xffff) return Pair(false, hex)
                hex.add(addr and 0xff)
                branchPC++
                hex.add((addr shr 8) and 0xff)
                branchPC++
                return Pair(true, hex)
            } else {
                hex.add(0xffff and 0xff)
                branchPC++
                hex.add((0xffff shr 8) and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }
        return Pair(false, hex)
    }
    private fun checkIndirect(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }

        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.IND })
        val checkRegex = Regex("^\\(([\\w$]+)\\)\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex) && opcodeIndex != -1) {
            val operand = parseWordOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(opcodeIndex)
                branchPC++
                hex.add(operand and 0xff)
                branchPC++
                hex.add((operand shr 8) and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }
        return Pair(false, hex)
    }
    private fun checkIndirectX(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }

        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.IZX })
        val checkRegex = Regex("^\\(([\\w$]+),X\\)\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex) && opcodeIndex != -1) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(opcodeIndex)
                branchPC++
                hex.add(operand and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }
        return Pair(false, hex)
    }
    private fun checkIndirectY(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }

        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.IZY })
        val checkRegex = Regex("^\\(([\\w$]+)\\),Y\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex) && opcodeIndex != -1) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(opcodeIndex)
                branchPC++
                hex.add(operand and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }
        return Pair(false, hex)
    }
    private fun checkRelative(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }
        var addr = -1
        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.REL })
        if (parameter.matches(Regex("\\w+")) && opcodeIndex != -1) addr = getPCfromLabel(parameter)
        if (addr == -1) { hex.addAll(listOf(0x00, 0x00)); branchPC++; branchPC++; return Pair(false, hex) }
        hex.add(opcodeIndex)
        branchPC++
        val distance = addr - branchPC - 1
        if (distance < -128 || distance > 127) {
            branchOutOfRange = true
            return Pair(false, hex)
        }
        hex.add(distance and 0xff)
        branchPC++
        return Pair(true, hex)
    }

    private fun checkImmediate(parameter : String, command : String) : Pair<Boolean, MutableList<Int>> {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return Pair(false, hex) }

        val opcodeIndex = lookup.indexOf(lookup.filter { it.name == command }.find { it.mode == AddressingMode.IMM })
        val checkRegex = Regex("^#([\\w$%]+)\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex) && opcodeIndex != -1) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(opcodeIndex)
                branchPC++
                hex.add(operand and 0xff)
                branchPC++
                return Pair(true, hex)
            }
        }

        // Label lo/hi
        if(parameter.matches(Regex("^#[<>]\\w+\$")) && opcodeIndex != -1) {
            val checkLabelRegex = Regex("^#[<>](\\w+)\$")
            val checkHiloRegex = Regex("^#([<>]).*\$")
            val label = checkLabelRegex.find(parameter)?.groupValues?.get(1)!!
            val hilo = checkHiloRegex.find(parameter)?.groupValues?.get(1)!!
            hex.add(opcodeIndex)
            branchPC++
            if (findLabel(label)) {
                val addr = getPCfromLabel(label)
                when (hilo) {
                    ">" -> {
                        hex.add((addr shr 8) and 0xff)
                        branchPC++
                        return Pair(true, hex)
                    }
                    "<" -> {
                        hex.add(addr and 0xff)
                        branchPC++
                        return Pair(true, hex)
                    }
                    else -> return Pair(false, hex)
                }
            } else {
                hex.add(0x00)
                branchPC++
                return Pair(true, hex)
            }
        }
        return Pair(false, hex)
    }

    private fun assemble(assembly: String) : MutableList<Int> {
        val lines : MutableList<String> = mutableListOf()
        assembly.split("\n").forEach { lines.add(it.trim()) }
        val hex : MutableList<Int> = mutableListOf()

        for (line in lines) {
            var label : String; var command : String; var parameter : String
            if (line.isEmpty()) {
                continue
            }

            if (line.matches(Regex("^\\w+:"))) {
                val checkLabelRegex = Regex("(^\\w+):.*\$")
                label = checkLabelRegex.find(line)?.groupValues?.get(1)!!
                if(!findLabel(label)){
                    labels.add("$label|$branchPC")
                }
                if (line.matches(Regex("^\\w+:[\\s]*\\w+.*\$"))) {
                    val inputRegex = Regex("^\\w+:[\\s]*(.*)\$", RegexOption.IGNORE_CASE)
                    val input = inputRegex.find(line)?.groupValues?.get(1)!!
                    val commandRegex = Regex("^(\\w+).*\$")
                    command = commandRegex.find(input)?.groupValues?.get(1)!!
                } else {
                    command = ""
                }
            } else {
                val checkCommandRegex = Regex("^(\\w+).*\$")
                command = checkCommandRegex.find(line)?.groupValues?.get(1)!!
            }

            command = command.uppercase()

            if (line.matches(Regex("^\\w+\\s+.*?\$"))) {
                parameter = line.replace(Regex("^\\w+\\s+(.*?)"), "$1")
            } else if (line.matches(Regex("^\\w+\$"))) {
                parameter = ""
            } else {
                continue
            }

            parameter = parameter.replace(" ", "")

            val (boolean_implied, hex_implied) = checkImplied(parameter, command)
            if (boolean_implied) { hex.addAll(hex_implied); continue }
            val (boolean_immediate, hex_immediate) = checkImmediate(parameter, command)
            if (boolean_immediate) { hex.addAll(hex_immediate); continue }
            val (boolean_zeroPage, hex_zeroPage) = checkZeroPage(parameter, command)
            if (boolean_zeroPage) { hex.addAll(hex_zeroPage); continue }
            val (boolean_zeroPageX, hex_zeroPageX) = checkZeroPageX(parameter, command)
            if (boolean_zeroPageX) { hex.addAll(hex_zeroPageX); continue }
            val (boolean_zeroPageY, hex_zeroPageY) = checkZeroPageY(parameter, command)
            if (boolean_zeroPageY) { hex.addAll(hex_zeroPageY); continue }
            val (boolean_absoluteX, hex_absoluteX) = checkAbsoluteX(parameter, command)
            if (boolean_absoluteX) { hex.addAll(hex_absoluteX); continue }
            val (boolean_absoluteY, hex_absoluteY) = checkAbsoluteY(parameter, command)
            if (boolean_absoluteY) { hex.addAll(hex_absoluteY); continue }
            val (boolean_indirect, hex_indirect) = checkIndirect(parameter, command)
            if (boolean_indirect) { hex.addAll(hex_indirect); continue }
            val (boolean_indirectX, hex_indirectX) = checkIndirectX(parameter, command)
            if (boolean_indirectX) { hex.addAll(hex_indirectX); continue }
            val (boolean_indirectY, hex_indirectY) = checkIndirectY(parameter, command)
            if (boolean_indirectY) { hex.addAll(hex_indirectY); continue }
            val (boolean_absolute, hex_absolute) = checkAbsolute(parameter, command)
            if (boolean_absolute) { hex.addAll(hex_absolute); continue }
            val (boolean_relative, hex_relative) = checkRelative(parameter, command)
            if (boolean_relative) { hex.addAll(hex_relative); continue }
        }
        return hex
    }

    fun loadMemoryAssembly(assembly: String, startAddress: Int = 0x8000): Map<Int, Disassembly> {
        val rom : MutableList<Int> = mutableListOf()
        branchOutOfRange = false
        labels.clear()
        branchPC = startAddress
        assemble(assembly).forEachIndexed{_, hex -> rom.add(hex)}
        if (branchOutOfRange) throw Exception("BRANCH OUT OF RANGE")
        val testRom : MutableList<String> = mutableListOf()
        rom.forEach { testRom.add(it.toString(16).uppercase()) }
        return loadMemory(rom, startAddress)
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
        return disassemble(startAddress, startAddress + rom.size)
    }

    fun disassemble(start: Int = 0x8000, stop: Int = 0xFFFF): Map<Int, Disassembly>{
        stopMemory = stop
        val disassembled: MutableMap<Int, Disassembly> = mutableMapOf()
        var currentAddress : Int = start
        var savedAddress: Int

        if(start > stop){
            throw Exception("Cannot start at position greater than stopping position")
        } else if(start < 0 || start > memory.ram.size){
            throw Exception("Start position '$start' out of bounds")
        } else if(stop > memory.ram.size) {
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

        reset()
        while(PC < stop) step()
//        outputTestDisassembly("./src/main/java/com/example/retrocomputer/disassembly.txt", disassembled, start, stop)
        return disassembled
    }

    fun step() {
        while (cycles > 0) {
            clock()
        }
        clock()
//        logTest()
    }

    fun displayMemory(page: Int = 0): String {
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

    fun displayASCII() : String {
        var output = ""
        for(i in 0..15) {
            for (j in 0..15) {
                if (memory.ram[((i * 16) + j)] != 0x00) {
                    output += hexToAscii("%02X".format(memory.ram[((i * 16) + j)]))
                }
            }
        }
        return output
    }

    private fun hexToAscii(hexStr: String): String {
        val output = StringBuilder("")
        var i = 0
        while (i < hexStr.length) {
            val str = hexStr.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }


//    TEST/DEBUG FUNCTIONS

//    private fun outputTestDisassembly(path: String, disassembled: Map<Int, Disassembly>, start: Int, stop: Int){
//        File(path).printWriter().use { out ->
//            out.println("-".repeat(66))
//            out.println("Index     Address   Assembly            Hex Dump     Mode   Cycles")
//            out.println("-".repeat(66))
//            disassembled.forEach{(k,v) ->
//                if(k in start..stop){
//                    out.println("${k.toString().padStart(5, '0')}     $v ")
//                }
//            }
//        }
//    }
//
//    init {
//        logTest(true)
//    }
//
//    private fun logTest(init: Boolean = false, path: String = "./src/main/java/com/example/retrocomputer/log.txt"){
//        if(!File(path).exists() || init){
//            File(path).printWriter().use{ out ->
//                out.println("-".repeat(55))
//                out.println("OP    INS    A     X     Y     PC      SP     NVUBDIZC")
//                out.println("-".repeat(55))
//            }
//        } else {
//            File(path).appendText(showDebugTest() + "\n")
//        }
//    }
//
//    private fun showDebugTest(): String {
//        return "%02X    %s    %02X    %02X    %02X    %04X    %02X    ".format(
//            opcode, lookup[opcode].name, A, X, Y, PC, SP) +
//                " ${status.toString(2).padStart(8,'0')}"
//    }
//
//    fun logMemoryTest(page: Int, path: String = "./src/main/java/com/example/retrocomputer/log.txt"){
//        var out = "\n" + "-".repeat(55) + " \n\n"
//        out += displayMemory(0x00) + "\n" + displayMemory(page)
//        File(path).appendText(out + "\n")
//    }

}