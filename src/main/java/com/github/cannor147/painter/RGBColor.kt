package com.github.cannor147.painter

import java.awt.Color

data class RGBColor internal constructor(
    override val red: Int,
    override val green: Int,
    override val blue: Int,
) : RGB {
    override fun add(other: RGB): RGB {
        return of(red + other.red, green + other.green, blue + other.blue)
    }

    override fun subtract(other: RGB): RGB {
        return of(red - other.red, green - other.green, blue - other.blue)
    }

    override fun multiply(proportion: Double): RGB {
        return of((red * proportion).toInt(), (green * proportion).toInt(), (blue * proportion).toInt())
    }

    override fun asColor(): RGBColor {
        return this
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun toColor(): Color {
        return Color(red, green, blue)
    }

    fun toInt(): Int {
        return toColor().rgb
    }

    companion object {
        @JvmStatic
        fun of(red: Int, green: Int, blue: Int): RGBColor {
            return RGBColor(red and 0xff, green and 0xff, blue and 0xff)
        }

        fun fromInt(rgbInt: Int): RGBColor {
            return of(rgbInt shr 16, rgbInt shr 8, rgbInt)
        }
    }
}