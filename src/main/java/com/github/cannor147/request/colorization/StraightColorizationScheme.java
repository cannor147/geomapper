package com.github.cannor147.request.colorization;

import com.github.cannor147.painter.RGBColor;

public class StraightColorizationScheme extends ColorizationScheme {
    @Override
    public RGBColor calculateColor(ColorizationParameter colorizationParameter) {
        return colorizationParameter.getColor().orElse(defaultColor).getRgbColor();
    }
}
