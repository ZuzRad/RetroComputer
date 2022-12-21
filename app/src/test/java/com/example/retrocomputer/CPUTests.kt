package com.example.retrocomputer

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CPUTests {
    private val disassembler: Disassembler = Disassembler()
    var assembly : String = ""

    @Before fun setup(){
        disassembler.reset()
    }

    @Test fun test_cpuInit(){
        Assert.assertEquals(256, disassembler.lookup.size)
        Assert.assertEquals(32, disassembler.status)
        Assert.assertEquals(65536, disassembler.memory.ram.size)
    }

    @Test fun test_getFlag(){
        Assert.assertEquals(0, disassembler.getFlag((1 shl 7))) // N
        Assert.assertEquals(0, disassembler.getFlag((1 shl 6))) // V
        Assert.assertEquals(1, disassembler.getFlag((1 shl 5))) // U
        Assert.assertEquals(0, disassembler.getFlag((1 shl 4))) // B
        Assert.assertEquals(0, disassembler.getFlag((1 shl 3))) // D
        Assert.assertEquals(0, disassembler.getFlag((1 shl 2))) // I
        Assert.assertEquals(0, disassembler.getFlag((1 shl 1))) // Z
        Assert.assertEquals(0, disassembler.getFlag((1 shl 0))) // C
    }

    @Test fun testAddressingModes() {
        // IMMediate + ADC(ADd with Carry) + LDA(LoaD Accumulator)
        assembly = "LDA #\$01\n" +
                "ADC #\$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ZeroPage0 + STA (STore Accumulator)
        assembly = "LDA #\$01\n" +
                "STA \$50\n" +
                "ADC \$50"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ZeroPage,X + LDX (LoaD X register)
        assembly = "LDA #\$01\n" +
                "STA 50\n" +
                "LDX #\$01\n" +
                "ADC 49,X"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ABSolute
        assembly = "LDA #\$01\n" +
                "STA $0200\n" +
                "ADC $0200"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ABsolute,X
        assembly = "LDA #\$01\n" +
                "STA $0200\n" +
                "LDX #\$01\n" +
                "ADC $01FF,X"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ABsolute,Y + LDY (LoaD Y register)
        assembly = "LDA #\$01\n" +
                "STA $0200\n" +
                "LDY #\$01\n" +
                "ADC $01FF,Y"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // IdirectZ,X + IMPlied + ZeroPage,Y
        // + TXA (Transfer X to A) + STY (STore Y register) + STX (STore X register)
        assembly = "LDA #$45\n" +
                "STA $1074\n" +
                "LDA #$74\n" +
                "STA $24\n" +
                "LDA #$10\n" +
                "STA $25\n" +
                "TXA\n" +
                "LDY #$04\n" +
                "STY $04\n" +
                "LDX $00,Y\n" +
                "STX $99\n" +
                "LDA ($20,X)\n"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x45, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x74, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x10, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x04, disassembler.Y)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x04, disassembler.X)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x45, disassembler.A)

        // IndirectZ,Y
        assembly = "LDA #\$45\n" +
                "STA \$1074\n" +
                "LDA #\$70\n" +
                "STA \$24\n" +
                "LDA #\$10\n" +
                "STA \$25\n" +
                "TXA\n" +
                "LDY #\$04\n" +
                "LDA (\$24),Y"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x45, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x70, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x10, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x04, disassembler.Y)
        disassembler.step()
        Assert.assertEquals(0x45, disassembler.A)
    }

    @Test fun testBitwiseInstructions() {
        // AND (bitwise AND with accumulator)
        assembly = "LDA #$50\n" +
                "AND #$10"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x50, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x10, disassembler.A)

        // ORA (bitwise OR with Accumulator)
        assembly = "LDA #$3F\n" +
                "ORA #$40"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x7F, disassembler.A)

        // EOR (bitwise Exclusive OR)
        assembly = "LDA #$3F\n" +
                "EOR #$41"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x7E, disassembler.A)

        // ASL (Arithmetic Shift Left)
        assembly = "LDA #$3F\n" +
                "ASL A"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x7E, disassembler.A)

        // LSR (Logical Shift Right)
        assembly = "LDA #$3F\n" +
                "LSR A"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x1F, disassembler.A)

        // BIT (test BITs)
        assembly = "LDA #\$FF\n" +
                "STA $00\n" +
                "BIT $00"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0xFF, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftV))

        // ROL (ROtate Left)
        assembly = "LDA #\$80\n" +
                "ROL A"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x80, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftN))
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftZ))
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftC))
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftN))

        // ROR (ROtate Right)
        assembly = "LDA #\$FF\n" +
                "ROR A"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0xFF, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftN))
        disassembler.step()
        Assert.assertEquals(0x7F, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftC))
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftN))
    }

    @Test fun testMemoryInstructions() {
        ////////// Memory instructions //////////

        // SBC (SuBtract with Carry)
        assembly = "LDA #$3F\n" +
                "SBC #$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x3D, disassembler.A)

        // CMP (CoMPare accumulator)
        assembly = "LDA #$3F\n" +
                "CMP #$3F"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        disassembler.step()
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftZ))
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftC))

        // CPX (ComPare X register)
        assembly = "LDX #$3F\n" +
                "CPX #$3F"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.X)
        disassembler.step()
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftZ))
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftC))

        // CPY (ComPare Y register)
        assembly = "LDY #$3F\n" +
                "CPY #$3F"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.Y)
        disassembler.step()
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftZ))
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftC))

        // DEC (DECrement memory)
        assembly = "LDA #$3F\n" +
                "STA $00\n" +
                "DEC $00\n" +
                "LDA $00"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        disassembler.step()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x3E, disassembler.A)

        // INC (INCrement memory)
        assembly = "LDA #$3F\n" +
                "STA $00\n" +
                "INC $00\n" +
                "LDA $00"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        disassembler.step()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x40, disassembler.A)

        // BRK (BReaK) - works at the end of program
        assembly = "LDA #$3F\n" +
                "BRK\n" +
                "LDA #$00"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x3F, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftI))
    }

    @Test fun testRegisterInstructions() {
        // ----- Register Instructions -----
        // TAX (Transfer A to X)
        // TXA (Transfer X to A)
        // DEX (DEcrement X)
        // INX (INcrement X)
        // TAY (Transfer A to Y)
        // TYA (Transfer Y to A)
        // DEY (DEcrement Y)
        // INY (INcrement Y)

        assembly = "LDA #$01\n" +
                "TAX\n" +
                "INX\n" +
                "TXA\n" +
                "DEX\n" +
                "TAY\n" +
                "INY\n" +
                "TYA\n" +
                "DEY"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.X)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.Y)
        disassembler.step()
        Assert.assertEquals(0x03, disassembler.Y)
        disassembler.step()
        Assert.assertEquals(0x03, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.Y)
    }

    @Test fun testBranchInstructions() {
        // ---------- BRANCH INSTRUCTIONS -----------
        // BPL (Branch on PLus)
        assembly = "LDA #\$7E\n" +
                "loop:\n" +
                "ADC #$01\n" +
                "BPL loop\n" +
                "LDX #$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x7E, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x7F, disassembler.A)
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftN))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x80, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftN))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)

        // BMI (Branch on MInus)
        assembly = "LDA #\$FE\n" +
                "loop:\n" +
                "ADC #$01\n" +
                "BMI loop\n" +
                "LDX #$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0xFE, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0xFF, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftN))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.A)
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftN))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)

        // BVC (Branch on oVerflow Clear)
        assembly = "LDA #\$7E\n" +
                "loop:\n" +
                "ADC #$01\n" +
                "BVC loop\n" +
                "LDX #$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x7E, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x7F, disassembler.A)
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftV))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x80, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftV))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)

        // BVS (Branch on oVerflow Set)
        assembly = "LDA #\$7F\n" +
                "loop:\n" +
                "ADC #$01\n" +
                "BVS loop\n" +
                "LDX #$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x7F, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x80, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftV))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x81, disassembler.A)
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftC))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)

        // BCC (Branch on Carry Clear)
        assembly = "LDA #\$FE\n" +
                "loop:\n" +
                "ADC #$01\n" +
                "BCC loop\n" +
                "LDX #$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0xFE, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0xFF, disassembler.A)
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftC))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftC))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)

        // BCS (Branch on Carry Set)
        assembly = "LDA #\$FF\n" +
                "loop:\n" +
                "ADC #$01\n" +
                "BCS loop\n" +
                "LDX #$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0xFF, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.A)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftC))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftC))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)

        // BNE (Branch on Not Equal)
        assembly = "LDA #$00\n" +
                "LDY #$02\n" +
                "loop:\n" +
                "ADC #$01\n" +
                "DEY\n" +
                "BNE loop\n" +
                "LDX #$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.Y)
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftZ))
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.Y)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.Y)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftZ))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)

        // BEQ (Branch on EQual)
        assembly = "LDA #$10\n" +
                "STA $02\n" +
                "LDY $00\n" +
                "loop:\n" +
                "INY\n" +
                "LDX $00,Y\n" +
                "BEQ loop\n" +
                "LDX #$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(0x10, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.Y)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftZ))
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.Y)
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.X)
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftZ))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.Y)
        disassembler.step()
        Assert.assertEquals(0x10, disassembler.X)
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftZ))
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)

        // JMP (JuMP)
        assembly = "LDA #$00\n" +
                "loop:\n" +
                "ADC #$01\n" +
                "JMP loop"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x03, disassembler.A)

        // JSR (Jump to SubRoutine)
        assembly = "JSR $8100\n"
        disassembler.loadMemoryAssembly(assembly)
        Assert.assertEquals(0x8000, disassembler.PC)
        disassembler.step()
        Assert.assertEquals(0x8100, disassembler.PC)

        // RTI (ReTurn from Interrupt)
        assembly = "RTI"
        disassembler.loadMemoryAssembly(assembly)
        Assert.assertEquals(0x8000, disassembler.PC)
        Assert.assertEquals(0xFD, disassembler.SP)
        disassembler.step()
        Assert.assertEquals(0x0000, disassembler.PC)
        Assert.assertEquals(32, disassembler.status)
        Assert.assertEquals(0x100, disassembler.SP)

        // RTS (ReTurn from Subroutine)
        assembly = "RTS"
        disassembler.loadMemoryAssembly(assembly)
        Assert.assertEquals(0x8000, disassembler.PC)
        Assert.assertEquals(0xFD, disassembler.SP)
        disassembler.step()
        Assert.assertEquals(0x0001, disassembler.PC)
        Assert.assertEquals(32, disassembler.status)
        Assert.assertEquals(0xFF, disassembler.SP)
    }

    @Test fun testFlagInstructions() {
        // ----- Flag (Processor Status) Instructions -----
        // CLC (CLear Carry)
        // SEC (SEt Carry)
        // CLI (CLear Interrupt)
        // SEI (SEt Interrupt)
        // CLV (CLear oVerflow)
        // CLD (CLear Decimal)
        // SED (SEt Decimal)
        assembly = "SEC\n" +
                "CLC\n" +
                "SEI\n" +
                "CLI\n" +
                "SED\n" +
                "CLD\n" +
                "LDA #$7F\n" +
                "ADC #$01\n" +
                "CLV"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.step()
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftC))
        disassembler.step()
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftC))
        disassembler.step()
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftI))
        disassembler.step()
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftI))
        disassembler.step()
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftD))
        disassembler.step()
