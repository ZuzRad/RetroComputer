package com.example.retrocomputer

import android.util.Log
import java.io.File

class Disassembler : CPU() {
    fun step() {
        while (cycles > 0) {
            clock()
        }
        clock()
        log()
    }

//    TODO: Dokończyć memory?? o ile bedzie potrzebne
//    TODO: parseWordOperand


    //    TODO: KLASA LABELS
    private class Labels {
        var labelIndex : MutableList<String> = mutableListOf()

        fun getPC(parameter: String) : Int {
            for(index in labelIndex){
                var nameAndAddr = index.split("|")
                if(parameter === nameAndAddr[0]){
                    return (nameAndAddr[1]).toInt()
                }
            }
            return -1
        }

        fun find(label: String):Boolean{
            for(index in labelIndex){
                var nameAndAddr = index.split("|")
                if(label === nameAndAddr[0]){
                    return true
                }
            }
            return false
        }
    }

    private var branchOutOfRange : Boolean = false
    private var labels : Labels = Labels()
    private var branchPC : Int = 0

    private fun parseByteOperand(parameter: String) : Int {
        var value : Int = -1
        if (parameter.matches(Regex("^([0-9]{1,3})\$"))) value = parameter.toInt(10)
        if (parameter.matches(Regex("^\\\$([0-9a-f]{1,2})\$", RegexOption.IGNORE_CASE))) value = parameter.toInt(16)
        if (parameter.matches(Regex("^%([0-1]{1,8})\$"))) value = parameter.toInt(2)
        if (value >= 0 && value <= 0xff) return value
        else return -1
    }

