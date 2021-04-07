package com.github.cannor147.request.colorization;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.Territory;
import com.github.cannor147.painter.Painter;
import com.github.cannor147.painter.RGBColor;

import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.summarizingDouble;

public class StepColorizationScheme extends ColorizationScheme {
    private final SortedSet<Double> valueSeparatorSet = new TreeSet<>();
    private Color maxColor = Color.GREEN;
    private Color minColor = Color.RED;

    private List<RGBColor> colorScheme = Collections.emptyList();
    private List<Double> valueSeparators = Collections.emptyList();

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

    public void registerSeparator(double separator) {
        valueSeparatorSet.add(separator);
    }

    @Override
    public void prepareForCalculation(Map<Territory, ColorizationParameter> territoryToParameterMap) {
        this.valueSeparators = Optional.of(valueSeparatorSet)
                .filter(Predicate.not(SortedSet::isEmpty))
                .<List<Double>>map(ArrayList::new)
                .orElseGet(() -> territoryToParameterMap.values().stream()
                        .map(ColorizationParameter::getValue)
                        .flatMap(Optional::stream)
                        .collect(collectingAndThen(summarizingDouble(Double::doubleValue), s -> Optional.of(s)
                                .filter(statistics -> statistics.getCount() > 0)
                                .map(DoubleSummaryStatistics::getAverage)))
                        .map(Collections::singletonList)
                        .orElseGet(Collections::emptyList));
        final int separatorCount = valueSeparators.size();
        this.colorScheme = Painter.generateScheme(minColor.getRgbColor(), maxColor.getRgbColor(), separatorCount);
    }

    @Override
    public RGBColor calculateColor(ColorizationParameter colorizationParameter) {
        return colorizationParameter.getValue()
                .map(this::findIndex)
                .map(colorScheme::get)
                .orElseGet(defaultColor::getRgbColor);
    }

    public int findIndex(Double value) {
        if (value.compareTo(this.valueSeparators.get(0)) < 0) {
            return 0;
        } else if (value.compareTo(this.valueSeparators.get(this.valueSeparators.size() - 1)) > 0) {
            return this.valueSeparators.size();
        }

        final int index = Collections.binarySearch(this.valueSeparators, value);
        return (index + 1) * (index > 0 ? 1 : -1);
    }
}
