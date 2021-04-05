package com.github.cannor147.request;

import com.github.cannor147.model.GeoMap;
import com.github.cannor147.model.Color;
import com.github.cannor147.painter.RGBColor;
import com.github.cannor147.painter.Painter;
import com.github.cannor147.util.CsvUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ScaleRequestBuilder extends RequestBuilder {
    private final Map<String, Double> territoryToValueMap = new HashMap<>();
    private Color maxColor = Color.GREEN;
    private Color minColor = Color.RED;
    private Double maxValue;
    private Double minValue;
    private Function<Double, Double> transformer = x -> x;

    public ScaleRequestBuilder(GeoMap geoMap) {
        super(geoMap);
    }

    public <N extends Number> ScaleRequestBuilder append(String territory, N value) {
        territoryToValueMap.put(territory, value.doubleValue());
        return this;
    }

    public <N extends Number> ScaleRequestBuilder append(Pair<String, N> territoryToValue) {
        return append(territoryToValue.getKey(), territoryToValue.getValue().doubleValue());
    }

    public <N extends Number> ScaleRequestBuilder append(Map.Entry<String, N> territoryToValue) {
        return append(territoryToValue.getKey(), territoryToValue.getValue().doubleValue());
    }

    public <N extends Number> ScaleRequestBuilder appendAll(Map<String, N> territoryToValueMap) {
        territoryToValueMap.forEach(this::append);
        return this;
    }

    public <N extends Number> ScaleRequestBuilder appendAll(Iterable<Pair<String, N>> territoryToValueList) {
        territoryToValueList.forEach(this::append);
        return this;
    }

    public ScaleRequestBuilder useColor(Color maxColor, Color minColor) {
        this.maxColor = maxColor;
        this.minColor = minColor;
        return this;
    }

    public ScaleRequestBuilder useColor(Color maxColor) {
        return useColor(maxColor, Optional.ofNullable(maxColor.getDefaultOpposite()).orElse(maxColor));
    }

    public ScaleRequestBuilder reverseColors() {
        return useColor(minColor, maxColor);
    }

    public ScaleRequestBuilder useDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
        return this;
    }

    public <N extends Number> ScaleRequestBuilder customizeMaximum(N maxValue) {
        this.maxValue = maxValue.doubleValue();
        return this;
    }

    public <N extends Number> ScaleRequestBuilder customizeMinimum(N minValue) {
        this.minValue = minValue.doubleValue();
        return this;
    }

    public <N1 extends Number, N2 extends Number> ScaleRequestBuilder customizeMinMax(N1 minValue, N2 maxValue) {
        this.minValue = minValue.doubleValue();
        this.maxValue = maxValue.doubleValue();
        return this;
    }

    public <N extends Number> ScaleRequestBuilder addLogarithmization(N base) {
        this.transformer = x -> Math.log(x) / Math.log(base.doubleValue());
        return this;
    }

    public ScaleRequestBuilder fromCsv(File file, int nameColumn, int valueColumn) throws IOException {
        return CsvUtils.readCsv(file, nameColumn, valueColumn).stream()
                .map(pair -> Pair.of(pair.getLeft(), safeParseNumber(pair.getRight())))
                .filter(p -> p.getRight() != null)
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::appendAll));
    }

    @Override
    public Request build() {
        final Map<String, Double> data = territoryToValueMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> transformer.apply(e.getValue())));
        final Double maxValue = Optional.ofNullable(this.maxValue)
                .map(transformer)
                .orElseGet(() -> data.values().stream().mapToDouble(x -> x).max().orElse(100.0));
        final Double minValue = Optional.ofNullable(this.maxValue)
                .map(transformer)
                .orElseGet(() -> data.values().stream().mapToDouble(x -> x).min().orElse(0.0));
        final List<RGBColor> scheme = Painter.generateScheme(minColor.getRgbColor(), maxColor.getRgbColor());
        final Queue<ColorizationTask> tasks = data.entrySet().stream()
                .map(e -> geoMap.find(e.getKey())
                        .map(t -> new ColorizationTask(t, scheme.get(toPercent(e.getValue(), minValue, maxValue))))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
        return new Request(tasks, geoMap, defaultColor, unofficialStateBehavior);
    }

    private static int toPercent(double x, double min, double max) {
        return Math.max(Math.min((int) Math.round((x - min) / (max - min) * 100), 100), 0);
    }
}