    private fun checkSingle(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }
        if (parameter !== "" && parameter !== "A") { return null }
        hex.add(lookup.indexOf(lookup.find { it.name == command }))
        branchPC++
        return hex
    }
    private fun checkZeroPage(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }
        val operand = parseByteOperand(parameter)
        if (operand >= 0) {
            hex.add(lookup.indexOf(lookup.find { it.name == command }))
            branchPC++
            hex.add(operand)
            branchPC++
        }
        return hex
    }
    private fun checkZeroPageX(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }
        val checkRegex = Regex("^([\\w$]+),X\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(lookup.indexOf(lookup.find { it.name == command }))
                branchPC++
                hex.add(operand)
                branchPC++
            }
        }
        return hex
    }
    private fun checkZeroPageY(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }
        val checkRegex = Regex("^([\\w$]+),Y\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(lookup.indexOf(lookup.find { it.name == command }))
                branchPC++
                hex.add(operand)
                branchPC++
            }
        }
        return hex
    }
    private fun checkAbsolute(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }

        val checkRegex = Regex("^([\\w$]+)\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            val operand = parseWordOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(lookup.indexOf(lookup.find { it.name == command }))
                branchPC++
                hex.add(operand)
                branchPC++
            }
        }

        var addr : Int
        if (parameter.matches(Regex("^\\w+\$"))) {
            hex.add(lookup.indexOf(lookup.find { it.name == command }))
            branchPC++
            if (labels.find(parameter)) {
                addr = labels.getPC(parameter)
                if (addr < 0 || addr > 0xffff) return hex
                hex.add(addr)
                branchPC++
                hex.add(addr shr 8)
                branchPC++
                return hex
            } else {
                hex.add(0xffff)
                branchPC++
                hex.add(0xffff shr 8)
                branchPC++
                return hex
            }
        }
        return hex
    }
    private fun checkAbsoluteX(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }

        val checkRegex = Regex("^([\\w$]+),X\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            val operand = parseWordOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(lookup.indexOf(lookup.find { it.name == command }))
                branchPC++
                hex.add(operand)
                branchPC++
                hex.add(operand shr 8)
                branchPC++
            }
        }

        var labelParameter : String; var addr : Int
        if (parameter.matches(Regex("^\\w+,X\$", RegexOption.IGNORE_CASE))) {
            labelParameter = parameter.replace(Regex(",X\$", RegexOption.IGNORE_CASE), "")
            hex.add(lookup.indexOf(lookup.find { it.name == command }))
            branchPC++
            if (labels.find(labelParameter)) {
                addr = labels.getPC(labelParameter)
                if (addr < 0 || addr > 0xffff) return hex
                hex.add(addr)
                branchPC++
                hex.add(addr shr 8)
                branchPC++
                return hex
            } else {
                hex.add(0xffff)
                branchPC++
                hex.add(0xffff shr 8)
                branchPC++
                return hex
            }
        }
        return hex
    }
    private fun checkAbsoluteY(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }

        val checkRegex = Regex("^([\\w$]+),Y\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            val operand = parseWordOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(lookup.indexOf(lookup.find { it.name == command }))
                branchPC++
                hex.add(operand)
                branchPC++
            }
        }

        var labelParameter : String; var addr : Int
        if (parameter.matches(Regex("^\\w+,Y\$", RegexOption.IGNORE_CASE))) {
            labelParameter = parameter.replace(Regex(",Y\$", RegexOption.IGNORE_CASE), "")
            hex.add(lookup.indexOf(lookup.find { it.name == command }))
            branchPC++
            if (labels.find(labelParameter)) {
                addr = labels.getPC(labelParameter)
                if (addr < 0 || addr > 0xffff) return hex
                hex.add(addr)
                branchPC++
                hex.add(addr shr 8)
                branchPC++
                return hex
            } else {
                hex.add(0xffff)
                branchPC++
                hex.add(0xffff shr 8)
                branchPC++
                return hex
            }
        }
        return hex
    }
    private fun checkIndirect(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }

        val checkRegex = Regex("^\\(([\\w$]+)\\)\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            val operand = parseWordOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(lookup.indexOf(lookup.find { it.name == command }))
                branchPC++
                hex.add(operand)
                branchPC++
                hex.add(operand shr 8)
                branchPC++
            }
        }
        return hex
    }
    private fun checkIndirectX(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }

        val checkRegex = Regex("^\\(([\\w$]+),X\\)\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(lookup.indexOf(lookup.find { it.name == command }))
                branchPC++
                hex.add(operand)
                branchPC++
            }
        }
        return hex
    }
    private fun checkIndirectY(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }

        val checkRegex = Regex("^\\(([\\w$]+)\\),Y\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(lookup.indexOf(lookup.find { it.name == command }))
                branchPC++
                hex.add(operand)
                branchPC++
            }
        }
        return hex
    }
    private fun checkBranch(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }
        var addr = -1
        if (parameter.matches(Regex("\\w+"))) addr = labels.getPC(parameter)
        if (addr === -1) { hex.addAll(listOf(0x00, 0x00)); branchPC++; branchPC++; return hex }
        hex.add(lookup.indexOf(lookup.find { it.name == command }))
        branchPC++
        var distance = addr - branchPC - 1
        if (distance < -128 || distance > 127) {
            branchOutOfRange = true
            return hex
        }
        hex.add(distance)
        branchPC++
        return hex
    }
    private fun checkImmediate(parameter : String, command : String) : MutableList<Int>? {
        val hex : MutableList<Int> = mutableListOf()
        if (command.isEmpty()) { return null }
        var value : Int; var label : String; var hilo : String; var addr : Int

        val checkRegex = Regex("^#([\\w$%]+)\$", RegexOption.IGNORE_CASE)
        if (parameter.matches(checkRegex)) {
            val operand = parseByteOperand(checkRegex.find(parameter)?.groupValues?.get(1)!!)
            if (operand >= 0) {
                hex.add(lookup.indexOf(lookup.find { it.name == command }))
                branchPC++
                hex.add(operand)
                branchPC++
                return hex
            }
        }

        // Label lo/hi
        if(parameter.matches(Regex("^#[<>]\\w+\$"))) {
            label = parameter.replace(Regex("^#[<>](\\w+)\$"), "$1")
            hilo = parameter.replace(Regex("^#([<>]).*\$"), "$1")
            hex.add(lookup.indexOf(lookup.find { it.name == command }))
            branchPC++
            if (labels.find(label)) {
                addr = labels.getPC(label)
                when (hilo) {
                    ">" -> {
                        hex.add((addr shr 8) and 0xff)
                        branchPC++
                        return hex
                    }
                    "<" -> {
                        hex.add(addr and 0xff)
                        return hex
                    }
                    else -> return hex
                }
            } else {
                hex.add(0x00)
                return hex
            }
        }
        return hex
    }

    private fun assemble(assembly: String) : MutableList<Int> {
        val lines : MutableList<String> = mutableListOf()
        assembly.split("\n").forEach { lines.add(it.trim()) }
        val hex : MutableList<Int> = mutableListOf()

        for (line in lines) {
            var label = ""; var command = ""; var parameter = "";
            if (line === "") {
                continue
            }
            var input : String = line

            if (input.matches(Regex("^\\w+:"))) {
                label = input.replace(Regex("(^\\w+):.*\$"), "$1")
                if (input.matches(Regex("^\\w+:[\\s]*\\w+.*\$"))) {
                    input = input.replace(Regex("^\\w+:[\\s]*(.*)\$"), "$1")
                    command = input.replace(Regex("^(\\w+).*\$"), "$1")
                } else {
                    command = ""
                }
            } else {
                command = input.replace(Regex("^(\\w+).*\$"), "$1")
            }

            command = command.uppercase()

            if (input.matches(Regex("^\\w+\\s+.*?\$"))) {
                parameter = input.replace(Regex("^\\w+\\s+(.*?)"), "$1")
            } else if (input.matches(Regex("^\\w+\$"))) {
                parameter = ""
            } else {
                continue
            }

            parameter = parameter.replace(" ", "")

            checkSingle(parameter, command)?.let { hex.addAll(it) }
            checkZeroPage(parameter, command)?.let { hex.addAll(it) }
            checkZeroPageX(parameter, command)?.let { hex.addAll(it) }
            checkZeroPageY(parameter, command)?.let { hex.addAll(it) }
            checkAbsolute(parameter, command)?.let { hex.addAll(it) }
            checkAbsoluteX(parameter, command)?.let { hex.addAll(it) }
            checkAbsoluteY(parameter, command)?.let { hex.addAll(it) }
            checkIndirect(parameter, command)?.let { hex.addAll(it) }
            checkIndirectX(parameter, command)?.let { hex.addAll(it) }
            checkIndirectY(parameter, command)?.let { hex.addAll(it) }
            checkImmediate(parameter, command)?.let { hex.addAll(it) }
            checkBranch(parameter, command)?.let { hex.addAll(it) }
        }
        return hex
    }

    fun loadMemoryAssembly(assembly: String, startAddress: Int = 0x8000) {
        val rom : MutableList<Int> = mutableListOf()
        branchOutOfRange = false
        reset()
        labels.reset()
        branchPC = startAddress
        labels.indexLines(assembly)
        branchPC = startAddress
        assemble(assembly).forEachIndexed{_, hex -> rom.add(hex)}
//        TODO: SOMETHING BETTER HERE / TOAST ON UI
        if (branchOutOfRange) return
        Log.d("rom", rom.toString())
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