package model.request;

import lombok.RequiredArgsConstructor;
import model.Color;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@RequiredArgsConstructor
public class StraightRequestBuilder {
    private final String configuration;
    private final Map<Color, List<String>> colorToTerritoriesMap = new HashMap<>();
    private Color defaultColor = Color.SILVER;
    private Color color = Color.BLUE;

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

    public StraightRequest build() {
        return new StraightRequest(configuration, colorToTerritoriesMap, defaultColor);
    }

    public Optional<StraightRequest> buildOptional() {
        return Optional.of(build());
    }
}
