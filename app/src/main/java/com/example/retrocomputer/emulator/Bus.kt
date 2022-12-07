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

//    fun getWord(addr: UShort): UShort {
//        return read(addr) + read(addr + 1).shl(8)
//    }


//    fun storeByte(addr: Int, value: Int) {
//        set(addr, value.and(0xff))
//        if (addr in 0x200..0x5ff) {
//            display.updatePixel(addr, mem[addr].and(0x0f))
//        }
//    }
//    // Store keycode in ZP $ff
//    fun storeKeypress(keyCode: Int) {
//        storeByte(0xff, keyCode)
//    }
//
//    fun format(start: Int, length: Int): String {
//        var i = 0
//        var n: Int
//        val dump = StringBuilder()
//        while (i < length) {
//            if (i.and(15) == 0) {
//                if (i > 0) {
//                    dump.append("\n")
//                }
//                n = start + i
//                dump.append(n.shr(8).and(0xff).toHexString())
//                dump.append(n.and(0xff).toHexString())
//                dump.append(": ")
//            }
//            dump.append(get(start + i).toHexString())
//            dump.append(" ")
//            i++
//        }
//        return dump.toString().trim()
//    }
}