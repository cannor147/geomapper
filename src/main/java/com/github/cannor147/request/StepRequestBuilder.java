package com.github.cannor147.request;

import com.github.cannor147.configuration.Configuration;
import com.github.cannor147.model.Color;
import com.github.cannor147.painter.RGBColor;
import com.github.cannor147.painter.Painter;
import com.github.cannor147.util.ReadUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class StepRequestBuilder extends RequestBuilder {
    private final Map<String, Double> territoryToValueMap = new HashMap<>();
    private final SortedSet<Double> valueSeparators = new TreeSet<>();
    private Color maxColor = Color.GREEN;
    private Color minColor = Color.RED;

    public StepRequestBuilder(Configuration configuration) {
        super(configuration);
    }

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
                .map(pair -> Pair.of(pair.getLeft(), safeParseNumber(pair.getRight())))
                .filter(p -> p.getRight() != null)
                .collect(Collectors.collectingAndThen(Collectors.toList(), this::appendAll));
    }

    @Override
    public Request build() {
        final List<Double> separators = new ArrayList<>(valueSeparators);
        final List<RGBColor> scheme = Painter.generateScheme(minColor.getRgbColor(),
                maxColor.getRgbColor(), valueSeparators.size());
        final Queue<ColorizationTask> tasks = territoryToValueMap.entrySet().stream()
                .map(e -> configuration.find(e.getKey())
                        .map(t -> new ColorizationTask(t, scheme.get(findIndex(separators, e.getValue()))))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
        return new Request(tasks, configuration, defaultColor);
    }

    public static <T extends Comparable<T>> int findIndex(List<T> elements, T element) {
        if (element.compareTo(elements.get(0)) < 0) {
            return 0;
        } else if (element.compareTo(elements.get(elements.size() - 1)) > 0) {
            return elements.size();
        }

        final int index = Collections.binarySearch(elements, element);
        return (index + 1) * (index > 0 ? 1 : -1);
    }
}
