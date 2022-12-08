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

    var flagShiftC : UByte = (1 shl 0).toUByte()
    var flagShiftZ : UByte = (1 shl 1).toUByte()
    var flagShiftI : UByte = (1 shl 2).toUByte()
    var flagShiftD : UByte = (1 shl 3).toUByte()
    var flagShiftB : UByte = (1 shl 4).toUByte()
    var flagShiftU : UByte = (1 shl 5).toUByte()
    var flagShiftV : UByte = (1 shl 6).toUByte()
    var flagShiftN : UByte = (1 shl 7).toUByte()

    private fun setFlag(flagShift : UByte, flagVal : Boolean) {
        status = if (flagVal) {
            status or flagShift
        } else {
            status and flagShift
        }
    }

    private fun getFlag(flagShift : UByte) : UByte {
        return (if ((status and flagShift) > 0u) 1U else 0U)
    }

//    Functions / Other Pins

    private fun clock() {
         if (cycles == 0) {
             opcode = bus.read(PC)
             PC++

             cycles = lookup[opcode.toInt()].cycles
             val additional_cycle1 : UByte = handleAddressingMode(lookup[opcode.toInt()].mode)
             val additional_cycle2 : UByte = handleOpcodes(lookup[opcode.toInt()].opcode)
             cycles += (additional_cycle1 and additional_cycle2).toInt()
         }
        cycles--
    }

    private fun reset() {
        var A: UByte = 0x00U
        var X: UByte = 0x00U
        var Y: UByte = 0x00U
        var SP: UByte = 0xFDU
        var status: UByte = ((0x00U).toUByte() or flagShiftU)

        absoluteAddress = 0xFFFCU
        val lo : UInt = bus.read((absoluteAddress + 0U).toUShort()).toUInt()
        val hi : UInt = bus.read((absoluteAddress + 1U).toUShort()).toUInt()

        PC = ((hi shl 8) or lo).toUShort()

        relativeAddress = 0x0000U
        absoluteAddress = 0x0000U
        fetched = 0x00U

        cycles = 8
    }

    private fun irq() {     // Interrupt Request
        if (getFlag(flagShiftI) == (0U).toUByte()) {
            bus.write((0x0100U + SP).toUShort(), (PC.toUInt() shr 8).toUByte() and 0x00FFU)
            SP--
            bus.write((0x0100U + SP).toUShort(), PC.toUByte() and 0x00FFU)
            SP--

            setFlag(flagShiftB, false)
            setFlag(flagShiftU, true)
            setFlag(flagShiftI, true)
            bus.write((0x0100U + SP).toUShort(), status)
            SP--

            absoluteAddress = 0xFFFEU
            val lo : UInt = bus.read((absoluteAddress + 0U).toUShort()).toUInt()
            val hi : UInt = bus.read((absoluteAddress + 1U).toUShort()).toUInt()
            PC = ((hi shl 8) or lo).toUShort()

            cycles = 7
        }
    }
    private fun nmi() {     // Non-Maskable Interrupt
        bus.write((0x0100U + SP).toUShort(), (PC.toUInt() shr 8).toUByte() and 0x00FFU)
        SP--
        bus.write((0x0100U + SP).toUShort(), PC.toUByte() and 0x00FFU)
        SP--

        setFlag(flagShiftB, false)
        setFlag(flagShiftU, true)
        setFlag(flagShiftI, true)
        bus.write((0x0100U + SP).toUShort(), status)
        SP--

        absoluteAddress = 0xFFFAU
        val lo : UInt = bus.read((absoluteAddress + 0U).toUShort()).toUInt()
        val hi : UInt = bus.read((absoluteAddress + 1U).toUShort()).toUInt()
        PC = ((hi shl 8) or lo).toUShort()

        cycles = 8
    }

    private fun fetch () : UByte {
        if((lookup[opcode.toInt()].mode) != AddressingMode.IMP)
            fetched = bus.read(absoluteAddress)
        return fetched
    }

    var fetched : UByte = 0x00U

    var absoluteAddress : UShort = 0x0000U
    var relativeAddress : UShort = 0x0000U
    var opcode : UByte = 0x00U
    var cycles : Int = 0
    var temp : UShort = 0U

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
        absoluteAddress = PC++

        return 0U
    }
    private fun IMP() : UByte {
        fetched = A

        return 0U
    }
    private fun ZP0() : UByte {
        absoluteAddress = bus.read(PC).toUShort()
        PC++
        absoluteAddress = absoluteAddress and 0x00FFU

        return 0U
    }
    private fun ZPX() : UByte {
        absoluteAddress = (bus.read(PC) + X).toUShort()
        PC++
        absoluteAddress = absoluteAddress and 0x00FFU

        return 0U
    }
    private fun ZPY() : UByte {
        absoluteAddress = (bus.read(PC) + Y).toUShort()
        PC++
        absoluteAddress = absoluteAddress and 0x00FFU

        return 0U
    }
    private fun REL() : UByte {
        relativeAddress = bus.read(PC).toUShort()
        PC++
        if ((relativeAddress and 0x80U) > 0U) {
            relativeAddress = relativeAddress or 0xFF00U
        }

        return 0U
    }
    private fun ABS() : UByte {
        val lo : UInt = bus.read(PC).toUInt()
        PC++
        val hi : UInt = bus.read(PC).toUInt()
        PC++

        absoluteAddress = ((hi shl 8) or lo).toUShort()

        return 0U
    }
    private fun ABX() : UByte {
        val lo : UInt = bus.read(PC).toUInt()
        PC++
        val hi : UInt = bus.read(PC).toUInt()
        PC++

        absoluteAddress = ((hi shl 8) or lo).toUShort()
        absoluteAddress = (absoluteAddress + X).toUShort()

        return if ((absoluteAddress and 0xFF00U) != (hi shl 8).toUShort()) {
            1U
        } else {
            0u
        }
    }
    private fun ABY() : UByte {
        val lo : UInt = bus.read(PC).toUInt()
        PC++
        val hi : UInt = bus.read(PC).toUInt()
        PC++

        absoluteAddress = ((hi shl 8) or lo).toUShort()
        absoluteAddress = (absoluteAddress + Y).toUShort()

        return if ((absoluteAddress and 0xFF00U) != (hi shl 8).toUShort()) {
            1U
        } else {
            0u
        }
        return 0U
    }
    private fun IND() : UByte {
        val ptr_lo : UInt = bus.read(PC).toUInt()
        PC++
        val ptr_hi : UInt = bus.read(PC).toUInt()
        PC++

        val ptr : UInt = (ptr_hi shl 8) or ptr_lo

        absoluteAddress = (bus.read((ptr + 1U).toUShort()).toUInt() shl 8).toUShort() or bus.read((ptr + 0U).toUShort()).toUShort()

        return 0U
    }
    private fun IZX() : UByte {
        val t: UShort = bus.read(PC).toUShort()
        PC++

        val lo: UInt = bus.read((t+X).toUShort() and 0x00FFU).toUInt()
        val hi: UInt = bus.read((t+X+1U).toUShort() and 0x00FFU).toUInt()

        absoluteAddress = ((hi shl 8) or lo).toUShort()

        return 0U
    }
    private fun IZY() : UByte {
        val t: UShort = bus.read(PC).toUShort()
        PC++

        val lo: UInt = bus.read(t and 0x00FFU).toUInt()
        val hi: UInt = bus.read((t+1U).toUShort() and 0x00FFU).toUInt()

        absoluteAddress = ((hi shl 8) or lo).toUShort()
        absoluteAddress = (absoluteAddress + Y).toUShort()

        return if ((absoluteAddress and 0xFF00U) != (hi shl 8).toUShort()) {
            return 1U;
        } else {
            return 0U
        }
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
        fetch()
        temp = (A.toUShort() + fetched.toUShort() + getFlag(flagShiftC).toUShort()).toUShort()
        setFlag(flagShiftC, temp > 255U)
        setFlag(flagShiftZ, (temp and 0x00FFU) == (0U).toUShort())
        setFlag(flagShiftN, (temp and 0x80U) > 0U)
        setFlag(flagShiftZ, (((A.toUShort() xor fetched.toUShort()).inv() and (A.toUShort() xor temp)) and 0x0080U) > 0U)
        A = (temp and 0x00FFU).toUByte()
        return 1U
    }
    private fun AND() : UByte {
        fetch()
        A = A and fetched
        setFlag(flagShiftZ, A == (0x00U).toUByte())
        setFlag(flagShiftN, (A and 0x80U) > 0U)
        return 1U
    }
    private fun ASL() : UByte {
        return 0U
    }
    private fun BIT() : UByte {
        return 0U
    }
    private fun BPL() : UByte {
        if (getFlag(flagShiftN) == (0U).toUByte()) {
            cycles++
            absoluteAddress = (PC + relativeAddress).toUShort()

            if ((absoluteAddress and 0xFF00U) != (PC and 0xFF00U))
                cycles++

            PC = absoluteAddress
        }
        return 0U
    }
    private fun BMI() : UByte {
        if (getFlag(flagShiftN) == (1U).toUByte()) {
            cycles++
            absoluteAddress = (PC + relativeAddress).toUShort()

            if ((absoluteAddress and 0xFF00U) != (PC and 0xFF00U))
                cycles++

            PC = absoluteAddress
        }
        return 0U
    }
    private fun BVC() : UByte {
        if (getFlag(flagShiftV) == (0U).toUByte()) {
            cycles++
            absoluteAddress = (PC + relativeAddress).toUShort()

            if ((absoluteAddress and 0xFF00U) != (PC and 0xFF00U))
                cycles++

            PC = absoluteAddress
        }
        return 0U
    }
    private fun BVS() : UByte {
        if (getFlag(flagShiftV) == (1U).toUByte()) {
            cycles++
            absoluteAddress = (PC + relativeAddress).toUShort()

            if ((absoluteAddress and 0xFF00U) != (PC and 0xFF00U))
                cycles++

            PC = absoluteAddress
        }
        return 0U
    }
    private fun BCC() : UByte {
        if (getFlag(flagShiftC) == (0U).toUByte()) {
            cycles++
            absoluteAddress = (PC + relativeAddress).toUShort()

            if ((absoluteAddress and 0xFF00U) != (PC and 0xFF00U))
                cycles++

            PC = absoluteAddress
        }
        return 0U
    }
    private fun BCS() : UByte {
        if (getFlag(flagShiftC) == (1U).toUByte()) {
            cycles++
            absoluteAddress = (PC + relativeAddress).toUShort()

            if ((absoluteAddress and 0xFF00U) != (PC and 0xFF00U))
                cycles++

            PC = absoluteAddress
        }
        return 0U
    }
    private fun BNE() : UByte {
        if (getFlag(flagShiftZ) == (0U).toUByte()) {
            cycles++
            absoluteAddress = (PC + relativeAddress).toUShort()

            if ((absoluteAddress and 0xFF00U) != (PC and 0xFF00U))
                cycles++

            PC = absoluteAddress
        }
        return 0U
    }
    private fun BEQ() : UByte {
        if (getFlag(flagShiftZ) == (1U).toUByte()) {
            cycles++
            absoluteAddress = (PC + relativeAddress).toUShort()

            if ((absoluteAddress and 0xFF00U) != (PC and 0xFF00U))
                cycles++

            PC = absoluteAddress
        }
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
        setFlag(flagShiftC, false)
        return 0U
    }
    private fun SEC() : UByte {
        return 0U
    }
    private fun CLI() : UByte {
        setFlag(flagShiftI, false)
        return 0U
    }
    private fun SEI() : UByte {
        return 0U
    }
    private fun CLV() : UByte {
        setFlag(flagShiftV, false)
        return 0U
    }
    private fun CLD() : UByte {
        setFlag(flagShiftD, false)
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
        SP++
        status = bus.read((0x0100U + SP).toUShort())
        status = status and flagShiftB.inv()
        status = status and flagShiftU.inv()

        SP++
        PC = bus.read((0x0100U + SP).toUShort()).toUShort()
        SP++
        PC = PC or ((bus.read((0x0100U + SP).toUShort()).toUShort()).toUInt() shl 8).toUShort()
        return 0U
    }
    private fun RTS() : UByte {
        return 0U
    }
    private fun SBC() : UByte {
        fetch()
        val value : UShort = fetched.toUShort() xor 0x00FFU

        temp = (A.toUShort() + value + getFlag(flagShiftC).toUShort()).toUShort()
        setFlag(flagShiftC, (temp and 0xFF00U) > 0U)
        setFlag(flagShiftZ, (temp and 0x00FFU) == (0U).toUShort())
        setFlag(flagShiftV, ((temp xor A.toUShort()) and (temp xor value) and 0x0080U) > 0U)
        setFlag(flagShiftN, (temp and 0x80U) > 0U)
        A = (temp and 0x00FFU).toUByte()
        return 1U
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
        bus.write((0x0100U + SP).toUShort(), A)
        SP--
        return 0U
    }
    private fun PLA() : UByte {
        SP++
        A = bus.read((0x0100U + SP).toUShort())
        setFlag(flagShiftZ, A == (0x00U).toUByte())
        setFlag(flagShiftN, (A and 0x80U) > 0U)
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
        val cycles: Int,
    )

    var lookup = MutableList<Instruction>(0x100) {
        Instruction("???", Opcode.XXX, AddressingMode.IMP, 2)
    }

    init {
//        ADC
        lookup[0x69] = Instruction("ADC",Opcode.ADC,AddressingMode.IMM,2);
        lookup[0x65] = Instruction("ADC",Opcode.ADC,AddressingMode.ZP0,3);
        lookup[0x75] = Instruction("ADC",Opcode.ADC,AddressingMode.ZPX,4);
        lookup[0x6D] = Instruction("ADC",Opcode.ADC,AddressingMode.ABS,4);
        lookup[0x7D] = Instruction("ADC",Opcode.ADC,AddressingMode.ABX,4);
        lookup[0x79] = Instruction("ADC",Opcode.ADC,AddressingMode.ABY,4);
        lookup[0x61] = Instruction("ADC",Opcode.ADC,AddressingMode.IZX,6);
        lookup[0x71] = Instruction("ADC",Opcode.ADC,AddressingMode.IZY,5);

//        AND
        lookup[0x29] = Instruction("AND",Opcode.AND,AddressingMode.IMM,2);
        lookup[0x25] = Instruction("AND",Opcode.AND,AddressingMode.ZP0,3);
        lookup[0x35] = Instruction("AND",Opcode.AND,AddressingMode.ZPX,4);
        lookup[0x2D] = Instruction("AND",Opcode.AND,AddressingMode.ABS,4);
        lookup[0x3D] = Instruction("AND",Opcode.AND,AddressingMode.ABX,4);
        lookup[0x39] = Instruction("AND",Opcode.AND,AddressingMode.ABY,4);
        lookup[0x21] = Instruction("AND",Opcode.AND,AddressingMode.IZX,6);
        lookup[0x31] = Instruction("AND",Opcode.AND,AddressingMode.IZY,5);

//        ASL
        lookup[0x0A] = Instruction("ASL",Opcode.ASL,AddressingMode.IMP,2);
        lookup[0x06] = Instruction("ASL",Opcode.ASL,AddressingMode.ZP0,5);
        lookup[0x16] = Instruction("ASL",Opcode.ASL,AddressingMode.ZPX,6);
        lookup[0x0E] = Instruction("ASL",Opcode.ASL,AddressingMode.ABS,6);
        lookup[0x1E] = Instruction("ASL",Opcode.ASL,AddressingMode.ABX,7);

//        BCC
        lookup[0x90] = Instruction("BCC",Opcode.BCC,AddressingMode.REL,2);

//        BCS
        lookup[0xB0] = Instruction("BCS",Opcode.BCS,AddressingMode.REL,2);

//        BEQ
        lookup[0xF0] = Instruction("BEQ",Opcode.BEQ,AddressingMode.REL,2);

//        BIT
        lookup[0x24] = Instruction("BIT",Opcode.BIT,AddressingMode.ZP0,3);
        lookup[0x2C] = Instruction("BIT",Opcode.BIT,AddressingMode.ABS,4);

//        BMI
        lookup[0x30] = Instruction("BMI",Opcode.BMI,AddressingMode.REL,2);

//        BNE
        lookup[0xD0] = Instruction("BNE",Opcode.BNE,AddressingMode.REL,2);

//        BPL
        lookup[0x10] = Instruction("BPL",Opcode.BPL,AddressingMode.REL,2);

//        BRK
        lookup[0x00] = Instruction("BRK",Opcode.BRK,AddressingMode.IMP,7);

//        BVC
        lookup[0x50] = Instruction("BVC",Opcode.BVC,AddressingMode.REL,2);

//        BVS
        lookup[0x70] = Instruction("BVS",Opcode.BVS,AddressingMode.REL,2);

//        CLC
        lookup[0x18] = Instruction("CLC",Opcode.CLC,AddressingMode.IMP,2);

//        CLD
        lookup[0xD8] = Instruction("CLD",Opcode.CLC,AddressingMode.IMP,2);

//        CLI
        lookup[0x58] = Instruction("CLI",Opcode.CLI,AddressingMode.IMP,2);

//        CLV
        lookup[0xB8] = Instruction("CLV",Opcode.CLV,AddressingMode.IMP,2);

//        CMP
        lookup[0xC9] = Instruction("CMP",Opcode.CMP,AddressingMode.IMM,2);
        lookup[0xC5] = Instruction("CMP",Opcode.CMP,AddressingMode.ZP0,3);
        lookup[0xD5] = Instruction("CMP",Opcode.CMP,AddressingMode.ZPX,4);
        lookup[0xCD] = Instruction("CMP",Opcode.CMP,AddressingMode.ABS,4);
        lookup[0xDD] = Instruction("CMP",Opcode.CMP,AddressingMode.ABX,4);
        lookup[0xD9] = Instruction("CMP",Opcode.CMP,AddressingMode.ABY,4);
        lookup[0xC1] = Instruction("CMP",Opcode.CMP,AddressingMode.IZX,6);
        lookup[0xD1] = Instruction("CMP",Opcode.CMP,AddressingMode.IZY,5);

//        CPX
        lookup[0xE0] = Instruction("CPX",Opcode.CPX,AddressingMode.IMM,2);
        lookup[0xE4] = Instruction("CPX",Opcode.CPX,AddressingMode.ZP0,3);
        lookup[0xEC] = Instruction("CPX",Opcode.CPX,AddressingMode.ABS,4);

//        CPY
        lookup[0xC0] = Instruction("CPY",Opcode.CPY,AddressingMode.IMM,2);
        lookup[0xC4] = Instruction("CPY",Opcode.CPY,AddressingMode.ZP0,3);
        lookup[0xCC] = Instruction("CPY",Opcode.CPY,AddressingMode.ABS,4);

//        DEC
        lookup[0xC6] = Instruction("DEC",Opcode.DEC,AddressingMode.ZP0,5);
        lookup[0xD6] = Instruction("DEC",Opcode.DEC,AddressingMode.ZPX,6);
        lookup[0xCE] = Instruction("DEC",Opcode.DEC,AddressingMode.ABS,6);
        lookup[0xDE] = Instruction("DEC",Opcode.DEC,AddressingMode.ABX,7);

//        DEX
        lookup[0xCA] = Instruction("DEX",Opcode.DEX,AddressingMode.IMP,2);

//        DEY
        lookup[0x88] = Instruction("DEY",Opcode.DEY,AddressingMode.IMP,2);

//        EOR
        lookup[0x49] = Instruction("EOR",Opcode.EOR,AddressingMode.IMM,2);
        lookup[0x45] = Instruction("EOR",Opcode.EOR,AddressingMode.ZP0,3);
        lookup[0x55] = Instruction("EOR",Opcode.EOR,AddressingMode.ZPX,4);
        lookup[0x4D] = Instruction("EOR",Opcode.EOR,AddressingMode.ABS,4);
        lookup[0x5D] = Instruction("EOR",Opcode.EOR,AddressingMode.ABX,4);
        lookup[0x59] = Instruction("EOR",Opcode.EOR,AddressingMode.ABY,4);
        lookup[0x41] = Instruction("EOR",Opcode.EOR,AddressingMode.IZX,6);
        lookup[0x51] = Instruction("EOR",Opcode.EOR,AddressingMode.IZY,5);

//        INC
        lookup[0xE6] = Instruction("INC",Opcode.INC,AddressingMode.ZP0,5);
        lookup[0xF6] = Instruction("INC",Opcode.INC,AddressingMode.ZPX,6);
        lookup[0xEE] = Instruction("INC",Opcode.INC,AddressingMode.ABS,6);
        lookup[0xFE] = Instruction("INC",Opcode.INC,AddressingMode.ABX,7);

//        INX
        lookup[0xE8] = Instruction("INX",Opcode.INX,AddressingMode.IMP,2);

//        INY
        lookup[0xC8] = Instruction("INY",Opcode.INY,AddressingMode.IMP,2);

//        JMP
        lookup[0x4C] = Instruction("JMP",Opcode.JMP,AddressingMode.ABS,3);
        lookup[0x6C] = Instruction("JMP",Opcode.JMP,AddressingMode.IND,5);

//        JSR
        lookup[0x20] = Instruction("JSR",Opcode.JSR,AddressingMode.ABS,6);

//        LDA
        lookup[0xA9] = Instruction("LDA",Opcode.LDA,AddressingMode.IMM,2);
        lookup[0xA5] = Instruction("LDA",Opcode.LDA,AddressingMode.ZP0,3);
        lookup[0xB5] = Instruction("LDA",Opcode.LDA,AddressingMode.ZPX,4);
        lookup[0xAD] = Instruction("LDA",Opcode.LDA,AddressingMode.ABS,4);
        lookup[0xBD] = Instruction("LDA",Opcode.LDA,AddressingMode.ABX,4);
        lookup[0xB9] = Instruction("LDA",Opcode.LDA,AddressingMode.ABY,4);
        lookup[0xA1] = Instruction("LDA",Opcode.LDA,AddressingMode.IZX,6);
        lookup[0xB1] = Instruction("LDA",Opcode.LDA,AddressingMode.IZY,5);

//        LDX
        lookup[0xA2] = Instruction("LDX",Opcode.LDX,AddressingMode.IMM,2);
        lookup[0xA6] = Instruction("LDX",Opcode.LDX,AddressingMode.ZP0,3);
        lookup[0xB6] = Instruction("LDX",Opcode.LDX,AddressingMode.ZPY,4);
        lookup[0xAE] = Instruction("LDX",Opcode.LDX,AddressingMode.ABS,4);
        lookup[0xBE] = Instruction("LDX",Opcode.LDX,AddressingMode.ABY,4);

//        LDY
        lookup[0xA0] = Instruction("LDY",Opcode.LDY,AddressingMode.IMM,2);
        lookup[0xA4] = Instruction("LDY",Opcode.LDY,AddressingMode.ZP0,3);
        lookup[0xB4] = Instruction("LDY",Opcode.LDY,AddressingMode.ZPX,4);
        lookup[0xAC] = Instruction("LDY",Opcode.LDY,AddressingMode.ABS,4);
        lookup[0xBC] = Instruction("LDY",Opcode.LDY,AddressingMode.ABX,4);

//        LSR
        lookup[0x4A] = Instruction("LSR",Opcode.LSR,AddressingMode.IMP,2);
        lookup[0x46] = Instruction("LSR",Opcode.LSR,AddressingMode.ZP0,5);
        lookup[0x56] = Instruction("LSR",Opcode.LSR,AddressingMode.ZPX,6);
        lookup[0x4E] = Instruction("LSR",Opcode.LSR,AddressingMode.ABS,6);
        lookup[0x5E] = Instruction("LSR",Opcode.LSR,AddressingMode.ABX,7);

//        NOP
        lookup[0xEA] = Instruction("???",Opcode.NOP,AddressingMode.IMP,2);

//        ORA
        lookup[0x09] = Instruction("ORA",Opcode.ORA,AddressingMode.IMM,2);
        lookup[0x05] = Instruction("ORA",Opcode.ORA,AddressingMode.ZP0,3);
        lookup[0x15] = Instruction("ORA",Opcode.ORA,AddressingMode.ZPX,4);
        lookup[0x0D] = Instruction("ORA",Opcode.ORA,AddressingMode.ABS,4);
        lookup[0x1D] = Instruction("ORA",Opcode.ORA,AddressingMode.ABX,4);
        lookup[0x19] = Instruction("ORA",Opcode.ORA,AddressingMode.ABY,4);
        lookup[0x01] = Instruction("ORA",Opcode.ORA,AddressingMode.IZX,6);
        lookup[0x11] = Instruction("ORA",Opcode.ORA,AddressingMode.IZY,5);

//        PHA
        lookup[0x48] = Instruction("PHA",Opcode.PHA,AddressingMode.IMP,3);

//        PHP
        lookup[0x08] = Instruction("PHP",Opcode.PHP,AddressingMode.IMP,3);

//        PLA
        lookup[0x68] = Instruction("PLA",Opcode.PLA,AddressingMode.IMP,4);

//        PLP
        lookup[0x28] = Instruction("PLP",Opcode.PLP,AddressingMode.IMP,4);

//        ROL
        lookup[0x2A] = Instruction("ROL",Opcode.ROL,AddressingMode.IMP,2);
        lookup[0x26] = Instruction("ROL",Opcode.ROL,AddressingMode.ZP0,5);
        lookup[0x36] = Instruction("ROL",Opcode.ROL,AddressingMode.ZPX,6);
        lookup[0x2E] = Instruction("ROL",Opcode.ROL,AddressingMode.ABS,6);
        lookup[0x3E] = Instruction("ROL",Opcode.ROL,AddressingMode.ABX,7);

//        ROR
        lookup[0x6A] = Instruction("ROR",Opcode.ROR,AddressingMode.IMP,2);
        lookup[0x66] = Instruction("ROR",Opcode.ROR,AddressingMode.ZP0,5);
        lookup[0x76] = Instruction("ROR",Opcode.ROR,AddressingMode.ZPX,6);
        lookup[0x6E] = Instruction("ROR",Opcode.ROR,AddressingMode.ABS,6);
        lookup[0x7E] = Instruction("ROR",Opcode.ROR,AddressingMode.ABX,7);

//        RTI
        lookup[0x40] = Instruction("RTI",Opcode.RTI,AddressingMode.IMP,6);

//        RTS
        lookup[0x60] = Instruction("RTS",Opcode.RTS,AddressingMode.IMP,6);

//        SBC
        lookup[0xE9] = Instruction("SBC",Opcode.SBC,AddressingMode.IMM,2);
        lookup[0xE5] = Instruction("SBC",Opcode.SBC,AddressingMode.ZP0,3);
        lookup[0xF5] = Instruction("SBC",Opcode.SBC,AddressingMode.ZPX,4);
        lookup[0xED] = Instruction("SBC",Opcode.SBC,AddressingMode.ABS,4);
        lookup[0xFD] = Instruction("SBC",Opcode.SBC,AddressingMode.ABX,4);
        lookup[0xF9] = Instruction("SBC",Opcode.SBC,AddressingMode.ABY,4);
        lookup[0xE1] = Instruction("SBC",Opcode.SBC,AddressingMode.IZX,6);
        lookup[0xF1] = Instruction("SBC",Opcode.SBC,AddressingMode.IZY,5);

//        SEC
        lookup[0x38] = Instruction("SEC",Opcode.SEC,AddressingMode.IMP,2);

//        SED
        lookup[0xF8] = Instruction("SED",Opcode.SED,AddressingMode.IMP,2);

//        SEI
        lookup[0x78] = Instruction("SEI",Opcode.SEI,AddressingMode.IMP,2);

//        STA
        lookup[0x85] = Instruction("STA",Opcode.STA,AddressingMode.ZP0,3);
        lookup[0x95] = Instruction("STA",Opcode.STA,AddressingMode.ZPX,4);
        lookup[0x8D] = Instruction("STA",Opcode.STA,AddressingMode.ABS,4);
        lookup[0x9D] = Instruction("STA",Opcode.STA,AddressingMode.ABX,5);
        lookup[0x99] = Instruction("STA",Opcode.STA,AddressingMode.ABY,5);
        lookup[0x81] = Instruction("STA",Opcode.STA,AddressingMode.IZX,6);
        lookup[0x91] = Instruction("STA",Opcode.STA,AddressingMode.IZY,6);

//        STX
        lookup[0x86] = Instruction("STX",Opcode.STX,AddressingMode.ZP0,3);
        lookup[0x96] = Instruction("STX",Opcode.STX,AddressingMode.ZPY,4);
        lookup[0x8E] = Instruction("STX",Opcode.STX,AddressingMode.ABS,4);

//        STY
        lookup[0x84] = Instruction("STY",Opcode.STY,AddressingMode.ZP0,3);
        lookup[0x94] = Instruction("STY",Opcode.STY,AddressingMode.ZPX,4);
        lookup[0x8C] = Instruction("STY",Opcode.STY,AddressingMode.ABS,4);

//        TAX
        lookup[0xAA] = Instruction("TAX",Opcode.TAX,AddressingMode.IMP,2);

//        TAY
        lookup[0xA8] = Instruction("TAY",Opcode.TAY,AddressingMode.IMP,2);

//        TSX
        lookup[0xBA] = Instruction("TSX",Opcode.TSX,AddressingMode.IMP,2);

//        TXA
        lookup[0x8A] = Instruction("TXA",Opcode.TXA,AddressingMode.IMP,2);

//        TXS
        lookup[0x9A] = Instruction("TXS",Opcode.TXS,AddressingMode.IMP,2);

//        TYA
        lookup[0x98] = Instruction("TYA",Opcode.TYA,AddressingMode.IMP,2);
    }
}
