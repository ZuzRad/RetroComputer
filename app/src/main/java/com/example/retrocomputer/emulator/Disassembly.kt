package com.example.retrocomputer.emulator

data class Disassembly(
    var address: Int,
    var assembly: String,
    var instruction: Instruction,
)