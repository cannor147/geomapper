package com.github.cannor147.request.colorization;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.Territory;
import com.github.cannor147.painter.RGBColor;

import java.util.Map;

public abstract class ColorizationScheme {
    protected Color defaultColor = Color.SILVER;

    public Color getDefaultColor() {
        return defaultColor;
    }

    public void registerDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void prepareForCalculation(Map<Territory, ColorizationParameter> territoryToParameterMap) {
        // No operations.
    }

    public abstract RGBColor calculateColor(ColorizationParameter colorizationParameter);
}
