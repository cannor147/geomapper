package com.github.cannor147.request.colorization

import com.github.cannor147.painter.RGBColor

class StraightColorizationScheme : ColorizationScheme() {
    override fun calculateColor(colorizationParameter: ColorizationParameter): RGBColor {
        return colorizationParameter.getColor().orElse(defaultColor).rgbColor
    }
}