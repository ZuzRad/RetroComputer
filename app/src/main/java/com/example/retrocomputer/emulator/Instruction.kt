package com.example.retrocomputer

// Struktura instrukcji
data class Instruction (
    val name: String,
    val opcode: Opcode,
    val mode: AddressingMode,
    val cycles: Int,
)