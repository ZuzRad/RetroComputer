package com.example.retrocomputer.emulator

class CPU() {
    private val bus = Bus()

//    Registers
    var A: UByte = 0x00U      // Accumulator
    var X: UByte = 0x00U      // Register X
    var Y: UByte = 0x00U      // Register Y
    var SP: UByte = 0x00U  // Stack pointer
    var PC: UShort = 0x0000U // Program counter
    var status: UByte = 0x00U

//    Flags
    var flagC : Boolean = false  // Carry Bit
    var flagZ : Boolean = false  // Zero
    var flagI : Boolean = false  // Disable Interrupts
    var flagD : Boolean = false  // DecimalMode
    var flagB : Boolean = false  // Break
    var flagU : Boolean = true   // Unused
    var flagV : Boolean = false  // Overflow
    var flagN : Boolean = false  // Negative

    var flagShiftC = (1 shl 0)
    var flagShiftZ = (1 shl 1)
    var flagShiftI = (1 shl 2)
    var flagShiftD = (1 shl 3)
    var flagShiftB = (1 shl 4)
    var flagShiftU = (1 shl 5)
    var flagShiftV = (1 shl 6)
    var flagShiftN = (1 shl 7)
}