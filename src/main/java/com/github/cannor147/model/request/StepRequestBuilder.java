package com.github.cannor147.model.request;

import lombok.RequiredArgsConstructor;
import com.github.cannor147.model.Color;
import org.apache.commons.lang3.tuple.Pair;
import com.github.cannor147.util.ReadUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@RequiredArgsConstructor
public class StepRequestBuilder {
    private final String configuration;
    private final Map<String, Double> territoryToValueMap = new HashMap<>();
    private Color maxColor = Color.GREEN;
    private Color minColor = Color.RED;
    private Color defaultColor = Color.SILVER;
    private final SortedSet<Double> valueSeparators = new TreeSet<>();

    public <N extends Number> StepRequestBuilder append(String territory, N value) {
        territoryToValueMap.put(territory, value.doubleValue());
        return this;
    }

    public <N extends Number> StepRequestBuilder append(Pair<String, N> territoryToValue) {
        return append(territoryToValue.getKey(), territoryToValue.getValue().doubleValue());
    }

    public <N extends Number> StepRequestBuilder append(Map.Entry<String, N> territoryToValue) {
        return append(territoryToValue.getKey(), territoryToValue.getValue().doubleValue());
    }

    public <N extends Number> StepRequestBuilder appendAll(Map<String, N> territoryToValueMap) {
        territoryToValueMap.forEach(this::append);
        return this;
    }

    public <N extends Number> StepRequestBuilder appendAll(Iterable<Pair<String, N>> territoryToValueList) {
        territoryToValueList.forEach(this::append);
        return this;
    }

    public StepRequestBuilder useColor(Color maxColor, Color minColor) {
        this.maxColor = maxColor;
        this.minColor = minColor;
        return this;
    }

    public StepRequestBuilder useColor(Color maxColor) {
        return useColor(maxColor, maxColor.getDefaultOpposite());
    }

    public StepRequestBuilder reverseColors() {
        return useColor(minColor, maxColor);
    }

    public StepRequestBuilder useDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
        return this;
    }

    public <N extends Number> StepRequestBuilder withSeparator(N separator) {
        valueSeparators.add(separator.doubleValue());
        return this;
    }

    public StepRequestBuilder withSeparators(double... separators) {
        Arrays.stream(separators).forEach(this::withSeparator);
        return this;
    }

    public <N extends Number> StepRequestBuilder withSeparators(N[] separators) {
        return withSeparators(Arrays.asList(separators));
    }

    public <N extends Number> StepRequestBuilder withSeparators(Iterable<N> separators) {
        separators.forEach(separator -> valueSeparators.add(separator.doubleValue()));
        return this;
    }

    public StepRequestBuilder fromCsv(File file, int nameColumn, int valueColumn) throws IOException {
        return ReadUtils.readCsv(file, nameColumn, valueColumn).stream()
                .map(pair -> Pair.of(pair.getLeft(), ReadUtils.safeParseNumber(pair.getRight())))
                .filter(p -> p.getRight() != null)
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::appendAll));
    }

    public StepRequest build() {
        return new StepRequest(configuration, territoryToValueMap, maxColor, minColor, defaultColor, valueSeparators);
    }

    public Optional<StepRequest> buildOptional() {
        return Optional.of(build());
    }
}
