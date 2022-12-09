package com.example.retrocomputer

enum class AddressingMode {
    IMM, // Immediate
    IMP, // Implied
    ZP0, // Zero Page absolute
    ZPX, // Zero Page X
    ZPY, // Zero Page Y
    REL,  // Relative
    ABS, // Absolute A
    ABX, // Absolute X
    ABY, // Absolute Y
    IND, // Indirect
    IZX, // Pre-indexed indirect
    IZY, // Post-indexed indirect
}