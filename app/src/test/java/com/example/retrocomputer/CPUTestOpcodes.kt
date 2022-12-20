package com.example.retrocomputer

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CPUTestOpcodes {
    private val disassembler: Disassembler = Disassembler()
    var assembly : String = ""

    @Before
    fun setup(){
        disassembler.reset()
    }
    @Test fun testAll() {
        // Addressing Modes

        // IMMediate + ADC(ADd with Carry) + LDA(LoaD Accumulator)
        assembly =  "LDA #\$01\n" +
                    "ADC #\$01"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.reset()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ZeroPage0 + STA (STore Accumulator)
        assembly =  "LDA #\$01\n" +
                "STA \$50\n" +
                "ADC \$50"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.reset()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ZeroPage,X + LDX (LoaD X register)
        assembly =  "LDA #\$01\n" +
                "STA 50\n" +
                "LDX #\$01\n" +
                "ADC 49,X"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.reset()
        disassembler.step()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ABSolute
        assembly =  "LDA #\$01\n" +
                "STA $0200\n" +
                "ADC $0200"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.reset()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ABsolute,X
        assembly =  "LDA #\$01\n" +
                "STA $0200\n" +
                "LDX #\$01\n" +
                "ADC $01FF,X"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.reset()
        disassembler.step()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // ABsolute,Y + LDY (LoaD Y register)
        assembly =  "LDA #\$01\n" +
                "STA $0200\n" +
                "LDY #\$01\n" +
                "ADC $01FF,Y"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.reset()
        disassembler.step()
        disassembler.step()
        disassembler.step()
        Assert.assertEquals(0x01, disassembler.A)
        disassembler.step()
        Assert.assertEquals(0x02, disassembler.A)

        // IdirectZ,X + IMPlied + ZeroPage,Y
        // + TXA (Transfer X to A) + STY (STore Y register) + STX (STore X register)
        assembly =  "LDA #\$45\n" +
                "STA \$1074\n" +
                "LDA #\$74\n" +
                "STA \$24\n" +
                "LDA #\$10\n" +
                "STA \$25\n" +
                "TXA\n" +
                "LDY #\$04\n" +
                "STY \$04\n" +
                "LDX \$00,Y\n" +
                "STX \$99" +
                "LDA (\$20,X)"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.reset()
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
        Assert.assertEquals(0x45, disassembler.A)

        // IndirectZ,Y
        assembly =  "LDA #\$45\n" +
                "STA \$1074\n" +
                "LDA #\$70\n" +
                "STA \$24\n" +
                "LDA #\$10\n" +
                "STA \$25\n" +
                "TXA\n" +
                "LDY #\$04\n" +
                "LDA (\$24),Y"
        disassembler.loadMemoryAssembly(assembly)
        disassembler.reset()
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


    // AND (bitwise AND with accumulator)
    // ORA (bitwise OR with Accumulator)
    // EOR (bitwise Exclusive OR)
    // ASL (Arithmetic Shift Left)
    // LSR (Logical Shift Right)
    // BIT (test BITs)

    // SBC (SuBtract with Carry)
    // CMP (CoMPare accumulator)
    // CPX (ComPare X register)
    // CPY (ComPare Y register)
    // DEC (DECrement memory)
    // INC (INCrement memory)

    // BRK (BReaK) - works at the end of program
    // NOP (No OPeration) - No operation

    // ---------- BRANCH INSTRUCTIONS -----------
    // BPL (Branch on PLus)
    // BMI (Branch on MInus)
    // BVC (Branch on oVerflow Clear)
    // BVS (Branch on oVerflow Set)
    // BCC (Branch on Carry Clear)
    // BCS (Branch on Carry Set)
    // BNE (Branch on Not Equal)
    // BEQ (Branch on EQual)

    // JMP (JuMP)
    // JSR (Jump to SubRoutine)

    // ----- Flag (Processor Status) Instructions -----
    // CLC (CLear Carry)
    // SEC (SEt Carry)
    // CLI (CLear Interrupt)
    // SEI (SEt Interrupt)
    // CLV (CLear oVerflow)
    // CLD (CLear Decimal)
    // SED (SEt Decimal)


    // ----- Register Instructions -----
    // TAX (Transfer A to X)
    // TXA (Transfer X to A)
    // DEX (DEcrement X)
    // INX (INcrement X)
    // TAY (Transfer A to Y)
    // TYA (Transfer Y to A)
    // DEY (DEcrement Y)
    // INY (INcrement Y)

    // ROL (ROtate Left)
    // ROR (ROtate Right)

    // RTI (ReTurn from Interrupt)
    // RTS (ReTurn from Subroutine)

    // ----- Stack Instructions -----
    // TXS (Transfer X to Stack ptr)
    // TSX (Transfer Stack ptr to X)
    // PHA (PusH Accumulator)
    // PLA (PuLl Accumulator)
    // PHP (PusH Processor status)
    // PLP (PuLl Processor status)
}