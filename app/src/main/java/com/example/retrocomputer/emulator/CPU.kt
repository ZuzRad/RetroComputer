package com.example.retrocomputer.emulator

class CPU() {
    private val bus = Bus()

//    Registers
    var A: UByte = 0x00U      // Accumulator
    var X: UByte = 0x00U      // Register X
    var Y: UByte = 0x00U      // Register Y
    var SP: UByte = 0x00U  // Stack pointer
    var PC: UShort = 0x0000U // Program counter
    var status: UByte = 0x00U // Status register (current flag)

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

//    Functions / Other Pins

    private fun clock() {

    }
    private fun reset() {

    }
    private fun irq() {     // Interrupt Request

    }
    private fun nmi() {     // Non-Maskable Interrupt

    }

    private fun fetch () : UByte {
        return 0U
    }

    var fetched : UByte = 0x00U

    var addr_abs : UShort = 0x0000U
    var addr_rel : UShort = 0x00U
    var opcode : UByte = 0x00U
    var cycles : UByte = 0U

//    Addressing modes

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

    private fun handleAddressingMode(addrMode: AddressingMode): UByte {
        return when(addrMode) {
            AddressingMode.IMM -> IMM()
            AddressingMode.IMP -> IMP()
            AddressingMode.ZP0 -> ZP0()
            AddressingMode.ZPX -> ZPX()
            AddressingMode.ZPY -> ZPY()
            AddressingMode.REL -> REL()
            AddressingMode.ABS -> ABS()
            AddressingMode.ABX -> ABX()
            AddressingMode.ABY -> ABY()
            AddressingMode.IND -> IND()
            AddressingMode.IZX -> IZX()
            AddressingMode.IZY -> IZY()
        }
    }

    private fun IMM() : UByte {
        return 0U
    }
    private fun IMP() : UByte {
       return 0U
    }
    private fun ZP0() : UByte {
        return 0U
    }
    private fun ZPX() : UByte {
        return 0U
    }
    private fun ZPY() : UByte {
        return 0U
    }
    private fun REL() : UByte {
        return 0U
    }
    private fun ABS() : UByte {
        return 0U
    }
    private fun ABX() : UByte {
        return 0U
    }
    private fun ABY() : UByte {
        return 0U
    }
    private fun IND() : UByte {
        return 0U
    }
    private fun IZX() : UByte {
        return 0U
    }
    private fun IZY() : UByte {
        return 0U
    }

//    Opcodes
    enum class Opcodes {
        ADC, AND, ASL, BIT, BPL, BMI, BVC, BVS, BCC, BCS, BNE, BEQ, BRK, CMP, CPX, CPY, DEC, EOR, CLC,
        SEC, CLI, SEI, CLV, CLD, SED, INC, JMP, JSR, LDA, LDX, LDY, LSR, NOP, ORA, TAX, TXA, DEX, INX,
        TAY, TYA, DEY, INY, ROR, ROL, RTI, RTS, SBC, STA, TXS, TSX, PHA, PLA, PHP, PLP, STX, STY, XXX
    }

    data class Instruction (
        val name: String = "???",
        val opcode: UByte,
        val mode: AddressingMode,
        val cycles: UByte = 0U
    )

    var lookup = List<Instruction>(0x100) { i -> i. }
}