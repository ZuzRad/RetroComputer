package com.example.retrocomputer

class Memory {
    // Inicjalizacja pamięci
    val ram = IntArray(65536)

    // Odczyt z pamięci
    fun read(addr: Int): Int {
        if (addr in 0x0000..0xFFFF) {
            return ram[addr and 0xFFFF]
        }
        return 0x00;
    }

    // Zapis w pamięci
    fun write(addr: Int, value: Int) {
        if (addr in 0x0000..0xFFFF) {
            ram[addr and 0xFFFF] = value
        }
    }
}
