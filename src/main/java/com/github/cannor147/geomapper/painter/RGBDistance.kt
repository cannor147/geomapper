package com.github.cannor147.geomapper.painter

import kotlin.math.max
import kotlin.math.min

data class RGBDistance internal constructor(
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
        return RGBColor.of(red, green, blue)
    }

    companion object {
        fun of(red: Int, green: Int, blue: Int): RGBDistance {
            val realRed = max(min(red, 255), -255)
            val realGreen = max(min(green, 255), -255)
            val realBlue = max(min(blue, 255), -255)
            return RGBDistance(realRed, realGreen, realBlue)
        }

        fun between(a: RGB, b: RGB): RGBDistance {
            return of(a.red - b.red, a.green - b.green, a.blue - b.blue)
        }
    }
}