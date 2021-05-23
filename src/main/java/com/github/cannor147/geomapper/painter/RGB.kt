package com.github.cannor147.geomapper.painter

interface RGB {
    val red: Int
    val green: Int
    val blue: Int

    fun add(other: RGB): RGB
    fun subtract(other: RGB): RGB
    fun multiply(proportion: Double): RGB

    fun asColor(): RGBColor
}