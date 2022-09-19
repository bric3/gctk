package io.github.bric3.gctk.app.units

import java.text.DecimalFormat

// IEC Binary Size Units https://en.wikipedia.org/wiki/Binary_prefix
enum class SizeUnit(val size: Int) {
    Bytes(1),
    KiBi(1024),
    MeBi(1024 * 1024),
    GiBi(1024 * 1024 * 1024);
}

class Size(val value: Double, val unit: SizeUnit) {
    fun formatted() = NATURAL_NUMBER.format(value) + " " + unit.name
    fun scaleFactor() = 1.toDouble() / unit.size

    fun to(unit: SizeUnit) = Size(value * scaleFactor(), unit)

    companion object {
        val NATURAL_NUMBER: DecimalFormat = DecimalFormat("0")
        val DECIMAL_WITH_PRECISION_OF_1: DecimalFormat = DecimalFormat("0.0")
        val DECIMAL_WITH_PRECISION_OF_3: DecimalFormat = DecimalFormat("0.000")
    }
}
