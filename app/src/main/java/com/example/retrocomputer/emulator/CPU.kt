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
        ADC, AND, ASL, BIT, BPL, BMI, BVC, BVS, BCC, BCS, BNE, BEQ, BRK, CMP,
        CPX, CPY, DEC, EOR, CLC, SEC, CLI, SEI, CLV, CLD, SED, INC, JMP, JSR,
        LDA, LDX, LDY, LSR, NOP, ORA, TAX, TXA, DEX, INX, TAY, TYA, DEY, INY,
        ROR, ROL, RTI, RTS, SBC, STA, TXS, TSX, PHA, PLA, PHP, PLP, STX, STY,
        XXX
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

    var lookup = MutableList<Instruction>(0x100) {
        Instruction("???", Opcode.XXX, AddressingMode.IMP, 2U)
    }

    init {
        lookup[0x69] = Instruction("ADC",Opcode.ADC,AddressingMode.IMM,2U);
        lookup[0x65] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0x75] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x6D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x7D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x79] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x61] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x71] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);

        lookup[0x29] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0x25] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0x35] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x2D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x3D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x39] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x21] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x31] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);

        lookup[0x0A] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0x06] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);
        lookup[0x16] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x0E] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x1E] = Instruction("???",Opcode.XXX,AddressingMode.IMP,7U);

        lookup[0x90] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xB0] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xF0] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x24] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0x2C] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);

        lookup[0x30] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xD0] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x10] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x00] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x50] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x70] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x18] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xD8] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x58] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xB8] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xC9] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0xC5] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0xD5] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xCD] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xDD] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xD9] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xC1] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0xD1] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);

        lookup[0xE0] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0xE4] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0xEC] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);

        lookup[0xC0] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0xC4] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0xCC] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);

        lookup[0xC6] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);
        lookup[0xD6] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0xCE] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0xDE] = Instruction("???",Opcode.XXX,AddressingMode.IMP,7U);

        lookup[0xCA] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x88] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x49] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0x45] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0x55] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x4D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x5D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x59] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x41] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x51] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);

        lookup[0xE6] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);
        lookup[0xF6] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0xEE] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0xFE] = Instruction("???",Opcode.XXX,AddressingMode.IMP,7U);

        lookup[0xE8] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xC8] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x4C] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0x6C] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);

        lookup[0x20] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);

        lookup[0xA9] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0xA5] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0xB5] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xAD] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xBD] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xB9] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xA1] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0xB1] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);

        lookup[0xA2] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0xA6] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0xB6] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xAE] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xBE] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);

        lookup[0xA0] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0xA4] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0xB4] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xAC] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xBC] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);

        lookup[0x4A] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0x46] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);
        lookup[0x56] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x4E] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x5E] = Instruction("???",Opcode.XXX,AddressingMode.IMP,7U);

        lookup[0xEA] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x09] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0x05] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0x15] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x0D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x1D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x19] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x01] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x11] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);

        lookup[0x48] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);

        lookup[0x08] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);

        lookup[0x68] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);

        lookup[0x28] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);

        lookup[0x2A] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0x26] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);
        lookup[0x36] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x2E] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x3E] = Instruction("???",Opcode.XXX,AddressingMode.IMP,7U);

        lookup[0x6A] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0x66] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);
        lookup[0x76] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x6E] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x7E] = Instruction("???",Opcode.XXX,AddressingMode.IMP,7U);

        lookup[0x40] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);

        lookup[0x60] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);

        lookup[0xE9] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
        lookup[0xE5] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0xF5] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xED] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xFD] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xF9] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0xE1] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0xF1] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);

        lookup[0x38] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xF8] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x78] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x85] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0x95] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x8D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x9D] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);
        lookup[0x99] = Instruction("???",Opcode.XXX,AddressingMode.IMP,5U);
        lookup[0x81] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);
        lookup[0x91] = Instruction("???",Opcode.XXX,AddressingMode.IMP,6U);

        lookup[0x86] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0x96] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x8E] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);

        lookup[0x84] = Instruction("???",Opcode.XXX,AddressingMode.IMP,3U);
        lookup[0x94] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);
        lookup[0x8C] = Instruction("???",Opcode.XXX,AddressingMode.IMP,4U);

        lookup[0xAA] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xA8] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0xBA] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x8A] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x9A] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);

        lookup[0x98] = Instruction("???",Opcode.XXX,AddressingMode.IMP,2U);
    }
}
