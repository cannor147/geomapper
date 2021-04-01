package com.github.cannor147.model.request;

import com.github.cannor147.configuration.Configuration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.github.cannor147.model.Color;
import com.github.cannor147.model.ColorizationTask;
import com.github.cannor147.model.rgb.RGBColor;
import com.github.cannor147.util.RGBUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ScaleRequest implements Request {
    private final String configuration;
    private final Map<String, Double> territoryToValueMap;
    private final Color maxColor;
    private final Color minColor;
    private final Color defaultColor;
    private final double maxValue;
    private final double minValue;

    @Override
    public Queue<ColorizationTask> toTasks(Configuration configuration) {
        final List<RGBColor> scheme = RGBUtils.generateScheme(minColor.getRgbColor(), maxColor.getRgbColor());
        return territoryToValueMap.entrySet().stream()
                .map(e -> configuration.find(e.getKey())
                        .map(t -> new ColorizationTask(t, scheme.get(toPercent(e.getValue(), minValue, maxValue))))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static int toPercent(double x, double min, double max) {
        return Math.max(Math.min((int) Math.round((x - min) / (max - min) * 100), 100), 0);
    }
}
