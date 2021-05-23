package com.github.cannor147.geomapper.request.colorization

import com.github.cannor147.geomapper.painter.RGBColor

class StraightColorizationScheme : ColorizationScheme() {
    override fun calculateColor(colorizationParameter: ColorizationParameter): RGBColor {
        return (colorizationParameter.color ?: defaultColor).rgbColor
    }
}