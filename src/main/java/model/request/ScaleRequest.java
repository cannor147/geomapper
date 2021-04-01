package model.request;

import configuration.Configuration;
import configuration.Configurer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import model.Color;
import model.ColorizationTask;
import model.rgb.RGBColor;
import util.RGBUtils;

import java.io.IOException;
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
