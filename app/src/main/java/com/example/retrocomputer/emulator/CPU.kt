package com.example.retrocomputer.emulator


class CPU() {
    private val bus = Bus()


    //    Registers
    var A: Int = 0x00      // Accumulator
    var X: Int = 0x00      // Register X
    var Y: Int = 0x00      // Register Y
    var SP: Int = 0x00  // Stack pointer
    var PC: Int = 0x0000 // Program counter
    var status: Int = 0x00 // Status register (current flag)

    //    Flags
    var flagC : Boolean = false  // Carry Bit
    var flagZ : Boolean = false  // Zero
    var flagI : Boolean = false  // Disable Interrupts
    var flagD : Boolean = false  // DecimalMode
    var flagB : Boolean = false  // Break
    var flagU : Boolean = true   // Unused
    var flagV : Boolean = false  // Overflow
    var flagN : Boolean = false  // Negative

    var flagShiftC : Int = (1 shl 0)
    var flagShiftZ : Int = (1 shl 1)
    var flagShiftI : Int = (1 shl 2)
    var flagShiftD : Int = (1 shl 3)
    var flagShiftB : Int = (1 shl 4)
    var flagShiftU : Int = (1 shl 5)
    var flagShiftV : Int = (1 shl 6)
    var flagShiftN : Int = (1 shl 7)

    private fun setFlag(flagShift : Int, flagVal : Boolean) {
        status = if (flagVal) {
            status or flagShift
        } else {
            status and flagShift.inv()
        }
    }

    private fun getFlag(flagShift : Int) : Int {
        return (if ((status and flagShift) > 0) 1 else 0)
    }

//    Functions / Other Pins

    var fetched : Int = 0x00

    var absoluteAddress : Int = 0x0000
    var relativeAddress : Int = 0x0000
    var opcode : Int = 0x00
    var cycles : Int = 0
    var temp : Int = 0

    private fun clock() {
        if (cycles == 0) {
            opcode = bus.read(PC)
            setFlag(flagShiftU, true)
            PC++

            cycles = lookup[opcode].cycles
            val additional_cycle1 : Int = handleAddressingMode(lookup[opcode].mode)
            val additional_cycle2 : Int = handleOpcodes(lookup[opcode].opcode)
            cycles += (additional_cycle1 and additional_cycle2)
            setFlag(flagShiftU, true)
        }
        cycles--
    }

    private fun reset() {
        absoluteAddress = 0xFFFC
        val lo : Int = bus.read((absoluteAddress + 0))
        val hi : Int = bus.read((absoluteAddress + 1))

        PC = ((hi shl 8) or lo)

        A = 0x00
        X = 0x00
        Y = 0x00
        SP = 0xFD
        status = ((0x00) or flagShiftU)

        relativeAddress = 0x0000
        absoluteAddress = 0x0000
        fetched = 0x00

        cycles = 8
    }

    private fun irq() {     // Interrupt Request
        if (getFlag(flagShiftI) == (0)) {
            bus.write((0x0100 + SP), (PC shr 8) and 0x00FF)
            SP--
            bus.write((0x0100 + SP), PC and 0x00FF)
            SP--

            setFlag(flagShiftB, false)
            setFlag(flagShiftU, true)
            setFlag(flagShiftI, true)
            bus.write((0x0100 + SP), status)
            SP--

            absoluteAddress = 0xFFFE
            val lo : Int = bus.read((absoluteAddress + 0))
            val hi : Int = bus.read((absoluteAddress + 1))
            PC = ((hi shl 8) or lo)

            cycles = 7
        }
    }
    private fun nmi() {     // Non-Maskable Interrupt
        bus.write((0x0100 + SP), (PC shr 8) and 0x00FF)
        SP--
        bus.write((0x0100 + SP), PC and 0x00FF)
        SP--

        setFlag(flagShiftB, false)
        setFlag(flagShiftU, true)
        setFlag(flagShiftI, true)
        bus.write((0x0100 + SP), status)
        SP--

        absoluteAddress = 0xFFFA
        val lo : Int = bus.read((absoluteAddress + 0))
        val hi : Int = bus.read((absoluteAddress + 1))
        PC = ((hi shl 8) or lo)

        cycles = 8
    }

