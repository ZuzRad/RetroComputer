package com.example.retrocomputer

class Memory {
    val ram = IntArray(65536)

    fun read(addr: Int): Int {
        if (addr in 0x0000..0xFFFF) {
            return ram[addr and 0xFFFF]
        }
        return 0x00;
    }

    fun write(addr: Int, value: Int) {
        if (addr in 0x0000..0xFFFF) {
            ram[addr and 0xFFFF] = value
        }
    }
}
