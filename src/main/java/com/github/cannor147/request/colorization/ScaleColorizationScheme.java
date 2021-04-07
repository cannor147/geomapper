package com.github.cannor147.request.colorization;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.Territory;
import com.github.cannor147.painter.Painter;
import com.github.cannor147.painter.RGBColor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScaleColorizationScheme extends ColorizationScheme {
    private Color maxColor = Color.GREEN;
    private Color minColor = Color.RED;
    private Double customMaxValue = null;
    private Double customMinValue = null;
    private Function<Double, Double> transformer = x -> x;

    private List<RGBColor> colorScheme = Collections.emptyList();
    private double maxValue;
    private double minValue;

    public void registerMaxColor(Color maxColor) {
        this.maxColor = maxColor;
    }

    public void registerMinColor(Color minColor) {
        this.minColor = minColor;
    }

    public void registerColors(Color maxColor, Color minColor) {
        registerMaxColor(maxColor);
        registerMinColor(minColor);
    }

    public <N extends Number> void registerMaxValue(N maxValue) {
        this.customMaxValue = maxValue.doubleValue();
    }

    public <N extends Number> void registerMinValue(N minValue) {
        this.customMinValue = minValue.doubleValue();
    }

    public <N1 extends Number, N2 extends Number> void registerBounds(N1 maxValue, N2 minValue) {
        registerMaxValue(maxValue);
        registerMinValue(minValue);
    }

    public <N extends Number> void registerLogarithmization(N base) {
        this.transformer = x -> Math.log(x) / Math.log(base.doubleValue());
    }

    @Override
    public void prepareForCalculation(Map<Territory, ColorizationParameter> territoryToParameterMap) {
        final DoubleSummaryStatistics valueStatistics = territoryToParameterMap.values().stream()
                .map(ColorizationParameter::getValue)
                .flatMap(Optional::stream)
                .collect(Collectors.summarizingDouble(Double::doubleValue));
        this.maxValue = transformer.apply(Optional.ofNullable(customMaxValue).orElseGet(valueStatistics::getMax));
        this.minValue = transformer.apply(Optional.ofNullable(customMinValue).orElseGet(valueStatistics::getMin));
        this.colorScheme = Painter.generateScheme(minColor.getRgbColor(), maxColor.getRgbColor());
    }

    @Override
    public RGBColor calculateColor(ColorizationParameter colorizationParameter) {
        return colorizationParameter.getValue()
                .map(transformer)
                .map(value -> Math.round((value - minValue) / (maxValue - minValue) * 100))
                .map(Long::intValue)
                .map(value -> Math.max(Math.min(value, 100), 0))
                .map(colorScheme::get)
                .orElseGet(defaultColor::getRgbColor);
    }
}