    private fun fetch () : Int {
        if((lookup[opcode].mode) != AddressingMode.IMP)
            fetched = bus.read(absoluteAddress)
        return fetched
    }

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

    private fun handleAddressingMode(addrMode: AddressingMode): Int {
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

    private fun IMM() : Int {
        absoluteAddress = PC++
        return 0
    }
    private fun IMP() : Int {
        fetched = A
        return 0
    }
    private fun ZP0() : Int {
        absoluteAddress = bus.read(PC)
        PC++
        absoluteAddress = absoluteAddress and 0x00FF
        return 0
    }
    private fun ZPX() : Int {
        absoluteAddress = (bus.read(PC) + X)
        PC++
        absoluteAddress = absoluteAddress and 0x00FF
        return 0
    }
    private fun ZPY() : Int {
        absoluteAddress = (bus.read(PC) + Y)
        PC++
        absoluteAddress = absoluteAddress and 0x00FF
        return 0
    }
    private fun REL() : Int {
        relativeAddress = bus.read(PC)
        PC++
        if ((relativeAddress and 0x80) > 0) {
            relativeAddress = relativeAddress or 0xFF00
        }
        return 0
    }
    private fun ABS() : Int {
        val lo : Int = bus.read(PC)
        PC++
        val hi : Int = bus.read(PC)
        PC++

        absoluteAddress = ((hi shl 8) or lo)

        return 0
    }
    private fun ABX() : Int {
        val lo : Int = bus.read(PC)
        PC++
        val hi : Int = bus.read(PC)
        PC++

        absoluteAddress = ((hi shl 8) or lo)
        absoluteAddress = (absoluteAddress + X)

        return if ((absoluteAddress and 0xFF00) != (hi shl 8)) {
            1
        } else {
            0
        }
    }
    private fun ABY() : Int {
        val lo : Int = bus.read(PC)
        PC++
        val hi : Int = bus.read(PC)
        PC++

        absoluteAddress = ((hi shl 8) or lo)
        absoluteAddress = (absoluteAddress + Y)

        return if ((absoluteAddress and 0xFF00) != (hi shl 8)) {
            1
        } else {
            0
        }
        return 0
    }
    private fun IND() : Int {
        val ptr_lo : Int = bus.read(PC)
        PC++
        val ptr_hi : Int = bus.read(PC)
        PC++

        val ptr : Int = (ptr_hi shl 8) or ptr_lo

        if (ptr_lo == 0x00FF) {
            absoluteAddress = (bus.read((ptr and 0xFF00)) shl 8) or bus.read((ptr + 0))
        } else {
            absoluteAddress = (bus.read((ptr + 1)) shl 8) or bus.read((ptr + 0))
        }

        return 0
    }
    private fun IZX() : Int {
        val t: Int = bus.read(PC)
        PC++

        val lo: Int = bus.read((t+X) and 0x00FF)
        val hi: Int = bus.read((t+X+1) and 0x00FF)

        absoluteAddress = ((hi shl 8) or lo)

        return 0
    }
    private fun IZY() : Int {
        val t: Int = bus.read(PC)
        PC++

        val lo: Int = bus.read(t and 0x00FF)
        val hi: Int = bus.read((t+1) and 0x00FF)

        absoluteAddress = ((hi shl 8) or lo)
        absoluteAddress = (absoluteAddress + Y)

        return if ((absoluteAddress and 0xFF00) != (hi shl 8)) {
            return 1;
        } else {
            return 0
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

    private fun handleOpcodes(opcode: Opcode): Int {
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

    private fun XXX() : Int {
        return 0
    }
    private fun ADC() : Int {
        fetch()
        temp = (A + fetched + getFlag(flagShiftC))
        setFlag(flagShiftC, temp > 255)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0))
        setFlag(flagShiftV, (((A xor fetched).inv() and (A xor temp)) and 0x0080) > 0)
        setFlag(flagShiftN, (temp and 0x80) > 0)
        A = (temp and 0x00FF)
        return 1
    }
    private fun AND() : Int {
        fetch()
        A = A and fetched
        setFlag(flagShiftZ, A == (0x00))
        setFlag(flagShiftN, (A and 0x80) > 0)
        return 1
    }
    private fun ASL() : Int {
        fetch()
        temp = (fetched shl 1)
        setFlag(flagShiftC, (temp and 0xFF00) > 0)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x00))
        setFlag(flagShiftN, (temp and 0x80) > 0)
        if (lookup[opcode].mode == AddressingMode.IMP)
            A = (temp and 0x00FF)
        else
            bus.write(absoluteAddress, (temp and 0x00FF))
        return 0
    }
    private fun BIT() : Int {
        fetch()
        temp = (A and fetched)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x00))
        setFlag(flagShiftN, (fetched and (1 shl 7)) > 0)
        setFlag(flagShiftV, (fetched and (1 shl 6)) > 0)
        return 0
    }
    private fun BPL() : Int {
        if (getFlag(flagShiftN) == (0)) {
            cycles++
            absoluteAddress = (PC + relativeAddress)

            if ((absoluteAddress and 0xFF00) != (PC and 0xFF00))
                cycles++

            PC = absoluteAddress
        }
        return 0
    }
    private fun BMI() : Int {
        if (getFlag(flagShiftN) == (1)) {
            cycles++
            absoluteAddress = (PC + relativeAddress)

            if ((absoluteAddress and 0xFF00) != (PC and 0xFF00))
                cycles++

            PC = absoluteAddress
        }
        return 0
    }
    private fun BVC() : Int {
        if (getFlag(flagShiftV) == (0)) {
            cycles++
            absoluteAddress = (PC + relativeAddress)

            if ((absoluteAddress and 0xFF00) != (PC and 0xFF00))
                cycles++

            PC = absoluteAddress
        }
        return 0
    }
    private fun BVS() : Int {
        if (getFlag(flagShiftV) == (1)) {
            cycles++
            absoluteAddress = (PC + relativeAddress)

            if ((absoluteAddress and 0xFF00) != (PC and 0xFF00))
                cycles++

            PC = absoluteAddress
        }
        return 0
    }
    private fun BCC() : Int {
        if (getFlag(flagShiftC) == (0)) {
            cycles++
            absoluteAddress = (PC + relativeAddress)

            if ((absoluteAddress and 0xFF00) != (PC and 0xFF00))
                cycles++

            PC = absoluteAddress
        }
        return 0
    }
    private fun BCS() : Int {
        if (getFlag(flagShiftC) == (1)) {
            cycles++
            absoluteAddress = (PC + relativeAddress)

            if ((absoluteAddress and 0xFF00) != (PC and 0xFF00))
                cycles++

            PC = absoluteAddress
        }
        return 0
    }
    private fun BNE() : Int {
        if (getFlag(flagShiftZ) == (0)) {
            cycles++
            absoluteAddress = (PC + relativeAddress)

            if ((absoluteAddress and 0xFF00) != (PC and 0xFF00))
                cycles++

            PC = absoluteAddress
        }
        return 0
    }
    private fun BEQ() : Int {
        if (getFlag(flagShiftZ) == (1)) {
            cycles++
            absoluteAddress = (PC + relativeAddress)

            if ((absoluteAddress and 0xFF00) != (PC and 0xFF00))
                cycles++

            PC = absoluteAddress
        }
        return 0
    }
    private fun BRK() : Int {
        PC++

        setFlag(flagShiftI,true)
        bus.write((0x0100 + SP), ((PC shr 8) and 0x00FF))
        SP--
        bus.write((0x0100 + SP), (PC and 0x00FF))
        SP--

        setFlag(flagShiftB,true)
        bus.write((0x0100 + SP), status)
        SP--
        setFlag(flagShiftB,false)

        PC = (bus.read(0xFFFE)) or (bus.read(0xFFFF) shl 8)
        return 0
    }
    private fun CMP() : Int {
        fetch()
        temp = (A - fetched)
        setFlag(flagShiftC, A >= fetched)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x0000))
        setFlag(flagShiftN, (temp and 0x0080) > 0)
        return 1
    }
    private fun CPX() : Int {
        fetch()
        temp = (X - fetched)
        setFlag(flagShiftC, X >= fetched)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x0000))
        setFlag(flagShiftN, (temp and 0x0080)>0)
        return 0
    }
    private fun CPY() : Int {
        fetch()
        temp = (Y - fetched)
        setFlag(flagShiftC, Y >= fetched)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x0000))
        setFlag(flagShiftN, (temp and 0x0080) > 0)
        return 0
    }
    private fun DEC() : Int {
        fetch()
        temp = (fetched - 1)
        bus.write(absoluteAddress, (temp and 0x00FF))
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x0000))
        setFlag(flagShiftN, (temp and 0x0080) > 0)
        return 0
    }
    private fun EOR() : Int {
        fetch()
        A = A xor fetched
        setFlag(flagShiftZ, A == (0x00))
        setFlag(flagShiftN, (A and 0x80) > 0)
        return 1
    }
    private fun CLC() : Int {
        setFlag(flagShiftC, false)
        return 0
    }
    private fun SEC() : Int {
        setFlag(flagShiftC, true)
        return 0
    }
    private fun CLI() : Int {
        setFlag(flagShiftI, false)
        return 0
    }
    private fun SEI() : Int {
        setFlag(flagShiftI, true)
        return 0
    }
    private fun CLV() : Int {
        setFlag(flagShiftV, false)
        return 0
    }
    private fun CLD() : Int {
        setFlag(flagShiftD, false)
        return 0
    }
    private fun SED() : Int {
        setFlag(flagShiftD, true)
        return 0
    }
    private fun INC() : Int {
        fetch()
        temp = (fetched + 1)
        bus.write(absoluteAddress, (temp and 0x00FF))
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x0000))
        setFlag(flagShiftN, (temp and 0x0080) > 0)
        return 0
    }
    private fun JMP() : Int {
        PC = absoluteAddress
        return 0
    }
    private fun JSR() : Int {
        PC--

        bus.write(((0x0100) + SP), ((PC shr 8) and 0x00FF))
        SP--
        bus.write(((0x0100) + SP), (PC and 0x00FF))
        SP--

        PC = absoluteAddress
        return 0
    }
    private fun LDA() : Int {
        fetch()
        A=fetched
        setFlag(flagShiftZ, A == (0x00))
        setFlag(flagShiftN, (A and 0x80) > 0)
        return 1
    }
    private fun LDX() : Int {
        fetch()
        X=fetched
        setFlag(flagShiftZ, X == (0x00))
        setFlag(flagShiftN, (X and 0x80) > 0)
        return 1
    }
    private fun LDY() : Int {
        fetch()
        Y=fetched
        setFlag(flagShiftZ, Y == (0x00))
        setFlag(flagShiftN, (Y and 0x80) > 0)
        return 1
    }
    private fun LSR() : Int {
        fetch();
        setFlag(flagShiftC, (fetched and 0x0001) > 0)
        temp = (fetched shr 1)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x0000))
        setFlag(flagShiftN, (temp and 0x0080) > 0)
        if(lookup[opcode].mode == AddressingMode.IMP)
            A = (temp and 0x00FF)
        else
            bus.write(absoluteAddress, (temp and 0x00FF))
        return 0;
    }
    private fun NOP() : Int {
        when (opcode) {
            (0x1C), (0x3C), (0x5C), (0x7C), (0xDC), (0xFC) -> return 1
        }
        return 0
    }
    private fun ORA() : Int {
        fetch()
        A = A or fetched
        setFlag(flagShiftZ, A == (0x00))
        setFlag(flagShiftN, (A and 0x80) > 0)
        return 1
    }
    private fun TAX() : Int {
        X = A
        setFlag(flagShiftZ, X == (0x00))
        setFlag(flagShiftN, (X and 0x80) > 0)
        return 0
    }
    private fun TXA() : Int {
        A = X
        setFlag(flagShiftZ, A == (0x00))
        setFlag(flagShiftN, (A and 0x80) > 0)
        return 0
    }
    private fun DEX() : Int {
        X--
        setFlag(flagShiftZ, X == (0x00))
        setFlag(flagShiftN, (X and (0x80)) > 0)
        return 0
    }
    private fun INX() : Int {
        X++
        setFlag(flagShiftZ, X == (0x00))
        setFlag(flagShiftN, (X and 0x80) > 0)
        return 0
    }
    private fun TAY() : Int {
        Y = A
        setFlag(flagShiftZ, Y == (0x00))
        setFlag(flagShiftN, (Y and 0x80) > 0)
        return 0
    }
    private fun TYA() : Int {
        A = Y
        setFlag(flagShiftZ, A == (0x00))
        setFlag(flagShiftN, (A and 0x80) > 0)
        return 0
    }
    private fun DEY() : Int {
        Y--
        setFlag(flagShiftZ, Y == (0x00))
        setFlag(flagShiftN, (Y and (0x80)) > 0)
        return 0
    }
    private fun INY() : Int {
        Y++
        setFlag(flagShiftZ, Y == (0x00))
        setFlag(flagShiftN, (Y and 0x80) > 0)
        return 0
    }
    private fun ROR() : Int {
        fetch()
        temp = (getFlag(flagShiftC) shl 7) or (fetched shr 1)
        setFlag(flagShiftC, (fetched and 0x01) > 0)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x00))
        setFlag(flagShiftN, (temp and 0x0080) > 0)
        if(lookup[opcode].mode == AddressingMode.IMP)
            A = (temp and 0x00FF)
        else
            bus.write(absoluteAddress, (temp and 0x00FF))
        return 0
    }
    private fun ROL() : Int {
        fetch()
        temp = (fetched shl 1) or (getFlag(flagShiftC))
        setFlag(flagShiftC, (temp and 0xFF00) > 0)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0x0000))
        setFlag(flagShiftN, (temp and 0x0080) > 0)
        if(lookup[opcode].mode == AddressingMode.IMP)
            A = (temp and 0x00FF)
        else
            bus.write(absoluteAddress, (temp and 0x00FF))
        return 0
    }
    private fun RTI() : Int {
        SP++
        status = bus.read((0x0100 + SP))
        status = status and flagShiftB.inv()
        status = status and flagShiftU.inv()

        SP++
        PC = bus.read((0x0100 + SP))
        SP++
        PC = PC or ((bus.read((0x0100 + SP))) shl 8)
        return 0
    }
    private fun RTS() : Int {
        SP++
        PC = (bus.read((0x0100 + SP)))
        SP++
        PC = PC or (bus.read((0x0100 + SP)) shl 8)

        PC++
        return 0
    }
    private fun SBC() : Int {
        fetch()
        val value : Int = fetched xor 0x00FF

        temp = (A + value + getFlag(flagShiftC))
        setFlag(flagShiftC, (temp and 0xFF00) > 0)
        setFlag(flagShiftZ, (temp and 0x00FF) == (0))
        setFlag(flagShiftV, ((temp xor A) and (temp xor value) and 0x0080) > 0)
        setFlag(flagShiftN, (temp and 0x80) > 0)
        A = (temp and 0x00FF)
        return 1
    }
    private fun STA() : Int {
        bus.write(absoluteAddress, A)
        return 0
    }
    private fun TXS() : Int {
        SP = X
        return 0
    }
    private fun TSX() : Int {
        X = SP
        setFlag(flagShiftZ, X == (0x00))
        setFlag(flagShiftN, (X and 0x80)>0)
        return 0

    }
    private fun PHA() : Int {
        bus.write((0x0100 + SP), A)
        SP--
        return 0
    }
    private fun PLA() : Int {
        SP++
        A = bus.read((0x0100 + SP))
        setFlag(flagShiftZ, A == (0x00))
        setFlag(flagShiftN, (A and 0x80) > 0)
        return 0
    }
    private fun PHP() : Int {
        bus.write((0x0100 + SP), status or flagShiftB or flagShiftU)
        setFlag(flagShiftB, false)
        setFlag(flagShiftU, false)
        SP--
        return 0
    }
    private fun PLP() : Int {
        SP++
        status = bus.read((0x0100 + SP))
        setFlag(flagShiftU, true)
        return 0
    }
    private fun STX() : Int {
        bus.write(absoluteAddress, X)
        return 0
    }
    private fun STY() : Int {
        bus.write(absoluteAddress, Y)
        return 0
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
