package com.github.cannor147.geomapper.request.colorization

import com.github.cannor147.geomapper.painter.RGBColor
import java.awt.Point

data class ColorizationTask(
    val points: List<Point>,
    val rgbColor: RGBColor,
    val onlyPixel: Boolean = false
)