package com.example.retrocomputer.emulator

class Disassembler : CPU() {

    private val cpu = CPU()

    fun step() {
        while (cycles > 0) {
            clock()
        }
        clock()
    }
}