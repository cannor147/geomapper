package com.github.cannor147.request.colorization

import com.github.cannor147.painter.RGBColor
import java.awt.Point

data class ColorizationTask(
    val points: List<Point>,
    val rgbColor: RGBColor,
    val onlyPixel: Boolean = false
)