//        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftD)) ???
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(1, disassembler.getFlag(disassembler.flagShiftV))
        disassembler.step()
        Assert.assertEquals(0, disassembler.getFlag(disassembler.flagShiftV))
    }

    @Test fun testStackInstructions() {
        // ----- Stack Instructions -----
        // TXS (Transfer X to Stack ptr)
        // TSX (Transfer Stack ptr to X)
        // PHA (PusH Accumulator)
        // PLA (PuLl Accumulator)
        // PHP (PusH Processor status)
        // PLP (PuLl Processor status)
        assembly = "LDA #$10\n" +
                "LDX #$01\n" +
                "TXS\n" +
                "LDX #$02\n" +
                "TSX\n" +
                "PHA\n" +
                "LDA #$05\n" +
                "PLA\n" +
                "PHP\n" +
                "PLP"
        disassembler.loadMemoryAssembly(assembly)
        Assert.assertEquals(0xFD, disassembler.SP)
        disassembler.step()
        Assert.assertEquals(0x10, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.SP)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.X)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.X)
        disassembler.step()
        Assert.assertEquals(0x00, disassembler.SP)
        disassembler.step()
        Assert.assertEquals(0x05, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.SP)
        Assert.assertEquals(0x10, disassembler.A)
        Assert.assertEquals(0x20, disassembler.status)
        disassembler.step()
        Assert.assertEquals(0x20, disassembler.status)
        Assert.assertEquals(0x00, disassembler.getFlag(disassembler.flagShiftB))
        Assert.assertEquals(0x00, disassembler.SP)
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.SP)
        Assert.assertEquals(0x30, disassembler.status)
        Assert.assertEquals(0x01, disassembler.getFlag(disassembler.flagShiftB))
    }
}