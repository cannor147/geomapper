package com.github.cannor147.model.request;

import lombok.RequiredArgsConstructor;
import com.github.cannor147.model.Color;
import org.apache.commons.lang3.tuple.Pair;
import com.github.cannor147.util.ReadUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@RequiredArgsConstructor
public class ScaleRequestBuilder {
    private final String configuration;
    private final Map<String, Double> territoryToValueMap = new HashMap<>();
    private Color maxColor = Color.GREEN;
    private Color minColor = Color.RED;
    private Color defaultColor = Color.SILVER;
    private Double maxValue;
    private Double minValue;
    private Function<Double, Double> transformer = x -> x;

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
        return ReadUtils.readCsv(file, nameColumn, valueColumn).stream()
                .map(pair -> Pair.of(pair.getLeft(), ReadUtils.safeParseNumber(pair.getRight())))
                .filter(p -> p.getRight() != null)
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::appendAll));
    }

    public ScaleRequest build() {
        final Map<String, Double> data = territoryToValueMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> transformer.apply(e.getValue())));
        final Double maxValue = Optional.ofNullable(this.maxValue)
                .map(transformer)
                .orElseGet(() -> data.values().stream().mapToDouble(x -> x).max().orElse(100.0));
        final Double minValue = Optional.ofNullable(this.maxValue)
                .map(transformer)
                .orElseGet(() -> data.values().stream().mapToDouble(x -> x).min().orElse(0.0));
        return new ScaleRequest(configuration, data, maxColor, minColor, defaultColor, maxValue, minValue);
    }

    public Optional<ScaleRequest> buildOptional() {
        return Optional.of(build());
    }
}
