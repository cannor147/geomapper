package com.github.cannor147.request;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.GeoMap;
import com.github.cannor147.util.CsvUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class StraightRequestBuilder extends RequestBuilder {
    private final Map<Color, List<String>> colorToTerritoriesMap = new HashMap<>();
    private Color defaultColor = Color.SILVER;
    private Color color = Color.BLUE;

    public StraightRequestBuilder(GeoMap geoMap) {
        super(geoMap);
    }

    public StraightRequestBuilder useColor(Color color) {
        this.color = color;
        return this;
    }

    public StraightRequestBuilder append(String territory) {
        colorToTerritoriesMap.putIfAbsent(color, new ArrayList<>());
        colorToTerritoriesMap.get(color).add(territory);
        return this;
    }

    public StraightRequestBuilder appendAll(Iterable<String> territories) {
        colorToTerritoriesMap.putIfAbsent(color, new ArrayList<>());
        territories.forEach(colorToTerritoriesMap.get(color)::add);
        return this;
    }

    public StraightRequestBuilder appendAll(String... territories) {
        return appendAll(Arrays.stream(territories).collect(Collectors.toList()));
    }

    public StraightRequestBuilder append(Color color, String territory) {
        useColor(color);
        return append(territory);
    }

    public StraightRequestBuilder appendAll(Color color, Iterable<String> territories) {
        useColor(color);
        return appendAll(territories);
    }

    public StraightRequestBuilder appendAll(Color color, String... territories) {
        return appendAll(color, Arrays.stream(territories).collect(Collectors.toList()));
    }

    public StraightRequestBuilder useDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
        return this;
    }

    public StraightRequestBuilder fromCsv(File file, int nameColumn) throws IOException {
        return appendAll(CsvUtils.readCsv(file, nameColumn));
    }

    @Override
    public Request build() {
        final Queue<ColorizationTask> tasks = colorToTerritoriesMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(territory -> geoMap.find(territory).orElse(null))
                        .filter(Objects::nonNull)
                        .map(territory -> new ColorizationTask(territory, entry.getKey().getRgbColor())))
                .collect(Collectors.toCollection(LinkedList::new));
        return new Request(tasks, geoMap, defaultColor, unofficialStateBehavior);
    }
}
