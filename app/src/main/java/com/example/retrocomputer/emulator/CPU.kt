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

    enum class Opcode {
        XXX, ADC, AND, ASL, BIT, BPL, BMI, BVC, BVS, BCC, BCS, BNE,
        BEQ, BRK, CMP, CPX, CPY, DEC, EOR, CLC,
        SEC, CLI, SEI, CLV, CLD, SED, INC, JMP, JSR, LDA, LDX, LDY, LSR, NOP, ORA, TAX, TXA, DEX, INX,
        TAY, TYA, DEY, INY, ROR, ROL, RTI, RTS, SBC, STA, TXS, TSX, PHA, PLA, PHP, PLP, STX, STY
    }

    private fun handleOpcodes(opcode: Opcode): UByte {
        return when(opcode) {
            Opcode.XXX -> XXX()
            Opcode.ADC -> ADC()
            Opcode.AND -> AND()
            Opcode.ASL -> ASL()
            Opcode.BIT -> BIT()
            Opcode.BPL -> BPL()
            Opcode.BMI -> BMI()
            Opcode.BVC -> BVC()
            Opcode.BVS -> BVS()
            Opcode.BCC -> BCC()
            Opcode.BCS -> BCS()
            Opcode.BNE -> BNE()
            Opcode.BEQ -> BEQ()
            Opcode.BRK -> BRK()
            Opcode.CMP -> CMP()
            Opcode.CPX -> CPX()
            Opcode.CPY -> CPY()
            Opcode.DEC -> DEC()
            Opcode.EOR -> EOR()
            Opcode.CLC -> CLC()
            Opcode.SEC -> SEC()
            Opcode.CLI -> CLI()
            Opcode.SEI -> SEI()
            Opcode.CLV -> CLV()
            Opcode.CLD -> CLD()
            Opcode.SED -> SED()
            Opcode.INC -> INC()
            Opcode.JMP -> JMP()
            Opcode.JSR -> JSR()
            Opcode.LDA -> LDA()
            Opcode.LDX -> LDX()
            Opcode.LDY -> LDY()
            Opcode.LSR -> LSR()
            Opcode.NOP -> NOP()
            Opcode.ORA -> ORA()
            Opcode.TAX -> TAX()
            Opcode.TXA -> TXA()
            Opcode.DEX -> DEX()
            Opcode.INX -> INX()
            Opcode.TAY -> TAY()
            Opcode.TYA -> TYA()
            Opcode.DEY -> DEY()
            Opcode.INY -> INY()
            Opcode.ROR -> ROR()
            Opcode.ROL -> ROL()
            Opcode.RTI -> RTI()
            Opcode.RTS -> RTS()
            Opcode.SBC -> SBC()
            Opcode.STA -> STA()
            Opcode.TXS -> TXS()
            Opcode.TSX -> TSX()
            Opcode.PHA -> PHA()
            Opcode.PLA -> PLA()
            Opcode.PHP -> PHP()
            Opcode.PLP -> PLP()
            Opcode.STX -> STX()
            Opcode.STY -> STY()
        }
    }

    private fun XXX() : UByte {
        return 0U
    }
    private fun ADC() : UByte {
        return 0U
    }
    private fun AND() : UByte {
        return 0U
    }
    private fun ASL() : UByte {
        return 0U
    }
    private fun BIT() : UByte {
        return 0U
    }
    private fun BPL() : UByte {
        return 0U
    }
    private fun BMI() : UByte {
        return 0U
    }
    private fun BVC() : UByte {
        return 0U
    }
    private fun BVS() : UByte {
        return 0U
    }
    private fun BCC() : UByte {
        return 0U
    }
    private fun BCS() : UByte {
        return 0U
    }
    private fun BNE() : UByte {
        return 0U
    }
    private fun BEQ() : UByte {
        return 0U
    }
    private fun BRK() : UByte {
        return 0U
    }
    private fun CMP() : UByte {
        return 0U
    }
    private fun CPX() : UByte {
        return 0U
    }
    private fun CPY() : UByte {
        return 0U
    }
    private fun DEC() : UByte {
        return 0U
    }
    private fun EOR() : UByte {
        return 0U
    }
    private fun CLC() : UByte {
        return 0U
    }
    private fun SEC() : UByte {
        return 0U
    }
    private fun CLI() : UByte {
        return 0U
    }
    private fun SEI() : UByte {
        return 0U
    }
    private fun CLV() : UByte {
        return 0U
    }
    private fun CLD() : UByte {
        return 0U
    }
    private fun SED() : UByte {
        return 0U
    }
    private fun INC() : UByte {
        return 0U
    }
    private fun JMP() : UByte {
        return 0U
    }
    private fun JSR() : UByte {
        return 0U
    }
    private fun LDA() : UByte {
        return 0U
    }
    private fun LDX() : UByte {
        return 0U
    }
    private fun LDY() : UByte {
        return 0U
    }
    private fun LSR() : UByte {
        return 0U
    }
    private fun NOP() : UByte {
        return 0U
    }
    private fun ORA() : UByte {
        return 0U
    }
    private fun TAX() : UByte {
        return 0U
    }
    private fun TXA() : UByte {
        return 0U
    }
    private fun DEX() : UByte {
        return 0U
    }
    private fun INX() : UByte {
        return 0U
    }
    private fun TAY() : UByte {
        return 0U
    }
    private fun TYA() : UByte {
        return 0U
    }
    private fun DEY() : UByte {
        return 0U
    }
    private fun INY() : UByte {
        return 0U
    }
    private fun ROR() : UByte {
        return 0U
    }
    private fun ROL() : UByte {
        return 0U
    }
    private fun RTI() : UByte {
        return 0U
    }
    private fun RTS() : UByte {
        return 0U
    }
    private fun SBC() : UByte {
        return 0U
    }
    private fun STA() : UByte {
        return 0U
    }
    private fun TXS() : UByte {
        return 0U
    }
    private fun TSX() : UByte {
        return 0U
    }
    private fun PHA() : UByte {
        return 0U
    }
    private fun PLA() : UByte {
        return 0U
    }
    private fun PHP() : UByte {
        return 0U
    }
    private fun PLP() : UByte {
        return 0U
    }
    private fun STX() : UByte {
        return 0U
    }
    private fun STY() : UByte {
        return 0U
    }

    data class Instruction (
        val name: String,
        val opcode: Opcode,
        val mode: AddressingMode,
        val cycles: UByte,
    )

    var lookup = List<Instruction>(0x100) {
        Instruction("???", Opcode.XXX, AddressingMode.IMP, 2U)
    }
}
