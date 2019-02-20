package drwdrd.ktdev.starfield

import drwdrd.ktdev.engine.RandomGenerator

object RandomColor {

    private var colorTable = arrayOf(0xffff67b6, 0xffbc8dff, 0xffff9bb6, 0xffa5d9ff, 0xff38858d, 0xff561548, 0xff262eb3, 0xff2129a1, 0xff0d134a, 0xff1b2ba8, 0xff674481, 0xff655fff)

    fun randomColor() : Int {
        require(colorTable.isNotEmpty())
        val index = RandomGenerator.rand(colorTable.size - 1)
        return colorTable[index].toInt()
    }

}