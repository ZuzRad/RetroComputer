package com.example.retrocomputer.emulator

class Bus() {
    private val ram = UByteArray(65536)

    fun read(addr: UShort): UByte {
        if (addr >= 0x0000U && addr <= 0xFFFFU) {
            return ram[addr.toInt()]
        }
        return 0x00U;
    }

    fun write(addr: UShort, value: UByte) {
        if (addr >= 0x0000U && addr <= 0xFFFFU) {
            ram[addr.toInt()] = value
        }
    }

    init {
        ram.forEach { _ ->
            0x00U
        }
    }
}